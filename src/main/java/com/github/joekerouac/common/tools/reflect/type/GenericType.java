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

import java.lang.reflect.GenericDeclaration;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 表示一个泛型，例如List&lt;T&gt;的T
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class GenericType extends JavaType {

    /**
     * 该类型的父类型，当泛型为（T extends JavaType）这种形式时存在该值
     * 
     * 注意，如果用户既没有指定T extends，也没有指定T super，那么默认是T extends Object
     */
    private JavaType parent;

    /**
     * 该类型的子类型，当泛型为（T super JavaType）这种形式时存在该值
     */
    private JavaType child;

    /**
     * 该泛型声明的位置，如果泛型不是匿名的（?这种形式的泛型），那么该参数是必须的；
     */
    private GenericDeclaration genericDeclaration;
}
