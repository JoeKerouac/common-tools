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
package com.github.joekerouac.common.tools.codec.xml.deserializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.dom4j.Attribute;
import org.dom4j.Element;

import com.github.joekerouac.common.tools.codec.exception.SerializeException;
import com.github.joekerouac.common.tools.codec.xml.XmlDeserializer;
import com.github.joekerouac.common.tools.codec.xml.XmlNode;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.reflect.AccessorUtil;
import com.github.joekerouac.common.tools.reflect.ClassUtils;
import com.github.joekerouac.common.tools.reflect.bean.BeanUtils;
import com.github.joekerouac.common.tools.reflect.bean.PropertyEditor;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.CustomLog;

/**
 * xml bean 解析器
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@CustomLog
public class BeanDeserializer<T> implements XmlDeserializer<T> {

    private final Class<T> type;

    private final Map<Class<?>, XmlDeserializer<?>> parentDeserializers;

    private final Map<PropertyEditor, XmlDeserializer<?>> localDeserializers;

    private final PropertyEditor[] propertyEditors;

    public BeanDeserializer(Class<T> type, Map<Class<?>, XmlDeserializer<?>> parentDeserializers) {
        this.type = type;
        this.parentDeserializers = parentDeserializers;
        this.localDeserializers = new ConcurrentHashMap<>();
        this.propertyEditors = BeanUtils.getPropertyDescriptors(type);
    }

    @SuppressWarnings("unchecked")
    public T read(Element root, String attrName) {
        Assert.isBlank(attrName, StringUtils.format("不能指定属性名, attrName: [{}], element: [{}]", attrName, root),
            ExceptionProviderConst.CodeErrorExceptionProvider);

        T pojo;
        // 获取pojo对象的实例
        try {
            // 没有权限访问该类或者该类（为接口、抽象类）不能实例化时将抛出异常
            pojo = ClassUtils.getInstance(type);
        } catch (Exception e) {
            LOGGER.error(e, "class对象生成失败，请检查代码；失败原因：");
            throw new RuntimeException(e);
        }

        boolean hasMapAttr = false;
        PropertyEditor mapEditor = null;
        Map<String, String> attrMap = null;

        for (PropertyEditor editor : propertyEditors) {
            XmlNode xmlNode = editor.getAnnotation(XmlNode.class);
            final String fieldName = editor.name();
            // 节点名
            String nodeName = null;
            // 属性名
            String attributeName = null;
            boolean isParent = false;
            boolean ignore = false;
            boolean isAttr = false;
            if (xmlNode == null) {
                nodeName = fieldName;
            } else if (!xmlNode.ignore()) {
                // 如果节点不是被忽略的节点那么继续
                // 获取节点名称，优先使用注解，如果注解没有设置名称那么使用字段名
                nodeName = StringUtils.isBlank(xmlNode.name()) ? fieldName : xmlNode.name();
                LOGGER.debug("字段[{}]对应的节点名为：{}", fieldName, nodeName);

                // 判断节点是否是属性值
                if (xmlNode.isAttribute()) {
                    isAttr = true;
                    // 如果节点是属性值，那么需要同时设置节点名和属性名，原则上如果是属性的话必须设置节点名，但是为了防止
                    // 用户忘记设置，在用户没有设置的时候使用字段名
                    if (StringUtils.isBlank(xmlNode.attributeName())) {
                        LOGGER.debug("字段[{}]是属性值，但是未设置属性名（attributeName字段），将采用字段名作为属性名", editor.name());
                        attributeName = fieldName;
                    } else {
                        attributeName = xmlNode.attributeName();
                    }

                    if (StringUtils.isBlank(xmlNode.name())) {
                        LOGGER.debug("该字段是属性值，并且未设置节点名（name字段），设置isParent为true");
                        isParent = true;
                    }

                    // 如果还是map类型，那么特殊处理一波，判断是否是存储属性集合的，如果是，则将所有属性都放入
                    if (Map.class.isAssignableFrom(editor.type())) {
                        if (hasMapAttr) {
                            throw new SerializeException(ErrorCodeEnum.SERIAL_EXCEPTION, StringUtils.format(
                                "当前字段[{}]是map类型的属性字段，当前已经有一个map类型的属性字段[{}]了，不能出现两个", editor.name(), mapEditor.name()));
                        } else {
                            hasMapAttr = true;
                            mapEditor = editor;
                            attrMap = mapEditor.read(pojo);
                            attrMap = attrMap == null ? newMap(mapEditor.type()) : attrMap;

                            editor.write(pojo, attrMap);
                        }
                    }
                } else {
                    LOGGER.debug("字段[{}]对应的是节点", fieldName);
                }
            } else {
                ignore = true;
            }

            // 不忽略并且字段不是map类型
            if (!ignore) {
                // 获取指定节点名的element
                List<Element> nodes = (isAttr && isParent) ? Collections.singletonList(root) : root.elements(nodeName);
                // 判断是否为空
                if (nodes.isEmpty()) {
                    // 如果为空那么将首字母大写后重新获取
                    nodes = root.elements(StringUtils.toFirstUpperCase(nodeName));
                }
                if (!nodes.isEmpty()) {
                    // 如果还不为空，那么为pojo赋值
                    Class<?> type = editor.type();

                    // 开始赋值
                    // 判断字段是否是集合
                    if (Collection.class.isAssignableFrom(type)) {
                        // 是集合
                        setValue(nodes, attributeName, pojo, editor);
                    } else if (Map.class.isAssignableFrom(type)) {
                        // 是Map

                        if (!isAttr) {
                            LOGGER.warn("当前暂时不支持解析map");
                            continue;
                        }

                        Element element = StringUtils.isBlank(xmlNode.name()) ? root : root.element(xmlNode.name());

                        if (element != null) {
                            // 获取所有
                            List<Attribute> attributes = element.attributes();

                            for (Attribute attribute : attributes) {
                                attrMap.put(attribute.getName(), attribute.getValue());
                            }
                        }
                    } else {
                        // 不是集合，直接赋值
                        setValue(nodes.get(0), attributeName, pojo, editor);
                    }
                }
            }
        }
        return pojo;
    }

    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> newMap(Class<?> mapClass) {
        if (!Map.class.isAssignableFrom(mapClass)) {
            throw new SerializeException(ErrorCodeEnum.CODE_ERROR, "请传入正确的Map的class");
        }

        Class<?> realMapClass = mapClass;
        if (Map.class.equals(mapClass)) {
            realMapClass = HashMap.class;
        }

        if (AccessorUtil.isAbstract(realMapClass)) {
            throw new SerializeException(ErrorCodeEnum.CODE_ERROR,
                String.format("不支持的map类型[%s]，请传入实际的map类型", mapClass));
        }

        return (Map<K, V>)ClassUtils.getInstance(realMapClass);
    }

    /**
     * 根据class尝试构建出集合实例，有可能返回null，禁止抛出异常
     *
     * @param clazz
     *            集合的Class对象
     * @return 集合实例
     */
    private <D> Collection<D> tryBuildCollection(Class<? extends Collection<D>> clazz) {
        if (List.class.isAssignableFrom(clazz)) {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                return new ArrayList<>();
            }
        } else if (Set.class.isAssignableFrom(clazz)) {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                return new HashSet<>();
            }
        } else {
            LOGGER.warn("未知集合类型：[{}]", clazz);
            return null;
        }
    }

    /**
     * 往pojo中指定字段设置值
     *
     * @param element
     *            要设置的数据节点
     * @param attrName
     *            要获取的属性名，如果该值不为空则认为数据需要从属性中取而不是从节点数据中取
     * @param pojo
     *            pojo
     * @param editor
     *            字段编辑器
     */
    private void setValue(Element element, String attrName, Object pojo, PropertyEditor editor) {
        XmlNode attrXmlNode = editor.getAnnotation(XmlNode.class);
        LOGGER.debug("要赋值的fieldName为{}", editor.name());
        final XmlDeserializer<?> deserializer = resolve(attrXmlNode, editor);
        if (!BeanUtils.setProperty(pojo, editor.name(), deserializer.read(element, attrName))) {
            LOGGER.debug("copy中复制{}时发生错误，属性[{}]的值将被忽略", editor.name(), editor.name());
        }
    }

    /**
     * 往pojo中指定字段设置值（字段为Collection类型）
     *
     * @param elements
     *            要设置的数据节点
     * @param attrName
     *            要获取的属性名，如果该值不为空则认为数据需要从属性中取而不是从节点数据中取
     * @param pojo
     *            pojo
     * @param editor
     *            字段编辑器
     * @param <D>
     *            字段类型
     */
    @SuppressWarnings("unchecked")
    private <D> void setValue(List<Element> elements, String attrName, Object pojo, PropertyEditor editor) {
        XmlNode attrXmlNode = editor.getAnnotation(XmlNode.class);
        LOGGER.debug("要赋值的fieldName为{}", editor.name());
        final XmlDeserializer<?> deserializer = resolve(attrXmlNode, editor);

        // 实际最终使用集合类型
        Class<? extends Collection<D>> collectionClass;
        // 字段真实类型
        Class<? extends Collection<D>> real = (Class<? extends Collection<D>>)editor.type();

        if (attrXmlNode != null) {
            collectionClass = (Class<? extends Collection<D>>)attrXmlNode.arrayType();

            if (Collection.class.equals(collectionClass)) {
                // 用户没有指定，使用字段真实类型
                collectionClass = real;
            } else if (!real.isAssignableFrom(collectionClass)) {
                // 强校验
                throw new SerializeException(ErrorCodeEnum.CODE_ERROR, StringUtils
                    .format("字段[{}]解析错误,用户指定的集合类型[{}]不是字段的实际集合类型[{}]的子类", editor.original(), collectionClass, real));
            }
        } else {
            collectionClass = real;
        }

        // 最终使用的列表
        List<Element> elementList = elements;
        if (attrXmlNode != null && StringUtils.isNotBlank(attrXmlNode.arrayRoot()) && !elements.isEmpty()) {
            elementList = elements.get(0).elements(attrXmlNode.arrayRoot());
        }

        // 将数据转换为用户指定数据
        List<D> list = elementList.stream().map(d -> (D)deserializer.read(d, attrName)).collect(Collectors.toList());

        if (!trySetValue(list, pojo, editor, collectionClass)) {
            LOGGER.warn("无法为字段[{}]赋值", editor.name());
        }
    }

    /**
     * 尝试为list类型的字段赋值
     *
     * @param data
     *            转换后的数据
     * @param pojo
     *            要赋值的pojo
     * @param editor
     *            要赋值的字段编辑器
     * @param clazz
     *            集合的Class对象
     * @return 返回true表示赋值成功，返回false表示赋值失败
     */
    private <D> boolean trySetValue(List<D> data, Object pojo, PropertyEditor editor,
        Class<? extends Collection<D>> clazz) {

        Collection<D> collection = tryBuildCollection(clazz);

        if (collection == null) {
            LOGGER.warn("无法为class[{}]构建实例", clazz);
            return false;
        }

        collection.addAll(data);
        try {
            return BeanUtils.setProperty(pojo, editor.name(), collection);
        } catch (Exception e) {
            LOGGER.debug(e, "字段[{}]赋值失败，使用的集合类为[{}]", editor.name(), clazz);
            return false;
        }
    }

    /**
     * 确定Deserializer
     *
     * @param attrXmlNode
     *            字段的注释
     * @param editor
     *            字段编辑器
     * @param <D>
     *            字段类型
     * @return 字段对应的Deserializer
     */
    @SuppressWarnings("unchecked")
    private <D extends XmlDeserializer<?>> D resolve(XmlNode attrXmlNode, PropertyEditor editor) {
        return (D)localDeserializers.compute(editor, (key, deserializer) -> {
            if (deserializer != null) {
                return deserializer;
            }

            if (attrXmlNode != null) {
                Class<D> fieldDeserializerClass;
                fieldDeserializerClass = (Class<D>)attrXmlNode.deserializer();
                // 判断用户是否指定Deserializer
                if (NullDeserializer.class.equals(fieldDeserializerClass)) {
                    // 用户没有指定Deserializer
                    deserializer = resolve(editor);
                } else {
                    try {
                        deserializer = fieldDeserializerClass.newInstance();
                    } catch (Exception e) {
                        deserializer = resolve(editor);
                        LOGGER.warn("指定的xml转换器[{}]无法实例化，请为该转换器增加公共无参数构造器，当前将使用默认转换器[{}]", fieldDeserializerClass,
                            deserializer.getClass(), e);
                    }
                }
            } else {
                deserializer = resolve(editor);
            }

            return deserializer.createContextual(editor);
        });
    }

    /**
     * 根据字段说明自动推断使用什么转换器
     *
     * @param editor
     *            字段编辑器
     * @param <D>
     *            字段的类型
     * @return 根据字段说明推断出来的转换器
     */
    @SuppressWarnings("unchecked")
    private <D extends XmlDeserializer<?>> D resolve(PropertyEditor editor) {
        Class<?> type = editor.type();
        D deserializer = (D)parentDeserializers.get(type);
        if (deserializer != null) {
            return deserializer;
        }

        if (Collection.class.isAssignableFrom(type)) {
            // 到这里的只有两种可能，一、用户没有指定Deserializer；二、用户没有加注解XmlNode
            XmlNode xmlnode = editor.getAnnotation(XmlNode.class);
            if (xmlnode == null) {
                // 用户没有添加xmlnode注解，使用默认Deserializer
                deserializer = (D)Deserializers.DEFAULT_DESERIALIZER;
            } else {
                // 用户指定了xmlnode注解但是没有指定Deserializer，使用general字段确定集合中的数据类型
                deserializer = (D)new BeanDeserializer<>(xmlnode.general(), parentDeserializers);
            }
        } else {
            deserializer = (D)new BeanDeserializer<>(editor.type(), parentDeserializers);
        }

        return deserializer;
    }

}
