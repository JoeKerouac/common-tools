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

import com.github.joekerouac.common.tools.exception.CommonException;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.net.http.cookie.Cookie;

import lombok.Setter;

/**
 * 将HttpClient的cookie包装为系统cookie
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class CookieImpl implements Cookie {

    /**
     * cookie name
     */
    private final String name;

    /**
     * cookie value
     */
    private String value;

    /**
     * cookie domain
     */
    @Setter
    private String domain;

    /**
     * cookie到期时间
     */
    @Setter
    private Date expiryDate;

    /**
     * cookie所属path
     */
    @Setter
    private String path;

    /**
     * cookie是否需要在安全连接下传输
     */
    @Setter
    private boolean secure;

    public CookieImpl(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public Date getExpiryDate() {
        return expiryDate;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public boolean isExpired(Date date) {
        if (date == null) {
            throw new CommonException(ErrorCodeEnum.CODE_ERROR, "日期不能为null");
        }

        return expiryDate != null && expiryDate.getTime() <= date.getTime();
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public boolean isPersistent() {
        return expiryDate == null;
    }
}
