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
package com.github.joekerouac.common.tools.net.http;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.github.joekerouac.common.tools.concurrent.FutureCallback;
import com.github.joekerouac.common.tools.constant.Const;
import com.github.joekerouac.common.tools.net.http.config.IHttpConfig;
import com.github.joekerouac.common.tools.net.http.exception.NetException;
import com.github.joekerouac.common.tools.net.http.exception.UnknownException;
import com.github.joekerouac.common.tools.net.http.request.IHttpMethod;
import com.github.joekerouac.common.tools.net.http.response.IHttpResponse;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * HTTP请求基类
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@Getter
@Setter
@ToString
public abstract class AbstractIHttpRequest implements IHttpRequest {

    /**
     * 发起请求的客户端
     */
    @ToString.Exclude
    protected final IHttpClient client;

    /**
     * Http配置
     */
    protected final IHttpConfig httpConfig;

    /**
     * contentType，默认json
     */
    @NotBlank
    protected final String contentType;

    /**
     * 请求URL
     */
    @NotBlank
    protected final String url;

    /**
     * 请求头
     */
    @NotNull
    protected final Map<String, String> headers;

    /**
     * 请求
     */
    @NotBlank
    protected final String charset;

    protected final IHttpMethod method;

    protected AbstractIHttpRequest(String url, IHttpMethod method, String contentType, String charset,
        Map<String, String> headers, IHttpClient client, IHttpConfig config) {
        Assert.argNotBlank(url, "url");
        Assert.argNotNull(client, "client");
        Assert.argNotNull(headers, "headers");
        Assert.argNotNull(method, "method");

        this.url = url;
        this.method = method;
        this.contentType = StringUtils.isBlank(contentType) ? ContentType.CONTENT_TYPE_JSON : contentType;
        this.charset = StringUtils.isBlank(charset) ? Const.DEFAULT_CHARSET.name() : charset;
        this.headers = Collections.unmodifiableMap(headers);
        this.client = client;
        this.httpConfig = config;
    }

    /**
     * 执行网络请求
     * 
     * @return 请求结果
     * @throws IOException
     *             网络IO异常
     */
    @Override
    public IHttpResponse exec() throws IOException {
        try {
            return client.execute(this, null).get();
        } catch (InterruptedException e) {
            throw new NetException("执行被中断", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException)cause;
            } else {
                throw new UnknownException(cause);
            }
        }
    }

    @Override
    public Future<IHttpResponse> exec(FutureCallback<IHttpResponse> futureCallback) {
        return client.execute(this, futureCallback);
    }

    @Override
    public String getMethod() {
        return null;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public IHttpConfig getHttpConfig() {
        return httpConfig;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public String getCharset() {
        return charset;
    }

    /**
     * 请求构建器
     */
    @SuppressWarnings("unchecked")
    public static abstract class AbstractBuilder<T extends AbstractIHttpRequest> {

        protected final IHttpMethod method;

        /**
         * Http配置
         */
        protected IHttpConfig httpConfig;

        /**
         * contentType，默认json
         */
        protected String contentType;

        /**
         * 请求URL
         */
        protected String url;

        /**
         * 请求头
         */
        protected Map<String, String> headers;

        /**
         * URL参数
         */
        protected Map<String, String> queryParams;

        /**
         * 请求
         */
        protected String charset;

        /**
         * 请求client
         */
        protected IHttpClient client;

        /**
         * 解析URL，从URL中解析参数
         *
         * @param url
         *            url
         * @return 解析出来的参数列表
         */
        private Map<String, String> parse(String url) {
            int index = url.indexOf("?");
            if (index > 0) {
                String data = url.substring(index + 1);
                Map<String, String> map = new HashMap<>();
                Arrays.stream(data.split("&")).forEach(str -> {
                    String[] params = str.split("=");
                    if (params.length >= 2) {
                        map.put(params[0], params[1]);
                    } else if (params.length == 1) {
                        map.put(params[0], "");
                    }
                });
                return map;
            } else {
                return Collections.emptyMap();
            }
        }

        protected AbstractBuilder(String url, IHttpMethod method, IHttpClient client) {
            Assert.argNotBlank(url, "url");
            Assert.argNotNull(client, "client");
            Assert.argNotNull(method, "method");

            this.client = client;
            this.headers = new HashMap<>();
            this.queryParams = new HashMap<>();
            this.method = method;

            int index = url.indexOf("?");
            if (index > 0) {
                this.queryParams.putAll(parse(url));
                this.url = url.substring(0, index);
            } else {
                this.url = url;
            }
        }

        /**
         * 设置请求配置
         *
         * @param config
         *            请求配置
         * @return builder
         */
        public <B extends AbstractBuilder<T>> B config(IHttpConfig config) {
            this.httpConfig = config;
            return (B)this;
        }

        /**
         * 设置content-type
         *
         * @param contentType
         *            content-type
         * @return builder
         */
        public <B extends AbstractBuilder<T>> B contentType(String contentType) {
            this.contentType = contentType;
            return (B)this;
        }

        /**
         * 添加path param
         * 
         * @param key
         *            要替换的key
         * @param value
         *            对应的value
         */
        public void pathParam(String key, String value) {
            this.url = url.replace("{" + key + "}", value);
        }

        /**
         * 增加header
         *
         * @param key
         *            key
         * @param value
         *            value
         * @return builder
         */
        public <B extends AbstractBuilder<T>> B header(String key, String value) {
            this.headers.put(key, value);
            return (B)this;
        }

        /**
         * 增加query param
         *
         * @param key
         *            key
         * @param value
         *            value
         * @return builder
         */
        public <B extends AbstractBuilder<T>> B queryParam(String key, String value) {
            queryParams.put(key, value);
            return (B)this;
        }

        /**
         * 设置编码
         *
         * @param charset
         *            编码
         * @return builder
         */
        public <B extends AbstractBuilder<T>> B charset(String charset) {
            Charset.forName(charset);
            this.charset = charset;
            return (B)this;
        }

        /**
         * 获取完整URL
         * 
         * @return 包含query param的完整URL
         */
        protected String getUrl() {
            boolean first = true;

            String url = this.url;
            StringBuilder urlBuilder = new StringBuilder(url);
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                String tag = entry.getKey() + "=" + entry.getValue();
                if (first) {
                    urlBuilder.append("?").append(tag);
                    first = false;
                } else {
                    urlBuilder.append("&").append(tag);
                }
            }

            return urlBuilder.toString();
        }

        /**
         * 构建实际的request，由子类实现
         *
         * @return 构建好后的request
         */
        public abstract T build();
    }
}
