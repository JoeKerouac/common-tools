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
package com.github.joekerouac.common.tools.cache;

/**
 * 缓存对象
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface CacheObject<T> {

    /**
     * 该缓存对象是否失效
     * 
     * @return true表示已经失效
     */
    boolean expire();

    /**
     * 缓存的对象
     * 
     * @return 缓存的对象，允许为null，但是如果为null，系统将会认为该缓存不存在，将会主动刷新缓存
     */
    T getTarget();

}
