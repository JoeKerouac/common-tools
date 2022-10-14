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

import java.net.SocketOption;

/**
 * epoll的socket选项
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public final class EpollSocketOption<T> implements SocketOption<T> {

    /**
     * 是否打开TFO功能，打开可以在三次握手期间发送数据，最终达到减少一个RTT进而加快网络请求的目的，1表示客户端开启，2表示服务端开启，3表示同时开启
     */
    public static final EpollSocketOption<Integer> TCP_FASTOPEN =
        new EpollSocketOption<>("TCP_FASTOPEN", Integer.class);

    /**
     * 开启keepAlive后链接空闲多久后发送keepAlive探测分组，单位秒
     */
    public static final EpollSocketOption<Integer> TCP_KEEPIDLE =
        new EpollSocketOption<>("TCP_KEEPIDLE", Integer.class);

    /**
     * 开启keepAlive后两次探测的间隔，单位秒
     */
    public static final EpollSocketOption<Integer> TCP_KEEPINTVL =
        new EpollSocketOption<>("TCP_KEEPINTVL", Integer.class);

    /**
     * 关闭一个非活跃连接之前的最大重试次数
     */
    public static final EpollSocketOption<Integer> TCP_KEEPCNT = new EpollSocketOption<>("TCP_KEEPCNT", Integer.class);

    /**
     * 选项名
     */
    private final String name;

    /**
     * 选项类型
     */
    private final Class<T> type;

    private EpollSocketOption(String name, Class<T> type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<T> type() {
        return type;
    }
}
