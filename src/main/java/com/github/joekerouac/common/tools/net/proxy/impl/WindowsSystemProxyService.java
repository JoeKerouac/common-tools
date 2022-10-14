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
package com.github.joekerouac.common.tools.net.proxy.impl;

import java.io.IOException;

import com.github.joekerouac.common.tools.net.HttpProxySettings;
import com.github.joekerouac.common.tools.net.proxy.SystemProxyService;
import com.github.joekerouac.common.tools.registry.RegistryKey;
import com.github.joekerouac.common.tools.registry.RegistryValue;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class WindowsSystemProxyService implements SystemProxyService {

    private static final String SETTINGS_KEY = "Software\\Microsoft\\Windows\\CurrentVersion\\Internet Settings";

    private static final String PROXY_ENABLE_VALUE = "ProxyEnable";

    private static final String PROXY_SERVER_VALUE = "ProxyServer";

    private static final String PROXY_OVERRIDE_VALUE = "ProxyOverride";

    /**
     * 获取当前代理配置
     *
     * @return 当前代理配置，如果当前代理配置为不生效则返回null
     * @throws IOException
     *             IO异常
     */
    public HttpProxySettings getCurrentProxySettings() throws IOException {
        RegistryKey internetSettings = RegistryKey.HKEY_CURRENT_USER.openSubKey(SETTINGS_KEY, RegistryKey.ACCESS_ALL);
        try {
            RegistryValue proxyEnable = internetSettings.getValue(PROXY_ENABLE_VALUE);
            if (proxyEnable != null && proxyEnable.getData() != null && (Integer)proxyEnable.getData() == 0) {
                return null;
            }
            RegistryValue proxyServer = internetSettings.getValue(PROXY_SERVER_VALUE);
            RegistryValue proxyOverride = internetSettings.getValue(PROXY_OVERRIDE_VALUE);

            HttpProxySettings settings = new HttpProxySettings();

            String server = proxyServer != null ? (String)proxyServer.getData() : null;
            if (StringUtils.isNotBlank(server)) {
                // 这里认为配置都是 host:port 这种形式（理论上只要是通过Windows的配置界面配置的都是这个形式）
                int index = server.lastIndexOf(":");
                settings.setHost(server.substring(0, index));
                settings.setPort(Integer.parseInt(server.substring(index + 1)));
            }

            String proxyOverrideStr = proxyOverride != null ? (String)proxyOverride.getData() : null;
            settings.setOverride(proxyOverrideStr);
            return settings;
        } finally {
            internetSettings.close();
        }
    }

    @Override
    public void setProxy(final HttpProxySettings settings) throws IOException {
        RegistryKey internetSettings = RegistryKey.HKEY_CURRENT_USER.openSubKey(SETTINGS_KEY, RegistryKey.ACCESS_ALL);
        try {
            RegistryValue proxyEnable =
                new RegistryValue(internetSettings, PROXY_ENABLE_VALUE, RegistryValue.REG_DWORD);
            proxyEnable.setData(1);

            RegistryValue proxyServer = new RegistryValue(internetSettings, PROXY_SERVER_VALUE, RegistryValue.REG_SZ);
            proxyServer.setData(String.format("http://%s:%d", settings.getHost(), settings.getPort()));

            RegistryValue proxyOverride =
                new RegistryValue(internetSettings, PROXY_OVERRIDE_VALUE, RegistryValue.REG_SZ);
            proxyOverride.setData(StringUtils.getOrDefault(settings.getOverride(), ""));

            internetSettings.setValue(proxyEnable);
            internetSettings.setValue(proxyServer);
            internetSettings.setValue(proxyOverride);
            internetSettings.flushKey();
        } finally {
            internetSettings.close();
        }
    }

    @Override
    public void closeProxy() throws IOException {
        RegistryKey internetSettings = RegistryKey.HKEY_CURRENT_USER.openSubKey(SETTINGS_KEY, RegistryKey.ACCESS_ALL);
        try {
            RegistryValue proxyEnable =
                new RegistryValue(internetSettings, PROXY_ENABLE_VALUE, RegistryValue.REG_DWORD);
            proxyEnable.setData(0);

            internetSettings.setValue(proxyEnable);
            internetSettings.flushKey();
        } finally {
            internetSettings.close();
        }
    }
}
