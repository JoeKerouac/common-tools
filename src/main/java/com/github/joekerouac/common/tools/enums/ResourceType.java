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
package com.github.joekerouac.common.tools.enums;

import java.lang.reflect.Constructor;
import java.util.Map;

import com.github.joekerouac.common.tools.resource.Resource;
import com.github.joekerouac.common.tools.resource.exception.ResourceException;
import com.github.joekerouac.common.tools.resource.impl.*;
import com.github.joekerouac.common.tools.string.StringUtils;

import lombok.Getter;

/**
 * 资源类型
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public enum ResourceType implements EnumInterface {

    URL_RESOURCE("URL_RESOURCE", "URL类型的资源", "URL_RESOURCE", URLResource.class),

    FILE_RESOURCE("FILE_RESOURCE", "本地文件类型的资源", "FILE_RESOURCE", FileResource.class),

    CLASS_PATH("CLASS_PATH", "类路径文件", "CLASS_PATH", ClassPathResource.class),

    OSS_RESOURCE("OSS_RESOURCE", "oss配置文件资源", "OSS_RESOURCE", OssResource.class),

    MAVEN_JAR_RESOURCE("MAVEN_JAR_RESOURCE", "maven jar资源", "MAVEN_JAR_RESOURCE", MavenJarResource.class),

    TEXT_RESOURCE("TEXT_RESOURCE", "text资源", "TEXT_RESOURCE", TextResource.class),

    ;

    static {
        // 重复检测
        EnumInterface.duplicateCheck(ResourceType.class);
    }

    private final String code;
    private final String desc;
    private final String englishName;
    @Getter
    private final Class<? extends Resource> resourceClass;
    private final Constructor<? extends Resource> constructor;

    ResourceType(String code, String desc, String englishName, Class<? extends Resource> clazz) {
        this.code = code;
        this.desc = desc;
        this.englishName = englishName;
        this.resourceClass = clazz;
        try {
            this.constructor = clazz.getConstructor(Map.class);
        } catch (NoSuchMethodException e) {
            throw new ResourceException(StringUtils.format("resource [{}] 定义错误，类中不包含仅有一个Map的构造器"));
        }
    }

    /**
     * 构建资源
     * 
     * @param map
     *            包含资源必要信息的map
     * @param <T>
     *            资源实际类型
     * @return 对应的资源
     */
    @SuppressWarnings("unchecked")
    public <T extends Resource> T build(Map<String, String> map) {
        try {
            return (T)constructor.newInstance(map);
        } catch (Throwable e) {
            throw new ResourceException(StringUtils.format("构建 [{}] 类型的资源失败", resourceClass), e);
        }
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String desc() {
        return desc;
    }

    @Override
    public String englishName() {
        return englishName;
    }
}
