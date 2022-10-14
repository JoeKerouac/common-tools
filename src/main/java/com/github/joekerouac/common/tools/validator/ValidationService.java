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
package com.github.joekerouac.common.tools.validator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import javax.validation.constraints.NotNull;

/**
 * 校验服务
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface ValidationService {

    /**
     * 校验bean是否符合规则
     *
     * @param bean
     *            要校验的bean
     * @throws IllegalArgumentException
     *             校验失败时抛出该异常
     */
    default <T> T validate(@NotNull T bean) throws IllegalArgumentException {
        return validate(bean, new Class<?>[0]);
    }

    /**
     * 校验bean是否符合规则
     *
     * @param bean
     *            要校验的bean
     * @param groups
     *            分组校验，允许为空
     * @throws IllegalArgumentException
     *             校验失败时抛出该异常
     */
    <T> T validate(@NotNull T bean, Class<?>... groups) throws IllegalArgumentException;

    /**
     * 验证方法参数是否符合规则
     *
     * @param instance
     *            方法所在的类的实例
     * @param method
     *            方法实例
     * @param params
     *            参数
     * @throws IllegalArgumentException
     *             校验失败时抛出该异常
     */
    <T> T validateParameters(@NotNull T instance, @NotNull Method method, Object[] params)
        throws IllegalArgumentException;

    /**
     * 校验构造器入参
     * 
     * @param constructor
     *            构造器
     * @param args
     *            构造器参数
     * @throws IllegalArgumentException
     *             校验失败时抛出该异常
     */
    void validateParameters(@NotNull Constructor<?> constructor, @NotNull Object[] args)
        throws IllegalArgumentException;

}
