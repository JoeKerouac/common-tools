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

import java.net.InetAddress;

import com.github.joekerouac.common.tools.io.StreamFilter;

import lombok.Data;

/**
 * 网络请求基本配置
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@Data
public abstract class AbstractHttpConfig {

    /**
     * 默认传输超时时间
     */
    private static final int DEFAULT_SOCKET_TIMEOUT = 60 * 1000;

    /**
     * 默认连接超时时间
     */
    private static final int DEFAULT_CONNECT_TIMEOUT = 5 * 1000;

    /**
     * 默认请求超时时间
     */
    private static final int DEFAULT_CONNECTION_REQUEST_TIMEOUT = 5 * 1000;

    /**
     * 默认最大响应体，当响应体大于该值时写入本地文件缓冲，单位byte
     */
    private static final int DEFAULT_WRITE_FILE_ON_LARGE = 1024 * 1024;

    /**
     * 数据传输超时时间，单位毫秒，默认一分钟
     */
    private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;

    /**
     * 连接建立超时，如果是TLS连接，则包含TLS握手时间，单位毫秒，默认5秒
     */
    private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;

    /**
     * 从连接池获取连接的超时时间，单位毫秒，默认5秒
     */
    private int connectionRequestTimeout = DEFAULT_CONNECTION_REQUEST_TIMEOUT;

    /**
     * 当响应超过多大时将响应写入文件，单位byte
     */
    private int writeFileOnLarge = DEFAULT_WRITE_FILE_ON_LARGE;

    /**
     * 初始化buffer大小，允许逐渐扩充到{@link #writeFileOnLarge}指定大小，然后将数据刷入临时文件
     */
    private int initBufferSize = 1024;

    /**
     * 响应数据filter
     */
    private StreamFilter filter;

    /**
     * 要绑定的本地网卡地址，允许为空
     */
    private InetAddress localAddress;

}
