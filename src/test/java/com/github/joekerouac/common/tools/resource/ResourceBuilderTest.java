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
package com.github.joekerouac.common.tools.resource;

import java.util.HashMap;
import java.util.Map;

import com.github.joekerouac.common.tools.enums.ResourceType;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.resource.impl.ClassPathResource;
import com.github.joekerouac.common.tools.resource.impl.FileResource;
import com.github.joekerouac.common.tools.resource.impl.MavenJarResource;
import com.github.joekerouac.common.tools.resource.impl.URLResource;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class ResourceBuilderTest {

    @Test(dataProvider = "testDataProvider")
    public void test(Map<String, String> param) {
        Resource resource = ResourceBuilder.build(param);
        Assert.assertNotNull(resource);
        Map<String, String> map = ResourceBuilder.resourceToMap(resource);
        Assert.assertEquals(map, param);
    }

    @DataProvider(name = "testDataProvider")
    public Object[][] testDataProvider() {
        Map<String, String> urlResourceMap = new HashMap<>();
        urlResourceMap.put("type", ResourceType.URL_RESOURCE.code());
        urlResourceMap.put(URLResource.NAME_FIELD_NAME, "test.jar");
        urlResourceMap.put(URLResource.URL_FIELD_NAME, "test.jar");

        Map<String, String> fileResourceMap = new HashMap<>();
        fileResourceMap.put("type", ResourceType.FILE_RESOURCE.code());
        fileResourceMap.put(FileResource.ABSOLUTE_PATH_FIELD_NAME, "/root/test/test.jar");

        Map<String, String> classPathResourceMap = new HashMap<>();
        classPathResourceMap.put("type", ResourceType.CLASS_PATH.code());
        classPathResourceMap.put(ClassPathResource.PATH_FIELD_NAME, "sql-error-codes.xml");

        Map<String, String> mavenJarResource = new HashMap<>();
        mavenJarResource.put("type", ResourceType.MAVEN_JAR_RESOURCE.code());
        mavenJarResource.put(MavenJarResource.MAVEN_REPO_FIELD_NAME, "repo");
        mavenJarResource.put(MavenJarResource.GROUP_ID_FIELD_NAME, "group");
        mavenJarResource.put(MavenJarResource.ARTIFACT_ID_FIELD_NAME, "artifact");
        mavenJarResource.put(MavenJarResource.VERSION_FIELD_NAME, "version");
        mavenJarResource.put(MavenJarResource.USERNAME, "username");
        mavenJarResource.put(MavenJarResource.PASSWORD, "password");

        return new Object[][] {{urlResourceMap}, {fileResourceMap}, {classPathResourceMap}, {mavenJarResource}};
    }

}
