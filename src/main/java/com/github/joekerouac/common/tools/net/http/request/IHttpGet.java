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
package com.github.joekerouac.common.tools.net.http.request;

import java.util.Map;

import com.github.joekerouac.common.tools.net.http.AbstractIHttpRequest;
import com.github.joekerouac.common.tools.net.http.IHttpClient;
import com.github.joekerouac.common.tools.net.http.config.IHttpConfig;

/**
 * Http GET请求方法
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class IHttpGet extends AbstractIHttpRequest {

    IHttpGet(String url, String contentType, String charset, Map<String, String> headers, IHttpClient client,
        IHttpConfig config) {
        super(url, contentType, charset, headers, client, config);
    }

    @Override
    public String getMethod() {
        return "GET";
    }

    /**
     * 构建GET构建器
     * 
     * @param url
     *            url
     * @param client
     *            HTTP客户端
     * @return GET构建器
     */
    public static Builder builder(String url, IHttpClient client) {
        return new Builder(url, client);
    }

    public static final class Builder extends AbstractBuilder<IHttpGet> {
        protected Builder(String url, IHttpClient client) {
            super(url, client);
        }

        @Override
        public IHttpGet build() {
            return new IHttpGet(getUrl(), super.contentType, super.charset, super.headers, super.client,
                super.httpConfig);
        }
    }
}
