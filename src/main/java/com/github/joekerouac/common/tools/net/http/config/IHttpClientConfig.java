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
import java.nio.charset.Charset;
import java.util.concurrent.ThreadFactory;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.github.joekerouac.common.tools.constant.Const;

import lombok.Data;

/**
 * HTTP客户端配置
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
@Data
public final class IHttpClientConfig {

    /**
     * 默认保持连接数
     */
    private static final int DEFAULT_MAX_TOTAL = 200;

    /**
     * 默认每个站点可以保持的最大连接数
     */
    private static final int DEFAULT_MAX_PER_ROUTE = 20;

    /**
     * 默认缓冲区大小，单位byte
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 8;

    /**
     * 默认传输超时时间
     */
    private static final int DEFAULT_SOCKET_TIMEOUT = 60 * 1000;

    /**
     * 数据传输超时时间，单位毫秒，默认一分钟
     */
    private int socketTimeout = DEFAULT_SOCKET_TIMEOUT;

    /**
     * HTTP1协议栈配置
     */
    @NotNull
    private IHttp1config http1config = new IHttp1config();

    /**
     * 总共可以保持的连接数
     */
    @Min(1)
    @Max(500)
    private int maxTotal = DEFAULT_MAX_TOTAL;

    /**
     * 每个站点可以保持的最大连接数（目标ip+port表示一个站点）
     */
    @Min(1)
    @Max(50)
    private int defaultMaxPerRoute = DEFAULT_MAX_PER_ROUTE;

    /**
     * 是否保持连接
     */
    private boolean keepAlive = true;

    /**
     * IO线程数；
     *
     * PS：注意，如果使用了异步请求，那么请求回调也在该线程执行，注意不要阻塞
     */
    @Min(1)
    @Max(10)
    private int ioThreadCount = Math.min(Math.max(2, Runtime.getRuntime().availableProcessors()), 10);

    /**
     * IO线程工厂
     */
    private ThreadFactory threadFactory;

    /**
     * socket的tcpNoDelay选项
     */
    private boolean tcpNoDelay = true;

    /**
     * socket发送缓冲，单位byte
     */
    @Min(512)
    @Max(1024 * 1024)
    private int sndBufSize = DEFAULT_BUFFER_SIZE;

    /**
     * socket接收缓冲，单位byte
     */
    @Min(512)
    @Max(1024 * 1024)
    private int rcvBufSize = DEFAULT_BUFFER_SIZE;

    /**
     * 默认连接编码字符集
     */
    private Charset charset = Const.DEFAULT_CHARSET;

    /**
     * 用户代理
     */
    @NotBlank
    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:49.0) Gecko/20100101 Firefox/49.0";

    /**
     * http代理
     */
    private HttpProxy proxy;

    /**
     * 要绑定的本地网卡地址，允许为空
     */
    private InetAddress localAddress;

    /**
     * 全局请求配置
     */
    @NotNull
    private IHttpConfig httpConfig = new IHttpConfig();

}
