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
package com.github.joekerouac.common.tools.net.http.exception;

import com.github.joekerouac.common.tools.string.StringUtils;

import lombok.Getter;

/**
 * 服务器异常
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @since 1.0.0
 */
@Getter
public class ServerException extends NetException {

    private static final long serialVersionUID = 4543409499814321713L;

    private static final String ERR_MSG = "请求[{}]发生异常，异常类型：[{}]，异常信息：[{}]，错误：[{}，HTTP状态：[{}]";

    /**
     * 异常请求路径
     */
    private final String path;

    /**
     * 服务端异常类型
     */
    private final String exceptionClass;

    /**
     * 服务端异常消息
     */
    private final String msg;

    /**
     * 错误信息
     */
    private final String error;

    /**
     * HTTP状态
     */
    private final int status;

    /**
     * 服务器异常
     * 
     * @param path
     *            请求路径
     * @param exceptionClass
     *            异常类
     * @param msg
     *            异常消息
     * @param error
     *            错误信息
     * @param status
     *            http状态
     */
    public ServerException(String path, String exceptionClass, String msg, String error, int status) {
        super(StringUtils.format(ERR_MSG, path, exceptionClass, msg, error, status));
        this.path = path;
        this.exceptionClass = exceptionClass;
        this.msg = msg;
        this.error = error;
        this.status = status;
    }
}
