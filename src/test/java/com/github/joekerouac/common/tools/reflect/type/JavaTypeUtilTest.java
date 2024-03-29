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
package com.github.joekerouac.common.tools.reflect.type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class JavaTypeUtilTest {

    // 这个字段是为了后边验证功能用，不能删除；
    private Map<String, List<String>> map;

    @Test
    public void testCreateJavaType() throws Exception {
        {
            // 为了方便，将从TypeReference构建和从Type构建合并，使用同一个类型构建
            List<JavaType> list = new ArrayList<>();
            // 测试从TypeReference构建JavaType
            AbstractTypeReference<Map<String, List<String>>> reference =
                new AbstractTypeReference<Map<String, List<String>>>() {};
            list.add(JavaTypeUtil.createJavaType(reference));

            // 测试从Type构建（有多种场景，先搭车验证一种）
            Field field = this.getClass().getDeclaredField("map");
            list.add(JavaTypeUtil.createJavaType(field.getGenericType()));

            // 验证两种方式构建出来的结果一致
            Assert.assertEquals(list.get(0), list.get(1));

            for (JavaType javaType : list) {
                Assert.assertTrue(javaType instanceof SimpleType);
                SimpleType simpleType = (SimpleType)javaType;
                Assert.assertEquals(simpleType.getRawClass(), Map.class);
                Assert.assertEquals(CollectionUtil.size(simpleType.getBindings()), 2);

                Assert.assertTrue(new ArrayList<>(simpleType.getBindings().values()).get(0) instanceof SimpleType);
                SimpleType stringBaseType = (SimpleType)new ArrayList<>(simpleType.getBindings().values()).get(0);
                Assert.assertEquals(stringBaseType.getRawClass(), String.class);
                Assert.assertEquals(CollectionUtil.size(stringBaseType.getBindings()), 0);

                Assert.assertTrue(new ArrayList<>(simpleType.getBindings().values()).get(1) instanceof SimpleType);
                SimpleType listBaseType = (SimpleType)new ArrayList<>(simpleType.getBindings().values()).get(1);
                Assert.assertEquals(listBaseType.getRawClass(), List.class);
                Assert.assertEquals(CollectionUtil.size(listBaseType.getBindings()), 1);
                Assert.assertTrue(new ArrayList<>(listBaseType.getBindings().values()).get(0) instanceof SimpleType);
                Assert.assertEquals(new ArrayList<>(listBaseType.getBindings().values()).get(0).getRawClass(),
                    String.class);
            }
        }

        {
            // 现在BaseJavaType验证通过，依靠该场景进行下一步验证
            // 先验证匿名泛型场景
            JavaType javaType = JavaTypeUtil.createJavaType(new AbstractTypeReference<List<? extends String>>() {});
            GenericType genericType = (GenericType)new ArrayList<>(javaType.getBindings().values()).get(0);
            Assert.assertEquals(genericType.getName(), "?");
            Assert.assertTrue(genericType.getParent() instanceof SimpleType);
            Assert.assertEquals(genericType.getParent().getRawClass(), String.class);

            // 不指定parent，默认是Object的场景
            javaType = JavaTypeUtil.createJavaType(new AbstractTypeReference<List<?>>() {});
            genericType = (GenericType)new ArrayList<>(javaType.getBindings().values()).get(0);
            Assert.assertEquals(genericType.getName(), "?");
            Assert.assertTrue(genericType.getParent() instanceof SimpleType);
            Assert.assertEquals(genericType.getParent().getRawClass(), Object.class);
        }

        {
            // 验证有名字的泛型
            Field field = A.class.getDeclaredField("map");
            JavaType javaType = JavaTypeUtil.createJavaType(field.getGenericType());
            Assert.assertTrue(javaType instanceof SimpleType);
            Assert.assertEquals(javaType.getRawClass(), Map.class);
            Assert.assertEquals(CollectionUtil.size(javaType.getBindings()), 2);
            Assert.assertTrue(new ArrayList<>(javaType.getBindings().values()).get(0) instanceof GenericType);
            GenericType genericType0 = (GenericType)new ArrayList<>(javaType.getBindings().values()).get(0);
            Assert.assertEquals(genericType0.getName(), "V");
            Assert.assertEquals(genericType0.getParent().getRawClass(), Object.class);
            Assert.assertTrue(new ArrayList<>(javaType.getBindings().values()).get(1) instanceof GenericType);
            GenericType genericType1 = (GenericType)new ArrayList<>(javaType.getBindings().values()).get(1);
            Assert.assertEquals(genericType1.getName(), "K");
            Assert.assertEquals(genericType1.getParent().getRawClass(), Object.class);
        }

        {
            // 验证类型包含泛型的，并且是循环包含的场景，即 T extends List<T>这种
            Method method = this.getClass().getDeclaredMethod("method1", A.class);
            JavaType javaType = JavaTypeUtil.createJavaType(method.getTypeParameters()[0]);

            Assert.assertTrue(javaType instanceof GenericType);
            Assert.assertEquals(javaType.getName(), "T");
            Assert.assertTrue(((GenericType)javaType).getParent() instanceof SimpleType);
            SimpleType simpleType = (SimpleType)((GenericType)javaType).getParent();
            Assert.assertEquals(simpleType.getRawClass(), List.class);
            Assert.assertEquals(CollectionUtil.size(simpleType.getBindings()), 1);
            Assert.assertTrue(new ArrayList<>(simpleType.getBindings().values()).get(0) instanceof GenericType);
            JavaType javaType1 = JavaTypeUtil.createJavaType(method.getTypeParameters()[0]);
            // 递归依赖了，这里直接验证
            Assert.assertEquals(javaType1, javaType);
        }

        // 泛型数组场景不好构造，这里不单独验证，但是后边会有验证

    }

    @Test
    public void testGetDeclareGenerics() {
        List<JavaType> list = JavaTypeUtil.getDeclareGenerics(A.class);
        Assert.assertEquals(list.size(), 2);
        Assert.assertTrue(list.get(0) instanceof GenericType);
        Assert.assertTrue(list.get(1) instanceof GenericType);
        Assert.assertEquals(list.get(0).getName(), "K");
        Assert.assertTrue(((GenericType)list.get(0)).getParent() instanceof SimpleType);
        Assert.assertEquals(list.get(1).getName(), "V");
        Assert.assertTrue(((GenericType)list.get(1)).getParent() instanceof SimpleType);

        Assert.assertEquals(((GenericType)list.get(0)).getParent().getRawClass(), Object.class);
        Assert.assertEquals(((GenericType)list.get(1)).getParent().getRawClass(), Object.class);
        list = JavaTypeUtil.getDeclareGenerics(C.class);
        Assert.assertTrue(list.isEmpty());
    }

    @Test
    public void testGetDeclareGenericSuperclasses() {
        List<JavaType> list = JavaTypeUtil.getDeclareGenericSuperclasses(C.class);
        Assert.assertTrue(list.isEmpty());

        list = JavaTypeUtil.getDeclareGenericSuperclasses(D.class);
        Assert.assertEquals(list.size(), 3);

        JavaType javaType = list.get(0);
        JavaType expectType = JavaTypeUtil.createJavaType(new AbstractTypeReference<List<String>>() {});
        Assert.assertEquals(javaType, expectType);

        javaType = list.get(1);
        expectType = JavaTypeUtil.createJavaType(new AbstractTypeReference<Integer[]>() {});
        Assert.assertEquals(javaType, expectType);

        javaType = list.get(2);
        expectType = JavaTypeUtil.createJavaType(new AbstractTypeReference<List<List<String>>>() {});
        Assert.assertEquals(javaType, expectType);
    }

    @Test(dataProvider = "signToClass")
    public void testSignToClass(String jvmClassName, Class<?> clazz) {
        Assert.assertEquals(JavaTypeUtil.signToClass(jvmClassName), clazz);
    }

    @Test
    public void baseTest() {
        // 基础测试，测试一些简单方法
        {
            Assert.assertEquals(Character.class, JavaTypeUtil.boxed(char.class));
            Assert.assertEquals(Byte.class, JavaTypeUtil.boxed(byte.class));
            Assert.assertEquals(Short.class, JavaTypeUtil.boxed(short.class));
            Assert.assertEquals(Integer.class, JavaTypeUtil.boxed(int.class));
            Assert.assertEquals(Long.class, JavaTypeUtil.boxed(long.class));
            Assert.assertEquals(Double.class, JavaTypeUtil.boxed(double.class));
            Assert.assertEquals(Float.class, JavaTypeUtil.boxed(float.class));
            Assert.assertEquals(Boolean.class, JavaTypeUtil.boxed(boolean.class));
        }

        {
            Assert.assertTrue(JavaTypeUtil.isNotPojo(Boolean.class));
            Assert.assertTrue(JavaTypeUtil.isNotPojo(Character.class));
            Assert.assertTrue(JavaTypeUtil.isNotPojo(Number.class));
            Assert.assertTrue(JavaTypeUtil.isNotPojo(Map.class));
            Assert.assertTrue(JavaTypeUtil.isNotPojo(String.class));
            Assert.assertTrue(JavaTypeUtil.isNotPojo(Collection.class));
            Assert.assertTrue(JavaTypeUtil.isNotPojo(Enum.class));
            Assert.assertTrue(JavaTypeUtil.isNotPojo(char.class));
            Assert.assertTrue(JavaTypeUtil.isNotPojo(boolean.class));
            Assert.assertTrue(JavaTypeUtil.isNotPojo(byte.class));
            Assert.assertTrue(JavaTypeUtil.isNotPojo(short.class));
            Assert.assertTrue(JavaTypeUtil.isNotPojo(int.class));
            Assert.assertTrue(JavaTypeUtil.isNotPojo(long.class));
            Assert.assertTrue(JavaTypeUtil.isNotPojo(float.class));
            Assert.assertTrue(JavaTypeUtil.isNotPojo(double.class));
            Assert.assertFalse(JavaTypeUtil.isNotPojo(A.class));
        }

        {
            Assert.assertTrue(JavaTypeUtil.isSimple(char.class));
            Assert.assertTrue(JavaTypeUtil.isSimple(byte.class));
            Assert.assertTrue(JavaTypeUtil.isSimple(short.class));
            Assert.assertTrue(JavaTypeUtil.isSimple(int.class));
            Assert.assertTrue(JavaTypeUtil.isSimple(long.class));
            Assert.assertTrue(JavaTypeUtil.isSimple(boolean.class));
            Assert.assertTrue(JavaTypeUtil.isSimple(double.class));
            Assert.assertTrue(JavaTypeUtil.isSimple(float.class));
            Assert.assertTrue(JavaTypeUtil.isSimple(ErrorCodeEnum.class));
            Assert.assertTrue(JavaTypeUtil.isSimple(String.class));

            Assert.assertTrue(JavaTypeUtil.isSimple(Character.class));
            Assert.assertTrue(JavaTypeUtil.isSimple(Byte.class));
            Assert.assertTrue(JavaTypeUtil.isSimple(Short.class));
            Assert.assertTrue(JavaTypeUtil.isSimple(Integer.class));
            Assert.assertTrue(JavaTypeUtil.isSimple(Long.class));
            Assert.assertTrue(JavaTypeUtil.isSimple(Boolean.class));
            Assert.assertTrue(JavaTypeUtil.isSimple(Double.class));
            Assert.assertTrue(JavaTypeUtil.isSimple(Float.class));

            Assert.assertTrue(JavaTypeUtil.isSimple(BigDecimal.class));

            Assert.assertFalse(JavaTypeUtil.isSimple(Collection.class));
            Assert.assertFalse(JavaTypeUtil.isSimple(Map.class));
        }

        {
            Assert.assertTrue(JavaTypeUtil.isBasic(Character.class));
            Assert.assertTrue(JavaTypeUtil.isBasic(Byte.class));
            Assert.assertTrue(JavaTypeUtil.isBasic(Short.class));
            Assert.assertTrue(JavaTypeUtil.isBasic(Integer.class));
            Assert.assertTrue(JavaTypeUtil.isBasic(Long.class));
            Assert.assertTrue(JavaTypeUtil.isBasic(Boolean.class));
            Assert.assertTrue(JavaTypeUtil.isBasic(Double.class));
            Assert.assertTrue(JavaTypeUtil.isBasic(Float.class));

        }

        {
            Assert.assertTrue(JavaTypeUtil.isGeneralType(char.class));
            Assert.assertTrue(JavaTypeUtil.isGeneralType(byte.class));
            Assert.assertTrue(JavaTypeUtil.isGeneralType(short.class));
            Assert.assertTrue(JavaTypeUtil.isGeneralType(int.class));
            Assert.assertTrue(JavaTypeUtil.isGeneralType(long.class));
            Assert.assertTrue(JavaTypeUtil.isGeneralType(boolean.class));
            Assert.assertTrue(JavaTypeUtil.isGeneralType(double.class));
            Assert.assertTrue(JavaTypeUtil.isGeneralType(float.class));
        }

        {
            Assert.assertTrue(JavaTypeUtil.isGeneralArrayType(char[].class));
            Assert.assertTrue(JavaTypeUtil.isGeneralArrayType(byte[].class));
            Assert.assertTrue(JavaTypeUtil.isGeneralArrayType(short[].class));
            Assert.assertTrue(JavaTypeUtil.isGeneralArrayType(int[].class));
            Assert.assertTrue(JavaTypeUtil.isGeneralArrayType(long[].class));
            Assert.assertTrue(JavaTypeUtil.isGeneralArrayType(boolean[].class));
            Assert.assertTrue(JavaTypeUtil.isGeneralArrayType(float[].class));
            Assert.assertTrue(JavaTypeUtil.isGeneralArrayType(double[].class));
        }
    }

    @DataProvider(name = "signToClass")
    public Object[][] signToClass() {
        return new Object[][] {{"Z", boolean.class}, {"C", char.class}, {"B", byte.class}, {"S", short.class},
            {"I", int.class}, {"J", long.class}, {"D", double.class}, {"F", float.class},
            {"Ljava.lang.String;", String.class}, {"[[Ljava.lang.String;", String[][].class}, {"[Z", boolean[].class}};
    }

    public <T extends List<T>> void method1(A<List<T>, T[]> a) {

    }

    public <T extends List<String>> void method2(A<List<T>, T[]> a, A aa) {

    }

    public void method3(List<?> list) {

    }

    /**
     * 具有两个泛型的基类
     * 
     * @param <K>
     * @param <V>
     */
    public static class A<K, V> {

        private Map<V, K> map;

        private List<K> keyList;

        private List<V> valueList;

        private K key;

        private V value;
    }

    /**
     * 复杂泛型继承； 1、测试同名泛型是否可以区分，理论上D中声明的K和E中声明的K是不一样的，E中的K等价于D中的V
     * 
     * @param <K>
     * @param <V>
     */
    public static class B<K, V, F extends List<K>> extends A<V, K[][]> {

        private Map<V, K> map;

        private List<K> keyList;

        private List<V> valueList;

        private K key;

        private V value;

        private F genericKeyList;

    }

    /**
     * 不从父类继承泛型
     */
    public static class C extends B {

    }

    /**
     * 从父类继承泛型，复杂泛型
     */
    public static class D extends B<List<String>, Integer[], List<List<String>>> {

    }
}
