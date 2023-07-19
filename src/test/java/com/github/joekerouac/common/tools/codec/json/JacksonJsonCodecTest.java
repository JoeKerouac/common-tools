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
package com.github.joekerouac.common.tools.codec.json;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.codec.json.annotations.LocalDateTimeFormat;
import com.github.joekerouac.common.tools.date.DateUtil;
import com.github.joekerouac.common.tools.io.InMemoryFile;
import com.github.joekerouac.common.tools.io.InMemoryFileOutputStream;
import com.github.joekerouac.common.tools.reflect.type.AbstractTypeReference;
import com.github.joekerouac.common.tools.resource.Resource;
import com.github.joekerouac.common.tools.resource.impl.ClassPathResource;
import com.github.joekerouac.common.tools.resource.impl.FileResource;
import com.github.joekerouac.common.tools.resource.impl.URLResource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class JacksonJsonCodecTest {

    @Test
    public void testInMemoryFile() throws IOException {
        // 构建一个10M的文件
        try (InMemoryFile testFile = new InMemoryFile(1024, 1024)) {
            byte[] data = new byte[1024 * 1024];
            for (int i = 0; i < 10; i++) {
                new Random().nextBytes(data);
                testFile.write(data);
            }
            testFile.writeFinish();

            JacksonJsonCodec codec = new JacksonJsonCodec();

            try (InMemoryFile writeFile = new InMemoryFile(256, 256)) {
                codec.write(testFile, Charset.defaultCharset(), new InMemoryFileOutputStream(writeFile));

                writeFile.writeFinish();

                try (InMemoryFile read =
                    codec.read(writeFile.getDataAsInputStream(), Charset.defaultCharset(), InMemoryFile.class)) {

                    // 开始比较最初的文件与经过序列化、反序列化后的文件是否相同
                    InputStream source = testFile.getDataAsInputStream();
                    InputStream dst = read.getDataAsInputStream();
                    byte[] srcBuffer = new byte[1024];
                    byte[] dstBuffer = new byte[1024];
                    int srcLen;
                    while ((srcLen = source.read(srcBuffer)) > 0) {
                        int dstLen = 0;
                        int l;
                        while (srcLen - dstLen > 0 && (l = dst.read(dstBuffer, dstLen, srcLen - dstLen)) > 0) {
                            dstLen += l;
                        }

                        Assert.assertEquals(dstLen, srcLen);
                        Assert.assertEquals(dstBuffer, srcBuffer);
                        Arrays.fill(srcBuffer, (byte)0);
                        Arrays.fill(dstBuffer, (byte)0);
                    }
                }
            }
        }

    }

    @Test(dataProvider = "testResourceDataProvider")
    public void testResource(JacksonJsonCodec jacksonJsonSerialization, ResourceHolder holder) {
        // 测试泛型的解析
        byte[] data = jacksonJsonSerialization.write(holder);
        Object objRead = jacksonJsonSerialization.read(data, holder.getClass());
        Assert.assertEquals(holder, objRead);
        objRead = jacksonJsonSerialization.read(data, new AbstractTypeReference<ResourceHolder>() {});
        Assert.assertEquals(holder, objRead);
    }

    @Test(dataProvider = "testLocalDateTimeProvider")
    public void testLocalDateTime(DateObj dateObj, String expect) {
        JacksonJsonCodec jacksonJsonSerialization = new JacksonJsonCodec();
        byte[] write = jacksonJsonSerialization.write(dateObj, StandardCharsets.UTF_8);
        Map<String, String> map = jacksonJsonSerialization.read(write, StandardCharsets.UTF_8,
            new AbstractTypeReference<Map<String, String>>() {});
        String date = map.get("date");
        Assert.assertEquals(date, expect);
    }

    @DataProvider(name = "testResourceDataProvider")
    public Object[][] testResourceDataProvider() {
        JacksonJsonCodec jacksonJsonSerialization = new JacksonJsonCodec();

        return new Object[][] {{jacksonJsonSerialization, new ResourceHolder(new URLResource("test.jar", "test.jar"),
            new FileResource("/root/test/test.jar"), new ClassPathResource("sql-error-codes.xml"))}};
    }

    @DataProvider(name = "testLocalDateTimeProvider")
    public Object[][] testLocalDateTimeProvider() {
        LocalDateTime now = LocalDateTime.now();
        return new Object[][] {{new DateObj1(now), DateUtil.getFormatDate(now, DateUtil.BASE)},
            {new DateObj2(now), DateUtil.getFormatDate(now, "yyyy-MM-dd HH:mm")},
            {new DateObj3(now), DateUtil.getFormatDate(now, "yyyy-MM-dd HH")}};
    }

    public interface DateObj {

        LocalDateTime getDate();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DateObj1 implements DateObj {

        private LocalDateTime date;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DateObj2 implements DateObj {

        @LocalDateTimeFormat("yyyy-MM-dd HH:mm")
        private LocalDateTime date;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DateObj3 implements DateObj {

        @LocalDateTimeFormat(serializer = "yyyy-MM-dd HH")
        private LocalDateTime date;
    }

    @Data
    @NoArgsConstructor
    public static class ResourceHolder {

        private Resource abstractURLResource;

        private URLResource urlResource;

        private Resource abstractFileResource;

        private FileResource fileResource;

        private Resource abstractClassPathResource;

        private ClassPathResource classPathResource;

        public ResourceHolder(URLResource urlResource, FileResource fileResource, ClassPathResource classPathResource) {
            this.abstractURLResource = urlResource;
            this.urlResource = urlResource;

            this.abstractFileResource = fileResource;
            this.fileResource = fileResource;
            this.classPathResource = classPathResource;
        }
    }

}
