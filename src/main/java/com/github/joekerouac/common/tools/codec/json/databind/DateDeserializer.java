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
import java.util.Date;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.github.joekerouac.common.tools.codec.json.annotations.DateTimeFormat;
import com.github.joekerouac.common.tools.date.DateUtil;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * @author JoeKerouac
 * @date 2023-05-22 11:22
 * @since 2.0.3
 */
public class DateDeserializer extends JsonDeserializer<Date> implements SerializeRegister, ContextualDeserializer {

    private final String format;

    public DateDeserializer() {
        this(DateUtil.BASE);
    }

    public DateDeserializer(String format) {
        this.format = format;
    }

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException, JsonProcessingException {
        String datetime = StringDeserializer.instance.deserialize(jsonParser, deserializationContext);
        if (StringUtils.isBlank(datetime)) {
            return null;
        }

        return DateUtil.parse(datetime, format);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext,
        BeanProperty beanProperty) throws JsonMappingException {
        DateTimeFormat dateTimeFormat =
            Optional.ofNullable(beanProperty).map(p -> p.getAnnotation(DateTimeFormat.class)).orElse(null);

        if (dateTimeFormat != null) {
            return new DateDeserializer(StringUtils.getOrDefault(dateTimeFormat.deserializer(),
                StringUtils.getOrDefault(dateTimeFormat.value(), format)));
        }

        JsonFormat jsonFormat =
            Optional.ofNullable(beanProperty).map(p -> p.getAnnotation(JsonFormat.class)).orElse(null);
        if (jsonFormat != null && StringUtils.isNotBlank(jsonFormat.pattern())) {
            return new DateDeserializer(jsonFormat.pattern());
        }

        return this;
    }
}
