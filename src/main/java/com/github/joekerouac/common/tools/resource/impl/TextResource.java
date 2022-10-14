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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.github.joekerouac.common.tools.constant.Const;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.resource.Resource;
import com.github.joekerouac.common.tools.enums.ResourceType;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 以提供的文本作为资源
 * 
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class TextResource implements Resource {

    /**
     * {@link #name}字段名
     */
    public static final String NAME_FIELD_NAME = "name";

    /**
     * {@link #text}字段名
     */
    public static final String TEXT_FIELD_NAME = "text";

    /**
     * 资源名
     */
    @EqualsAndHashCode.Include
    private final String name;

    /**
     * 实际的数据
     */
    @EqualsAndHashCode.Include
    @Getter
    private final String text;

    public TextResource(Map<String, String> map) {
        this(map.get(NAME_FIELD_NAME), map.get(TEXT_FIELD_NAME));
    }

    public TextResource(String name, String text) {
        Assert.notBlank(name, "name must not be blank", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.notBlank(text, "text must not be blank", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        this.name = name;
        this.text = text;
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put(NAME_FIELD_NAME, name);
        map.put(TEXT_FIELD_NAME, text);
        return map;
    }

    @Override
    public ResourceType type() {
        return ResourceType.TEXT_RESOURCE;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(text.getBytes(Const.DEFAULT_CHARSET));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URL getUrl() throws IOException {
        return null;
    }
}
