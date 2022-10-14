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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.joekerouac.common.tools.resource.Resource;
import com.github.joekerouac.common.tools.resource.ResourceBuilder;
import com.github.joekerouac.common.tools.enums.ResourceType;

/**
 * 反序列化URLResource使用
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class ResourceDeserializer<T extends Resource> extends JsonDeserializer<T> implements SerializeRegister {

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException, JsonProcessingException {
        JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);
        Map<String, String> map = new HashMap<>();

        Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> next = fields.next();
            map.put(next.getKey(), next.getValue().textValue());
        }

        return (T)ResourceBuilder.build(map);
    }

    @Override
    public void register(SimpleModule module) {
        module.addDeserializer(Resource.class, this);
        for (ResourceType value : ResourceType.values()) {
            register(module, value.getResourceClass());
        }
    }

    @SuppressWarnings("unchecked")
    private void register(SimpleModule module, Class<? extends Resource> clazz) {
        module.addDeserializer((Class<T>)clazz, this);
    }

}
