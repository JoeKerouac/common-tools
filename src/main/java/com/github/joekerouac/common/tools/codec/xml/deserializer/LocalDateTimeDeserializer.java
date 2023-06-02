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
package com.github.joekerouac.common.tools.codec.xml.deserializer;

import java.time.LocalDateTime;
import java.util.Optional;

import org.dom4j.Element;

import com.github.joekerouac.common.tools.codec.common.CommonLocalDateTimeDeserializer;
import com.github.joekerouac.common.tools.codec.json.annotations.LocalDateTimeFormat;
import com.github.joekerouac.common.tools.codec.xml.XmlDeserializer;
import com.github.joekerouac.common.tools.reflect.bean.PropertyEditor;

/**
 * LocalDateTime反序列化器
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class LocalDateTimeDeserializer implements XmlDeserializer<LocalDateTime> {

    public static final LocalDateTimeDeserializer INSTANCE = new LocalDateTimeDeserializer();

    private final CommonLocalDateTimeDeserializer commonLocalDateTimeDeserializer;

    public LocalDateTimeDeserializer() {
        this(null);
    }

    public LocalDateTimeDeserializer(CommonLocalDateTimeDeserializer commonLocalDateTimeDeserializer) {
        this.commonLocalDateTimeDeserializer = commonLocalDateTimeDeserializer == null
            ? CommonLocalDateTimeDeserializer.INSTANCE : commonLocalDateTimeDeserializer;
    }

    @Override
    public LocalDateTime read(Element element, String attrName) {
        String read = StringDeserializer.INSTANCE.read(element, attrName);
        return commonLocalDateTimeDeserializer.read(read);
    }

    @Override
    public LocalDateTimeDeserializer createContextual(PropertyEditor propertyEditor) {
        CommonLocalDateTimeDeserializer contextual = commonLocalDateTimeDeserializer.createContextual(() -> Optional
            .ofNullable(propertyEditor).map(p -> p.getAnnotation(LocalDateTimeFormat.class)).orElse(null));
        return new LocalDateTimeDeserializer(contextual);
    }

}
