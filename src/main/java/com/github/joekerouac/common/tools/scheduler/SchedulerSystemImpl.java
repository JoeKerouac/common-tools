/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.github.joekerouac.common.tools.scheduler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.lock.LockTaskUtil;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.CustomLog;

/**
 * 多任务调度系统，任务满足以下条件时可以使用：
 * 
 * <li>一组任务，定时执行，也可被提前唤起执行；</li>
 * <li>任务量可能很大，但是每个任务可能很久才被唤起一次；</li>
 * <li>任务可以被连续唤醒，但是单个任务的唤醒调用并发不高；</li>
 * <li>同一时间不会有太多任务并发执行，或者同一时间有较多任务并发执行，但是对任务的时效性要求不高</li>
 * 
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
@CustomLog
public class SchedulerSystemImpl implements SchedulerSystem {

    private final ReadWriteLock statusLock = new ReentrantReadWriteLock();

    /**
     * 用于实际执行任务的线程池
     */
    private final ExecutorService executorService;

    /**
     * 合并调度，true表示当有多个调度的时候允许只执行一次，false表示调度一次就需要执行一次
     */
    private final boolean mergeScheduler;

    /**
     * 调度线程
     */
    private final Thread schedulerThread;

    /**
     * 当前调度器是否启动，true表示已经启动
     */
    private volatile boolean start;

    /**
     * 任务集合
     */
    private final Map<String, InternalTaskDescriptor> tasks;

    /**
     * 调度线程使用信号量
     */
    private Semaphore semaphore;

    /**
     * 调度线程下次唤醒时间
     */
    private final AtomicLong nextSchedulerTime;

    /**
     * 默认构造器
     * 
     * @param name
     *            调度系统名称
     * @param executorService
     *            任务执行线程池
     * @param mergeScheduler
     *            是否合并调度，如果合并调度，任务连续多次被调度可能只会执行一次（PS：保证合并调度的最后一次调度肯定会被执行）
     */
    public SchedulerSystemImpl(final String name, final ExecutorService executorService, final boolean mergeScheduler) {
        Assert.notBlank(name, "调度系统名称不能为空", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.notNull(executorService, "调度系统的任务执行线程池不能为空", ExceptionProviderConst.IllegalArgumentExceptionProvider);

        this.executorService = executorService;
        this.mergeScheduler = mergeScheduler;

        this.tasks = new ConcurrentHashMap<>();
        this.semaphore = new Semaphore(0);
        this.nextSchedulerTime = new AtomicLong(0);
        this.start = false;

        // 调度线程，主要就是负责任务到期唤醒
        this.schedulerThread = new Thread(() -> {
            while (start) {
                long waitTime = nextSchedulerTime.get() - System.currentTimeMillis();

                // 开始等待调度
                if (waitTime > 0) {
                    tryAcquire(waitTime);
                }

                LockTaskUtil.runWithLock(statusLock.readLock(), () -> {
                    // 因为上边的tryAcquire执行时间可能很长，所以执行完毕后重新判断一次当前是否还运行
                    if (!start) {
                        return;
                    }

                    // 当前时间
                    long now = System.currentTimeMillis();
                    // 下次调度时间
                    nextSchedulerTime.set(Long.MAX_VALUE);

                    // 开始唤醒任务，并确定下次调度时间
                    for (final InternalTaskDescriptor task : tasks.values()) {
                        try {
                            check(task, now);
                        } catch (Throwable throwable) {
                            LOGGER.warn(throwable, "[{}] 调度线程调度任务 [{}] 时发生异常，异常将被忽略，同时该任务本次调度也将被忽略", name, task.id);
                        }

                    }
                });
            }
        }, name);

        schedulerThread.setDaemon(false);
    }

    @Override
    public void start() {
        LockTaskUtil.runWithLock(statusLock.writeLock(), () -> {
            if (start) {
                LOGGER.warn("当前调度系统已经启动，请勿重复启动");
                return;
            }
            start = true;
            schedulerThread.start();
        });
    }

    @Override
    public void stop() {
        LockTaskUtil.runWithLock(statusLock.writeLock(), () -> {
            if (!start) {
                LOGGER.warn("当前调度系统已经关闭，请勿重复关闭");
            }
            start = false;
            // 中断线程
            schedulerThread.interrupt();
            // 重置semaphore
            semaphore = new Semaphore(0);
            // 重置下次调度时间
            nextSchedulerTime.set(0);
            // 清空任务
            tasks.clear();
        });
    }

    @Override
    public TaskDescriptor registerTask(TaskDescriptor taskDescriptor) {
        Assert.notNull(taskDescriptor, "要注册的任务说明不能为空", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        return LockTaskUtil.runWithLock(statusLock.readLock(), () -> {
            checkStart();

            Boolean mergeScheduler = taskDescriptor.getMergeScheduler();
            mergeScheduler = mergeScheduler == null ? this.mergeScheduler : mergeScheduler;

            InternalTaskDescriptor old =
                tasks.putIfAbsent(taskDescriptor.getId(), new InternalTaskDescriptor(taskDescriptor.getId(),
                    taskDescriptor.getExecuteInterval(), taskDescriptor.getTask(), mergeScheduler));
            semaphore.release();
            return old == null ? null : new TaskDescriptor(old.id, old.executeInterval, old.task);
        });
    }

    @Override
    public TaskDescriptor removeTask(String id) {
        Assert.notBlank(id, "要移除的任务ID不能为空", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        return LockTaskUtil.runWithLock(statusLock.readLock(), () -> {
            checkStart();

            InternalTaskDescriptor old = tasks.remove(id);
            return old == null ? null : new TaskDescriptor(old.id, old.executeInterval, old.task);
        });
    }

    @Override
    public List<TaskDescriptor> getAll() {
        return tasks.values().stream().map(internalTaskDescriptor -> new TaskDescriptor(internalTaskDescriptor.id,
            internalTaskDescriptor.executeInterval, internalTaskDescriptor.task)).collect(Collectors.toList());
    }

    @Override
    public void scheduler(final String id, final boolean throwIfTaskNotFound) {
        Assert.notBlank(id, "要调度的任务ID不能为空", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        LockTaskUtil.runWithLock(statusLock.readLock(), () -> {
            checkStart();

            InternalTaskDescriptor internalTaskDescriptor = tasks.get(id);
            if (internalTaskDescriptor == null) {
                if (throwIfTaskNotFound) {
                    throw new IllegalArgumentException(StringUtils.format("要调度的任务 [{}] 不存在", id));
                } else {
                    return;
                }
            }

            wakeUpTask(internalTaskDescriptor);
        });
    }

    /**
     * 校验当前调度系统已经启动
     */
    private void checkStart() {
        Assert.assertTrue(start, "当前调度系统尚未启动，无法操作", ExceptionProviderConst.IllegalStateExceptionProvider);
    }

    /**
     * 检查任务在指定时间是否该执行了，如果该执行了就执行
     *
     * @param task
     *            要检查的任务
     * @param now
     *            指定时间
     */
    private void check(InternalTaskDescriptor task, long now) {
        Boolean wakeUp = LockTaskUtil.runWithLock(task.statusMutex.readLock(), () -> {
            if (task.status == TaskStatus.IDLE) {
                // 如果任务当前还未到期并且任务到期时间小于当前最早到期任务的到期时间，那么更新
                if (task.nextExecuteTime > now) {
                    while (true) {
                        long currentNextScheduler = nextSchedulerTime.get();
                        // 注意，这里因为对任务加了锁，所以任务的状态和下次执行时间是肯定不会变的，所以不需要考虑其他线程修改下次执行时间或者状态导致的程序问题
                        if (task.nextExecuteTime < currentNextScheduler
                            && !nextSchedulerTime.compareAndSet(currentNextScheduler, task.nextExecuteTime)) {
                            // 因为其他线程也可能修改这个值，所以cas可能更新失败，如果更新失败就继续判断
                            continue;
                        }

                        break;
                    }

                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        });

        // 调度任务
        if (wakeUp) {
            wakeUpTask(task);
        }
    }

    /**
     * 主调度等待；
     * 
     * @param time
     *            等待超时时间，单位毫秒
     */
    private void tryAcquire(long time) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start <= time) {
            try {
                // 注意，tryAcquire不会并发调用，所以这里的两次semaphore.availablePermits()调用不存在并发问题
                if (semaphore.tryAcquire(time - (System.currentTimeMillis() - start), TimeUnit.MILLISECONDS)
                    && semaphore.availablePermits() > 0) {
                    // 合并调度，将所有的semaphore消耗完毕
                    semaphore.acquire(semaphore.availablePermits());
                }
                break;
            } catch (InterruptedException e) {
                // 如果当前任务已经结束则返回，否则忽略异常
                if (!this.start) {
                    return;
                }
            }
        }
    }

    /**
     * 唤醒指定任务
     * 
     * @param taskDescriptor
     *            任务描述
     */
    private void wakeUpTask(InternalTaskDescriptor taskDescriptor) {
        // 先将任务计数加1
        taskDescriptor.counter.incrementAndGet();

        // 修改任务状态
        LockTaskUtil.runWithLock(taskDescriptor.statusMutex.writeLock(), () -> {
            switch (taskDescriptor.status) {
                case IDLE:
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("任务 [{}] 当前是idle状态，将其加入线程池", taskDescriptor.id);
                    }

                    taskDescriptor.status = TaskStatus.RUNNING;
                    executorService.submit(build(taskDescriptor));
                    break;
                case RUNNING:
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("任务 [{}] 当前是running状态，继续添加一个任务到线程池", taskDescriptor.id);
                    }

                    taskDescriptor.status = TaskStatus.QUEUE;
                    break;
                case QUEUE:
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("任务 [{}] 当前是queue状态，无需重复添加任务，只需要增加计数器即可", taskDescriptor.id);
                    }

                    break;
                default:
                    throw new IllegalStateException(StringUtils.format("不支持的任务描述状态： [{}]", taskDescriptor.status));
            }
        });
    }

    /**
     * 构建任务执行器
     * 
     * @param taskDescriptor
     *            任务描述
     * @return 任务执行器，用于最终执行任务
     */
    private Runnable build(InternalTaskDescriptor taskDescriptor) {
        return () -> {
            boolean exec = true;
            while (exec) {
                // 执行计数，判断本次需要执行多少次
                long executeCounter;
                // 这段逻辑在任务级别低并发的调度下效率较高，但是在高并发的调度下效率较低，不过我们这个就是设计给任务级别低并发调度设计的，问题不大；
                do {
                    executeCounter = taskDescriptor.counter.get();
                } while (!taskDescriptor.counter.compareAndSet(executeCounter, 0));

                if (taskDescriptor.mergeScheduler) {
                    executeCounter = executeCounter > 0 ? 1 : 0;
                }

                // 执行指定次数
                for (long i = 0; i < executeCounter; i++) {
                    try {
                        taskDescriptor.task.run();
                    } catch (Throwable throwable) {
                        LOGGER.warn(throwable, "任务 [{}] 执行过程中发生了异常]", taskDescriptor.id);
                    }
                }

                // 这个主要是打印日志使用
                long currentExecuteCounter = executeCounter;
                // 加锁修改状态，同时返回是否继续执行
                exec = LockTaskUtil.runWithLock(taskDescriptor.statusMutex.writeLock(), () -> {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("本次任务 [{}] 执行 [{}] 次，当前任务状态为： [{}]", taskDescriptor.id, currentExecuteCounter,
                            taskDescriptor.status);
                    }
                    // 注意三个break的区别
                    switch (taskDescriptor.status) {
                        case RUNNING:
                            taskDescriptor.status = TaskStatus.IDLE;
                            // 注意，这里没有break是正确的，因为后边逻辑是公用的，所以这里没有加break，注意，下边的fall through注释是为
                            // 了让check style忽略这里少写了一个break
                            // fall through
                        case IDLE:
                            // 修改任务并退出while执行循环
                            // 更新下次执行时间，因为下次执行时间只有在IDLE状态才会消费，所以只有状态更新为IDLE时才更新；
                            taskDescriptor.nextExecuteTime =
                                System.currentTimeMillis() + taskDescriptor.executeInterval;
                            // 任务的下次执行时间修改了，需要重新调度计算下次执行时间；
                            semaphore.release();
                            return false;
                        case QUEUE:
                            // 修改任务状态，继续循环
                            taskDescriptor.status = TaskStatus.RUNNING;
                            return true;
                        default:
                            throw new IllegalStateException(
                                StringUtils.format("不支持的任务描述状态： [{}]", taskDescriptor.status));
                    }
                });
            }

        };
    }

    private static class InternalTaskDescriptor {

        /**
         * 任务状态锁
         */
        private final ReadWriteLock statusMutex;

        /**
         * 任务ID
         */
        private final String id;

        /**
         * 任务执行时间间隔，单位毫秒，执行间隔从上一次执行完成后开始计算；
         */
        private final long executeInterval;

        /**
         * 任务执行器
         */
        private final Runnable task;

        /**
         * 任务堆积数量，代表任务当前最多能执行的次数
         */
        private final AtomicLong counter;

        /**
         * 下次执行时间戳；PS：该字段目前读写都加的有锁，所以没必要使用其他同步手段；
         */
        private long nextExecuteTime;

        /**
         * 任务状态
         */
        private TaskStatus status;

        /**
         * 合并调度
         */
        private boolean mergeScheduler;

        public InternalTaskDescriptor(final String id, long executeInterval, final Runnable task,
            final boolean mergeScheduler) {
            this.statusMutex = new ReentrantReadWriteLock();
            this.id = id;
            this.executeInterval = executeInterval;
            this.task = task;
            this.counter = new AtomicLong(0);
            this.nextExecuteTime = System.currentTimeMillis() + executeInterval;
            this.status = TaskStatus.IDLE;
            this.mergeScheduler = mergeScheduler;
        }
    }

    /**
     * 任务状态
     */
    private enum TaskStatus {

        /**
         * 当前任务是空闲状态，此时如果触发任务需要往线程池添加一个任务；
         */
        IDLE,

        /**
         * 当前任务是执行状态，新加任务不一定能被这个执行中的任务调度起来，所以触发任务需要往线程池再添加一个任务；
         */
        RUNNING,

        /**
         * 此时任务是执行状态，并且还有一个任务在排队，此时触发任务只需要增加计数器即可，无需往线程池新增任务；
         */
        QUEUE
    }

}
