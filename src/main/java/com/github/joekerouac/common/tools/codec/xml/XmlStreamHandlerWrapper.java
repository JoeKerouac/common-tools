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

import java.io.IOException;
import java.nio.charset.Charset;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.joekerouac.common.tools.io.InMemoryFile;

/**
 * 将数据转为stream输出
 * 
 * @author JoeKerouac
 * @date 2023-06-08 09:17
 * @since 2.0.3
 */
public class XmlStreamHandlerWrapper extends DefaultHandler {

    private final XmlStreamHandler handler;

    private final int bufferSize;

    private final int limit;

    private final Charset charset;

    private InMemoryFile memoryFile;

    public XmlStreamHandlerWrapper(XmlStreamHandler handler, int bufferSize, int limit, Charset charset) {
        this.handler = handler;
        this.bufferSize = bufferSize;
        this.limit = limit;
        this.charset = charset;
    }

    @Override
    public void endDocument() throws SAXException {
        closeStream();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        closeStream();

        handler.startElement(uri, localName, qName, attributes);
        newStream();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        closeStream();

        handler.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        byte[] bytes = new String(ch, start, length).getBytes(charset);
        if (memoryFile == null) {
            newStream();
        }

        try {
            memoryFile.write(bytes);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    @Override
    public void fatalError(SAXParseException e) throws SAXException {
        closeStream();
    }

    private void closeStream() throws SAXException {
        if (memoryFile == null) {
            return;
        }

        try {
            memoryFile.writeFinish();

            handler.onData(memoryFile.getDataAsInputStream());
        } catch (IOException e) {
            throw new SAXException(e);
        }

        memoryFile = null;
    }

    private void newStream() {
        memoryFile = new InMemoryFile(bufferSize, limit);
    }

}
