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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import com.github.joekerouac.common.tools.io.IOUtils;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * @author JoeKerouac
 * @date 2023-06-14 15:55
 * @since 2.0.3
 */
public class XmlStreamHandlerWrapperTest {

    @Test
    public void test() {
        String data = "<ROOT><A>1</A><B>你好</B><C>2</C></ROOT>";

        Map<String, InputStream> map = new HashMap<>();

        XmlStreamHandler handler = new XmlStreamHandler() {

            private volatile String name;

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) {
                this.name = qName;
            }

            @Override
            public void endElement(String uri, String localName, String qName) {
                this.name = null;
            }

            @Override
            public void onData(InputStream inputStream) throws IOException {
                if (StringUtils.isBlank(name)) {
                    // 直接关闭流
                    inputStream.close();
                    inputStream = null;
                } else {
                    map.put(name, inputStream);
                }
            }
        };

        Charset charset = StandardCharsets.UTF_8;
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = spf.newSAXParser();
            InputSource inputSource = new InputSource(new ByteArrayInputStream(data.getBytes(charset)));
            inputSource.setEncoding(charset.name());
            saxParser.parse(inputSource, new XmlStreamHandlerWrapper(handler, 16, 16, charset));
        } catch (Throwable throwable) {
            throw new RuntimeException("xml解析失败", throwable);
        }

        Assert.assertEquals("1", new String(IOUtils.read(map.get("A"), true), charset));
        Assert.assertEquals("你好", new String(IOUtils.read(map.get("B"), true), charset));
        Assert.assertEquals("2", new String(IOUtils.read(map.get("C"), true), charset));
    }

}
