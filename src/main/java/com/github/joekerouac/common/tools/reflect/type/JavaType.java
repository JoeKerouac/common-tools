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
import java.util.HashSet;
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
    private Class<?> rawClass;

    public Class<?> getRawClass() {
        return getRawClass(new HashSet<>());
    }

    private Class<?> getRawClass(Set<JavaType> set) {
        if (rawClass != null) {
            return rawClass;
        }

        if (rawType != null) {
            if (set.contains(rawType)) {
                return Object.class;
            } else {
                set.add(rawType);
                return rawType.getRawClass(set);
            }
        }

        return null;
    }

}
