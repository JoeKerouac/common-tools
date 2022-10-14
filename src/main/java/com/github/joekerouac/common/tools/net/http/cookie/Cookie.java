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

/**
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface Cookie {

    /**
     * 获取cookie的名字
     * 
     * @return cookie的名字
     */
    String getName();

    /**
     * 获取cookie的值
     * 
     * @return cookie的值
     */
    String getValue();

    /**
     * 获取cookie的过期时间
     * 
     * @return cookie的过期时间
     */
    Date getExpiryDate();

    /**
     * 获取cookie所属的domain
     * 
     * @return domain
     */
    String getDomain();

    /**
     * 获取cookie的Path
     * 
     * @return path
     */
    String getPath();

    /**
     * 指示该cookie是否需要安全链接（SSL）
     * 
     * @return 返回true表示该cookie需要使用安全链接发送
     */
    boolean isSecure();

    /**
     * 指示cookie是否应该在会话结束后丢弃
     * 
     * @return 返回true表示不需要在会话结束后丢弃
     */
    boolean isPersistent();

    /**
     * cookie在指定时间是否过期
     * 
     * @param date
     *            指定时间
     * @return 返回true表示cookie已经过期
     */
    boolean isExpired(Date date);

}
