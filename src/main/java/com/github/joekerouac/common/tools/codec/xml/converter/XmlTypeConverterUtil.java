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
package com.github.joekerouac.common.tools.codec.xml.converter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.github.joekerouac.common.tools.codec.xml.XmlNode;
import com.github.joekerouac.common.tools.codec.xml.XmlTypeConvert;
import com.github.joekerouac.common.tools.reflect.bean.PropertyEditor;
import com.github.joekerouac.common.tools.reflect.type.JavaTypeUtil;

import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.NoArgsConstructor;

/**
 * XmlTypeConverter工具
 *
 * @author JoeKerouac
 */
@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class XmlTypeConverterUtil {

    public static final Map<String, XmlTypeConvert<?>> converters;

    public static final XmlTypeConvert<?> DEFAULT_CONVERTER = new StringConverter();

    static {
        Map<String, XmlTypeConvert<?>> convertMap = new HashMap<>();
        convertMap.put("byte", new ByteConverter());
        convertMap.put("short", new ShortConverter());
        convertMap.put("int", new IntConverter());
        convertMap.put("long", new LongConverter());
        convertMap.put("double", new DoubleConverter());
        convertMap.put("float", new FloatConverter());
        convertMap.put("boolean", new BooleanConverter());
        convertMap.put("char", new CharConverter());

        convertMap.put(Byte.class.getName(), new ByteConverter());
        convertMap.put(Short.class.getName(), new ShortConverter());
        convertMap.put(Integer.class.getName(), new IntConverter());
        convertMap.put(Long.class.getName(), new LongConverter());
        convertMap.put(Double.class.getName(), new DoubleConverter());
        convertMap.put(Float.class.getName(), new FloatConverter());
        convertMap.put(Boolean.class.getName(), new BooleanConverter());
        convertMap.put(Character.class.getName(), new CharConverter());

        converters = Collections.unmodifiableMap(convertMap);
    }

    /**
     * 确定converter
     *
     * @param attrXmlNode
     *            字段的注释
     * @param editor
     *            字段编辑器
     * @param <T>
     *            字段类型
     * @return 字段对应的converter
     */
    @SuppressWarnings("unchecked")
    public static <T extends XmlTypeConvert<?>> T resolve(XmlNode attrXmlNode, PropertyEditor editor) {
        T convert;
        if (attrXmlNode != null) {
            Class<T> fieldConverterClass;
            fieldConverterClass = (Class<T>)attrXmlNode.converter();
            // 判断用户是否指定converter
            if (NullConverter.class.equals(fieldConverterClass)) {
                // 用户没有指定converter
                convert = resolve(editor);
            } else {
                try {
                    convert = fieldConverterClass.newInstance();
                } catch (Exception e) {
                    convert = resolve(editor);
                    LOGGER.warn("指定的xml转换器[{}]无法实例化，请为该转换器增加公共无参数构造器，当前将使用默认转换器[{}]", fieldConverterClass,
                        convert.getClass(), e);
                }
            }
        } else {
            convert = resolve(editor);
        }
        return convert;
    }

    /**
     * 根据字段说明自动推断使用什么转换器
     *
     * @param editor
     *            字段编辑器
     * @param <T>
     *            字段的类型
     * @return 根据字段说明推断出来的转换器
     */
    @SuppressWarnings("unchecked")
    public static <T extends XmlTypeConvert<?>> T resolve(PropertyEditor editor) {
        T convert;
        if (JavaTypeUtil.isGeneralType(editor.type()) || JavaTypeUtil.isBasic(editor.type())) {
            convert = (T)XmlTypeConverterUtil.converters.get(editor.type().getName());
        } else if (String.class.equals(editor.type())) {
            convert = (T)DEFAULT_CONVERTER;
        } else if (Collection.class.isAssignableFrom(editor.type())) {
            // 到这里的只有两种可能，一、用户没有指定converter；二、用户没有加注解XmlNode
            XmlNode xmlnode = editor.getAnnotation(XmlNode.class);
            if (xmlnode == null) {
                // 用户没有添加xmlnode注解，使用默认converter
                convert = (T)DEFAULT_CONVERTER;
            } else {
                // 用户指定了xmlnode注解但是没有指定converter，使用general字段确定集合中的数据类型
                convert = (T)((XmlConverter<T>)() -> (Class<T>)xmlnode.general());
            }
        } else {
            // 字段不是基本类型，假设是pojo，使用xml转换器
            convert = (T)((XmlConverter<T>)() -> (Class<T>)editor.type());
        }
        return convert;
    }

}
