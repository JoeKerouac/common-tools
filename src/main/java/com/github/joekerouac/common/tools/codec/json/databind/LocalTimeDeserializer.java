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

import com.fasterxml.jackson.databind.JsonDeserializer;

import java.time.LocalTime;
import java.time.temporal.TemporalAccessor;

/**
 * @author JoeKerouac
 * @date 2024-07-13 11:17:28
 * @since 2.1.5
 */
public class LocalTimeDeserializer extends AbstractTimeDeserializer<LocalTime> {

    public LocalTimeDeserializer() {
        this("yyyy-MM-dd");
    }

    public LocalTimeDeserializer(String format) {
        super(format);
    }

    public LocalTimeDeserializer(String format, DateTimeFormatSupplier formatSupplier) {
        super(format, formatSupplier);
    }

    @Override
    protected LocalTime from(TemporalAccessor temporal) {
        return LocalTime.from(temporal);
    }

    @Override
    protected JsonDeserializer<?> createInstance(String format, DateTimeFormatSupplier formatSupplier) {
        return new LocalTimeDeserializer(format, formatSupplier);
    }

}
