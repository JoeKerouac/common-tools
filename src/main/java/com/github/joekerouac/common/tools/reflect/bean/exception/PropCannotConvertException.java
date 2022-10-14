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
package com.github.joekerouac.common.tools.reflect.bean.exception;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class PropCannotConvertException extends RuntimeException {

    public PropCannotConvertException(Class<?> srcType, Class<?> targetType) {
        super(String.format("类型[%s]无法转换为类型[%s]", srcType, targetType));
    }

    public PropCannotConvertException(Object src, Class<?> srcType, Class<?> targetType) {
        super(String.format("数据[%s],类型[%s]无法转换为类型[%s]", src, srcType, targetType));
    }
}
