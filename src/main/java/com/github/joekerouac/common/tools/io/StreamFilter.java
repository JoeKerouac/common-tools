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
package com.github.joekerouac.common.tools.io;

/**
 * 流filter
 * 
 * @author JoeKerouac
 * @date 2023-06-09 10:54
 * @since 2.0.3
 */
public interface StreamFilter {

    /**
     * 过滤数据
     * 
     * @param ref
     *            过滤前的数据
     * @return 过滤后的数据
     */
    ByteBufferRef filter(ByteBufferRef ref);

    /**
     * 数据结束
     */
    default void finish() {

    }

}
