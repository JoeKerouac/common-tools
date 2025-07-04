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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.github.joekerouac.common.tools.codec.json.annotations.NumberFormat;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * @author JoeKerouac
 * @date 2025-06-30 18:51:05
 * @since 2.1.6
 */
public class BigDecimalAsStringSerializer extends JsonSerializer<BigDecimal>
    implements ContextualSerializer, SerializeRegister {

    private final NumberFormat.Rule rule;

    public BigDecimalAsStringSerializer() {
        this(NumberFormat.Rule.DEFAULT);
    }

    public BigDecimalAsStringSerializer(NumberFormat.Rule rule) {
        this.rule = rule;
    }

    @Override
    public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        switch (rule) {
            case DEFAULT:
                gen.writeString(value.toPlainString());
                return;
            case AUTO_TRIM:
                if (value.setScale(0, RoundingMode.DOWN).compareTo(value) == 0) {
                    gen.writeString(value.setScale(0, RoundingMode.DOWN).toPlainString());
                } else {
                    gen.writeString(value.toPlainString());
                }
                return;
            default:
                throw new UnsupportedOperationException(StringUtils.format("不支持的规则: [{}]", rule));
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
        throws JsonMappingException {
        NumberFormat numberFormat =
            Optional.ofNullable(property).map(p -> p.getAnnotation(NumberFormat.class)).orElse(null);

        if (numberFormat != null && numberFormat.rule() != this.rule) {
            return new BigDecimalAsStringSerializer(numberFormat.rule());
        }

        return this;
    }
}
