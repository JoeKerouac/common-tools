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
package com.github.joekerouac.common.tools.function;

/**
 * 过滤函数
 *
 * @author JoeKerouac
 * @date 2022-10-17 17:59
 * @since 2.0.0
 */
public interface Filter<T> {

    /**
     * 对所有都返回true
     */
    Filter<?> TRUE = obj -> true;

    /**
     * 对所有都返回false
     */
    Filter<?> FALSE = obj -> false;

    /**
     * 过滤
     * 
     * @param t
     *            过滤对象
     * @return 过滤结果
     */
    boolean filter(T t);

}
