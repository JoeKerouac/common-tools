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
import java.util.Objects;

import com.github.joekerouac.common.tools.enums.EnumInterface;

/**
 * String转换enum
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class StringToEnumConverter extends AbstractTypeConverter {

    @Override
    public boolean test(Class<?> srcType, Class<?> targetType, Annotation[] annotations) {
        return String.class.equals(srcType) && Enum.class.isAssignableFrom(targetType);
    }

    @Override
    @SuppressWarnings("all")
    protected <S, T> T convert(Class<S> srcType, Class<T> targetType, S src, Annotation[] annotations) {
        if (EnumInterface.class.isAssignableFrom(targetType)) {
            return EnumInterface.getByCode((String)src, targetType);
        }

        T[] enumConstants = targetType.getEnumConstants();
        for (T enumConstant : enumConstants) {
            if (Objects.equals(((Enum)enumConstant).name(), src)) {
                return enumConstant;
            }
        }

        throw new IllegalArgumentException("No enum constant " + targetType.getCanonicalName() + "." + src);
    }
}
