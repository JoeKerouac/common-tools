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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.resource.Resource;
import com.github.joekerouac.common.tools.enums.ResourceType;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.Getter;

/**
 * 文件类型的资源，注意，只能是文件，不能是目录
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class FileResource implements Resource {

    /**
     * {@link #absolutePath}字段名
     */
    public static final String ABSOLUTE_PATH_FIELD_NAME = "absolutePath";

    @Getter
    private final String absolutePath;

    private transient final File resource;

    private transient final String name;

    public FileResource(Map<String, String> map) {
        this(map.get(ABSOLUTE_PATH_FIELD_NAME));
    }

    public FileResource(String absolutePath) {
        Assert.notBlank(absolutePath, "absolutePath must not be null",
            ExceptionProviderConst.IllegalArgumentExceptionProvider);

        this.absolutePath = absolutePath;
        this.resource = new File(absolutePath);
        this.name = resource.getName();
    }

    @Override
    public Map<String, String> toMap() {
        return Collections.singletonMap(ABSOLUTE_PATH_FIELD_NAME, absolutePath);
    }

    @Override
    public ResourceType type() {
        return ResourceType.FILE_RESOURCE;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(resource);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URL getUrl() throws IOException {
        return resource.toURI().toURL();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FileResource that = (FileResource)o;
        return absolutePath.equals(that.absolutePath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(absolutePath);
    }
}
