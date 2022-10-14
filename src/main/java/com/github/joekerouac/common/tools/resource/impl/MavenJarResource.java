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
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotBlank;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.constant.StringConst;
import com.github.joekerouac.common.tools.resource.Resource;
import com.github.joekerouac.common.tools.enums.ResourceType;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.Getter;

/**
 * maven jar resource
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class MavenJarResource implements Resource {

    /**
     * jar包后缀
     */
    private static final String JAR_SUFFIX = ".jar";

    /**
     * {@link #mavenRepo}字段名
     */
    public static final String MAVEN_REPO_FIELD_NAME = "mavenRepo";

    /**
     * {@link #groupId}字段名
     */
    public static final String GROUP_ID_FIELD_NAME = "groupId";

    /**
     * {@link #artifactId}字段名
     */
    public static final String ARTIFACT_ID_FIELD_NAME = "artifactId";

    /**
     * {@link #version}字段名
     */
    public static final String VERSION_FIELD_NAME = "version";

    /**
     * 账号
     */
    public static final String USERNAME = "username";

    /**
     * 密码
     */
    public static final String PASSWORD = "password";

    /**
     * 资源URL，设置完maven仓库地址后初始化
     */
    private transient final String resourceUrl;

    /**
     * maven仓库地址
     */
    private final String mavenRepo;

    /**
     * maven groupId
     */
    @Getter
    private final String groupId;

    /**
     * maven artifactId
     */
    @Getter
    private final String artifactId;

    /**
     * maven version
     */
    @Getter
    private final String version;

    /**
     * maven仓库账号，允许为空
     */
    private final String username;

    /**
     * maven仓库密码，允许为空，后续需要考虑密码安全性
     */
    private final String password;

    /**
     * 名字
     */
    private final transient String name;

    public MavenJarResource(Map<String, String> map) {
        this(map.get(MAVEN_REPO_FIELD_NAME), map.get(GROUP_ID_FIELD_NAME), map.get(ARTIFACT_ID_FIELD_NAME),
            map.get(VERSION_FIELD_NAME), map.get(USERNAME), map.get(PASSWORD));
    }

    public MavenJarResource(@NotBlank String mavenRepo, @NotBlank String groupId, @NotBlank String artifactId,
        @NotBlank String version, String username, String password) {
        Assert.notBlank(mavenRepo, "mavenRepo不能为空", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.notBlank(groupId, "group id不能为空", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.notBlank(artifactId, "artifact id不能为空", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.notBlank(version, "version不能为空", ExceptionProviderConst.IllegalArgumentExceptionProvider);

        this.mavenRepo = mavenRepo;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.name = groupId.replaceAll("\\.", Character.toString(StringConst.UNDERLINE)) + StringConst.HYPHEN
            + artifactId + StringConst.HYPHEN + version + JAR_SUFFIX;
        // 拼接出来对应的maven仓库的实际地址
        this.resourceUrl = mavenRepo + StringConst.SLASH + groupId.replaceAll("\\.", "/") + StringConst.SLASH
            + artifactId + StringConst.SLASH + version + StringConst.SLASH + artifactId + StringConst.HYPHEN + version
            + JAR_SUFFIX;
        this.username = username;
        this.password = password;
    }

    @Override
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put(MAVEN_REPO_FIELD_NAME, mavenRepo);
        map.put(GROUP_ID_FIELD_NAME, groupId);
        map.put(ARTIFACT_ID_FIELD_NAME, artifactId);
        map.put(VERSION_FIELD_NAME, version);
        map.put(USERNAME, username);
        map.put(PASSWORD, password);
        return map;
    }

    @Override
    public ResourceType type() {
        return ResourceType.MAVEN_JAR_RESOURCE;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        final URL url = new URL(resourceUrl);
        final URLConnection urlConnection = url.openConnection();
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            String author = String.format("%s:%s", username, password);
            urlConnection.addRequestProperty("Authorization",
                "Basic " + Base64.getEncoder().encodeToString(author.getBytes(Charset.defaultCharset())));
        }
        return urlConnection.getInputStream();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public URL getUrl() throws IOException {
        return new URL(resourceUrl);
    }

}
