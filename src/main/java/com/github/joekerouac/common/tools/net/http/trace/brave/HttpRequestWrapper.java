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
package com.github.joekerouac.common.tools.net.http.trace.brave;

import java.net.URISyntaxException;

import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.net.URIAuthority;

import brave.http.HttpClientRequest;

/**
 * @author JoeKerouac
 * @date 2023-02-17 09:22
 * @since 1.0.0
 */
public class HttpRequestWrapper extends HttpClientRequest {

    private final HttpRequest request;

    HttpRequestWrapper(HttpRequest request) {
        this.request = request;
    }

    @Override
    public Object unwrap() {
        return request;
    }

    @Override
    public String method() {
        return request.getMethod();
    }

    @Override
    public String path() {
        return request.getPath();
    }

    @Override
    public String url() {
        try {
            return request.getUri().toString();
        } catch (URISyntaxException e) {
            URIAuthority authority = request.getAuthority();
            StringBuilder sb = new StringBuilder();
            sb.append(request.getScheme()).append("://").append(authority.getHostName());
            if (authority.getPort() > 0) {
                sb.append(":").append(authority.getPort());
            }
            sb.append(request.getPath());
            return sb.toString();
        }
    }

    @Override
    public String header(String name) {
        try {
            return request.getHeader(name).getValue();
        } catch (ProtocolException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void header(String name, String value) {
        request.setHeader(name, value);
    }

}
