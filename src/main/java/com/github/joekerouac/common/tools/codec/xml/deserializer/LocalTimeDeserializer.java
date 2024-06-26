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

import java.time.LocalTime;
import java.util.Optional;

import org.dom4j.Element;

import com.github.joekerouac.common.tools.codec.common.CommonLocalTimeDeserializer;
import com.github.joekerouac.common.tools.codec.json.annotations.DateTimeFormat;
import com.github.joekerouac.common.tools.codec.xml.XmlDeserializer;
import com.github.joekerouac.common.tools.reflect.bean.PropertyEditor;

/**
 * LocalDateTime反序列化器
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class LocalTimeDeserializer implements XmlDeserializer<LocalTime> {

    public static final LocalTimeDeserializer INSTANCE = new LocalTimeDeserializer();

    private final CommonLocalTimeDeserializer commonLocalTimeDeserializer;

    public LocalTimeDeserializer() {
        this(null);
    }

    public LocalTimeDeserializer(CommonLocalTimeDeserializer commonLocalTimeDeserializer) {
        this.commonLocalTimeDeserializer =
            commonLocalTimeDeserializer == null ? CommonLocalTimeDeserializer.INSTANCE : commonLocalTimeDeserializer;
    }

    @Override
    public LocalTime read(Element element, String attrName) {
        String read = StringDeserializer.INSTANCE.read(element, attrName);
        return commonLocalTimeDeserializer.read(read);
    }

    @Override
    public LocalTimeDeserializer createContextual(PropertyEditor propertyEditor) {
        CommonLocalTimeDeserializer contextual = commonLocalTimeDeserializer.createContextual(
            () -> Optional.ofNullable(propertyEditor).map(p -> p.getAnnotation(DateTimeFormat.class)).orElse(null));
        return new LocalTimeDeserializer(contextual);
    }

}
