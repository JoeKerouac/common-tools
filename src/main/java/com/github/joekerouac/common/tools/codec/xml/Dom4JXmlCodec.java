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

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.github.joekerouac.common.tools.codec.Codec;
import com.github.joekerouac.common.tools.codec.exception.SerializeException;
import com.github.joekerouac.common.tools.codec.xml.deserializer.BeanDeserializer;
import com.github.joekerouac.common.tools.codec.xml.deserializer.Deserializers;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.exception.CommonException;
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
 * 注意：非线程安全，同时可能会有内存问题；
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@CustomLog
public class Dom4JXmlCodec implements Codec {

    private static final String DEFAULT_ROOT = "root";

    protected final SAXReader reader;

    private final Map<Class<?>, XmlDeserializer<?>> deserializers;

    private final boolean writeHeader;

    public Dom4JXmlCodec() {
        this(false);
    }

    public Dom4JXmlCodec(boolean writeHeader) {
        this.writeHeader = writeHeader;
        reader = new SAXReader();
        deserializers = new ConcurrentHashMap<>();
        deserializers.putAll(Deserializers.defaultDeserializers);
        enableDTD(false);
    }

    @SuppressWarnings("unchecked")
    public <T> XmlDeserializer<T> addDeserializer(Class<T> type, XmlDeserializer<T> deserializer) {
        return (XmlDeserializer<T>)deserializers.put(type, deserializer);
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
     * PS：对象中的集合字段必须添加注解，必须是简单集合，即集合中只有一种数据类型，并且当类型不是String时需要指定Deserializer，否则将会解析失败。
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

        Document document;

        // 解析XML
        try {
            document = parseText(xml);
        } catch (Exception e) {
            LOGGER.error(e, "xml解析错误");
            return null;
        }

        Element root = document.getRootElement();

        if (root == null) {
            return null;
        }

        XmlDeserializer<T> xmlDeserializer = (XmlDeserializer<T>)deserializers.compute(clazz, (key, deserializer) -> {
            if (deserializer == null) {
                deserializer = new BeanDeserializer<>(clazz, deserializers);
            }
            return deserializer;
        });

        return xmlDeserializer.read(root, null);
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
     * @param charset
     *            字符集
     * @return 解析结果
     */
    public String toXml(Object source, Charset charset) {
        return toXml(source, charset, null, false);
    }

    /**
     * 将Object解析为xml，暂时只能解析基本类型（可以正确解析list、map）
     *
     * @param source
     *            bean
     * @param charset
     *            字符集
     * @param rootName
     *            根节点名称，如果为null则会尝试使用默认值
     * @param hasNull
     *            是否包含null元素（true：包含）
     * @return 解析结果
     */
    public String toXml(Object source, Charset charset, String rootName, boolean hasNull) {
        return toXml(source, charset, rootName, hasNull, false);
    }

    /**
     * 将Object解析为xml，暂时只能解析基本类型（可以正确解析list、map）
     *
     * @param source
     *            bean
     * @param charset
     *            字符集
     * @param defaultRootName
     *            根节点名称，如果为null则会尝试使用默认值
     * @param hasNull
     *            是否包含null元素（true：包含）
     * @param pretty
     *            是否美化输出，true表示美化输出
     * @return 解析结果
     */
    public String toXml(Object source, Charset charset, String defaultRootName, boolean hasNull, boolean pretty) {
        if (source == null) {
            LOGGER.warn("传入的source为null，返回null");
            return null;
        }

        String rootName = defaultRootName;

        if (rootName == null) {
            XmlNode xmlNode = getXmlNodeFromClass(source.getClass());
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

        OutputFormat format = pretty ? OutputFormat.createPrettyPrint() : new OutputFormat();
        format.setEncoding(charset.name());
        StringWriter out = new StringWriter();

        try {
            XMLWriter writer = new XMLWriter(out, format);

            if (writeHeader) {
                Document document = DocumentHelper.createDocument(root);
                writer.write(document);
            } else {
                writer.write(root);
            }
            writer.flush();
            return out.toString();
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
     * 从类上获取XmlNode注解，如果不存在则尝试从父类上获取
     * 
     * @param clazz
     *            class
     * @return XmlNode
     */
    private XmlNode getXmlNodeFromClass(Class<?> clazz) {
        XmlNode xmlNode = clazz.getDeclaredAnnotation(XmlNode.class);
        if (xmlNode != null) {
            return xmlNode;
        }

        if (clazz.getSuperclass().equals(Object.class)) {
            return null;
        }

        return getXmlNodeFromClass(clazz.getSuperclass());
    }

    @Override
    public <T> T read(byte[] data, Charset charset, Class<T> type) throws SerializeException {
        return parse(new String(data, charset == null ? StandardCharsets.UTF_8 : charset), type);
    }

    @Override
    public <T> T read(byte[] data, Charset charset, AbstractTypeReference<T> typeReference) throws SerializeException {
        throw new CommonException(ErrorCodeEnum.CODE_ERROR, "不支持的方法");
    }

    @Override
    public byte[] write(Object data, Charset charset) {
        return toXml(data, charset).getBytes(charset == null ? StandardCharsets.UTF_8 : charset);
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
