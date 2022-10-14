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

import java.util.Date;
import java.util.List;

/**
 * cookie管理
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface CookieStore {

    /**
     * 添加cookie，如果要添加的cookie已经存在，那么将移除老的cookie，如果要添加的cookie已经过期，那么将不会添加，不过老的cookie仍然会被 删除；
     * 
     * @param cookie
     *            要添加的cookie
     */
    void addCookie(Cookie cookie);

    /**
     * 获取当前所有cookie
     *
     * @return 当前所有cookie
     */
    List<Cookie> getCookies();

    /**
     * 将在指定日期到期的cookie全部清除
     *
     * @return 如果有任何cookie被清除将会返回true
     */
    boolean clearExpired(Date date);

    /**
     * 清除所有cookie
     */
    void clear();

}
