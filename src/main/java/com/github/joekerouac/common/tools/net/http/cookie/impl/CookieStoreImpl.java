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
package com.github.joekerouac.common.tools.net.http.cookie.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.cookie.BasicCookieStore;

import com.github.joekerouac.common.tools.net.http.cookie.Cookie;
import com.github.joekerouac.common.tools.net.http.cookie.CookieStore;
import com.github.joekerouac.common.tools.net.http.cookie.CookieUtil;

/**
 * 默认cookieStore实现，依赖于HttpClient
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class CookieStoreImpl implements CookieStore {

    private BasicCookieStore basicCookieStore;

    public CookieStoreImpl() {
        this.basicCookieStore = new BasicCookieStore();
    }

    /**
     * 获取HttpClient的cookieStore
     * 
     * @return HttpClient的CookieStore
     */
    public org.apache.hc.client5.http.cookie.CookieStore getHttpClientCookieStore() {
        return basicCookieStore;
    }

    @Override
    public void addCookie(Cookie cookie) {
        basicCookieStore.addCookie(CookieUtil.convert(cookie));
    }

    @Override
    public List<Cookie> getCookies() {
        return basicCookieStore.getCookies().stream().map(CookieUtil::convert).collect(Collectors.toList());
    }

    @Override
    public boolean clearExpired(Date date) {
        return basicCookieStore.clearExpired(date);
    }

    @Override
    public void clear() {
        basicCookieStore.clear();
    }
}
