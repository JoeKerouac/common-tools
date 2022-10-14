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

import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.joekerouac.common.tools.codec.xml.Dom4JXmlCodec;
import com.github.joekerouac.common.tools.codec.xml.XmlTypeConvert;
import com.github.joekerouac.common.tools.reflect.type.JavaTypeUtil;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * xml解析器，只需要实现该类并且实现{@link #resolve()}方法即可解析pojo类型的字段
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface XmlConverter<T> extends XmlTypeConvert<T> {

    Logger logger = LoggerFactory.getLogger(XmlConverter.class);

    Dom4JXmlCodec PARSER = new Dom4JXmlCodec();

    @SuppressWarnings("unchecked")
    @Override
    default T read(Element element, String attrName) {
        String data = StringUtils.isBlank(attrName) ? element.asXML() : element.attributeValue(attrName);
        Class<T> clazz = resolve();
        if (String.class.equals(clazz)) {
            logger.info("xml转换器确定的字段类型为String，转到String转换器");
            return (T)data;
        } else if (JavaTypeUtil.isBasic(clazz) || JavaTypeUtil.isGeneralType(clazz)) {
            logger.info("xml转换器确定的字段类型为" + clazz.getName() + "，转到基本类型转换器");
            return (T)XmlTypeConverterUtil.converters.get(clazz.getName()).read(element, attrName);
        }
        return PARSER.parse(data, clazz);
    }
}
