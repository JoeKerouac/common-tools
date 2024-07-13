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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.github.joekerouac.common.tools.codec.json.annotations.DateTimeFormat;
import com.github.joekerouac.common.tools.string.StringUtils;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Optional;

/**
 * @author JoeKerouac
 * @date 2024-07-13 11:17:28
 * @since 2.1.5
 */
public abstract class AbstractTimeSerializer<T extends Temporal> extends JsonSerializer<T>
    implements SerializeRegister, ContextualSerializer {

    protected final DateTimeFormatter formatter;

    protected final String format;

    public AbstractTimeSerializer(String format) {
        this.format = format;
        this.formatter = DateTimeFormatter.ofPattern(format);
    }

    protected abstract JsonSerializer<?> createInstance(String format);

    @Override
    public void serialize(final T value, final JsonGenerator gen, final SerializerProvider serializers)
        throws IOException {
        gen.writeString(formatter.format(value));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty)
        throws JsonMappingException {
        DateTimeFormat annotation =
            Optional.ofNullable(beanProperty).map(p -> p.getAnnotation(DateTimeFormat.class)).orElse(null);
        if (annotation == null) {
            return this;
        }

        return createInstance(
            StringUtils.getOrDefault(annotation.serializer(), StringUtils.getOrDefault(annotation.value(), format)));
    }

}
