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
package com.github.joekerouac.common.tools.lock;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

import com.github.joekerouac.common.tools.function.IOTaskWithResult;
import com.github.joekerouac.common.tools.function.IOTaskWithoutResult;
import com.github.joekerouac.common.tools.function.InterruptedTaskWithResult;
import com.github.joekerouac.common.tools.function.InterruptedTaskWithoutResult;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 加锁任务工具
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LockTaskUtil {

    /**
     * 加锁运行指定任务，有返回值
     * 
     * @param lock
     *            要加的锁
     * @param task
     *            任务
     * @param <T>
     *            结果类型
     * @return 结果
     */
    public static <T> T runWithLock(Lock lock, Supplier<T> task) {
        lock.lock();
        try {
            return task.get();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 加锁运行指定任务，无返回值
     * 
     * @param lock
     *            要加的锁
     * @param task
     *            任务
     */
    public static void runWithLock(Lock lock, Runnable task) {
        lock.lock();
        try {
            task.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 加锁运行可被中断的任务
     * 
     * @param lock
     *            锁
     * @param task
     *            可被中断的任务
     * @param <T>
     *            结果实际类型
     * @throws InterruptedException
     *             中断异常
     */
    public static <T> T runInterruptedTaskWithLock(Lock lock, InterruptedTaskWithResult<T> task)
        throws InterruptedException {
        lock.lock();
        try {
            return task.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 加锁运行可被中断的任务
     * 
     * @param lock
     *            锁
     * @param task
     *            可被中断的任务
     * @throws InterruptedException
     *             中断异常
     */
    public static void runInterruptedTaskWithLock(Lock lock, InterruptedTaskWithoutResult task)
        throws InterruptedException {
        lock.lock();
        try {
            task.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 加锁运行指定IO任务，无返回值
     * 
     * @param lock
     *            要加的锁
     * @param task
     *            任务
     * @throws IOException
     *             IO异常
     */
    public static void runIOTaskWithLock(Lock lock, IOTaskWithoutResult task) throws IOException {
        lock.lock();
        try {
            task.run();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 加锁运行指定IO任务，有返回值
     * 
     * @param lock
     *            要加的锁
     * @param task
     *            任务
     * @param <T>
     *            返回值实际类型
     * @return 返回值
     * @throws IOException
     *             IO异常
     */
    public static <T> T runIOTaskWithLock(Lock lock, IOTaskWithResult<T> task) throws IOException {
        lock.lock();
        try {
            return task.run();
        } finally {
            lock.unlock();
        }
    }

}
