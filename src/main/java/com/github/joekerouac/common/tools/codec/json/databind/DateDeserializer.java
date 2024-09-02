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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.github.joekerouac.common.tools.date.DateUtil;
import com.github.joekerouac.common.tools.string.StringUtils;

import java.io.IOException;
import java.util.Date;

/**
 * @author JoeKerouac
 * @date 2023-05-22 11:22
 * @since 2.0.3
 */
public class DateDeserializer extends JsonDeserializer<Date> implements SerializeRegister, ContextualDeserializer {

    private final DateTimeFormatSupplier formatSupplier;

    private final String format;

    public DateDeserializer() {
        this(DateUtil.BASE);
    }

    public DateDeserializer(String format) {
        this(format, DateTimeFormatSupplier.DEFAULT);
    }

    public DateDeserializer(String format, DateTimeFormatSupplier formatSupplier) {
        this.formatSupplier = formatSupplier;
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
        DateTimeFormatSupplier.Format format = formatSupplier.getFormat(beanProperty);

        if (format == null) {
            return this;
        }

        return new DateDeserializer(StringUtils.getOrDefault(format.getDeserializer(),
            StringUtils.getOrDefault(format.getValue(), this.format)), formatSupplier);
    }
}
