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
package com.github.joekerouac.common.tools.util;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.lock.LockTaskUtil;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class Starter {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private volatile boolean start;

    /**
     * 启动服务，如果当前服务未启动，则执行用户启动任务，如果已经启动，则不执行
     * 
     * @param startTask
     *            用户启动任务，执行异常时启动失败
     */
    public void start(Runnable startTask) {
        LockTaskUtil.runWithLock(lock.writeLock(), () -> {
            if (!start) {
                startTask.run();
                start = true;
            }
        });
    }

    /**
     * 关闭服务，如果当前服务未关闭，则执行用户关闭任务，如果已经关闭，则不执行
     * 
     * @param stopTask
     *            用户关闭任务，执行异常时关闭失败
     */
    public void stop(Runnable stopTask) {
        LockTaskUtil.runWithLock(lock.writeLock(), () -> {
            if (start) {
                stopTask.run();
                start = false;
            }
        });
    }

    /**
     * 在开启状态下执行任务，保证任务执行完成前服务不会关闭
     * 
     * @param task
     *            要执行的任务
     * @throws IllegalStateException
     *             如果当前服务尚未开启，则抛出该异常
     */
    public void runWithStarted(Runnable task) throws IllegalStateException {
        LockTaskUtil.runWithLock(lock.readLock(), () -> {
            Assert.assertTrue(start, "当前尚未启动，无法执行", ExceptionProviderConst.IllegalStateExceptionProvider);
            task.run();
        });
    }

    /**
     * 在开启状态下执行任务并返回结果，保证任务执行完成前服务不会关闭
     *
     * @param task
     *            要执行的任务
     * @return 任务结果
     * @throws IllegalStateException
     *             如果当前服务尚未开启，则抛出该异常
     */
    public <T> T runWithStarted(Supplier<T> task) throws IllegalStateException {
        return LockTaskUtil.runWithLock(lock.readLock(), () -> {
            Assert.assertTrue(start, "当前尚未启动，无法执行", ExceptionProviderConst.IllegalStateExceptionProvider);
            return task.get();
        });
    }

}
