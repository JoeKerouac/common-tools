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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.joekerouac.common.tools.net.http.AbstractIHttpRequest;
import com.github.joekerouac.common.tools.net.http.ContentType;
import com.github.joekerouac.common.tools.net.http.IHttpClient;
import com.github.joekerouac.common.tools.net.http.config.IHttpConfig;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.validator.ValidationService;
import com.github.joekerouac.common.tools.validator.ValidationServiceImpl;

import lombok.Getter;

/**
 * @author JoeKerouac
 * @date 2023-07-03 16:02
 * @since 2.0.3
 */
public class IHttpGenericRequest extends AbstractIHttpRequest {

    private static final ValidationService VALIDATION_SERVICE = new ValidationServiceImpl();

    /**
     * 待上传文件
     */
    @Getter
    private final List<UploadFile> files;

    /**
     * 请求body，如果files不为空，则这个必须是form表单数据
     */
    @Getter
    protected String body;

    IHttpGenericRequest(String url, IHttpMethod method, String contentType, String charset, String body,
        Map<String, String> headers, IHttpClient client, IHttpConfig config, List<UploadFile> files) {
        super(url, method, contentType, charset, headers, client, config);
        this.body = body;
        this.files = files;
    }

    @Override
    public String getMethod() {
        return method.name();
    }

    /**
     * 构建POST构建器
     *
     * @param url
     *            请求url
     * @param client
     *            HTTP客户端
     * @return POST构建器
     */
    public static Builder builder(String url, IHttpMethod method, IHttpClient client) {
        return new Builder(url, method, client);
    }

    public static final class Builder extends AbstractIHttpRequest.AbstractBuilder<IHttpGenericRequest> {

        private final List<UploadFile> files = new ArrayList<>();

        /**
         * 请求body
         */
        private String body;

        private Builder(String url, IHttpMethod method, IHttpClient client) {
            super(url, method, client);
        }

        @Override
        public IHttpGenericRequest build() {
            return new IHttpGenericRequest(getUrl(), method, super.contentType, super.charset, body, super.headers,
                super.client, super.httpConfig, files);
        }

        /**
         * 添加要上传的文件
         *
         * @param file
         *            文件
         * @return builder
         */
        public Builder addFile(UploadFile file) {
            VALIDATION_SERVICE.validate(file);
            files.add(file);
            return this;
        }

        /**
         * 设置请求body
         *
         * @param body
         *            body
         * @return builder
         */
        public Builder body(String body) {
            this.body = body;
            return this;
        }

        /**
         * 设置form param
         *
         * @param key
         *            key
         * @param value
         *            value
         * @return builder
         */
        public Builder formParam(String key, String value) {
            String tag = key + "=" + value;
            if (StringUtils.isBlank(body)) {
                body = tag;
            } else {
                body = body + "&" + tag;
            }

            this.contentType = ContentType.CONTENT_TYPE_FORM;
            return this;
        }
    }

}
