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
package com.github.joekerouac.common.tools.resource;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.github.joekerouac.common.tools.enums.EnumInterface;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.enums.ResourceType;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 需要添加支持只需要在这个类中添加相关代码即可
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceBuilder {

    /**
     * 资源初始化器
     */
    public static volatile Consumer<Resource> RESOURCE_INIT_FACTORY;

    /**
     * 构建指定类型的资源
     * 
     * @param param
     *            资源参数
     * @return 构建的资源
     */
    public static Resource build(Map<String, String> param) {
        Assert.notNull(param, "资源map不能为null", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        String typeStr = param.get("type");
        Assert.notBlank(typeStr, "资源类型不能为空", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        ResourceType type = EnumInterface.getByCode(typeStr, ResourceType.class);
        Resource resource = type.build(param);
        Consumer<Resource> resourceInitFactory = RESOURCE_INIT_FACTORY;
        if (resourceInitFactory != null) {
            resourceInitFactory.accept(resource);
        }
        return resource;
    }

    /**
     * 资源序列化为map
     * 
     * @param resource
     *            资源
     * @return 序列化后的map
     */
    public static Map<String, String> resourceToMap(Resource resource) {
        ResourceType type = resource.type();
        // 这里防止toMap返回的是不可变map，我们重新构建一个map
        Map<String, String> map = new HashMap<>(resource.toMap());
        map.put("type", type.code());
        return map;
    }

}
