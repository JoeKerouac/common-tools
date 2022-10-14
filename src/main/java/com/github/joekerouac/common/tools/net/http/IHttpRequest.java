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
package com.github.joekerouac.common.tools.net.http;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Future;

import com.github.joekerouac.common.tools.concurrent.FutureCallback;
import com.github.joekerouac.common.tools.net.http.config.IHttpConfig;
import com.github.joekerouac.common.tools.net.http.exception.NetException;
import com.github.joekerouac.common.tools.net.http.exception.UnknownException;
import com.github.joekerouac.common.tools.net.http.response.IHttpResponse;

/**
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface IHttpRequest {

    /**
     * 执行请求
     * 
     * @return 请求结果
     * @throws IOException
     *             IO异常
     * @throws NetException
     *             执行被中断时抛出该异常，对应的cause就是InterruptedException
     * @throws UnknownException
     *             执行遇到未知异常（非IO异常）时抛出该异常，cause是真正的异常
     */
    IHttpResponse exec() throws IOException, NetException, UnknownException;

    /**
     * 异步执行请求
     * 
     * @param futureCallback
     *            请求回调，允许为空
     * @return 请求异步结果
     */
    Future<IHttpResponse> exec(FutureCallback<IHttpResponse> futureCallback);

    /**
     * 请求方法
     * 
     * @return 请求方法
     */
    String getMethod();

    /**
     * 获取URL
     * 
     * @return 请求URL，包含query parameter
     */
    String getUrl();

    /**
     * 获取请求配置
     * 
     * @return 请求配置
     */
    IHttpConfig getHttpConfig();

    /**
     * 获取contentType
     * 
     * @return contentType
     */
    String getContentType();

    /**
     * 获取当前所有请求头
     * 
     * @return 当前所有请求头
     */
    Map<String, String> getHeaders();

    /**
     * 获取当前编码字符集
     * 
     * @return 当前编码字符集
     */
    String getCharset();

}
