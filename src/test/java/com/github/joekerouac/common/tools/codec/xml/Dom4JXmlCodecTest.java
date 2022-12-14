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

import java.util.*;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.string.StringUtils;

import lombok.*;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class Dom4JXmlCodecTest {

    private static final Dom4JXmlCodec PARSER = new Dom4JXmlCodec();
    private static final String NOTHASNULL = "<USER><users2><user><ALIAS>u1</ALIAS><age>0</age><NAME>u1</NAME></user"
        + "></users2><users1><ALIAS>u1</ALIAS><age>0</age><NAME>u1</NAME></users1><ALIAS>qiao</ALIAS><userSet"
        + "><ALIAS>u2</ALIAS><age>0</age><NAME>u2</NAME></userSet><age>18</age><NAME>joe</NAME></USER>";
    private static final String HASNULL = "<USER><users2><user><ALIAS>u1</ALIAS><age>0</age><NAME>u1</NAME></user"
        + "></users2><users1><ALIAS>u1</ALIAS><age>0</age><NAME>u1</NAME></users1><ALIAS>qiao</ALIAS><userSet"
        + "><ALIAS>u2</ALIAS><age>0</age><NAME>u2</NAME></userSet><age>18</age><NAME>joe</NAME></USER>";
    private static final String MAP_XML = "<root><test>test</test><user><users2><user><ALIAS>u1</ALIAS><age>0</age"
        + "><NAME>u1</NAME></user></users2><users1><ALIAS>u1</ALIAS><age>0</age><NAME>u1</NAME></users1><ALIAS>qiao"
        + "</ALIAS><userSet><ALIAS>u2</ALIAS><age>0</age><NAME>u2</NAME></userSet><age>18</age><NAME>joe</NAME"
        + "></user></root>";

    private static final String CONTAIN_HEADER =
        "<?xml version='1.0' encoding='UTF-8'?><USER><users2><user><ALIAS>u1</ALIAS><age>0</age><NAME>u1</NAME></user>"
            + "</users2><users1><ALIAS>u1</ALIAS><age>0</age><NAME>u1</NAME></users1><ALIAS>qiao</ALIAS><userSet><ALIAS>u2</ALIAS>"
            + "<age>0</age><NAME>u2</NAME></userSet><age>18</age><NAME>joe</NAME></USER>";

    @Test
    public void doSimpleTest() {
        PARSER.enableDTD(true);
        PARSER.enableDTD(false);
    }

    @Test
    public void doMapToXml() {
        User user = build();
        Map<String, Object> map = new HashMap<>();
        map.put("test", "test");
        map.put("user", user);
        String xml = PARSER.toXml(map);
        Assert.assertEquals(xml, MAP_XML);
    }

    @Test
    public void doToXml() {
        User user = build();

        String xml = PARSER.toXml(user, "USER", true);
        Assert.assertEquals(xml, HASNULL);
        xml = PARSER.toXml(user, "USER", false);
        Assert.assertEquals(xml, NOTHASNULL);

        Assert.assertTrue(StringUtils.isNotBlank(PARSER.toXml(user, null, true, true)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void doParse() {
        User user = build();
        User u1 = PARSER.parse(NOTHASNULL, User.class);
        Assert.assertEquals(user, u1);
        User u2 = PARSER.parse(HASNULL, User.class);
        Assert.assertEquals(user, u2);

        // ????????????????????????????????????
        Map<String, Object> map1 = PARSER.parseToMap(CONTAIN_HEADER, Map.class);
        Assert.assertNotNull(map1);
        map1 = PARSER.parseToMap(CONTAIN_HEADER, HashMap.class);
        Assert.assertNotNull(map1);

        Map<String, Object> map2 = PARSER.parse(CONTAIN_HEADER, Map.class);
        Assert.assertEquals(map1, map2);
    }

    private User build() {
        User user = new User();
        user.setName("joe");
        user.setOtherName("qiao");
        user.setAge(18);
        List<User> list = new ArrayList<>();
        User u1 = new User();
        u1.setName("u1");
        u1.setOtherName("u1");
        list.add(u1);
        Set<User> set = new HashSet<>();
        User u2 = new User();
        u2.setName("u2");
        u2.setOtherName("u2");
        set.add(u2);

        user.setUsers1(list);
        user.setUsers2(list);
        user.setUserSet(set);
        return user;
    }

    @Test
    public void testAttr() {
        Map<String, String> map = new HashMap<>();
        map.put("port", "8080");
        XmlObj xmlObj = new XmlObj();
        xmlObj.setMap(map);

        Assert.assertEquals(PARSER.read(PARSER.toXml(new XmlObj()).getBytes(), XmlObj.class), xmlObj);

        xmlObj.setList(Arrays.asList("list1", "list2"));

        String xml = "<root><name>123</name><list>list1</list><list>list2</list></root>";

        Map<String, Object> read = PARSER.parseToMap(xml, Map.class);

        Map<String, Object> expect = new HashMap<>();
        expect.put("name", "123");
        expect.put("list", Arrays.asList("list1", "list2"));

        Assert.assertEquals(expect, read);
    }

    @Test
    public void testException() {
        Assert.assertNull(PARSER.parseToMap("xml", Map.class));
    }

    @Getter
    @Setter
    @ToString
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @NoArgsConstructor
    static class XmlObj {

        @EqualsAndHashCode.Include
        @XmlNode
        private String name = "123";

        @EqualsAndHashCode.Include
        private List<String> list;

        @EqualsAndHashCode.Include
        @XmlNode(isAttribute = true)
        private Integer port = 8080;

        @EqualsAndHashCode.Include
        @XmlNode(isAttribute = true)
        private Map<String, String> map;
    }

    @Data
    static class User {
        @XmlNode(name = "NAME")
        private String name;
        @XmlNode(name = "ALIAS")
        private String otherName;
        @XmlNode(ignore = true)
        private String other = "abc";
        private int age;

        /**
         * ?????????????????????general??????
         */
        @XmlNode(general = User.class)
        private List<User> users1;

        /**
         * ?????????arrayRoot?????????????????????????????????users2??????????????????users2???????????????????????????user???????????????User??????
         */
        @XmlNode(general = User.class, arrayRoot = "user")
        private List<User> users2;

        @XmlNode(general = User.class)
        private Set<User> userSet;
    }

}
