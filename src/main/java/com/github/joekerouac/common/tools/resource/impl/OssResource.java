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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.OSSObject;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.constant.StringConst;
import com.github.joekerouac.common.tools.io.IOUtils;
import com.github.joekerouac.common.tools.resource.Resource;
import com.github.joekerouac.common.tools.enums.ResourceType;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * OSS类型的资源
 * 
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OssResource implements Resource {

    private static final Class<?>[] INIT_ARG_TYPES = new Class[] {OSS.class};

    /**
     * {@link #buckName}字段名
     */
    public static final String BUCK_NAME_FIELD_NAME = "buckName";

    /**
     * {@link #dir}字段名
     */
    public static final String DIR_FIELD_NAME = "dir";

    /**
     * {@link #fileName}字段名
     */
    public static final String FILE_NAME_FIELD_NAME = "fileName";

    private transient OSS oss;

    @Getter
    @EqualsAndHashCode.Include
    private final String buckName;

    @Getter
    @EqualsAndHashCode.Include
    private final String dir;

    @Getter
    @EqualsAndHashCode.Include
    private final String fileName;

    private transient volatile OSSObject ossObject;

    private transient volatile byte[] content;

    public OssResource(Map<String, String> map) {
        this(map.get(BUCK_NAME_FIELD_NAME), map.get(DIR_FIELD_NAME), map.get(FILE_NAME_FIELD_NAME));
    }

    public OssResource(String buckName, String dir, String fileName) {
        Assert.notBlank(buckName, "oss buck name must not be blank",
            ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.notBlank(dir, "oss dir must not be blank", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.notBlank(fileName, "oss file name must not be blank",
            ExceptionProviderConst.IllegalArgumentExceptionProvider);
        this.buckName = buckName;
        this.dir = StringUtils.trim(dir, Character.toString(StringConst.SLASH));
        this.fileName = StringUtils.trim(fileName, Character.toString(StringConst.SLASH));
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put(BUCK_NAME_FIELD_NAME, buckName);
        map.put(DIR_FIELD_NAME, dir);
        map.put(FILE_NAME_FIELD_NAME, fileName);
        return map;
    }

    @Override
    public void init(Object[] args) {
        Resource.argsCheck(args, INIT_ARG_TYPES);
        Assert.notNull(args[0], "参数不能为空", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        this.oss = (OSS)args[0];
    }

    @Override
    public ResourceType type() {
        return ResourceType.OSS_RESOURCE;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        Assert.notNull(oss, StringUtils.format("资源未初始化： [{}:{}:{}]", buckName, dir, fileName),
            ExceptionProviderConst.IllegalStateExceptionProvider);
        readFromRemote();
        return new ByteArrayInputStream(content);

    }

    @Override
    public String getName() {
        return dir + StringConst.SLASH + fileName;
    }

    @Override
    public URL getUrl() throws IOException {
        return null;
    }

    /**
     * 获取最后更新时间
     * 
     * @return 最后更新时间
     */
    public Date getLastModified() {
        readFromRemote();
        return ossObject.getObjectMetadata().getLastModified();
    }

    /**
     * 从远程读取OSS资源，注意，该读取会缓存
     */
    private void readFromRemote() {
        if (ossObject == null) {
            synchronized (this) {
                if (ossObject == null) {
                    // 注意，如果指定的key（文件）不存在，那么将直接抛出异常
                    this.ossObject = oss.getObject(buckName, dir + StringConst.SLASH + fileName);
                    this.content = IOUtils.read(ossObject.getObjectContent(), true);
                }
            }
        }
    }

}
