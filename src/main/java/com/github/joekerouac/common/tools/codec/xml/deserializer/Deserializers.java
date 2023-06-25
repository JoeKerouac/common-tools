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
package com.github.joekerouac.common.tools.codec.xml.deserializer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.joekerouac.common.tools.codec.xml.XmlDeserializer;
import com.github.joekerouac.common.tools.reflect.type.JavaType;
import com.github.joekerouac.common.tools.reflect.type.JavaTypeUtil;

/**
 * @author JoeKerouac
 * @date 2023-06-02 15:13
 * @since 2.0.3
 */
public class Deserializers {

    public static final XmlDeserializer<?> DEFAULT_DESERIALIZER = new StringDeserializer();

    public static final Map<JavaType, XmlDeserializer<?>> defaultDeserializers;

    static {
        Map<Class<?>, XmlDeserializer<?>> deserializers = new ConcurrentHashMap<>();
        deserializers.put(byte.class, ByteDeserializer.INSTANCE);
        deserializers.put(short.class, ShortDeserializer.INSTANCE);
        deserializers.put(int.class, IntDeserializer.INSTANCE);
        deserializers.put(long.class, LongDeserializer.INSTANCE);
        deserializers.put(double.class, DoubleDeserializer.INSTANCE);
        deserializers.put(float.class, FloatDeserializer.INSTANCE);
        deserializers.put(boolean.class, BooleanDeserializer.INSTANCE);
        deserializers.put(char.class, CharDeserializer.INSTANCE);

        deserializers.put(Byte.class, ByteDeserializer.INSTANCE);
        deserializers.put(Short.class, ShortDeserializer.INSTANCE);
        deserializers.put(Integer.class, IntDeserializer.INSTANCE);
        deserializers.put(Long.class, LongDeserializer.INSTANCE);
        deserializers.put(Double.class, DoubleDeserializer.INSTANCE);
        deserializers.put(Float.class, FloatDeserializer.INSTANCE);
        deserializers.put(Boolean.class, BooleanDeserializer.INSTANCE);
        deserializers.put(Character.class, CharDeserializer.INSTANCE);
        deserializers.put(String.class, StringDeserializer.INSTANCE);
        deserializers.put(BigDecimal.class, BigDecimalDeserializer.INSTANCE);
        deserializers.put(LocalDateTime.class, LocalDateTimeDeserializer.INSTANCE);
        deserializers.put(LocalDate.class, LocalDateDeserializer.INSTANCE);
        deserializers.put(LocalTime.class, LocalTimeDeserializer.INSTANCE);

        defaultDeserializers = Collections.unmodifiableMap(deserializers.entrySet().stream()
            .collect(Collectors.toMap(e -> JavaTypeUtil.createJavaType(e.getKey()), Map.Entry::getValue)));
    }

}
