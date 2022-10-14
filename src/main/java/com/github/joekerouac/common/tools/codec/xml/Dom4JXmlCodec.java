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

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.joekerouac.common.tools.codec.Codec;
import com.github.joekerouac.common.tools.codec.exception.SerializeException;
import com.github.joekerouac.common.tools.codec.xml.converter.XmlTypeConverterUtil;
import com.github.joekerouac.common.tools.constant.Const;
import com.github.joekerouac.common.tools.exception.CommonException;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.reflect.AccessorUtil;
import com.github.joekerouac.common.tools.reflect.ClassUtils;
import com.github.joekerouac.common.tools.reflect.bean.BeanUtils;
import com.github.joekerouac.common.tools.reflect.bean.PropertyEditor;
import com.github.joekerouac.common.tools.reflect.type.AbstractTypeReference;
import com.github.joekerouac.common.tools.reflect.type.JavaTypeUtil;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.AllArgsConstructor;
import lombok.CustomLog;
import lombok.Data;

/**
 * dom4j实现的xml解析器
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@CustomLog
public class Dom4JXmlCodec implements Codec {

    private static final String DEFAULT_ROOT = "root";

    protected final SAXReader reader;

    public Dom4JXmlCodec() {
        this.reader = new SAXReader();
        enableDTD(false);
    }

    /**
     * 设置DTD支持
     *
     * @param enable
     *            true表示支持DTD，false表示不支持
     */
    public void enableDTD(boolean enable) {
        // 允许DTD会有XXE漏洞，关于XXE漏洞：https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet
        if (enable) {
            // 打开DTD支持，高危操作，除非你清楚你在做什么，否则不要打开
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
            setFeature("http://xml.org/sax/features/external-general-entities", true);
            setFeature("http://xml.org/sax/features/external-parameter-entities", true);
        } else {
            // 不允许DTD
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            setFeature("http://xml.org/sax/features/external-general-entities", false);
            setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        }
    }

    /**
     * 配置SAXReader
     *
     * @param k
     *            name
     * @param enable
     *            是否允许，true表示允许
     */
    public void setFeature(String k, boolean enable) {
        try {
            reader.setFeature(k, enable);
        } catch (SAXException e) {
            throw new RuntimeException("设置属性失败:[" + k + ":" + enable + "]");
        }
    }

    /**
     * 将xml解析为Document
     *
     * @param text
     *            xml文本
     * @return Document
     * @throws DocumentException
     *             DocumentException
     */
    private Document parseText(String text) throws DocumentException {
        Document result;

        String encoding = getEncoding(text);

        InputSource source = new InputSource(new StringReader(text));
        source.setEncoding(encoding);

        result = reader.read(source);

        // if the XML parser doesn't provide a way to retrieve the encoding,
        // specify it manually
        if (result.getXMLEncoding() == null) {
            result.setXMLEncoding(encoding);
        }

        return result;
    }

    /**
     * 获取xml的编码方式
     *
     * @param text
     *            xml文件
     * @return xml的编码
     */
    private String getEncoding(String text) {
        String result = null;

        String xml = text.trim();

        if (xml.startsWith("<?xml")) {
            int end = xml.indexOf("?>");
            String sub = xml.substring(5, end).trim();
            StringTokenizer tokens = new StringTokenizer(sub, " =\"'");

            while (tokens.hasMoreTokens()) {
                String token = tokens.nextToken();

                if ("encoding".equals(token)) {
                    if (tokens.hasMoreTokens()) {
                        result = tokens.nextToken();
                    }

                    break;
                }
            }
        }

        return result;
    }

    /**
     * 解析XML，将xml解析为map（注意：如果XML是&lt;a&gt;data&lt;b&gt;bbb&lt;/b&gt;&lt;/a&gt; 这种格式那么data将不被解析，对于list可以正确解析）
     *
     * @param xml
     *            xml字符串
     * @param mapClass
     *            map的class
     * @return 由xml解析的Map，可能是String类型或者Map&lt;String, Object&gt;类型，其中Map的value有可能 是Stirng类型，也有可能是List&lt;String&gt;类型
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> parseToMap(String xml, Class<?> mapClass) {
        Assert.argNotNull(mapClass, "mapClass");
        Assert.argNotBlank(xml, "xml");

        Map<String, Object> map = newMap(mapClass);
        try {
            Document document = parseText(xml);
            Element root = document.getRootElement();
            if (root.elements().size() == 0) {
                map.put(root.getName(), root.getText());
            } else {
                map.putAll((Map<String, Object>)parse(root));
            }
            return map;
        } catch (Exception e) {
            LOGGER.error(e, "xml格式不正确");
            return null;
        }
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
     * 将XML解析为POJO对象，暂时无法解析map，当需要解析的字段是{@link java.util.Collection}的子类时必须带有注 解{@link XmlNode}，否则将解析失败。
     * <p>
     * PS：对象中的集合字段必须添加注解，必须是简单集合，即集合中只有一种数据类型，并且当类型不是String时需要指定 converter，否则将会解析失败。
     *
     * @param xml
     *            XML源
     * @param clazz
     *            POJO对象的class
     * @param <T>
     *            POJO的实际类型
     * @return 解析结果
     */
    @SuppressWarnings("unchecked")
    public <T> T parse(String xml, Class<T> clazz) {
        Assert.argNotBlank(xml, "xml");
        Assert.argNotNull(clazz, "clazz");

        if (StringUtils.isBlank(xml)) {
            return null;
        }

        if (Map.class.isAssignableFrom(clazz)) {
            return (T)parseToMap(xml, clazz);
        } else if (clazz.getName().startsWith("java.")) {
            // 对java内置对象不支持，其实该解析器仅支持自定义pojo类，其他对象都不支持，不过java内置对象排除成本最低，所以先排除
            throw new CommonException(ErrorCodeEnum.CODE_ERROR, StringUtils.format("不支持的类型:[{}]", clazz));
        }

        T pojo;
        Document document;

        // 获取pojo对象的实例
        try {
            // 没有权限访问该类或者该类（为接口、抽象类）不能实例化时将抛出异常
            pojo = ClassUtils.getInstance(clazz);
        } catch (Exception e) {
            LOGGER.error(e, "class对象生成失败，请检查代码；失败原因：");
            throw new RuntimeException(e);
        }

        // 解析XML
        try {
            document = parseText(xml);
        } catch (Exception e) {
            LOGGER.error(e, "xml解析错误");
            return null;
        }

        // 获取pojo对象的说明
        PropertyEditor[] propertyEditors = BeanUtils.getPropertyDescriptors(clazz);
        Element root = document.getRootElement();

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

    /**
     * 解析element
     *
     * @param element
     *            element
     * @return 解析结果，可能是String类型或者Map&lt;String, Object&gt;类型，其中Map的value有可能是Stirng类型，也有可能 是List&lt;String&gt;类型
     */
    @SuppressWarnings("unchecked")
    private Object parse(Element element) {
        List<Element> elements = element.elements();
        if (elements.size() == 0) {
            return element.getText();
        } else {
            Map<String, Object> map = new HashMap<>();
            for (Element ele : elements) {
                Object result = parse(ele);
                if (map.containsKey(ele.getName())) {
                    // 如果map中已经包含该key，说明该key有多个，是个list
                    Object obj = map.get(ele.getName());
                    List<String> list;
                    if (obj instanceof List) {
                        // 如果obj不等于null并且是list对象，说明map中存的已经是一个list
                        list = (List<String>)obj;
                    } else {
                        // 如果obj等于null或者不是list对象，那么新建一个list对象
                        list = new ArrayList<>();
                        list.add(obj == null ? null : String.valueOf(obj));
                    }
                    list.add(result == null ? null : String.valueOf(result));
                    map.put(ele.getName(), list);
                } else {
                    map.put(ele.getName(), result);
                }
            }
            return map;
        }
    }

    /**
     * 将Object解析为xml，根节点为root，字段值为null的将不包含在xml中，暂时只能解析基本类型（可以正确解析list、map）
     *
     * @param source
     *            bean
     * @return 解析结果
     */
    public String toXml(Object source) {
        return toXml(source, null, false);
    }

    /**
     * 将Object解析为xml，暂时只能解析基本类型（可以正确解析list、map）
     *
     * @param source
     *            bean
     * @param rootName
     *            根节点名称，如果为null则会尝试使用默认值
     * @param hasNull
     *            是否包含null元素（true：包含）
     * @return 解析结果
     */
    public String toXml(Object source, String rootName, boolean hasNull) {
        return toXml(source, rootName, hasNull, false);
    }

    /**
     * 将Object解析为xml，暂时只能解析基本类型（可以正确解析list、map）
     *
     * @param source
     *            bean
     * @param defaultRootName
     *            根节点名称，如果为null则会尝试使用默认值
     * @param hasNull
     *            是否包含null元素（true：包含）
     * @param pretty
     *            是否美化输出，true表示美化输出
     * @return 解析结果
     */
    public String toXml(Object source, String defaultRootName, boolean hasNull, boolean pretty) {
        if (source == null) {
            LOGGER.warn("传入的source为null，返回null");
            return null;
        }

        String rootName = defaultRootName;

        if (rootName == null) {
            XmlNode xmlNode = source.getClass().getDeclaredAnnotation(XmlNode.class);
            rootName = xmlNode == null ? null : xmlNode.name();
        }

        if (rootName == null) {
            rootName = DEFAULT_ROOT;
        }

        Long start = System.currentTimeMillis();
        Element root = DocumentHelper.createElement(rootName);
        buildDocument(root, source, source.getClass(), !hasNull);
        Long end = System.currentTimeMillis();
        LOGGER.debug("解析xml用时" + (end - start) + "ms");

        if (!pretty) {
            return root.asXML();
        }
        OutputFormat format = OutputFormat.createPrettyPrint();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            XMLWriter writer = new XMLWriter(outputStream, format);
            writer.write(root);
            writer.close();
            return outputStream.toString(Const.DEFAULT_CHARSET.name());
        } catch (Exception e) {
            throw new SerializeException(ErrorCodeEnum.SERIAL_EXCEPTION, "序列化异常", e);
        }
    }

    /**
     * 根据pojo构建xml的document（方法附件参考附件xml解析器思路）
     *
     * @param parent
     *            父节点
     * @param pojo
     *            pojo，不能为空
     * @param clazz
     *            pojo的Class
     * @param ignoreNull
     *            是否忽略空元素
     */
    @SuppressWarnings("unchecked")
    private void buildDocument(Element parent, Object pojo, Class<?> clazz, boolean ignoreNull) {
        // 字段描述，key是节点名，value是XmlData
        Map<String, XmlData> map = new HashMap<>();
        // 构建字段描述
        if (pojo instanceof Map) {
            Map<?, ?> pojoMap = (Map<?, ?>)pojo;
            pojoMap.forEach((k, v) -> {
                if (k == null) {
                    LOGGER.debug("忽略map中key为null的值");
                } else {
                    if (ignoreNull && v == null) {
                        LOGGER.debug("当前配置为忽略空值，[{}]的值为空，忽略", k);
                    } else {
                        map.put(String.valueOf(k), new XmlData(null, v, v == null ? null : v.getClass()));
                    }
                }
            });
        } else {
            PropertyEditor[] propertyEditors =
                BeanUtils.getPropertyDescriptors(clazz == null ? pojo.getClass() : clazz);

            for (PropertyEditor editor : propertyEditors) {
                XmlNode xmlNode = editor.getAnnotation(XmlNode.class);
                // 如果没有读取方法，并且没有加注解，忽略该字段
                if (xmlNode == null && !editor.hasReadMethod()) {
                    LOGGER.debug("字段[{}]没有XmlNode注解并且没有读取方法，忽略该字段", editor.name());
                    continue;
                }

                if (AccessorUtil.isTransient(editor.original())) {
                    LOGGER.debug("字段[{}]是transient修饰的，不进行写出序列化", editor.name());
                    continue;
                }

                // 字段值
                try {
                    Object valueObj = pojo == null ? null : BeanUtils.getProperty(pojo, editor.name());
                    // 判断是否忽略
                    if ((ignoreNull && valueObj == null) || (xmlNode != null && xmlNode.ignore())) {
                        LOGGER.debug("忽略空节点或者节点被注解忽略");
                        continue;
                    }

                    // 节点名
                    String nodeName =
                        (xmlNode == null || StringUtils.isBlank(xmlNode.name())) ? editor.name() : xmlNode.name();
                    map.put(nodeName, new XmlData(xmlNode, valueObj, editor.type()));
                } catch (Exception e) {
                    LOGGER.error(e, "获取字段值时发生异常，忽略改值");
                }
            }
        }

        map.forEach((k, v) -> {
            XmlNode xmlNode = v.getXmlNode();
            Object valueObj = v.getData();

            // 节点名
            // 属性名
            String attrName =
                (xmlNode == null || StringUtils.isBlank(xmlNode.attributeName())) ? k : xmlNode.attributeName();
            // 是否是cdata
            boolean isCDATA = xmlNode != null && xmlNode.isCDATA();
            // 数据类型
            Class<?> type = v.getType();
            // 构建一个对应的节点
            Element node = parent.element(k);
            if (node == null) {
                // 搜索不到，创建一个（在属性是父节点属性的情况和节点是list的情况需要将该节点删除）
                node = DocumentHelper.createElement(k);
                parent.add(node);
            }

            // 判断字段对应的是否是属性
            if (xmlNode != null && xmlNode.isAttribute()) {
                // 判断是否是父节点的属性
                if (StringUtils.isBlank(xmlNode.name())) {
                    // 如果是父节点那么删除之前添加的
                    parent.remove(node);
                    node = parent;
                }
                Element finalNode = node;

                if (Map.class.isAssignableFrom(type)) {
                    // 这里要判断valueObj是否等于null，因为如果不忽略null的话这里是有可能传过来一个null值的
                    if (valueObj != null) {
                        Map<String, String> attrs = (Map<String, String>)valueObj;
                        attrs.forEach(finalNode::addAttribute);
                    }
                } else {
                    // 属性值，属性值只能是简单值
                    String attrValue = valueObj == null ? "" : String.valueOf(valueObj);

                    // 为属性对应的节点添加属性
                    finalNode.addAttribute(attrName, attrValue);
                }
            } else if (type == null) {
                LOGGER.debug("当前不知道节点[{}]的类型，忽略该节点", k);
            } else if (JavaTypeUtil.isNotPojo(type)) {
                // 是简单类型或者集合类型
                if (Map.class.isAssignableFrom(type)) {
                    LOGGER.warn("当前字段[{}]是map类型", k);
                    buildDocument(node, v.getData(), type, ignoreNull);
                } else if (Collection.class.isAssignableFrom(type)) {
                    parent.remove(node);
                    // 集合类型
                    // 判断字段值是否为null
                    if (valueObj != null) {
                        String arrayNodeName;
                        Element root;
                        if (xmlNode == null || StringUtils.isBlank(xmlNode.arrayRoot())) {
                            arrayNodeName = k;
                            root = parent;
                        } else {
                            arrayNodeName = xmlNode.arrayRoot();
                            root = DocumentHelper.createElement(k);
                            parent.add(root);
                        }
                        Collection<?> collection = (Collection<?>)valueObj;
                        collection.forEach(obj -> {
                            Element n = DocumentHelper.createElement(arrayNodeName);
                            root.add(n);
                            buildDocument(n, obj, null, ignoreNull);
                        });
                    }
                } else {
                    String text = valueObj == null ? "" : String.valueOf(valueObj);
                    if (isCDATA) {
                        LOGGER.debug("内容[{}]需要CDATA标签包裹", text);
                        node.add(DocumentHelper.createCDATA(text));
                    } else {
                        node.setText(text);
                    }
                }
            } else {
                // 猜测字段类型（防止字段的声明是一个接口，优先采用xmlnode中申明的类型）
                Class<?> realType = resolveRealType(type, xmlNode);
                // pojo类型
                buildDocument(node, valueObj, realType, ignoreNull);
            }
        });
    }

    /**
     * 确定字段的真实类型
     *
     * @param fieldType
     *            字段类型
     * @param xmlNode
     *            字段XmlNode注解
     * @return 字段实际类型而不是接口或者抽象类
     */
    private Class<?> resolveRealType(Class<?> fieldType, XmlNode xmlNode) {
        // 猜测字段类型（防止字段的声明是一个接口，优先采用xmlnode中申明的类型）
        Class<?> type = (xmlNode == null) ? fieldType : xmlNode.general();

        if (!fieldType.isAssignableFrom(type)) {
            type = fieldType;
        }
        return type;
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
        final XmlTypeConvert<?> convert = XmlTypeConverterUtil.resolve(attrXmlNode, editor);
        if (!BeanUtils.setProperty(pojo, editor.name(), convert.read(element, attrName))) {
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
     * @param <T>
     *            字段类型
     */
    @SuppressWarnings("unchecked")
    private <T> void setValue(List<Element> elements, String attrName, Object pojo, PropertyEditor editor) {
        XmlNode attrXmlNode = editor.getAnnotation(XmlNode.class);
        LOGGER.debug("要赋值的fieldName为{}", editor.name());
        final XmlTypeConvert<?> convert = XmlTypeConverterUtil.resolve(attrXmlNode, editor);

        // 实际最终使用集合类型
        Class<? extends Collection<T>> collectionClass;
        // 字段真实类型
        Class<? extends Collection<T>> real = (Class<? extends Collection<T>>)editor.type();

        if (attrXmlNode != null) {
            collectionClass = (Class<? extends Collection<T>>)attrXmlNode.arrayType();

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
        List<T> list = elementList.stream().map(d -> (T)convert.read(d, attrName)).collect(Collectors.toList());

        if (!trySetValue(list, pojo, editor, collectionClass)) {
            LOGGER.warn("无法为字段[{}]赋值", editor.name());
        }
    }

    /**
     * 尝试为list类型的字段赋值
     *
     * @param datas
     *            转换后的数据
     * @param pojo
     *            要赋值的pojo
     * @param editor
     *            要赋值的字段编辑器
     * @param clazz
     *            集合的Class对象
     * @return 返回true表示赋值成功，返回false表示赋值失败
     */
    private <T> boolean trySetValue(List<T> datas, Object pojo, PropertyEditor editor,
        Class<? extends Collection<T>> clazz) {

        Collection<T> collection = tryBuildCollection(clazz);

        if (collection == null) {
            LOGGER.warn("无法为class[{}]构建实例", clazz);
            return false;
        }

        collection.addAll(datas);
        try {
            return BeanUtils.setProperty(pojo, editor.name(), collection);
        } catch (Exception e) {
            LOGGER.debug(e, "字段[{}]赋值失败，使用的集合类为[{}]", editor.name(), clazz);
            return false;
        }
    }

    /**
     * 根据class尝试构建出集合实例，有可能返回null，禁止抛出异常
     *
     * @param clazz
     *            集合的Class对象
     * @return 集合实例
     */
    private <T> Collection<T> tryBuildCollection(Class<? extends Collection<T>> clazz) {
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

    @Override
    public <T> T read(byte[] data, Class<T> type) throws SerializeException {
        return parse(new String(data, Const.DEFAULT_CHARSET), type);
    }

    @Override
    public <T> T read(byte[] data, AbstractTypeReference<T> typeReference) throws SerializeException {
        throw new CommonException(ErrorCodeEnum.CODE_ERROR, "不支持的方法");
    }

    @Override
    public byte[] write(Object data) {
        return toXml(data).getBytes(Const.DEFAULT_CHARSET);
    }

    /**
     * XML节点数据
     */
    @Data
    @AllArgsConstructor
    private static class XmlData {

        /**
         * 节点注解，可以为空
         */
        private XmlNode xmlNode;

        /**
         * 节点数据
         */
        private Object data;

        /**
         * 节点数据的实际类型，可以为空
         */
        private Class<?> type;
    }

}
