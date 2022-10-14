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
package com.github.joekerouac.common.tools.net.proxy;

import java.io.IOException;

import com.github.joekerouac.common.tools.net.HttpProxySettings;

/**
 * 系统网络代理
 * 
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public interface SystemProxyService {

    /**
     * 设置系统网络代理
     * 
     * @param settings
     *            代理配置
     * @throws IOException
     *             IO异常
     */
    void setProxy(HttpProxySettings settings) throws IOException;

    /**
     * 关闭当前代理
     * 
     * @throws IOException
     *             IO异常
     */
    void closeProxy() throws IOException;
}
