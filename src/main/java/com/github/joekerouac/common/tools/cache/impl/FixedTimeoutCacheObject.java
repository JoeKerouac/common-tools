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

import com.github.joekerouac.common.tools.cache.CacheObject;

/**
 * 定时失效缓存对象
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
class FixedTimeoutCacheObject<T> implements CacheObject<T> {

    /**
     * 缓存失效时长，单位毫秒，缓存超过该时间自动失效
     */
    private final long timeout;

    /**
     * 实际的缓存对象
     */
    private final T cacheObj;

    /**
     * 缓存生效开始时间
     */
    private final long beginTime;

    public FixedTimeoutCacheObject(long timeout, T cacheObj) {
        this.timeout = timeout;
        this.cacheObj = cacheObj;
        this.beginTime = System.currentTimeMillis();
    }

    @Override
    public boolean expire() {
        return System.currentTimeMillis() - beginTime >= timeout;
    }

    @Override
    public T getTarget() {
        return cacheObj;
    }
}
