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
import java.util.Date;

import com.github.joekerouac.common.tools.enums.EnumInterface;
import com.github.joekerouac.common.tools.date.DateUtil;
import com.github.joekerouac.common.tools.reflect.bean.DateFormat;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * 映射所有类型转换为String类型
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class AllToStringTypeConverter extends AbstractTypeConverter {

    @Override
    protected <S, T> T convert(Class<S> srcType, Class<T> targetType, S src, Annotation[] annotations) {
        if (srcType.equals(String.class)) {
            return targetType.cast(src);
        } else if (EnumInterface.class.isAssignableFrom(srcType)) {
            return targetType.cast(((EnumInterface)src).code());
        } else if (Date.class.isAssignableFrom(srcType)) {
            String format = DateUtil.BASE;
            if (annotations != null) {
                for (Annotation annotation : annotations) {
                    if (annotation instanceof DateFormat) {
                        format = ((DateFormat)annotation).value();
                        break;
                    }
                }
            }
            return targetType.cast(DateUtil.getFormatDate((Date)src, StringUtils.getOrDefault(format, DateUtil.BASE)));
        } else {
            return targetType.cast(src.toString());
        }
    }

    @Override
    public boolean test(Class<?> srcType, Class<?> targetType, Annotation[] annotations) {
        return String.class.equals(targetType);
    }
}
