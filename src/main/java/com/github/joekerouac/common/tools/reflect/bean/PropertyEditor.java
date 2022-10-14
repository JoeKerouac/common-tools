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
import java.lang.reflect.Field;

/**
 * 属性编辑器
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface PropertyEditor {

    /**
     * 是否有写入方法
     * 
     * @return true表示有写入方法
     */
    boolean hasWriteMethod();

    /**
     * 是否有读取方法
     * 
     * @return true表示有读取方法
     */
    boolean hasReadMethod();

    /**
     * 将指定值写入指定对象的指定字段
     * 
     * @param target
     *            指定对象
     * @param value
     *            要写入的值
     */
    void write(Object target, Object value);

    /**
     * 读取指定对象的字段值
     * 
     * @param target
     *            指定对象
     * @param <T>
     *            字段实际类型
     * @return 字段值
     */
    <T> T read(Object target);

    /**
     * 原始字段
     * 
     * @return 字段
     */
    Field original();

    /**
     * 字段名
     * 
     * @return 字段名
     */
    String name();

    /**
     * 字段类型
     * 
     * @return 字段类型
     */
    Class<?> type();

    /**
     * 字段所属class
     * 
     * @return 字段所属class
     */
    Class<?> owner();

    /**
     * 获取指定类型的枚举
     * 
     * @param annotationClass
     *            枚举class
     * @param <T>
     *            枚举实际类型
     * @return 枚举
     */
    <T extends Annotation> T getAnnotation(Class<T> annotationClass);
}
