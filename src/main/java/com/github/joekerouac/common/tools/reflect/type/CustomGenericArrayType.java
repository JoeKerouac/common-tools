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
import java.util.Objects;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 普通数组或者泛型的数组类型，例如T[]
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@Getter
@Setter
@ToString(callSuper = true)
public class CustomGenericArrayType extends JavaType {

    /**
     * 数组中存放的数据的类型
     */
    private JavaType componentType;

    /**
     * 数组维度
     */
    private int dimensions;

    /**
     * 该类型声明的泛型
     */
    @ToString.Exclude
    private LinkedHashMap<String, JavaType> bindings;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        CustomGenericArrayType that = (CustomGenericArrayType)o;
        return dimensions == that.dimensions && Objects.equals(componentType, that.componentType)
            && Objects.equals(bindings, that.bindings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), componentType, dimensions);
    }
}
