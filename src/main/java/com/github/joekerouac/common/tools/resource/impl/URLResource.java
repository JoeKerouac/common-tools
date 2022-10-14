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
package com.github.joekerouac.common.tools.resource.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.resource.Resource;
import com.github.joekerouac.common.tools.enums.ResourceType;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * 没有认证信息的URL资源
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class URLResource implements Resource {

    /**
     * {@link #url}字段名
     */
    public static final String URL_FIELD_NAME = "url";

    /**
     * {@link #name}字段名
     */
    public static final String NAME_FIELD_NAME = "name";

    /**
     * 资源URL字符串
     */
    private final String url;

    /**
     * 资源名称，例如test.jar
     */
    private final String name;

    /**
     * URL
     */
    private transient URL urlObj;

    public URLResource(Map<String, String> map) {
        this(map.get(URL_FIELD_NAME), map.get(NAME_FIELD_NAME));
    }

    public URLResource(String url, String name) {
        Assert.notBlank(url, "url must not be blank", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.notBlank(name, "name must not be blank", ExceptionProviderConst.IllegalArgumentExceptionProvider);

        this.url = url;
        this.name = name;
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put(URLResource.NAME_FIELD_NAME, name);
        map.put(URLResource.URL_FIELD_NAME, url);
        return map;
    }

    @Override
    public ResourceType type() {
        return ResourceType.URL_RESOURCE;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return getUrl().openStream();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public synchronized URL getUrl() throws IOException {
        if (urlObj == null) {
            urlObj = new URL(url);
        }
        return urlObj;
    }

    @Override
    public String toString() {
        return "URLResource{" + "url='" + url + '\'' + ", name='" + name + '\'' + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        URLResource that = (URLResource)o;
        return url.equals(that.url) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, name);
    }
}
