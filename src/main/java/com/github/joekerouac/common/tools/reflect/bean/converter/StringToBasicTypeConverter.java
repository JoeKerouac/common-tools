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
package com.github.joekerouac.common.tools.reflect.bean.converter;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.github.joekerouac.common.tools.reflect.ReflectUtil;
import com.github.joekerouac.common.tools.reflect.bean.exception.PropCannotConvertException;
import com.github.joekerouac.common.tools.reflect.type.JavaTypeUtil;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * 将string转换为指定类型
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class StringToBasicTypeConverter extends AbstractTypeConverter {

    /**
     * valueOf方法名
     */
    private static final String VALUE_OF_METHOD_NAME = "valueOf";

    /**
     * valueOf缓存，主要是缓存method对象使用，不用每次反射获取一个新的method对象
     */
    private final Map<Class<?>, Method> cache;

    public StringToBasicTypeConverter() {
        Map<Class<?>, Method> map = new HashMap<>();
        map.put(Boolean.class, ReflectUtil.getMethod(Boolean.class, VALUE_OF_METHOD_NAME, String.class));
        map.put(Byte.class, ReflectUtil.getMethod(Byte.class, VALUE_OF_METHOD_NAME, String.class));
        map.put(Short.class, ReflectUtil.getMethod(Short.class, VALUE_OF_METHOD_NAME, String.class));
        map.put(Integer.class, ReflectUtil.getMethod(Integer.class, VALUE_OF_METHOD_NAME, String.class));
        map.put(Long.class, ReflectUtil.getMethod(Long.class, VALUE_OF_METHOD_NAME, String.class));
        map.put(Double.class, ReflectUtil.getMethod(Double.class, VALUE_OF_METHOD_NAME, String.class));
        map.put(Float.class, ReflectUtil.getMethod(Float.class, VALUE_OF_METHOD_NAME, String.class));
        map.put(Character.class, ReflectUtil.getMethod(Character.class, VALUE_OF_METHOD_NAME, char.class));
        this.cache = Collections.unmodifiableMap(map);
    }

    @Override
    public boolean test(Class<?> srcType, Class<?> targetType, Annotation[] annotations) {
        return String.class.equals(srcType) && (JavaTypeUtil.isBasic(targetType)
            || JavaTypeUtil.isGeneralType(targetType) || String.class.equals(targetType));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <S, T> T convert(Class<S> srcType, Class<T> targetType, S src, Annotation[] annotations) {
        if (String.class.equals(targetType)) {
            return (T)src;
        }

        String str = (String)src;
        if (StringUtils.isBlank(str)) {
            return null;
        }

        Class<T> type = targetType;
        if (JavaTypeUtil.isGeneralType(targetType)) {
            type = (Class<T>)JavaTypeUtil.boxed(targetType);
        }

        if (Boolean.class.equals(type) || Byte.class.equals(type) || Short.class.equals(type)
            || Integer.class.equals(type) || Long.class.equals(type) || Double.class.equals(type)
            || Float.class.equals(type)) {
            return ReflectUtil.invoke(type, cache.get(type), str);
        } else if (Character.class.equals(type)) {
            return ReflectUtil.invoke(type, cache.get(type), str.charAt(0));
        }

        throw new PropCannotConvertException(srcType, targetType);
    }

}
