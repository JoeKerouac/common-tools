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

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author JoeKerouac
 * @date 2023-06-21 11:45
 * @since 2.0.3
 */
@Getter
@Setter
@ToString(callSuper = true)
public class SimpleType extends JavaType {

    /**
     * 该类型的父类型，注意，如果rawClass是Object.class，那么该值为null
     */
    @ToString.Exclude
    private JavaType parent;

    /**
     * 该类型的接口
     */
    @ToString.Exclude
    private JavaType[] interfaces;

}
