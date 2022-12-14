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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
 * dom4j?????????xml?????????
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
     * ??????DTD??????
     *
     * @param enable
     *            true????????????DTD???false???????????????
     */
    public void enableDTD(boolean enable) {
        // ??????DTD??????XXE???????????????XXE?????????https://www.owasp.org/index.php/XML_External_Entity_(XXE)_Prevention_Cheat_Sheet
        if (enable) {
            // ??????DTD???????????????????????????????????????????????????????????????????????????
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", true);
            setFeature("http://xml.org/sax/features/external-general-entities", true);
            setFeature("http://xml.org/sax/features/external-parameter-entities", true);
        } else {
            // ?????????DTD
            setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            setFeature("http://xml.org/sax/features/external-general-entities", false);
            setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        }
    }

    /**
     * ??????SAXReader
     *
     * @param k
     *            name
     * @param enable
     *            ???????????????true????????????
     */
    public void setFeature(String k, boolean enable) {
        try {
            reader.setFeature(k, enable);
        } catch (SAXException e) {
            throw new RuntimeException("??????????????????:[" + k + ":" + enable + "]");
        }
    }

    /**
     * ???xml?????????Document
     *
     * @param text
     *            xml??????
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
     * ??????xml???????????????
     *
     * @param text
     *            xml??????
     * @return xml?????????
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
     * ??????XML??????xml?????????map??????????????????XML???&lt;a&gt;data&lt;b&gt;bbb&lt;/b&gt;&lt;/a&gt; ??????????????????data????????????????????????list?????????????????????
     *
     * @param xml
     *            xml?????????
     * @param mapClass
     *            map???class
     * @return ???xml?????????Map????????????String????????????Map&lt;String, Object&gt;???????????????Map???value????????? ???Stirng????????????????????????List&lt;String&gt;??????
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
            LOGGER.error(e, "xml???????????????");
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private <K, V> Map<K, V> newMap(Class<?> mapClass) {
        if (!Map.class.isAssignableFrom(mapClass)) {
            throw new SerializeException(ErrorCodeEnum.CODE_ERROR, "??????????????????Map???class");
        }

        Class<?> realMapClass = mapClass;
        if (Map.class.equals(mapClass)) {
            realMapClass = HashMap.class;
        }

        if (AccessorUtil.isAbstract(realMapClass)) {
            throw new SerializeException(ErrorCodeEnum.CODE_ERROR,
                String.format("????????????map??????[%s]?????????????????????map??????", mapClass));
        }

        return (Map<K, V>)ClassUtils.getInstance(realMapClass);
    }

    /**
     * ???XML?????????POJO???????????????????????????map??????????????????????????????{@link java.util.Collection}??????????????????????????? ???{@link XmlNode}???????????????????????????
     * <p>
     * PS????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????String??????????????? converter??????????????????????????????
     *
     * @param xml
     *            XML???
     * @param clazz
     *            POJO?????????class
     * @param <T>
     *            POJO???????????????
     * @return ????????????
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
            // ???java????????????????????????????????????????????????????????????pojo???????????????????????????????????????java????????????????????????????????????????????????
            throw new CommonException(ErrorCodeEnum.CODE_ERROR, StringUtils.format("??????????????????:[{}]", clazz));
        }

        T pojo;
        Document document;

        // ??????pojo???????????????
        try {
            // ????????????????????????????????????????????????????????????????????????????????????????????????
            pojo = ClassUtils.getInstance(clazz);
        } catch (Exception e) {
            LOGGER.error(e, "class??????????????????????????????????????????????????????");
            throw new RuntimeException(e);
        }

        // ??????XML
        try {
            document = parseText(xml);
        } catch (Exception e) {
            LOGGER.error(e, "xml????????????");
            return null;
        }

        // ??????pojo???????????????
        PropertyEditor[] propertyEditors = BeanUtils.getPropertyDescriptors(clazz);
        Element root = document.getRootElement();

        boolean hasMapAttr = false;
        PropertyEditor mapEditor = null;
        Map<String, String> attrMap = null;

        for (PropertyEditor editor : propertyEditors) {
            XmlNode xmlNode = editor.getAnnotation(XmlNode.class);
            final String fieldName = editor.name();
            // ?????????
            String nodeName = null;
            // ?????????
            String attributeName = null;
            boolean isParent = false;
            boolean ignore = false;
            boolean isAttr = false;
            if (xmlNode == null) {
                nodeName = fieldName;
            } else if (!xmlNode.ignore()) {
                // ????????????????????????????????????????????????
                // ?????????????????????????????????????????????????????????????????????????????????????????????
                nodeName = StringUtils.isBlank(xmlNode.name()) ? fieldName : xmlNode.name();
                LOGGER.debug("??????[{}]????????????????????????{}", fieldName, nodeName);

                // ??????????????????????????????
                if (xmlNode.isAttribute()) {
                    isAttr = true;
                    // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    // ??????????????????????????????????????????????????????????????????
                    if (StringUtils.isBlank(xmlNode.attributeName())) {
                        LOGGER.debug("??????[{}]??????????????????????????????????????????attributeName?????????????????????????????????????????????", editor.name());
                        attributeName = fieldName;
                    } else {
                        attributeName = xmlNode.attributeName();
                    }

                    if (StringUtils.isBlank(xmlNode.name())) {
                        LOGGER.debug("???????????????????????????????????????????????????name??????????????????isParent???true");
                        isParent = true;
                    }

                    // ????????????map??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                    if (Map.class.isAssignableFrom(editor.type())) {
                        if (hasMapAttr) {
                            throw new SerializeException(ErrorCodeEnum.SERIAL_EXCEPTION, StringUtils.format(
                                "????????????[{}]???map?????????????????????????????????????????????map?????????????????????[{}]????????????????????????", editor.name(), mapEditor.name()));
                        } else {
                            hasMapAttr = true;
                            mapEditor = editor;
                            attrMap = mapEditor.read(pojo);
                            attrMap = attrMap == null ? newMap(mapEditor.type()) : attrMap;

                            editor.write(pojo, attrMap);
                        }
                    }
                } else {
                    LOGGER.debug("??????[{}]??????????????????", fieldName);
                }
            } else {
                ignore = true;
            }

            // ???????????????????????????map??????
            if (!ignore) {
                // ????????????????????????element
                List<Element> nodes = (isAttr && isParent) ? Collections.singletonList(root) : root.elements(nodeName);
                // ??????????????????
                if (nodes.isEmpty()) {
                    // ???????????????????????????????????????????????????
                    nodes = root.elements(StringUtils.toFirstUpperCase(nodeName));
                }
                if (!nodes.isEmpty()) {
                    // ??????????????????????????????pojo??????
                    Class<?> type = editor.type();

                    // ????????????
                    // ???????????????????????????
                    if (Collection.class.isAssignableFrom(type)) {
                        // ?????????
                        setValue(nodes, attributeName, pojo, editor);
                    } else if (Map.class.isAssignableFrom(type)) {
                        // ???Map

                        if (!isAttr) {
                            LOGGER.warn("???????????????????????????map");
                            continue;
                        }

                        Element element = StringUtils.isBlank(xmlNode.name()) ? root : root.element(xmlNode.name());

                        if (element != null) {
                            // ????????????
                            List<Attribute> attributes = element.attributes();

                            for (Attribute attribute : attributes) {
                                attrMap.put(attribute.getName(), attribute.getValue());
                            }
                        }
                    } else {
                        // ???????????????????????????
                        setValue(nodes.get(0), attributeName, pojo, editor);
                    }
                }
            }
        }
        return pojo;
    }

    /**
     * ??????element
     *
     * @param element
     *            element
     * @return ????????????????????????String????????????Map&lt;String, Object&gt;???????????????Map???value????????????Stirng????????????????????? ???List&lt;String&gt;??????
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
                    // ??????map??????????????????key????????????key??????????????????list
                    Object obj = map.get(ele.getName());
                    List<String> list;
                    if (obj instanceof List) {
                        // ??????obj?????????null?????????list???????????????map????????????????????????list
                        list = (List<String>)obj;
                    } else {
                        // ??????obj??????null????????????list???????????????????????????list??????
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
     * ???Object?????????xml???????????????root???????????????null??????????????????xml?????????????????????????????????????????????????????????list???map???
     *
     * @param source
     *            bean
     * @return ????????????
     */
    public String toXml(Object source) {
        return toXml(source, null, false);
    }

    /**
     * ???Object?????????xml??????????????????????????????????????????????????????list???map???
     *
     * @param source
     *            bean
     * @param rootName
     *            ???????????????????????????null???????????????????????????
     * @param hasNull
     *            ????????????null?????????true????????????
     * @return ????????????
     */
    public String toXml(Object source, String rootName, boolean hasNull) {
        return toXml(source, rootName, hasNull, false);
    }

    /**
     * ???Object?????????xml??????????????????????????????????????????????????????list???map???
     *
     * @param source
     *            bean
     * @param defaultRootName
     *            ???????????????????????????null???????????????????????????
     * @param hasNull
     *            ????????????null?????????true????????????
     * @param pretty
     *            ?????????????????????true??????????????????
     * @return ????????????
     */
    public String toXml(Object source, String defaultRootName, boolean hasNull, boolean pretty) {
        if (source == null) {
            LOGGER.warn("?????????source???null?????????null");
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
        LOGGER.debug("??????xml??????" + (end - start) + "ms");

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
            throw new SerializeException(ErrorCodeEnum.SERIAL_EXCEPTION, "???????????????", e);
        }
    }

    /**
     * ??????pojo??????xml???document???????????????????????????xml??????????????????
     *
     * @param parent
     *            ?????????
     * @param pojo
     *            pojo???????????????
     * @param clazz
     *            pojo???Class
     * @param ignoreNull
     *            ?????????????????????
     */
    @SuppressWarnings("unchecked")
    private void buildDocument(Element parent, Object pojo, Class<?> clazz, boolean ignoreNull) {
        // ???????????????key???????????????value???XmlData
        Map<String, XmlData> map = new HashMap<>();
        // ??????????????????
        if (pojo instanceof Map) {
            Map<?, ?> pojoMap = (Map<?, ?>)pojo;
            pojoMap.forEach((k, v) -> {
                if (k == null) {
                    LOGGER.debug("??????map???key???null??????");
                } else {
                    if (ignoreNull && v == null) {
                        LOGGER.debug("??????????????????????????????[{}]?????????????????????", k);
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
                // ??????????????????????????????????????????????????????????????????
                if (xmlNode == null && !editor.hasReadMethod()) {
                    LOGGER.debug("??????[{}]??????XmlNode????????????????????????????????????????????????", editor.name());
                    continue;
                }

                if (AccessorUtil.isTransient(editor.original())) {
                    LOGGER.debug("??????[{}]???transient????????????????????????????????????", editor.name());
                    continue;
                }

                // ?????????
                try {
                    Object valueObj = pojo == null ? null : BeanUtils.getProperty(pojo, editor.name());
                    // ??????????????????
                    if ((ignoreNull && valueObj == null) || (xmlNode != null && xmlNode.ignore())) {
                        LOGGER.debug("??????????????????????????????????????????");
                        continue;
                    }

                    // ?????????
                    String nodeName =
                        (xmlNode == null || StringUtils.isBlank(xmlNode.name())) ? editor.name() : xmlNode.name();
                    map.put(nodeName, new XmlData(xmlNode, valueObj, editor.type()));
                } catch (Exception e) {
                    LOGGER.error(e, "?????????????????????????????????????????????");
                }
            }
        }

        map.forEach((k, v) -> {
            XmlNode xmlNode = v.getXmlNode();
            Object valueObj = v.getData();

            // ?????????
            // ?????????
            String attrName =
                (xmlNode == null || StringUtils.isBlank(xmlNode.attributeName())) ? k : xmlNode.attributeName();
            // ?????????cdata
            boolean isCDATA = xmlNode != null && xmlNode.isCDATA();
            // ????????????
            Class<?> type = v.getType();
            // ???????????????????????????
            Element node = parent.element(k);
            if (node == null) {
                // ??????????????????????????????????????????????????????????????????????????????list????????????????????????????????????
                node = DocumentHelper.createElement(k);
                parent.add(node);
            }

            // ????????????????????????????????????
            if (xmlNode != null && xmlNode.isAttribute()) {
                // ?????????????????????????????????
                if (StringUtils.isBlank(xmlNode.name())) {
                    // ?????????????????????????????????????????????
                    parent.remove(node);
                    node = parent;
                }
                Element finalNode = node;

                if (Map.class.isAssignableFrom(type)) {
                    // ???????????????valueObj????????????null????????????????????????null???????????????????????????????????????null??????
                    if (valueObj != null) {
                        Map<String, String> attrs = (Map<String, String>)valueObj;
                        attrs.forEach(finalNode::addAttribute);
                    }
                } else {
                    // ???????????????????????????????????????
                    String attrValue = valueObj == null ? "" : String.valueOf(valueObj);

                    // ????????????????????????????????????
                    finalNode.addAttribute(attrName, attrValue);
                }
            } else if (type == null) {
                LOGGER.debug("?????????????????????[{}]???????????????????????????", k);
            } else if (JavaTypeUtil.isNotPojo(type)) {
                // ?????????????????????????????????
                if (Map.class.isAssignableFrom(type)) {
                    LOGGER.warn("????????????[{}]???map??????", k);
                    buildDocument(node, v.getData(), type, ignoreNull);
                } else if (Collection.class.isAssignableFrom(type)) {
                    parent.remove(node);
                    // ????????????
                    // ????????????????????????null
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
                        LOGGER.debug("??????[{}]??????CDATA????????????", text);
                        node.add(DocumentHelper.createCDATA(text));
                    } else {
                        node.setText(text);
                    }
                }
            } else {
                // ????????????????????????????????????????????????????????????????????????xmlnode?????????????????????
                Class<?> realType = resolveRealType(type, xmlNode);
                // pojo??????
                buildDocument(node, valueObj, realType, ignoreNull);
            }
        });
    }

    /**
     * ???????????????????????????
     *
     * @param fieldType
     *            ????????????
     * @param xmlNode
     *            ??????XmlNode??????
     * @return ????????????????????????????????????????????????
     */
    private Class<?> resolveRealType(Class<?> fieldType, XmlNode xmlNode) {
        // ????????????????????????????????????????????????????????????????????????xmlnode?????????????????????
        Class<?> type = (xmlNode == null) ? fieldType : xmlNode.general();

        if (!fieldType.isAssignableFrom(type)) {
            type = fieldType;
        }
        return type;
    }

    /**
     * ???pojo????????????????????????
     *
     * @param element
     *            ????????????????????????
     * @param attrName
     *            ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @param pojo
     *            pojo
     * @param editor
     *            ???????????????
     */
    private void setValue(Element element, String attrName, Object pojo, PropertyEditor editor) {
        XmlNode attrXmlNode = editor.getAnnotation(XmlNode.class);
        LOGGER.debug("????????????fieldName???{}", editor.name());
        final XmlTypeConvert<?> convert = XmlTypeConverterUtil.resolve(attrXmlNode, editor);
        if (!BeanUtils.setProperty(pojo, editor.name(), convert.read(element, attrName))) {
            LOGGER.debug("copy?????????{}????????????????????????[{}]??????????????????", editor.name(), editor.name());
        }
    }

    /**
     * ???pojo????????????????????????????????????Collection?????????
     *
     * @param elements
     *            ????????????????????????
     * @param attrName
     *            ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
     * @param pojo
     *            pojo
     * @param editor
     *            ???????????????
     * @param <T>
     *            ????????????
     */
    @SuppressWarnings("unchecked")
    private <T> void setValue(List<Element> elements, String attrName, Object pojo, PropertyEditor editor) {
        XmlNode attrXmlNode = editor.getAnnotation(XmlNode.class);
        LOGGER.debug("????????????fieldName???{}", editor.name());
        final XmlTypeConvert<?> convert = XmlTypeConverterUtil.resolve(attrXmlNode, editor);

        // ??????????????????????????????
        Class<? extends Collection<T>> collectionClass;
        // ??????????????????
        Class<? extends Collection<T>> real = (Class<? extends Collection<T>>)editor.type();

        if (attrXmlNode != null) {
            collectionClass = (Class<? extends Collection<T>>)attrXmlNode.arrayType();

            if (Collection.class.equals(collectionClass)) {
                // ?????????????????????????????????????????????
                collectionClass = real;
            } else if (!real.isAssignableFrom(collectionClass)) {
                // ?????????
                throw new SerializeException(ErrorCodeEnum.CODE_ERROR, StringUtils
                    .format("??????[{}]????????????,???????????????????????????[{}]?????????????????????????????????[{}]?????????", editor.original(), collectionClass, real));
            }
        } else {
            collectionClass = real;
        }

        // ?????????????????????
        List<Element> elementList = elements;
        if (attrXmlNode != null && StringUtils.isNotBlank(attrXmlNode.arrayRoot()) && !elements.isEmpty()) {
            elementList = elements.get(0).elements(attrXmlNode.arrayRoot());
        }

        // ????????????????????????????????????
        List<T> list = elementList.stream().map(d -> (T)convert.read(d, attrName)).collect(Collectors.toList());

        if (!trySetValue(list, pojo, editor, collectionClass)) {
            LOGGER.warn("???????????????[{}]??????", editor.name());
        }
    }

    /**
     * ?????????list?????????????????????
     *
     * @param datas
     *            ??????????????????
     * @param pojo
     *            ????????????pojo
     * @param editor
     *            ???????????????????????????
     * @param clazz
     *            ?????????Class??????
     * @return ??????true???????????????????????????false??????????????????
     */
    private <T> boolean trySetValue(List<T> datas, Object pojo, PropertyEditor editor,
        Class<? extends Collection<T>> clazz) {

        Collection<T> collection = tryBuildCollection(clazz);

        if (collection == null) {
            LOGGER.warn("?????????class[{}]????????????", clazz);
            return false;
        }

        collection.addAll(datas);
        try {
            return BeanUtils.setProperty(pojo, editor.name(), collection);
        } catch (Exception e) {
            LOGGER.debug(e, "??????[{}]????????????????????????????????????[{}]", editor.name(), clazz);
            return false;
        }
    }

    /**
     * ??????class?????????????????????????????????????????????null?????????????????????
     *
     * @param clazz
     *            ?????????Class??????
     * @return ????????????
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
            LOGGER.warn("?????????????????????[{}]", clazz);
            return null;
        }
    }

    @Override
    public <T> T read(byte[] data, Charset charset, Class<T> type) throws SerializeException {
        return parse(new String(data, charset == null ? StandardCharsets.UTF_8 : charset), type);
    }

    @Override
    public <T> T read(byte[] data, Charset charset, AbstractTypeReference<T> typeReference) throws SerializeException {
        throw new CommonException(ErrorCodeEnum.CODE_ERROR, "??????????????????");
    }

    @Override
    public byte[] write(Object data, Charset charset) {
        return toXml(data).getBytes(charset == null ? StandardCharsets.UTF_8 : charset);
    }

    /**
     * XML????????????
     */
    @Data
    @AllArgsConstructor
    private static class XmlData {

        /**
         * ???????????????????????????
         */
        private XmlNode xmlNode;

        /**
         * ????????????
         */
        private Object data;

        /**
         * ??????????????????????????????????????????
         */
        private Class<?> type;
    }

}
