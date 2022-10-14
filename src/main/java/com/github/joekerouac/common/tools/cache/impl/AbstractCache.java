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
package com.github.joekerouac.common.tools.cache.impl;

import java.util.function.Supplier;

import com.github.joekerouac.common.tools.cache.CacheObject;
import com.github.joekerouac.common.tools.cache.RefreshableCache;

/**
 * 通用的cache接口，cache会懒加载，第一次获取时才会真正的加载，非线程安全
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public abstract class AbstractCache<T> implements RefreshableCache<T> {

    /**
     * 缓存提供者
     */
    private final Supplier<CacheObject<T>> supplier;

    /**
     * 缓存对象
     */
    private volatile CacheObject<T> cacheObj;

    public AbstractCache(Supplier<CacheObject<T>> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T refresh() {
        this.cacheObj = supplier.get();
        return cacheObj.getTarget();
    }

    @Override
    public T getTarget() {
        if (cacheObj == null || cacheObj.expire() || cacheObj.getTarget() == null) {
            refresh();
        }

        return cacheObj.getTarget();
    }
}
