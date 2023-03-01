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

import org.apache.hc.core5.http.HttpResponse;

import brave.http.HttpClientResponse;

/**
 * @author JoeKerouac
 * @date 2023-02-17 09:23
 * @since 1.0.0
 */
public class HttpResponseWrapper extends HttpClientResponse {

    private final HttpResponse response;

    HttpResponseWrapper(HttpResponse response) {
        this.response = response;
    }

    @Override
    public Object unwrap() {
        return response;
    }

    @Override
    public int statusCode() {
        if (response == null) {
            return 0;
        }
        return response.getCode();
    }
}
