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

import org.dom4j.Element;

/**
 * xml类型转换，将String类型转换为对应的类型
 *
 * @param <T>
 *            要转换的类型
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface XmlTypeConvert<T> {
    /**
     * 数据转换，将xml中的字符串数据转换为用户需要的指定类型数据
     *
     * @param element
     *            节点
     * @param attrName
     *            要获取的属性名，如果该值不为空则认为数据需要从属性中取而不是从节点数据中取
     * @return 转换后的数据
     */
    T read(Element element, String attrName);

    // /**
    // * 数据转换，将字段的值转换为xml中的内容
    // *
    // * @param obj
    // *
    // * @return
    // */
    // String write(Object obj);

    /**
     * 确定转换后的类型
     *
     * @return 转换后的类型
     */
    Class<T> resolve();
}
