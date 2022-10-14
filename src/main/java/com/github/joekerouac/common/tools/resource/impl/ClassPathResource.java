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
import java.util.*;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.reflect.ClassUtils;
import com.github.joekerouac.common.tools.resource.Resource;
import com.github.joekerouac.common.tools.enums.ResourceType;
import com.github.joekerouac.common.tools.resource.exception.ResourceLoadException;
import com.github.joekerouac.common.tools.resource.exception.ResourceNotExistException;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * ClassPathResource
 * 
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ClassPathResource implements Resource {

    /**
     * {@link #path}字段名
     */
    public static final String PATH_FIELD_NAME = "path";

    /**
     * 路径，例如com/github/joekerouac/common/tools/resource/impl/ClassPathResource
     */
    @EqualsAndHashCode.Include
    @Getter
    private final String path;

    private transient final InputStream stream;

    private transient final URL url;

    public ClassPathResource(Map<String, String> map) {
        this(map.get(PATH_FIELD_NAME));
    }

    public ClassPathResource(String path) {
        Assert.notBlank(path, "path must not be blank", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        this.path = path;
        ClassLoader loader = ClassUtils.getDefaultClassLoader();
        try {
            Enumeration<URL> resources = loader.getResources(path);

            List<URL> urls = new ArrayList<>();

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                urls.add(url);
            }

            if (urls.size() <= 0) {
                throw new ResourceNotExistException(path);
            }

            this.url = urls.get(urls.size() - 1);
            this.stream = url.openStream();
        } catch (IOException e) {
            throw new ResourceLoadException(e);
        }
    }

    @Override
    public Map<String, String> toMap() {
        return Collections.singletonMap(PATH_FIELD_NAME, path);
    }

    @Override
    public ResourceType type() {
        return ResourceType.CLASS_PATH;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return stream;
    }

    @Override
    public String getName() {
        return path;
    }

    @Override
    public URL getUrl() throws IOException {
        return url;
    }
}
