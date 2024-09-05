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
import com.fasterxml.jackson.databind.BeanProperty;
import com.github.joekerouac.common.tools.codec.json.annotations.DateTimeFormat;
import com.github.joekerouac.common.tools.string.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Optional;

/**
 * @author JoeKerouac
 * @date 2024-09-02 16:21:42
 * @since 2.1.5
 */
public interface DateTimeFormatSupplier {

    DateTimeFormatSupplier DEFAULT = beanProperty -> {
        DateTimeFormat dateTimeFormat =
            Optional.ofNullable(beanProperty).map(p -> p.getAnnotation(DateTimeFormat.class)).orElse(null);

        if (dateTimeFormat != null) {
            return new Format(dateTimeFormat.value(), dateTimeFormat.serializer(),
                dateTimeFormat.deserializer());
        }

        JsonFormat jsonFormat =
            Optional.ofNullable(beanProperty).map(p -> p.getAnnotation(JsonFormat.class)).orElse(null);
        if (jsonFormat != null && StringUtils.isNotBlank(jsonFormat.pattern())) {
            return new Format(jsonFormat.pattern(), jsonFormat.pattern(), jsonFormat.pattern());
        }

        return null;
    };

    Format getFormat(BeanProperty beanProperty);

    @Data
    @AllArgsConstructor
    class Format {

        private final String value;

        private final String serializer;

        private final String deserializer;

    }

}
