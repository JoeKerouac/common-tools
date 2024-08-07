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
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.github.joekerouac.common.tools.io.InMemoryFile;

import java.io.IOException;

/**
 * 用于处理LocalDateTime
 * 
 * @author JoeKerouac
 * @date 2023-07-18 16:44
 * @since 1.0.0
 */
public class InMemoryFileSerializer extends JsonSerializer<InMemoryFile> implements SerializeRegister {

    public InMemoryFileSerializer() {}

    @Override
    public void serialize(final InMemoryFile value, final JsonGenerator gen, final SerializerProvider serializers)
        throws IOException {
        gen.writeBinary(value.getDataAsInputStream(), value.getLen());
    }

}
