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
package com.github.joekerouac.common.tools.net.http.cookie;

import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;

import com.github.joekerouac.common.tools.net.http.cookie.impl.CookieImpl;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * cookie工具
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CookieUtil {

    /**
     * 将HttpClient的cookie转换为系统cookie
     * 
     * @param cookie
     *            HttpClient的cookie
     * @return 系统cookie
     */
    public static Cookie convert(org.apache.hc.client5.http.cookie.Cookie cookie) {
        CookieImpl cookieImpl = new CookieImpl(cookie.getName(), cookie.getValue());
        cookieImpl.setDomain(cookie.getDomain());
        cookieImpl.setExpiryDate(cookie.getExpiryDate());
        cookieImpl.setPath(cookie.getPath());
        cookieImpl.setSecure(cookie.isSecure());
        return cookieImpl;
    }

    /**
     * 将系统cookie转换为HttpClient的cookie
     * 
     * @param cookie
     *            系统cookie
     * @return HttpClient的cookie
     */
    public static org.apache.hc.client5.http.cookie.Cookie convert(Cookie cookie) {
        BasicClientCookie basicClientCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
        basicClientCookie.setDomain(cookie.getDomain());
        basicClientCookie.setExpiryDate(cookie.getExpiryDate());
        basicClientCookie.setPath(cookie.getPath());
        basicClientCookie.setSecure(cookie.isSecure());
        return basicClientCookie;
    }

}
