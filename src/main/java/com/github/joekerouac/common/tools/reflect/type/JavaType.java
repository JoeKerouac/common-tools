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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.ToString;

/**
 * java类型表示
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@Data
public class JavaType implements Type {

    /**
     * 类型名称，例如String（当该类型为泛型时该值为泛型名称，例如T，不是实际名称）
     */
    @NotNull
    protected String name;

    /**
     * 实际类型，可能为空，为空时{@link #rawClass}肯定不为空
     */
    @ToString.Exclude
    protected JavaType rawType;

    /**
     * 该类型的基本类型
     */
    protected Class<?> rawClass;

    /**
     * 原始类型
     */
    protected Type origin;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JavaType javaType = (JavaType)o;
        return equals(javaType, new HashMap<>(), new HashMap<>());
    }

    protected boolean equals(JavaType javaType, Map<Integer, List<JavaType>> contains,
        Map<Integer, List<JavaType>> targetContains) {
        if (javaType == null) {
            return false;
        }

        if (!Objects.equals(name, javaType.name)) {
            return false;
        }

        // 先确定rawClass，后比较
        getRawClass();
        javaType.getRawClass();

        if ((rawClass == null) ^ (javaType.rawClass == null)) {
            return false;
        }

        if (rawClass != null && !rawClass.equals(javaType.rawClass)) {
            return false;
        }

        int hash = this.hashCode();
        boolean recursion = false;
        List<JavaType> javaTypes = contains.get(hash);
        if (javaTypes != null) {
            for (JavaType type : javaTypes) {
                if (type == this) {
                    // 递归了
                    recursion = true;
                    break;
                }
            }
        }

        boolean targetRecursion = false;
        javaTypes = targetContains.get(hash);
        if (javaTypes != null) {
            for (JavaType type : javaTypes) {
                if (type == javaType) {
                    // 同样递归了
                    targetRecursion = true;
                    break;
                }
            }
        }

        if (targetRecursion != recursion) {
            return false;
        }

        if (recursion) {
            return true;
        }

        putCache(this, hash, contains);
        putCache(javaType, hash, targetContains);

        if (rawType == null ^ javaType.rawType == null) {
            return false;
        }

        if (rawType != null && !rawType.equals(javaType.rawType, contains, targetContains)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, rawClass);
    }

    private void putCache(JavaType javaType, int hash, Map<Integer, List<JavaType>> cache) {
        cache.compute(hash, (key, list) -> {
            if (list == null) {
                list = new ArrayList<>();
            }
            list.add(javaType);
            return list;
        });
    }

    public Class<?> getRawClass() {
        return getRawClass(new HashSet<>());
    }

    private Class<?> getRawClass(Set<JavaType> set) {
        if (rawClass != null) {
            return rawClass;
        }

        if (rawType != null) {
            if (set.contains(rawType)) {
                rawClass = Object.class;
            } else {
                set.add(rawType);
                rawClass = rawType.getRawClass(set);
            }
        }

        if (rawClass != null) {
            return rawClass;
        }

        // 理论上不会到这里
        throw new RuntimeException("未知异常");
    }

}
