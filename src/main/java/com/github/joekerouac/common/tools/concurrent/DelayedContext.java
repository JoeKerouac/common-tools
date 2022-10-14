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

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 延迟上下文
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DelayedContext<T> implements Delayed {

    /**
     * 实际存储的对象
     */
    @Getter
    @EqualsAndHashCode.Include
    private final T context;

    /**
     * 过期时间，到该时间时可以从延迟队列中取出
     */
    private final long expire;

    /**
     * 默认构造器
     * 
     * @param obj
     *            实际存储的对象
     * @param delayed
     *            延迟时间，单位毫秒
     */
    public DelayedContext(T obj, long delayed) {
        this.context = obj;
        this.expire = delayed + System.currentTimeMillis();
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(expire - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
    }

}
