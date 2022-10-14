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
package com.github.joekerouac.common.tools.reflect.bean;

import java.util.ArrayList;
import java.util.List;

import com.github.joekerouac.common.tools.reflect.bean.converter.*;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 类型转换器注册表
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TypeConverterRegistry {

    private static final List<TypeConverter> TYPE_CONVERTER_LIST = new ArrayList<>();

    static {
        TYPE_CONVERTER_LIST.add(new ClassHierarchicalTypeConverter());
        TYPE_CONVERTER_LIST.add(new StringToBasicTypeConverter());
        TYPE_CONVERTER_LIST.add(new StringToFileTypeConverter());
        TYPE_CONVERTER_LIST.add(new AllToStringTypeConverter());
        TYPE_CONVERTER_LIST.add(new StringToEnumConverter());
    }

    /**
     * 获取所有的类型转换器
     * 
     * @return 所有的类型转换器
     */
    public static List<TypeConverter> getAllTypeConverter() {
        return new ArrayList<>(TYPE_CONVERTER_LIST);
    }

    /**
     * 根据源类型以及目标类型查找转换器
     * 
     * @param srcType
     *            原类型
     * @param targetType
     *            目标类型
     * @return 转换器
     */
    public static TypeConverter findConverter(Class<?> srcType, Class<?> targetType) {
        for (TypeConverter converter : TYPE_CONVERTER_LIST) {
            if (converter.test(srcType, targetType)) {
                return converter;
            }
        }
        return null;
    }

}
