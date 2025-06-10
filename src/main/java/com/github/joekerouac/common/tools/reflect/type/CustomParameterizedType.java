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
package com.github.joekerouac.common.tools.reflect.type;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.joekerouac.common.tools.collection.CollectionUtil;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author JoeKerouac
 * @date 2025-06-10 09:49
 * @since 4.0.0
 */
@Getter
@Setter
@ToString(callSuper = true)
public class CustomParameterizedType extends JavaType {

    /**
     * 拥有者类型，如果是嵌套类，则这个返回
     */
    private JavaType ownerType;

    /**
     * 该类型声明的泛型
     */
    @ToString.Exclude
    private LinkedHashMap<String, JavaType> bindings;

    @Override
    protected boolean equals(JavaType javaType, Map<Integer, List<JavaType>> contains,
        Map<Integer, List<JavaType>> targetContains) {
        if (!super.equals(javaType, contains, targetContains)) {
            return false;
        }

        CustomParameterizedType other = (CustomParameterizedType)javaType;
        int bindingsSize;
        if ((bindingsSize = CollectionUtil.size(bindings)) != CollectionUtil.size(other.bindings)) {
            return false;
        }

        if (bindingsSize > 0) {
            for (Map.Entry<String, JavaType> entry : bindings.entrySet()) {
                String key = entry.getKey();
                JavaType value = entry.getValue();
                JavaType binding = other.bindings.get(key);

                if (value == null ^ binding == null) {
                    return false;
                }

                if (value == null) {
                    continue;
                }

                if (!value.equals(binding, contains, targetContains)) {
                    return false;
                }
            }
        }

        return true;
    }
}
