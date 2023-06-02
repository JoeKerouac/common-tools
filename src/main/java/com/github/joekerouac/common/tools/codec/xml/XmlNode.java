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
package com.github.joekerouac.common.tools.codec.xml;

import java.lang.annotation.*;
import java.util.Collection;

import com.github.joekerouac.common.tools.codec.xml.deserializer.NullDeserializer;

/**
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Documented
public @interface XmlNode {

    /**
     * 标记该字段是否用CDATA包裹
     *
     * @return 该字段需要CDATA则返回true
     */
    boolean isCDATA() default false;

    /**
     * 该节点的名称，默认为字段的名称（如果{@link #isAttribute}为true那么该名称为xml属性值对应 的节点名称，如果不指定该值或者为空时表示该属性是当前节点父节点的属性，如果指定的话表示就是当前同级节点
     * 的属性）
     *
     * @return 节点的名称
     */
    String name() default "";

    /**
     * 该字段是否是xml的属性
     *
     * @return true表示该字段是xml的某个节点的属性（对应的节点名为{@link #name()})
     */
    boolean isAttribute() default false;

    /**
     * 该属性的名称
     *
     * @return 属性名，默认为字段名称
     */
    String attributeName() default "";

    /**
     * 忽略该字段
     *
     * @return 如果为true则为忽略
     */
    boolean ignore() default false;

    /**
     * 类型转换器，默认都是String类型的，如果字段不是String类型那么请使用类型转换器
     * <p>
     * 如果字段是集合类型，而集合类型的泛型不是String，那么必须要有该转换器，否则最后注入的集合将是一个 泛型为String的集合
     *
     * @return 类型转换器
     */
    Class<? extends XmlDeserializer<?>> deserializer() default NullDeserializer.class;

    /**
     * 当字段为带泛型的集合时，如果不指定converter，那么需要指定该字段，表示集合中的泛型类型
     * <p>
     * 当字段为不带泛型的类时该方法应该返回字段的真实类型而不是接口或抽象类（当字段声明是一个接口或者抽象类 时必须有该字段）
     *
     * @return 集合中的泛型类型
     */
    Class<?> general() default String.class;

    /**
     * 当字段为集合时该选项有用
     * <p>
     * 例如字段名为set，arrayRoot值为user，则生成的xml为&lt;set&gt;&lt;user&gt;123&lt;/user&gt;&lt;user&gt;456&lt;/user&gt;&lt;/set&gt;
     * <p>
     * 而当该字段为空时则会直接以字段名为节点名生成xml，生成xml如下：&lt;set&gt;123&lt;/set&gt;&lt;set&gt;456&lt;/set&gt;
     *
     * @return 集合节点的节点名，当该值不为null或者空时会为集合单独创建一个以该函数返回值为名的节点，在节点里边存放集合，否则不创建
     */
    String arrayRoot() default "";

    /**
     * 当字段对应的类型是集合类型时应该有该字段，指定需要的集合类
     *
     * @return 字段对应的集合类型，如果不指定那么将会采用默认值
     */
    Class<? extends Collection> arrayType() default Collection.class;
}
