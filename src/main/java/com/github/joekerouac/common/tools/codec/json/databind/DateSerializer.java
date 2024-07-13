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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.github.joekerouac.common.tools.codec.json.annotations.DateTimeFormat;
import com.github.joekerouac.common.tools.date.DateUtil;
import com.github.joekerouac.common.tools.string.StringUtils;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

/**
 * 用于处理LocalDateTime
 * 
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class DateSerializer extends JsonSerializer<Date> implements SerializeRegister, ContextualSerializer {

    private final String format;

    public DateSerializer() {
        this(DateUtil.BASE);
    }

    public DateSerializer(String format) {
        this.format = format;
    }

    @Override
    public void serialize(final Date value, final JsonGenerator gen, final SerializerProvider serializers)
        throws IOException {
        gen.writeString(DateUtil.getFormatDate(value, format));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty)
        throws JsonMappingException {
        DateTimeFormat dateTimeFormat =
            Optional.ofNullable(beanProperty).map(p -> p.getAnnotation(DateTimeFormat.class)).orElse(null);

        if (dateTimeFormat != null) {
            return new DateSerializer(StringUtils.getOrDefault(dateTimeFormat.deserializer(),
                StringUtils.getOrDefault(dateTimeFormat.value(), format)));
        }

        JsonFormat jsonFormat =
            Optional.ofNullable(beanProperty).map(p -> p.getAnnotation(JsonFormat.class)).orElse(null);
        if (jsonFormat != null && StringUtils.isNotBlank(jsonFormat.pattern())) {
            return new DateSerializer(jsonFormat.pattern());
        }

        return this;
    }
}
