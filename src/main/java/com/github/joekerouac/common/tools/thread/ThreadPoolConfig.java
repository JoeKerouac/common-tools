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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * 线程池配置
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@Data
public class ThreadPoolConfig {

    /**
     * 核心线程池大小
     */
    @Min(1)
    private int corePoolSize = 10;

    /**
     * 最大线程池大小
     */
    @Min(1)
    private int maximumPoolSize = 10;

    /**
     * 线程存活时间，如果当前线程池大小超过核心大小，那么超过该存活时间的线程将被清理
     */
    @Min(1)
    private long keepAliveTime = 300;

    /**
     * 时间单位
     */
    @NotNull
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    /**
     * 工作队列，允许为空，为空时使用{@link #queueSize}来构建队列
     */
    private BlockingQueue<Runnable> workQueue;

    /**
     * 如果{@link #workQueue}为null，将会使用该字段构建队列；
     */
    @Min(1)
    private int queueSize = 100;

    /**
     * 线程池工厂，允许为空
     */
    private ThreadFactory threadFactory;

    /**
     * 拒绝策略，允许为空
     */
    private RejectedExecutionHandler rejectedExecutionHandler;

    /**
     * 异常处理，允许为空，为空时使用默认处理器，只打印日志
     */
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    /**
     * 默认线程上下文类加载器，{@link #threadFactory}为空时有用；
     */
    private ClassLoader defaultContextClassLoader;

}
