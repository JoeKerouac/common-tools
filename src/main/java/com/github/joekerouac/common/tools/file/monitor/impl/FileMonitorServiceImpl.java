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
package com.github.joekerouac.common.tools.file.monitor.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.constant.Const;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.exception.CommonException;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.file.monitor.FileMonitorCallback;
import com.github.joekerouac.common.tools.file.monitor.FileMonitorService;
import com.github.joekerouac.common.tools.lock.LockTaskUtil;
import com.github.joekerouac.common.tools.thread.ThreadPoolConfig;
import com.github.joekerouac.common.tools.thread.ThreadUtil;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.CustomLog;

/**
 * 文件变更监听服务，关闭后不能重新启动，同时文件变更会被压缩，例如如果短时间内有多个modify，则会压缩为一个，而如果有delete事件，那么delete事件前 的所有事件都会被丢弃；
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@CustomLog
public class FileMonitorServiceImpl implements FileMonitorService {

    /**
     * 收到文件变更事件后延迟多久处理，因为对于某些工具修改（例如vim），用户感知的一次修改实际底层触发了多次系统文件变更，如果不延迟处理将会频繁的 收到文件变更消息，并且会发现实际上文件并没有变更；
     */
    private static final long DELAYED = 100;

    /**
     * 当前支持的事件类型
     */
    private static final List<WatchEvent.Kind<?>> SUPPORTS = new ArrayList<>();

    static {
        SUPPORTS.add(StandardWatchEventKinds.ENTRY_CREATE);
        SUPPORTS.add(StandardWatchEventKinds.ENTRY_DELETE);
        SUPPORTS.add(StandardWatchEventKinds.ENTRY_MODIFY);
    }

    /**
     * 状态检测间隔，单位毫秒，主要是线程中使用，线程中最多休眠该时间就必须检测一次当前状态
     */
    private final long statusCheck = 500;

    /**
     * 启动锁，启动的时候加锁
     */
    private final ReadWriteLock startLock = new ReentrantReadWriteLock();

    /**
     * 当前状态，是否启动
     */
    private volatile boolean start;

    /**
     * 文件监听，key是文件的绝对路径，value是相应的动作
     */
    private Map<String, Set<FileMonitorCallback>> callbackMap;

    /**
     * watchKey到文件绝对路径父目录的映射，因为WatchEvent中获取到的context（Path）包含的路径可能不是绝对路径
     */
    private Map<WatchKey, String> watchKeyToPathMap;

    /**
     * path到watchKey的映射，与{@link #watchKeyToPathMap}是一对
     */
    private Map<String, WatchKey> pathToWatchKeyMap;

    /**
     * 对{@link #watchKeyToPathMap}和{@link #pathToWatchKeyMap}进行修改时加的锁
     */
    private final Lock keyLock = new ReentrantLock();

    /**
     * 待处理的任务队列
     */
    private DelayQueue<WatchKeyDelayed> delayQueue;

    /**
     * 监控服务
     */
    private WatchService watchService;

    /**
     * 执行文件监听回调的线程池
     */
    private ExecutorService executor;

    /**
     * 事件轮询线程，有新的事件只需要将其放入队列即可
     */
    private Thread eventPollThread;

    /**
     * 事件处理线程
     */
    private Thread workThread;

    public FileMonitorServiceImpl(ThreadPoolConfig config) {
        this(ThreadUtil.newThreadPool(config));
    }

    public FileMonitorServiceImpl(ExecutorService executor) {
        this.callbackMap = new ConcurrentHashMap<>();
        this.watchKeyToPathMap = new ConcurrentHashMap<>();
        this.pathToWatchKeyMap = new ConcurrentHashMap<>();
        this.delayQueue = new DelayQueue<>();
        this.executor = executor;
        this.start = false;
    }

    @Override
    public void start() {
        LockTaskUtil.runWithLock(startLock.writeLock(), () -> {
            if (!start) {
                // 检测是否是关闭的服务重新打开，如果是则抛出异常
                String msg = "当前文件监听服务已经启动并且关闭，不能重新打开";
                Assert.notNull(callbackMap, msg, ExceptionProviderConst.IllegalStateExceptionProvider);
                Assert.notNull(delayQueue, msg, ExceptionProviderConst.IllegalStateExceptionProvider);
                Assert.notNull(executor, msg, ExceptionProviderConst.IllegalStateExceptionProvider);

                try {
                    watchService = FileSystems.getDefault().newWatchService();
                } catch (IOException e) {
                    throw new CommonException(ErrorCodeEnum.IO_EXCEPTION, e);
                }

                // 事件轮询线程，有新的事件只需要将其放入队列即可
                eventPollThread = new Thread(() -> {
                    WatchKey key = null;
                    while (start) {
                        // 每次循环保证最多sleep statusCheck毫秒就会重新检测一次状态
                        try {
                            // 将这个放在poll前边，这样即使线程被非法中断，重试的时候引用仍然没有丢失，可以保证肯定放入队列成功
                            if (key != null) {
                                // 延迟100ms处理，因为有些操作会导致触发多个变更，例如使用vim编辑文件时，保存的时候会一次性触发多个变更，如果
                                // 不延迟处理将会频繁收到变更
                                WatchKeyDelayed delayed = new WatchKeyDelayed(key, DELAYED);

                                // 延迟队列放入肯定能成功（除非放入元素超过Integer.MAX，一般在这之前应该就OOM了）
                                delayQueue.offer(delayed, statusCheck, TimeUnit.MILLISECONDS);

                                // 处理完要将key重新置为null
                                key = null;
                            } else {
                                key = watchService.poll(statusCheck, TimeUnit.MILLISECONDS);
                            }

                        } catch (InterruptedException e) {
                            // 不处理线程中断，除非start变为false，此时线程将会自动退出
                        }

                    }
                }, "文件变更监听线程");
                eventPollThread.setContextClassLoader(null);
                eventPollThread.setDaemon(true);

                // 事件处理线程
                workThread = new Thread(() -> {
                    try {
                        while (start) {
                            WatchKeyDelayed watchKeyDelayed = delayQueue.poll(statusCheck, TimeUnit.MILLISECONDS);
                            if (watchKeyDelayed == null) {
                                continue;
                            }

                            WatchKey watchKey = watchKeyDelayed.getContext();
                            List<WatchEvent<?>> events = watchKey.pollEvents();

                            // 文件变更事件分组，key是文件的绝对路径
                            Map<String, List<WatchEvent<?>>> map = new ConcurrentHashMap<>();

                            // 先对event进行分组
                            String directory = null;
                            for (WatchEvent<?> event : events) {
                                File file = convert(event, watchKey);
                                if (file == null) {
                                    continue;
                                }

                                // 保存父目录
                                directory = file.getParentFile().getAbsolutePath();
                                // 当前变更文件没有注册相应的监视，跳过
                                if (!callbackMap.containsKey(file.getAbsolutePath())) {
                                    LOGGER.debug("当前文件[{}]没有注册相应的处理", file.getAbsolutePath());
                                    continue;
                                }

                                if (SUPPORTS.contains(event.kind())) {
                                    map.compute(file.getAbsolutePath(), (k, v) -> {
                                        if (v == null) {
                                            v = new ArrayList<>();
                                        }
                                        v.add(event);
                                        return v;
                                    });
                                }
                            }

                            if (map.isEmpty()) {
                                String path = directory;
                                // 如果map是空，那么判断当前的watchKey是否还有回调，如果没有则将key删除
                                LockTaskUtil.runWithLock(keyLock, () -> {
                                    boolean flag = true;
                                    for (Map.Entry<String, Set<FileMonitorCallback>> entry : callbackMap.entrySet()) {
                                        if (entry.getKey().startsWith(path)
                                            && !CollectionUtil.isEmpty(entry.getValue())) {
                                            flag = false;
                                            break;
                                        }
                                    }

                                    // 将已经没有监听的key删除
                                    if (flag) {
                                        Assert.notNull(pathToWatchKeyMap.remove(path),
                                            ExceptionProviderConst.IllegalStateExceptionProvider);
                                        Assert.notNull(watchKeyToPathMap.remove(watchKey),
                                            ExceptionProviderConst.IllegalStateExceptionProvider);
                                        // 将watchKey失效掉
                                        watchKey.cancel();
                                    }
                                });
                            } else {
                                // event处理
                                map.forEach((key, value) -> {
                                    Runnable task = () -> {
                                        // 先合并event
                                        List<WatchEvent<?>> mergedEvents = merge(value);
                                        mergedEvents.forEach(event -> {
                                            // 找到对应的callback
                                            Set<FileMonitorCallback> callbacks = callbackMap.get(key);
                                            callbacks.stream().filter(callback -> callback.canDeal(event.kind()))
                                                .forEach(
                                                    callback -> callback.call(event.kind(), convert(event, watchKey)));
                                        });
                                    };
                                    try {
                                        executor.submit(task);
                                    } catch (Throwable throwable) {
                                        LOGGER.warn("文件监控回调任务放入线程池失败，改为同步执行", throwable);
                                        // 放入线程池异常，应该是线程池已经满了被拒绝放入，此时退为同步执行策略
                                        task.run();
                                    }
                                });
                            }

                            // 如果此时key还有效，将reset，重新对其监视
                            if (watchKey.isValid()) {
                                watchKey.reset();
                            }
                        }
                    } catch (InterruptedException e) {
                        // 不处理线程中断，除非start变为false，此时线程将会自动退出
                    }
                }, "文件变更处理线程");
                workThread.setContextClassLoader(null);
                workThread.setDaemon(true);

                // 注意，要先将start状态切换过来
                start = true;
                eventPollThread.start();
                workThread.start();
            }
        });
    }

    @Override
    public void stop() {
        LockTaskUtil.runWithLock(startLock.writeLock(), () -> {
            if (start) {
                start = false;

                // 通知这两个线程中断，线程内部也会检查当前状态
                eventPollThread.interrupt();
                workThread.interrupt();

                try {
                    // 这里尝试等待线程执行完毕
                    eventPollThread.join();
                    workThread.join();
                } catch (InterruptedException e) {
                    LOGGER.warn("线程等待被中断", e);
                }

                eventPollThread = null;
                workThread = null;

                // 立即停止线程池
                executor.shutdown();
                executor = null;

                // 释放队列
                callbackMap.clear();
                callbackMap = null;
                watchKeyToPathMap.clear();
                watchKeyToPathMap = null;
                pathToWatchKeyMap.clear();
                pathToWatchKeyMap = null;
                delayQueue.clear();
                delayQueue = null;

                // 最后关闭watchService
                try {
                    watchService.close();
                } catch (IOException e) {
                    LOGGER.error("关闭watchService发生异常", e);
                    throw new CommonException(ErrorCodeEnum.IO_EXCEPTION, e);
                }
            }
        });
    }

    @Override
    public <T extends FileMonitorCallback> boolean addWatch(String filePath, T callback) {
        return LockTaskUtil.runWithLock(startLock.readLock(), () -> {
            Assert.assertTrue(start, "当前文件监听服务还未启动", ExceptionProviderConst.IllegalStateExceptionProvider);

            File file = new File(filePath);

            // 如果父文件不存在
            if (!file.getParentFile().exists()) {
                throw new CommonException(ErrorCodeEnum.FILE_NOT_EXIST);
            }

            if (file.isDirectory()) {
                throw new CommonException(ErrorCodeEnum.CODE_ERROR, "当前只能监控文件，不能监控文件夹");
            }

            // 这里主要是为了尽可能重用WatchKey，不去注册过多的系统监听
            String pathStr = file.getParentFile().getAbsolutePath();
            return LockTaskUtil.runWithLock(keyLock, () -> {
                try {
                    // 只有当前不存在该路径的监听时才需要新建
                    if (!pathToWatchKeyMap.containsKey(pathStr)) {
                        // 这里要获取对应的文件夹，因为只能监听文件夹
                        Path path = Paths.get(pathStr);
                        // 注册监听，监听创建、删除、更改动作
                        WatchKey key = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,
                            StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
                        // 将这个放入缓存中，方便后续使用
                        watchKeyToPathMap.put(key, pathStr);
                        pathToWatchKeyMap.put(pathStr, key);
                    }
                } catch (IOException e) {
                    ErrorCodeEnum codeEnum = ErrorCodeEnum.IO_EXCEPTION;
                    if (e instanceof NoSuchFileException) {
                        codeEnum = ErrorCodeEnum.FILE_NOT_EXIST;
                    }
                    throw new CommonException(codeEnum, e);
                }

                // 注册实际的callback
                AtomicBoolean atomicBoolean = new AtomicBoolean(false);

                // 注意，这个要放锁里边操作
                callbackMap.compute(file.getAbsolutePath(), (k, set) -> {
                    if (set == null) {
                        set = new ConcurrentSkipListSet<>(Comparator.comparingInt(Object::hashCode));
                    }
                    atomicBoolean.set(set.add(callback));
                    return set;
                });
                return atomicBoolean.get();
            });

        });
    }

    @Override
    public <T extends FileMonitorCallback> boolean removeWatch(String filePath, T callback) {
        return LockTaskUtil.runWithLock(startLock.readLock(), () -> {
            Assert.assertTrue(start, "当前文件监听服务还未启动", ExceptionProviderConst.IllegalStateExceptionProvider);
            Set<FileMonitorCallback> callbacks = callbackMap.get(filePath);
            boolean result = callbacks.remove(callback);

            // 如果已经为空，则直接移除
            if (callbacks.isEmpty()) {
                callbackMap.remove(filePath);
            }

            return result;
        });
    }

    /**
     * 将观察事件转换为文件
     * 
     * @param event
     *            事件
     * @param key
     *            事件对应的watchKey
     * @return 转换的文件，如果不能转换则返回null
     */
    private File convert(WatchEvent<?> event, WatchKey key) {
        // 暂时不能处理的
        if (!(event.context() instanceof Path)) {
            LOGGER.warn("当前不能处理的文件变更事件context类型：{}", event.context().getClass());
            return null;
        }

        Path path = (Path)event.context();
        // 获取绝对路径
        String absolutePath = watchKeyToPathMap.get(key) + Const.FILE_SEPARATOR + path.toFile().getName();
        return new File(absolutePath);
    }

    /**
     * 如果可能的话，将多个事件合并为单个，例如事件列表：C,M,M,D,C,M，将被合并为：D,C,M，事件列表C,M,M,M将被合并为C,M
     * 
     * @param events
     *            要合并的事件
     * @return 合并后的事件列表
     */
    private List<WatchEvent<?>> merge(List<WatchEvent<?>> events) {
        List<WatchEvent<?>> result = new ArrayList<>();
        boolean hasModify = false;
        boolean hasCreate = false;
        for (int i = events.size() - 1; i >= 0; i--) {
            WatchEvent<?> event = events.get(i);
            WatchEvent.Kind<?> kind = event.kind();

            // 如果当前是删除，那么没必要再往前遍历了
            if (kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                result.add(event);
                break;
            } else if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                // 如果已经有modify了，那么忽略
                if (!hasModify) {
                    result.add(event);
                    hasModify = true;
                }
            } else if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                if (hasCreate) {
                    throw new CommonException(ErrorCodeEnum.UNKNOWN_EXCEPTION, "当前不应该还有create事件的");
                }
                hasCreate = true;
                result.add(event);
            }
        }

        Collections.reverse(result);
        return result;
    }

}
