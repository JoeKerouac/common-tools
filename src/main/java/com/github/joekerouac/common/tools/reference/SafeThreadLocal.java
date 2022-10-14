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
package com.github.joekerouac.common.tools.reference;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 安全的ThreadLocal，无需手动回收内存，不过带来的副作用就是会增大内存消耗，因为需要为每个线程保存一个监听回调
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class SafeThreadLocal<T> {

    private static final Map<WeakReference<Thread>, Map<SafeThreadLocal<?>, Object>> CONTEXT =
        new ConcurrentHashMap<>();

    /**
     * 给当前线程上下文设置数据
     * 
     * @param obj
     *            数据
     */
    public void set(Object obj) {
        CONTEXT.compute(new CustomWeakReference<>(Thread.currentThread()), (key, value) -> {
            if (value == null) {
                value = new HashMap<>();
                ReferenceUtils.listenDestroy(Thread.currentThread(), () -> CONTEXT.remove(key));
            }
            value.put(this, obj);
            return value;
        });
    }

    /**
     * 获取数据
     * 
     * @return 当前线程对应的数据
     */
    @SuppressWarnings("unchecked")
    public T get() {
        Map<SafeThreadLocal<?>, Object> context = CONTEXT.get(new CustomWeakReference<>(Thread.currentThread()));
        if (context == null) {
            return null;
        }
        return (T)context.get(this);
    }

    /**
     * 移除数据
     */
    public void remove() {
        Map<SafeThreadLocal<?>, Object> context = CONTEXT.get(new CustomWeakReference<>(Thread.currentThread()));
        if (context != null) {
            context.remove(this);
        }
    }
}
