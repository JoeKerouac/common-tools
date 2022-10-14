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
package com.github.joekerouac.common.tools.reflect.bean;

import java.util.*;

import org.testng.Assert;
import org.testng.annotations.Test;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * BeanUtils测试用例
 * 
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class BeanUtilsTest {

    @Test
    public void testConvertToPlaceholder() {
        User user = new User();
        user.nameAlias = "parent";
        user.age = 30;
        user.sex = Sex.MAN;
        user.strs = Arrays.asList("01", "02", "03");

        Map<String, Object> map = new HashMap<>();
        map.put("k1", "v1");
        map.put("k2", "v2");
        user.map = map;

        User child = new User();
        child.nameAlias = "child";
        child.age = 5;
        child.sex = Sex.WOMAN;
        user.child = child;

        Map<String, Object> expectPlaceholder = new HashMap<>();
        expectPlaceholder.put("user.name", "parent");
        expectPlaceholder.put("user.age", 30);
        expectPlaceholder.put("user.sex", Sex.MAN);
        expectPlaceholder.put("user.strs.[0]", "01");
        expectPlaceholder.put("user.strs.[1]", "02");
        expectPlaceholder.put("user.strs.[2]", "03");
        expectPlaceholder.put("user.map.k1", "v1");
        expectPlaceholder.put("user.map.k2", "v2");
        expectPlaceholder.put("user.child.name", "child");
        expectPlaceholder.put("user.child.age", 5);
        expectPlaceholder.put("user.child.sex", Sex.WOMAN);

        Map<String, Object> placeholder = BeanUtils.convertToPlaceholder(user, "user", null, false);
        Assert.assertEquals(placeholder, expectPlaceholder);

        expectPlaceholder.put("user.child.strs", null);
        expectPlaceholder.put("user.child.map", null);
        expectPlaceholder.put("user.child.child", null);

        placeholder = BeanUtils.convertToPlaceholder(user, "user", null, false);
        Assert.assertNotEquals(placeholder, expectPlaceholder);

        placeholder = BeanUtils.convertToPlaceholder(user, "user");
        Assert.assertEquals(placeholder, expectPlaceholder);
    }

    @Test
    public void test() {
        BeanA beanA = new BeanA("bean", 10);

        Map<String, Object> map = BeanUtils.convert(beanA);
        Assert.assertEquals("bean", map.get("name"));
        Assert.assertEquals(10, map.get("age"));

        BeanB beanB1 = BeanUtils.copyFromObjToClass(BeanB.class, beanA);
        Assert.assertEquals("bean", beanB1.getName());
        Assert.assertEquals(10, beanB1.getAge());

        BeanB beanB2 = new BeanB();
        beanB2 = BeanUtils.copyFromMultiObjToObj(beanB2, new Name("bean"), new Age(10));
        Assert.assertEquals("bean", beanB2.getName());
        Assert.assertEquals(10, beanB2.getAge());

        BeanUtils.setProperty(beanB2, "name", "changed");
        Assert.assertEquals("changed", beanB2.getName());
        Assert.assertEquals("changed", BeanUtils.getProperty(beanB2, "name"));

        Assert.assertEquals(1, BeanUtils.copyFromMultiObjToClass(BeanB.class, Collections.singletonList(beanA)).size());

        Assert.assertNotNull(BeanUtils.getPropertyDescriptors(BeanA.class));
        Assert.assertTrue(BeanUtils.getPropertyDescriptors(BeanA.class).length > 0);
    }

    @Data
    public static class User {

        @Alias("name")
        private String nameAlias;

        private int age;

        private Sex sex;

        private List<String> strs;

        private Map<String, Object> map;

        private User child;
    }

    public enum Sex {
        MAN, WOMAN
    }

    @Data
    @AllArgsConstructor
    public static class BeanA {

        @Alias("name")
        private String alias;

        private int age;

    }

    @Data
    @AllArgsConstructor
    public static class BeanB {

        private String name;

        private int age;

        public BeanB() {

        }
    }

    @Data
    @AllArgsConstructor
    public static class Name {
        private String name;
    }

    @Data
    @AllArgsConstructor
    public static class Age {
        private int age;
    }

}
