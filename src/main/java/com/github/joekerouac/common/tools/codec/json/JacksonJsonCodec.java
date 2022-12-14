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
package com.github.joekerouac.common.tools.codec.json;

import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.joekerouac.common.tools.codec.Codec;
import com.github.joekerouac.common.tools.codec.exception.SerializeException;
import com.github.joekerouac.common.tools.codec.json.databind.LocalDateTimeSerializer;
import com.github.joekerouac.common.tools.codec.json.databind.ResourceDeserializer;
import com.github.joekerouac.common.tools.codec.json.databind.ResourceSerializer;
import com.github.joekerouac.common.tools.codec.json.databind.SerializeRegister;
import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.constant.Const;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.reflect.type.AbstractTypeReference;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * jackson?????????json????????????
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class JacksonJsonCodec implements Codec {

    private static final Charset JSON_DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * ????????????????????????????????????
     */
    public static final List<SerializeRegister> DEFAULT_SERIALIZE_REGISTERS;

    static {
        List<SerializeRegister> serializeRegisters = new ArrayList<>();
        serializeRegisters.add(new ResourceDeserializer<>());
        serializeRegisters.add(new ResourceSerializer());
        serializeRegisters.add(new LocalDateTimeSerializer());
        DEFAULT_SERIALIZE_REGISTERS = Collections.unmodifiableList(serializeRegisters);
    }

    protected final ObjectMapper mapper;

    public JacksonJsonCodec() {
        this(null);
    }

    /**
     * ?????????
     * 
     * @param visitor
     *            ObjectMapper????????????????????????????????????ObjectMapper?????????????????????null
     */
    public JacksonJsonCodec(Consumer<ObjectMapper> visitor) {
        this(visitor, DEFAULT_SERIALIZE_REGISTERS);
    }

    public JacksonJsonCodec(Consumer<ObjectMapper> visitor, List<SerializeRegister> serializeRegisters) {
        this.mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        if (CollectionUtil.isNotEmpty(serializeRegisters)) {
            SimpleModule module = new SimpleModule();
            serializeRegisters.forEach(serializeRegister -> serializeRegister.register(module));
            mapper.registerModule(module);
        }

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (visitor != null) {
            visitor.accept(mapper);
        }
    }

    @Override
    public <T> T read(byte[] data, Charset charset, AbstractTypeReference<T> typeReference) throws SerializeException {
        try {
            if (data == null || data.length == 0 || typeReference == null) {
                return null;
            }

            // ?????????jackson????????????utf8????????????????????????????????????????????????utf8
            byte[] jsonData =
                StringUtils.convert(data, charset == null ? JSON_DEFAULT_CHARSET : charset, JSON_DEFAULT_CHARSET);
            return mapper.readValue(jsonData, new com.fasterxml.jackson.core.type.TypeReference<T>() {
                @Override
                public Type getType() {
                    return typeReference.getType();
                }
            });
        } catch (Throwable e) {
            String msg = String.format("????????????[%s]??????,?????????[%s]", new String(data, Const.DEFAULT_CHARSET),
                typeReference.getType().getTypeName());
            throw new SerializeException(ErrorCodeEnum.NULL_POINT, msg, e);
        }
    }

    @Override
    public byte[] write(Object data, Charset charset) {
        if (data == null) {
            return new byte[0];
        }

        try {
            // ????????????????????????utf8????????????
            byte[] result = mapper.writeValueAsBytes(data);
            return StringUtils.convert(result, JSON_DEFAULT_CHARSET, charset == null ? JSON_DEFAULT_CHARSET : charset);
        } catch (Throwable e) {
            throw new SerializeException(ErrorCodeEnum.NULL_POINT, e);
        }
    }

}
