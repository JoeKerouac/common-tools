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
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.joekerouac.common.tools.codec.json.annotations.InMemoryFileSize;
import com.github.joekerouac.common.tools.io.InMemoryFile;
import com.github.joekerouac.common.tools.io.InMemoryFileOutputStream;

/**
 * @author JoeKerouac
 * @date 2023-07-18 16:44
 * @since 2.0.3
 */
public class InMemoryFileDeserializer extends JsonDeserializer<InMemoryFile>
    implements SerializeRegister, ContextualDeserializer {

    private final int initMemoryBufferSize;

    private final int memoryBufferSize;

    public InMemoryFileDeserializer() {
        this(InMemoryFileSize.initMemoryBufferSize, InMemoryFileSize.memoryBufferSize);
    }

    public InMemoryFileDeserializer(int initMemoryBufferSize, int memoryBufferSize) {
        this.initMemoryBufferSize =
            initMemoryBufferSize <= 0 ? InMemoryFileSize.initMemoryBufferSize : initMemoryBufferSize;
        this.memoryBufferSize = memoryBufferSize <= 0 ? InMemoryFileSize.memoryBufferSize : memoryBufferSize;
    }

    @Override
    public InMemoryFile deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException, JsonProcessingException {
        InMemoryFile memoryFile = new InMemoryFile(initMemoryBufferSize, memoryBufferSize);
        jsonParser.readBinaryValue(new InMemoryFileOutputStream(memoryFile));

        memoryFile.writeFinish();
        return memoryFile;
    }

    @Override
    public void register(SimpleModule module) {
        module.addDeserializer(InMemoryFile.class, this);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext,
        BeanProperty beanProperty) throws JsonMappingException {
        InMemoryFileSize annotation =
            Optional.ofNullable(beanProperty).map(p -> p.getAnnotation(InMemoryFileSize.class)).orElse(null);
        if (annotation == null) {
            return this;
        }

        return new InMemoryFileDeserializer(annotation.initMemoryBufferSize(), annotation.memoryBufferSize());
    }

}
