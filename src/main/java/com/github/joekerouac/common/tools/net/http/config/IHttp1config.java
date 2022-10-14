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
package com.github.joekerouac.common.tools.net.http.config;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import lombok.Data;

/**
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@Data
public class IHttp1config {

    /**
     * 默认缓冲区大小，单位byte
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;

    /**
     * http session缓冲区大小，socket的数据会先读取到该缓冲区，然后供外部读取，{@link org.apache.hc.core5.http.io.SessionInputBuffer
     * SessionInputBuffer}和{@link org.apache.hc.core5.http.io.SessionOutputBuffer SessionOutputBuffer}中会使用该参数，不能小于
     * 0，小于等于0时会使用默认值8K
     */
    @Min(512)
    @Max(1024 * 1024)
    private int httpSessionBufferSize = DEFAULT_BUFFER_SIZE;

    /**
     * {@link org.apache.hc.core5.http.io.SessionOutputBuffer SessionOutputBuffer}中会使用，当写出数据小于该值时会先存储到缓冲区，
     * 如果写出数据大于该大小时则会直接写出到socket，如果该值小于等于0则会使用{@link #httpSessionBufferSize}
     */
    private int httpChunkSizeHint = -1;

    /**
     * http单行最大长度，小于等于0时不限制
     */
    private int httpMaxLineLength = -1;

    /**
     * 最大header数量，小于等于0时不限制
     */
    private int httpMaxHeaderCount = -1;

}
