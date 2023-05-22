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
package com.github.joekerouac.common.tools.codec.json.databind;

import java.io.IOException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.github.joekerouac.common.tools.codec.json.annotations.LocalDateTimeFormat;
import com.github.joekerouac.common.tools.date.DateUtil;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * 用于处理LocalDateTime
 * 
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime>
    implements SerializeRegister, ContextualSerializer {

    private final String format;

    public LocalDateTimeSerializer() {
        this(DateUtil.BASE);
    }

    public LocalDateTimeSerializer(String format) {
        this.format = format;
    }

    @Override
    public void serialize(final LocalDateTime value, final JsonGenerator gen, final SerializerProvider serializers)
        throws IOException {
        gen.writeString(DateUtil.getFormatDate(value, format));
    }

    @Override
    public void register(final SimpleModule module) {
        module.addSerializer(LocalDateTime.class, this);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty)
        throws JsonMappingException {
        LocalDateTimeFormat annotation = beanProperty.getAnnotation(LocalDateTimeFormat.class);
        if (annotation == null) {
            return this;
        }

        return new LocalDateTimeSerializer(
            StringUtils.getOrDefault(annotation.serializer(), StringUtils.getOrDefault(annotation.value(), format)));
    }
}
