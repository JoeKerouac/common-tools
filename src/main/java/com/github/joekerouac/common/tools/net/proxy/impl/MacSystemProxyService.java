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
import java.util.HashMap;
import java.util.Map;

import com.github.joekerouac.common.tools.console.CommandConsole;
import com.github.joekerouac.common.tools.console.CommandConsoleFactory;
import com.github.joekerouac.common.tools.console.CommandResult;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.net.HttpProxySettings;
import com.github.joekerouac.common.tools.net.proxy.SystemProxyService;
import com.github.joekerouac.common.tools.string.StringUtils;

import lombok.CustomLog;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
@CustomLog
public class MacSystemProxyService implements SystemProxyService {

    public static final String SUDO_PWD = "SUDO_PWD";

    private static final String GET_NETWORK_SERVICES = "networksetup -listallnetworkservices";

    /**
     * 设置http网络代理
     */
    private static final String SET_WEB_PROXY =
        "echo ${SUDO_PWD} | sudo -S networksetup -setwebproxy \"${SERVICE}\" ${HOST} ${PORT}";

    /**
     * 设置https网络代理
     */
    private static final String SET_SECURE_WEB_PROXY =
        "echo ${SUDO_PWD} | sudo -S networksetup -setsecurewebproxy \"${SERVICE}\" ${HOST} ${PORT}";

    /**
     * 打开http网络代理
     */
    private static final String OPEN_WEB_PROXY =
        "echo ${SUDO_PWD} | sudo -S networksetup -setwebproxystate \"${SERVICE}\" ${ACTION}";

    /**
     * 打开https网络代理
     */
    private static final String OPEN_SECURE_WEB_PROXY =
        "echo ${SUDO_PWD} | sudo -S networksetup -setsecurewebproxystate \"${SERVICE}\" ${ACTION}";

    private CommandConsole console = CommandConsoleFactory.create(LOGGER);

    @Override
    public void setProxy(final HttpProxySettings settings) throws IOException {
        CommandResult result = console.execOrThrow(GET_NETWORK_SERVICES, ErrorCodeEnum.UNKNOWN_EXCEPTION,
            StringUtils.format("获取mac网络配置失败，当前执行命令： [{}]", GET_NETWORK_SERVICES));

        String host = settings.getHost();
        int port = settings.getPort();
        Map<String, String> env = new HashMap<>();
        env.put("HOST", host);
        env.put("PORT", Integer.toString(port));
        env.put("ACTION", "on");
        env.put(SUDO_PWD, System.getProperty(SUDO_PWD));

        String msg = result.getMsg();
        String[] split = msg.split("\n");
        // 跳过第一行，第一行不是我们要的结果，是一个说明
        for (int i = 1; i < split.length; i++) {
            String service = split[i];
            if (service.startsWith("*")) {
                LOGGER.debug("网卡[{}]已经禁用，跳过该网卡", service);
                continue;
            }

            env.put("SERVICE", service);
            LOGGER.debug("为网卡 [{}] 设置代理", service);
            console.execOrThrow(SET_WEB_PROXY, env, ErrorCodeEnum.UNKNOWN_EXCEPTION, "http代理设置失败");
            console.execOrThrow(SET_SECURE_WEB_PROXY, env, ErrorCodeEnum.UNKNOWN_EXCEPTION, "https代理设置失败");
            console.execOrThrow(OPEN_WEB_PROXY, env, ErrorCodeEnum.UNKNOWN_EXCEPTION, "https代理开启失败");
            console.execOrThrow(OPEN_SECURE_WEB_PROXY, env, ErrorCodeEnum.UNKNOWN_EXCEPTION, "https代理开启失败");
            LOGGER.info("网卡 [{}] 设置代理 [{}:{}] 成功", service, host, port);
        }
    }

    @Override
    public void closeProxy() throws IOException {

        CommandResult result = console.execOrThrow(GET_NETWORK_SERVICES, ErrorCodeEnum.UNKNOWN_EXCEPTION,
            StringUtils.format("获取mac网络配置失败，当前执行命令： [{}]", GET_NETWORK_SERVICES));

        Map<String, String> env = new HashMap<>();
        env.put("ACTION", "off");
        env.put(SUDO_PWD, System.getProperty(SUDO_PWD));

        String msg = result.getMsg();
        String[] split = msg.split("\n");
        // 跳过第一行，第一行不是我们要的结果，是一个说明
        for (int i = 1; i < split.length; i++) {
            String service = split[i];
            if (service.startsWith("*")) {
                LOGGER.debug("网卡[{}]已经禁用，跳过该网卡", service);
                continue;
            }

            env.put("SERVICE", service);
            LOGGER.debug("为网卡 [{}] 取消设置代理", service);
            console.execOrThrow(OPEN_WEB_PROXY, env, ErrorCodeEnum.UNKNOWN_EXCEPTION, "https代理关闭失败");
            console.execOrThrow(OPEN_SECURE_WEB_PROXY, env, ErrorCodeEnum.UNKNOWN_EXCEPTION, "https代理关闭失败");
            LOGGER.info("网卡 [{}] 取消设置代理成功", service);
        }
    }

}
