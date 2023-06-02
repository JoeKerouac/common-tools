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
package com.github.joekerouac.common.tools.codec.common;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Objects;
import java.util.function.Supplier;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.github.joekerouac.common.tools.codec.json.annotations.LocalDateTimeFormat;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * @author JoeKerouac
 * @date 2023-06-02 16:31
 * @since 2.0.3
 */
public abstract class AbstractDateDeserializer<T, M extends AbstractDateDeserializer<T, M>> {

    private final String pattern;

    private final DateTimeFormatter formatter;

    public AbstractDateDeserializer() {
        this(null, null);
    }

    public AbstractDateDeserializer(String pattern, DateTimeFormatter formatter) {
        this.pattern = StringUtils.getOrDefault(pattern, getDefaultPattern());
        this.formatter = formatter == null ? getDefaultFormatter() : formatter;
    }

    protected abstract @NotBlank String getDefaultPattern();

    protected abstract @NotNull DateTimeFormatter getDefaultFormatter();

    protected abstract T from(TemporalAccessor accessor);

    protected abstract M newInstance(String format, DateTimeFormatter formatter);

    public T read(String date) {
        if (StringUtils.isBlank(date)) {
            return null;
        }

        return from(formatter.parse(date));
    }

    @SuppressWarnings("unchecked")
    public M createContextual(Supplier<LocalDateTimeFormat> localDateTimeFormatSupplier) {
        LocalDateTimeFormat annotation = localDateTimeFormatSupplier.get();

        if (annotation == null) {
            return (M)this;
        }

        String pattern = StringUtils.getOrDefault(annotation.deserializer(), annotation.value());

        if (StringUtils.isBlank(pattern) || Objects.equals(pattern, this.pattern)) {
            return (M)this;
        }

        return newInstance(pattern, DateTimeFormatter.ofPattern(pattern));
    }

}
