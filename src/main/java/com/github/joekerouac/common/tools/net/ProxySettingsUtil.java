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
package com.github.joekerouac.common.tools.net;

import java.io.IOException;

import com.github.joekerouac.common.tools.constant.Const;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.net.proxy.SystemProxyService;
import com.github.joekerouac.common.tools.net.proxy.impl.MacSystemProxyService;
import com.github.joekerouac.common.tools.net.proxy.impl.WindowsSystemProxyService;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class ProxySettingsUtil {

    static {
        if (Const.IS_WINDOWS) {
            PROXY_SERVICE = new WindowsSystemProxyService();
        } else if (Const.IS_MAC_OS) {
            PROXY_SERVICE = new MacSystemProxyService();
        } else {
            PROXY_SERVICE = null;
        }
    }

    private static final SystemProxyService PROXY_SERVICE;

    /**
     * 打开系统代理，并设置指定代理服务
     * 
     * @param settings
     *            代理服务器配置
     * @throws IOException
     *             IO异常
     */
    public static void setProxy(HttpProxySettings settings) throws IOException {
        Assert.assertTrue(PROXY_SERVICE != null, "只有mac和Windows系统支持设置网络代理",
            ExceptionProviderConst.UnsupportedOperationExceptionProvider);
        PROXY_SERVICE.setProxy(settings);
    }

    /**
     * 关闭当前代理
     * 
     * @throws IOException
     *             IO异常
     */
    public static void closeProxy() throws IOException {
        Assert.assertTrue(PROXY_SERVICE != null, "只有mac和Windows系统支持取消设置网络代理",
            ExceptionProviderConst.UnsupportedOperationExceptionProvider);
        PROXY_SERVICE.closeProxy();
    }

    /**
     * 获取当前代理配置
     *
     * @return 当前代理配置，如果当前代理配置为不生效则返回null
     * @throws IOException
     *             IO异常
     */
    public static HttpProxySettings getCurrentProxySettings() throws IOException {
        Assert.assertTrue(PROXY_SERVICE instanceof WindowsSystemProxyService,
            StringUtils.format("只有Windows系统支持获取当前网络代理配置"),
            ExceptionProviderConst.UnsupportedOperationExceptionProvider);
        return ((WindowsSystemProxyService)PROXY_SERVICE).getCurrentProxySettings();
    }

}
