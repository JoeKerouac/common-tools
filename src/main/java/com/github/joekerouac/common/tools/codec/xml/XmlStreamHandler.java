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

import java.io.InputStream;

import org.xml.sax.Attributes;

/**
 * @author JoeKerouac
 * @date 2023-06-08 09:13
 * @since 2.0.3
 */
public interface XmlStreamHandler {

    /**
     * Receive notification of the start of an element.
     *
     * <p>
     * By default, do nothing. Application writers may override this method in a subclass to take specific actions at
     * the start of each element (such as allocating a new tree node or writing output to a file).
     * </p>
     *
     * @param uri
     *            The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing
     *            is not being performed.
     * @param localName
     *            The local name (without prefix), or the empty string if Namespace processing is not being performed.
     * @param qName
     *            The qualified name (with prefix), or the empty string if qualified names are not available.
     * @param attributes
     *            The attributes attached to the element. If there are no attributes, it shall be an empty Attributes
     *            object.
     */
    void startElement(String uri, String localName, String qName, Attributes attributes);

    /**
     * Receive notification of the end of an element.
     *
     * <p>
     * By default, do nothing. Application writers may override this method in a subclass to take specific actions at
     * the end of each element (such as finalising a tree node or writing output to a file).
     * </p>
     *
     * @param uri
     *            The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing
     *            is not being performed.
     * @param localName
     *            The local name (without prefix), or the empty string if Namespace processing is not being performed.
     * @param qName
     *            The qualified name (with prefix), or the empty string if qualified names are not available.
     */
    void endElement(String uri, String localName, String qName);

    /**
     * 接收数据回调，注意，不仅可能在{@link #startElement(String, String, String, Attributes)}后
     * {@link #endElement(String, String, String)}前调用，还可能在{@link #startElement(String, String, String, Attributes)}前
     * 调用或者在{@link #endElement(String, String, String)}后调用；
     *
     * 注意：该方法必须在一个新线程中执行！！！
     * 
     * @param stream
     *            数据输入流，无需关闭
     */
    void onData(InputStream stream);

}
