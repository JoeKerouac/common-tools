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
package com.github.joekerouac.common.tools.net.http.dns;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author JoeKerouac
 * @date 2024-01-13 17:37:19
 * @since 2.1.4
 */
public interface IDnsResolver {

    /**
     * 解析给定host
     *
     * @param host
     *            host
     * @return 解析地址，如果未知则返回null
     */
    default InetAddress[] resolve(String host) throws UnknownHostException {
        return InetAddress.getAllByName(host);
    }

    /**
     * 解析给定host为全限定域名（FQDN）
     * 
     * @param host
     *            host
     * @return 全限定域名
     */
    default String resolveCanonicalHostname(String host) throws UnknownHostException {
        if (host == null) {
            return null;
        }
        final InetAddress in = InetAddress.getByName(host);
        final String canonicalServer = in.getCanonicalHostName();
        if (in.getHostAddress().contentEquals(canonicalServer)) {
            return host;
        }
        return canonicalServer;
    }

}
