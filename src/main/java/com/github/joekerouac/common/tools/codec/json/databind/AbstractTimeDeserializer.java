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
import com.github.joekerouac.common.tools.codec.json.annotations.DateTimeFormat;
import com.github.joekerouac.common.tools.string.StringUtils;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;

/**
 * @author JoeKerouac
 * @date 2024-07-13 11:27:36
 * @since 2.1.5
 */
public abstract class AbstractTimeDeserializer<T extends Temporal> extends JsonDeserializer<T>
    implements SerializeRegister, ContextualDeserializer {

    protected final DateTimeFormatter formatter;

    protected final String format;

    public AbstractTimeDeserializer(String format) {
        this.format = format;
        this.formatter = DateTimeFormatter.ofPattern(format);
    }

    protected abstract JsonDeserializer<?> createInstance(String format);

    protected abstract T from(TemporalAccessor temporal);

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException, JsonProcessingException {
        String datetime = StringDeserializer.instance.deserialize(jsonParser, deserializationContext);
        if (StringUtils.isBlank(datetime)) {
            return null;
        }

        TemporalAccessor temporal = formatter.parse(datetime);
        return from(temporal);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext,
        BeanProperty beanProperty) throws JsonMappingException {
        DateTimeFormat annotation =
            Optional.ofNullable(beanProperty).map(p -> p.getAnnotation(DateTimeFormat.class)).orElse(null);
        if (annotation == null) {
            return this;
        }

        return createInstance(
            StringUtils.getOrDefault(annotation.deserializer(), StringUtils.getOrDefault(annotation.value(), format)));
    }

}
