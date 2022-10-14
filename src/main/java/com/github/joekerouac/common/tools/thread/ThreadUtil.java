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
package com.github.joekerouac.common.tools.thread;

import java.util.concurrent.*;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;
import com.github.joekerouac.common.tools.validator.ValidationServiceImpl;

import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.NoArgsConstructor;

/**
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ThreadUtil {

    private static final Thread.UncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLER =
        (t, e) -> LOGGER.warn(e, "线程 [{}] 执行过程中发生异常", t.getName());

    /**
     * 强制sleep指定时间，如果sleep被中断，会忽略该中断继续sleep
     *
     * @param time
     *            要sleep的时间
     * @param timeUnit
     *            时间单位
     */
    public static void sleepIgnoreInterrupt(long time, TimeUnit timeUnit) {
        sleepIgnoreInterrupt(timeUnit.toMillis(time));
    }

    /**
     * 强制sleep指定时间，如果sleep被中断，会忽略该中断继续sleep
     * 
     * @param time
     *            要sleep的时间，单位毫秒
     */
    public static void sleepIgnoreInterrupt(long time) {
        long end = System.currentTimeMillis() + time;
        while (true) {
            try {
                long sleep = end - System.currentTimeMillis();
                Thread.sleep(sleep);
                return;
            } catch (InterruptedException interruptedException) {
                // 异常忽略
            }
        }
    }

    /**
     * 当前线程睡眠一段时间，当线程被中断时会抛出RuntimeException而不是InterruptedException，如果 你的实现依赖于该异常请勿调用该方法休眠
     *
     * @param time
     *            时长
     * @param unit
     *            单位
     */
    public static void sleep(long time, TimeUnit unit) {
        try {
            if (time <= 0 || unit == null) {
                return;
            }

            Thread.sleep(unit.toMillis(time));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 当前线程睡眠一段时间（单位为秒），当线程被中断时不会抛出异常，如果 你的实现依赖于该异常请勿调用该方法休眠
     *
     * @param time
     *            时长，单位为秒
     */
    public static void sleep(long time) {
        sleep(time, TimeUnit.SECONDS);
    }

    /**
     * 使用指定参数创建一个线程池
     * 
     * @param config
     *            线程池参数
     * @return 线程池
     */
    public static ThreadPoolExecutor newThreadPool(ThreadPoolConfig config) {
        Assert.argNotNull(config, "config");
        new ValidationServiceImpl().validate(config);
        Assert.assertTrue(
            config.getMaximumPoolSize() >= config.getCorePoolSize(), StringUtils
                .format("传入参数有误，线程池最大大小应该大于等于核心大小，当前配置：[{}:{}]", config.getMaximumPoolSize(), config.getCorePoolSize()),
            ExceptionProviderConst.IllegalArgumentExceptionProvider);
        BlockingQueue<Runnable> workQueue = config.getWorkQueue();

        if (config.getWorkQueue() == null) {
            workQueue = new ArrayBlockingQueue<>(config.getQueueSize());
        }

        ThreadFactory threadFactory = config.getThreadFactory();

        if (threadFactory == null) {
            ThreadFactory threadFactoryOriginal = Executors.defaultThreadFactory();
            threadFactory = r -> {
                Thread thread = threadFactoryOriginal.newThread(r);
                thread.setContextClassLoader(config.getDefaultContextClassLoader());
                return thread;
            };
        }

        // 未捕获异常处理
        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = config.getUncaughtExceptionHandler();
        if (uncaughtExceptionHandler == null) {
            uncaughtExceptionHandler = UNCAUGHT_EXCEPTION_HANDLER;
        }

        RejectedExecutionHandler rejectedExecutionHandler = config.getRejectedExecutionHandler();
        if (rejectedExecutionHandler == null) {
            rejectedExecutionHandler = new ThreadPoolExecutor.AbortPolicy();
        }

        return new ThreadPoolExecutor(config.getCorePoolSize(), config.getMaximumPoolSize(), config.getKeepAliveTime(),
            config.getTimeUnit(), workQueue,
            new UncaughtExceptionHandlerThreadFactory(threadFactory, uncaughtExceptionHandler),
            rejectedExecutionHandler);
    }
}
