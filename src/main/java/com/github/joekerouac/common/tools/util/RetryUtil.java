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

import java.util.function.Supplier;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;

import lombok.CustomLog;

/**
 * 重试工具
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
@CustomLog
public class RetryUtil {

    /**
     * 执行指定函数，最多重试指定次数，发生异常时根据参数判断是抛出异常还是什么都不做
     * 
     * @param runnable
     *            要执行的函数
     * @param retry
     *            最大重试次数
     * @param throwIfError
     *            发生异常时是否抛出，true 表示需要抛出
     */
    public static void run(Runnable runnable, int retry, boolean throwIfError) {
        runWithRetry(() -> {
            runnable.run();
            return null;
        }, null, retry, throwIfError);
    }

    /**
     * 执行指定函数，最多重试指定次数，发生异常时返回默认结果
     * 
     * @param supplier
     *            指定函数
     * @param defaultResult
     *            默认结果
     * @param retry
     *            最大重试次数
     * @param <T>
     *            结果类型
     * @return 结果
     */
    public static <T> T runWithRetry(Supplier<T> supplier, T defaultResult, int retry) {
        return runWithRetry(supplier, defaultResult, retry, false);
    }

    /**
     * 执行指定函数，最多重试指定次数，如果仍然失败将会抛出异常
     * 
     * @param supplier
     *            指定函数
     * @param retry
     *            最大重试次数
     * @param <T>
     *            结果类型
     * @return 结果
     */
    public static <T> T runWithRetry(Supplier<T> supplier, int retry) {
        return runWithRetry(supplier, null, retry, true);
    }

    /**
     * 执行指定函数，最多重试指定次数，发生异常时根据参数确认是返回默认结果还是抛出异常
     *
     * @param supplier
     *            指定函数
     * @param defaultResult
     *            默认结果
     * @param retry
     *            最大重试次数
     * @param throwIfError
     *            重试超限后异常是否抛出，true 表示需要抛出
     * @param <T>
     *            结果类型
     * @return 结果
     */
    private static <T> T runWithRetry(Supplier<T> supplier, T defaultResult, int retry, boolean throwIfError) {
        Assert.argNotNull(supplier, "supplier");
        Assert.assertTrue(retry > 0, "retry 必须大于0", ExceptionProviderConst.IllegalArgumentExceptionProvider);

        int i = 0;
        RuntimeException throwable = null;
        while (i < retry) {
            i += 1;
            try {
                return supplier.get();
            } catch (RuntimeException e) {
                LOGGER.info(e, "任务执行过程中发生异常");
                throwable = e;
            }
        }

        if (throwIfError) {
            throw throwable;
        } else {
            return defaultResult;
        }
    }

}
