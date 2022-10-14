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
package com.github.joekerouac.common.tools.codec.xml.converter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.github.joekerouac.common.tools.codec.xml.XmlTypeConvert;

import lombok.CustomLog;

/**
 * 抽象xml类型转换
 *
 * @param <T>
 *            要转换的类型
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@CustomLog
public abstract class AbstractXmlTypeConvert<T> implements XmlTypeConvert<T> {

    private Class<T> type;

    @SuppressWarnings("unchecked")
    public AbstractXmlTypeConvert() {
        Type genericSuperclass = getClass().getGenericSuperclass();
        // 只检查一层Repository泛型参数，不检查父类
        if (genericSuperclass instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)genericSuperclass;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            type = (Class<T>)actualTypeArguments[0];
        } else {
            LOGGER.warn("请检查[{}]类的泛型", this.getClass());
        }
    }

    @Override
    public Class<T> resolve() {
        return type;
    }
}
