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
package com.github.joekerouac.common.tools.codec.json.annotations;

/**
 * @author JoeKerouac
 * @date 2023-07-18 16:44
 * @since 2.0.3
 */
public @interface InMemoryFileSize {

    int initMemoryBufferSize = 256;

    int memoryBufferSize = 4096;

    /**
     * 内存缓存初始大小
     *
     * @return 内存缓存初始大小，默认256，小于等于0时使用256
     */
    int initMemoryBufferSize() default initMemoryBufferSize;

    /**
     * 内存缓存大小
     * 
     * @return 内存缓存大小，默认4096，小于等于0时使用4096
     */
    int memoryBufferSize() default memoryBufferSize;

}
