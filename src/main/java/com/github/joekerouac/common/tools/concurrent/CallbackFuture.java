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

import java.util.concurrent.Future;

/**
 * 可以添加回调的future
 * 
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public interface CallbackFuture<T> extends Future<T> {

    /**
     * 添加一个回调，完成时调用该回调，如果当前已经完成则会立即调用回调
     * 
     * @param callback
     *            要添加的回调
     */
    void addCallback(FutureCallback<T> callback);

    /**
     * 删除指定回调
     *
     * @param callback
     *            回调
     */
    void removeCallback(FutureCallback<T> callback);

}
