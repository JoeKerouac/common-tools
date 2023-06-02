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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * LocalDate反序列化器
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public final class CommonLocalDateDeserializer
    extends AbstractDateDeserializer<LocalDate, CommonLocalDateDeserializer> {

    private static final String DEFAULT_PATTERN = "yyyyMMdd";

    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_PATTERN);

    public static final CommonLocalDateDeserializer INSTANCE = new CommonLocalDateDeserializer();

    private CommonLocalDateDeserializer() {}

    private CommonLocalDateDeserializer(String pattern, DateTimeFormatter formatter) {
        super(pattern, formatter);
    }

    @Override
    protected String getDefaultPattern() {
        return DEFAULT_PATTERN;
    }

    @Override
    protected DateTimeFormatter getDefaultFormatter() {
        return DEFAULT_FORMATTER;
    }

    @Override
    protected LocalDate from(TemporalAccessor accessor) {
        return LocalDate.from(accessor);
    }

    @Override
    protected CommonLocalDateDeserializer newInstance(String pattern, DateTimeFormatter formatter) {
        return new CommonLocalDateDeserializer(pattern, formatter);
    }

}
