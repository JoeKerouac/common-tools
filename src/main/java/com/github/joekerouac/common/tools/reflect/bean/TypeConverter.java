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

import java.lang.annotation.Annotation;

/**
 * 类型转换器，注意，实现类必须包含一个无参构造器
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface TypeConverter {

    /**
     * 测试源类型能否转换为目标类型
     *
     * @param srcType
     *            源类型
     * @param targetType
     *            目标类型
     * @return 如果源类型能转换为目标类型，返回true
     */
    default boolean test(Class<?> srcType, Class<?> targetType) {
        return test(srcType, targetType, null);
    }

    /**
     * 测试源类型能否转换为目标类型
     * 
     * @param srcType
     *            源类型
     * @param targetType
     *            目标类型
     * @param annotations
     *            源对象上的注解，允许为null
     * @return 如果源类型能转换为目标类型，返回true
     */
    boolean test(Class<?> srcType, Class<?> targetType, Annotation[] annotations);

    /**
     * 执行转换
     *
     * @param src
     *            源对象
     * @param targetType
     *            目标类型class
     * @param <S>
     *            源实际类型
     * @param <T>
     *            目标实际类型
     * @return 目标
     */
    default <S, T> T convert(S src, Class<T> targetType) {
        return convert(src, null, targetType);
    }

    /**
     * 执行转换
     * 
     * @param src
     *            源对象
     * @param annotations
     *            源对象上的注解，允许为null
     * @param targetType
     *            目标类型class
     * @param <S>
     *            源实际类型
     * @param <T>
     *            目标实际类型
     * @return 目标
     */
    <S, T> T convert(S src, Annotation[] annotations, Class<T> targetType);

}
