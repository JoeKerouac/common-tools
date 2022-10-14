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
package com.github.joekerouac.common.tools.concurrent;

/**
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface FutureCallback<T> {

    /**
     * 完成回调，无论成功失败都会调用
     *
     * @param result
     *            成功结果，如果是成功则可能不为空
     * @param ex
     *            异常，如果异常肯定不为空
     * @param status
     *            状态，0表示成功，1表示异常，2表示取消
     */
    default void complete(T result, Throwable ex, int status) {

    }

    /**
     * 成功回调
     * 
     * @param result
     *            结果
     */
    default void success(T result) {

    }

    /**
     * 失败回调
     * 
     * @param ex
     *            异常
     */
    default void failed(Throwable ex) {

    }

    /**
     * 取消回调
     */
    default void cancelled() {

    }

}
