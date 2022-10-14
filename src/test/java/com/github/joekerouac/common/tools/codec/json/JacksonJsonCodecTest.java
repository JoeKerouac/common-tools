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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.reflect.type.AbstractTypeReference;
import com.github.joekerouac.common.tools.resource.Resource;
import com.github.joekerouac.common.tools.resource.impl.ClassPathResource;
import com.github.joekerouac.common.tools.resource.impl.FileResource;
import com.github.joekerouac.common.tools.resource.impl.URLResource;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class JacksonJsonCodecTest {

    @Test(dataProvider = "testResourceDataProvider")
    public void testResource(JacksonJsonCodec jacksonJsonSerialization, ResourceHolder holder) {
        // 测试泛型的解析
        byte[] data = jacksonJsonSerialization.write(holder);
        Object objRead = jacksonJsonSerialization.read(data, holder.getClass());
        Assert.assertEquals(holder, objRead);
        objRead = jacksonJsonSerialization.read(data, new AbstractTypeReference<ResourceHolder>() {});
        Assert.assertEquals(holder, objRead);
    }

    @DataProvider(name = "testResourceDataProvider")
    public Object[][] testResourceDataProvider() {
        JacksonJsonCodec jacksonJsonSerialization = new JacksonJsonCodec();

        return new Object[][] {{jacksonJsonSerialization, new ResourceHolder(new URLResource("test.jar", "test.jar"),
            new FileResource("/root/test/test.jar"), new ClassPathResource("Formatter.xml"))}};
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
