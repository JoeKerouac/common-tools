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
package com.github.joekerouac.common.tools.reflect;

import java.lang.reflect.Method;

import javax.validation.constraints.NotNull;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * 反射工具测试
 * 
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class ReflectUtilTest {

    @Test
    public void test() {
        Assert.assertNotNull(ReflectUtil.getConstructor(Pojo.class));
        Assert.assertNotNull(ReflectUtil.getConstructor(Pojo.class, String.class));

        Assert.assertEquals("default", ReflectUtil.invoke(new Pojo(), "getAlias"));
        Assert.assertEquals("default", ReflectUtil.invoke(new Pojo(), "getAlias"));
        Assert.assertEquals("default-test",
            ReflectUtil.invoke(new Pojo(), "appendName", new Class[] {String.class}, "-test"));
        Assert.assertFalse(ReflectUtil.getAllMethod(Pojo.class).isEmpty());
        Assert.assertFalse(ReflectUtil.getAllMethod(TestM.class).isEmpty());
        Assert.assertNotNull(ReflectUtil.getMethod(Pojo.class, "getAlias"));
        Assert.assertNotNull(ReflectUtil.getMethod(Pojo.class, "appendName", String.class));
        Method method = ReflectUtil.getMethod(Pojo.class, "appendName", String.class);
        Assert.assertEquals("default-test", ReflectUtil.execMethod(method, new Pojo(), "-test"));
        Assert.assertEquals(1, ReflectUtil.getAllAnnotationPresentMethod(Pojo.class, NotNull.class).size());

        Throwable ex = null;
        try {
            ReflectUtil.getField(Pojo.class, "name", false);
        } catch (Throwable e) {
            ex = e;
        }
        Assert.assertNotNull(ex);

        Assert.assertEquals("default", ReflectUtil.getFieldValue(new Pojo(), "name"));
        Assert.assertEquals("default", ReflectUtil.getFieldValue(new Pojo(), ReflectUtil.getField(Pojo.class, "name")));
        Pojo pojo = new Pojo();
        ReflectUtil.setFieldValue(pojo, ReflectUtil.getField(Pojo.class, "name"), "1");
        Assert.assertEquals("1", pojo.getName());
        ReflectUtil.setFieldValue(pojo, "name", "2");
        Assert.assertEquals("2", pojo.getName());
        Assert.assertTrue(ReflectUtil.getAllFields(Pojo.class).length > 0);
    }

    public interface PojoInterface {}

    public static class AbstractPojo implements PojoInterface {

        protected String name;

        AbstractPojo(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public interface TestM {
        void test();
    }

    public static class Pojo extends AbstractPojo {

        public Pojo() {
            super("default");
        }

        private Pojo(String name) {
            super(name);
        }

        @NotNull
        private String getAlias() {
            return name;
        }

        private String appendName(String append) {
            return name + append;
        }

    }
}
