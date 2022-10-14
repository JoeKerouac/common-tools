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
package com.github.joekerouac.common.tools.reflect.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * 用来方便的获取泛型类型
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public abstract class AbstractTypeReference<T> {

    protected final Type type;

    protected AbstractTypeReference() {
        Type superClass = getClass().getGenericSuperclass();
        // sanity check, should never happen
        if (superClass instanceof Class<?>) {
            throw new IllegalArgumentException(
                "Internal error: TypeReference constructed without actual type information");
        }
        /* 22-Dec-2008, tatu: Not sure if this case is safe -- I suspect
         *   it is possible to make it fail?
         *   But let's deal with specific
         *   case when we know an actual use case, and thereby suitable
         *   workarounds for valid case(s) and/or error to throw
         *   on invalid one(s).
         */
        type = ((ParameterizedType)superClass).getActualTypeArguments()[0];
    }

    public Type getType() {
        return type;
    }
}
