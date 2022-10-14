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

import lombok.Data;

/**
 * 代理服务器
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@Data
public final class HttpProxy {

    /**
     * 代理主机
     */
    private String host;

    /**
     * 代理端口
     */
    private int port;

    /**
     * 代理主机如果需要认证的话需要填这个参数，用户名，当用户名不为空时将会开启认证
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    public HttpProxy(String host, int port) {
        this(host, port, null, null);
    }

    public HttpProxy(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }
}
