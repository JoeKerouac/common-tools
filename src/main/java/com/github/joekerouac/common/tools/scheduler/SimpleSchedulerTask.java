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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.CustomLog;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
@CustomLog
public class SimpleSchedulerTask implements SchedulerTask {

    private final Object mutex = new Object();

    private final ExecutorService executorService;

    /**
     * 任务名
     */
    private final String taskName;

    /**
     * 真正执行的任务，注意，该任务中不要做while true之类的逻辑，调度任务会自动定义执行该任务
     */
    private final Runnable task;

    /**
     * 调度信号量
     */
    private final Semaphore semaphore;

    /**
     * 是否合并调度，true表示合并调度，即如果有多个主动调度{@link #scheduler()}同时过来或者多个调度堆积，此时只会触发一次调度
     */
    private final boolean mergeScheduler;

    /**
     * 启动标识
     */
    private volatile boolean start;

    /**
     * 任务调度间隔（上次任务结束到下次任务开始）
     */
    private volatile long fixedDelay;

    /**
     * 初始延迟时间
     */
    private volatile long initialDelay;

    /**
     * 任务
     */
    private Thread taskThread;

    public SimpleSchedulerTask(Runnable task, String taskName, boolean mergeScheduler) {
        this(task, taskName, mergeScheduler, null);
    }

    public SimpleSchedulerTask(Runnable task, String taskName, boolean mergeScheduler,
        ExecutorService executorService) {
        Assert.notNull(task, "任务不能为空", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.notBlank(taskName, "任务名不能为空", ExceptionProviderConst.IllegalArgumentExceptionProvider);

        this.task = task;
        this.taskName = taskName;
        this.mergeScheduler = mergeScheduler;
        this.executorService = executorService;
        this.semaphore = new Semaphore(0);
        this.start = false;
        this.fixedDelay = 0;
        this.initialDelay = 0;
    }

    /**
     * 尝试获取信号量，最多等待指定时间，超时后返回，不会被中断，注意，不要并发调用
     * 
     * @param time
     *            超时时间，单位毫秒
     */
    private void tryAcquire(long time) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start <= time) {
            try {
                // 注意，tryAcquire不会并发调用，所以这里的两次semaphore.availablePermits()调用不存在并发问题
                if (semaphore.tryAcquire(time - (System.currentTimeMillis() - start), TimeUnit.MILLISECONDS)
                    && mergeScheduler && semaphore.availablePermits() > 0) {
                    // 如果合并调度，则将所有的semaphore消耗完毕
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

    @Override
    public void start() {
        synchronized (mutex) {
            if (start) {
                LOGGER.warn("当前任务 [{}] 已经启动，无需重复启动", taskName);
                return;
            }

            Assert.assertTrue(fixedDelay > 0, StringUtils.format("当前任务 [{}] fixedDelay还未初始化，请初始化后启动", taskName),
                ExceptionProviderConst.IllegalStateExceptionProvider);

            start = true;
            Runnable run = () -> {
                if (initialDelay > 0) {
                    tryAcquire(initialDelay);
                }

                while (start) {
                    try {
                        task.run();
                    } catch (Throwable throwable) {
                        LOGGER.warn(throwable, "定时任务 [{}] 本轮执行失败（不影响后续执行）", taskName);
                    }

                    tryAcquire(fixedDelay);
                }
            };

            if (executorService == null) {
                taskThread = new Thread(run, taskName);
                taskThread.setDaemon(false);
                taskThread.start();
            } else {
                executorService.submit(run);
            }
        }
    }

    @Override
    public void stop() {
        synchronized (mutex) {
            if (start) {
                start = false;
                if (taskThread != null) {
                    // 主动调用interrupt，中断线程
                    taskThread.interrupt();
                    taskThread = null;
                }
            }
        }
    }

    @Override
    public void scheduler() {
        semaphore.release();
    }

    @Override
    public long fixedDelay() {
        return fixedDelay;
    }

    @Override
    public void setFixedDelay(final long fixedDelay) {
        this.fixedDelay = fixedDelay;
    }

    @Override
    public long initialDelay() {
        return initialDelay;
    }

    @Override
    public void setInitialDelay(final long initialDelay) {
        this.initialDelay = initialDelay;
    }
}
