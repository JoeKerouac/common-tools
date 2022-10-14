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
import java.util.*;

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
            // 测试createBaseJavaType
            BaseType baseType = new BaseType();
            baseType.setType(Object.class);
            baseType.setName(Object.class.getSimpleName());
            Assert.assertEquals(JavaTypeUtil.createBaseType(Object.class), baseType);
        }

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
                Assert.assertTrue(javaType instanceof BaseType);
                BaseType baseType = (BaseType)javaType;
                Assert.assertEquals(baseType.getType(), Map.class);
                Assert.assertEquals(CollectionUtil.size(baseType.getGenerics()), 2);
                Assert.assertTrue(baseType.getGenerics()[0] instanceof BaseType);
                BaseType stringBaseType = (BaseType)baseType.getGenerics()[0];
                Assert.assertEquals(stringBaseType.getType(), String.class);
                Assert.assertEquals(CollectionUtil.size(stringBaseType.getGenerics()), 0);
                Assert.assertTrue(baseType.getGenerics()[1] instanceof BaseType);
                BaseType listBaseType = (BaseType)baseType.getGenerics()[1];
                Assert.assertEquals(listBaseType.getType(), List.class);
                Assert.assertEquals(CollectionUtil.size(listBaseType.getGenerics()), 1);
                Assert.assertTrue(listBaseType.getGenerics()[0] instanceof BaseType);
                Assert.assertEquals(((BaseType)listBaseType.getGenerics()[0]).getType(), String.class);
            }
        }

        {
            // 现在BaseJavaType验证通过，依靠该场景进行下一步验证
            // 先验证匿名泛型场景
            JavaType javaType = JavaTypeUtil.createJavaType(new AbstractTypeReference<List<? extends String>>() {});
            GenericType genericType = (GenericType)((BaseType)javaType).getGenerics()[0];
            Assert.assertEquals(genericType.getName(), "?");
            Assert.assertTrue(genericType.getParent() instanceof BaseType);
            Assert.assertEquals(((BaseType)genericType.getParent()).getType(), String.class);

            // 不指定parent，默认是Object的场景
            javaType = JavaTypeUtil.createJavaType(new AbstractTypeReference<List<?>>() {});
            genericType = (GenericType)((BaseType)javaType).getGenerics()[0];
            Assert.assertEquals(genericType.getName(), "?");
            Assert.assertTrue(genericType.getParent() instanceof BaseType);
            Assert.assertEquals(((BaseType)genericType.getParent()).getType(), Object.class);
        }

        {
            // 验证有名字的泛型
            Field field = A.class.getDeclaredField("map");
            JavaType javaType = JavaTypeUtil.createJavaType(field.getGenericType());
            Assert.assertTrue(javaType instanceof BaseType);
            Assert.assertEquals(((BaseType)javaType).getType(), Map.class);
            Assert.assertEquals(CollectionUtil.size(((BaseType)javaType).getGenerics()), 2);
            Assert.assertTrue(((BaseType)javaType).getGenerics()[0] instanceof GenericType);
            GenericType genericType0 = (GenericType)((BaseType)javaType).getGenerics()[0];
            Assert.assertEquals(genericType0.getName(), "V");
            Assert.assertEquals(((BaseType)genericType0.getParent()).getType(), Object.class);
            Assert.assertTrue(((BaseType)javaType).getGenerics()[1] instanceof GenericType);
            GenericType genericType1 = (GenericType)((BaseType)javaType).getGenerics()[1];
            Assert.assertEquals(genericType1.getName(), "K");
            Assert.assertEquals(((BaseType)genericType1.getParent()).getType(), Object.class);
        }

        {
            // 验证类型包含泛型的，并且是循环包含的场景，即 T extends List<T>这种
            Method method = this.getClass().getDeclaredMethod("method1", A.class);
            JavaType javaType = JavaTypeUtil.createJavaType(method.getTypeParameters()[0]);

            Assert.assertTrue(javaType instanceof GenericType);
            Assert.assertEquals(javaType.getName(), "T");
            Assert.assertTrue(((GenericType)javaType).getParent() instanceof BaseType);
            BaseType baseType = (BaseType)((GenericType)javaType).getParent();
            Assert.assertEquals(baseType.getType(), List.class);
            Assert.assertEquals(CollectionUtil.size(baseType.getGenerics()), 1);
            Assert.assertTrue(baseType.getGenerics()[0] instanceof GenericType);
            GenericType genericType = (GenericType)baseType.getGenerics()[0];
            // 递归依赖了，这里直接验证
            Assert.assertEquals(genericType, javaType);
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
        Assert.assertTrue(((GenericType)list.get(0)).getParent() instanceof BaseType);
        Assert.assertEquals(list.get(1).getName(), "V");
        Assert.assertTrue(((GenericType)list.get(1)).getParent() instanceof BaseType);

        Assert.assertEquals(((BaseType)((GenericType)list.get(0)).getParent()).getType(), Object.class);
        Assert.assertEquals(((BaseType)((GenericType)list.get(1)).getParent()).getType(), Object.class);
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

    @Test
    public void testGetGenericRegistry() throws Exception {
        {
            // 测试获取方法上的泛型注册表
            Method method1 = this.getClass().getDeclaredMethod("method1", A.class);
            Map<GenericDefinition, JavaType> registry = JavaTypeUtil.getGenericRegistry(method1);
            Assert.assertEquals(registry.size(), 1);
            // 虽然这里是for循环，但实际上集合里边只有一个
            for (JavaType value : registry.values()) {
                Assert.assertTrue(value instanceof BaseType);
                BaseType baseType = (BaseType)value;
                Assert.assertEquals(baseType.getType(), List.class);
                Assert.assertEquals(CollectionUtil.size(baseType.getGenerics()), 1);
                Assert.assertTrue(baseType.getGenerics()[0] instanceof GenericType);
                Assert.assertTrue(((GenericType)baseType.getGenerics()[0]).getParent() instanceof BaseType);

                // method1的泛型递归了，这里验证下
                Assert.assertEquals(baseType, ((GenericType)baseType.getGenerics()[0]).getParent());
            }
        }

        {
            // 测试获取类上的泛型注册表
            Map<GenericDefinition, JavaType> registry =
                JavaTypeUtil.getGenericRegistry(D.class, A.class, Collections.emptyMap());
            Assert.assertEquals(registry.size(), 5);

            {
                // B上的泛型定义
                // 实际类型
                JavaType javaType = registry.get(new GenericDefinition("K", B.class));
                // 目标类型
                JavaType dstType = JavaTypeUtil.createJavaType(new AbstractTypeReference<List<String>>() {});
                Assert.assertEquals(javaType, dstType);

                // 实际类型
                javaType = registry.get(new GenericDefinition("V", B.class));
                // 目标类型
                dstType = JavaTypeUtil.createJavaType(new AbstractTypeReference<Integer[]>() {});
                Assert.assertEquals(javaType, dstType);

                // 实际类型
                javaType = registry.get(new GenericDefinition("F", B.class));
                // 目标类型
                dstType = JavaTypeUtil.createJavaType(new AbstractTypeReference<List<List<String>>>() {});
                Assert.assertEquals(javaType, dstType);
            }

            {
                // A上的泛型定义
                JavaType javaType = registry.get(new GenericDefinition("K", A.class));
                // 目标类型
                JavaType dstType = JavaTypeUtil.createJavaType(new AbstractTypeReference<Integer[]>() {});
                Assert.assertEquals(javaType, dstType);

                // 实际类型
                javaType = registry.get(new GenericDefinition("V", A.class));
                // 注意，这个比较特殊，是一个泛型数组
                Assert.assertTrue(javaType instanceof CustomGenericArrayType);
                // 二维数组
                Assert.assertEquals(((CustomGenericArrayType)javaType).getDimensions(), 2);

                // 取出数组的componentType，数组componentType仍然有泛型
                javaType = ((CustomGenericArrayType)javaType).getComponentType();

                // 目标类型
                dstType = JavaTypeUtil.createJavaType(new AbstractTypeReference<List<String>>() {});
                Assert.assertEquals(javaType, dstType);
            }
        }
    }

    @Test
    public void testGetRealType() throws Exception {
        // 测试getRealType
        {
            // 基础测试
            AbstractTypeReference<Map<String, List<String>>> reference =
                new AbstractTypeReference<Map<String, List<String>>>() {};

            JavaType javaType = JavaTypeUtil.createJavaType(reference.getType());
            Assert.assertTrue(javaType instanceof BaseType);
            BaseType baseType = (BaseType)javaType;
            Assert.assertEquals(2, baseType.getGenerics().length);

            JavaType realType1 = JavaTypeUtil.getRealType(baseType.getGenerics()[0]);
            JavaType realType2 = JavaTypeUtil.getRealType(baseType.getGenerics()[1]);
            Assert.assertTrue(realType1 instanceof BaseType);

            Assert.assertEquals(String.class, ((BaseType)realType1).getType());

            Assert.assertEquals(List.class, ((BaseType)realType2).getType());

            Assert.assertEquals(1, ((BaseType)realType2).getGenerics().length);

            JavaType realType3 = JavaTypeUtil.getRealType(((BaseType)realType2).getGenerics()[0]);
            Assert.assertTrue(realType3 instanceof BaseType);
            Assert.assertEquals(((BaseType)realType3).getType(), String.class);
        }

        {
            // 使用泛型类型作为getRealType的入参
            Method method2 = this.getClass().getDeclaredMethod("method2", A.class, A.class);
            // 这里应该返回的类型是A<List<T>, T[]>
            JavaType paramType = JavaTypeUtil.createJavaType(method2.getParameters()[0].getParameterizedType());

            Assert.assertTrue(paramType instanceof BaseType);
            BaseType baseType = (BaseType)paramType;
            Assert.assertEquals(baseType.getType(), A.class);
            Assert.assertEquals(CollectionUtil.size(baseType.getGenerics()), 2);
            Assert.assertTrue(baseType.getGenerics()[0] instanceof BaseType);
            Assert.assertTrue(((BaseType)baseType.getGenerics()[0]).getGenerics()[0] instanceof GenericType);
            Assert.assertTrue(baseType.getGenerics()[1] instanceof CustomGenericArrayType);

            {
                // 取出List<T>中的泛型
                GenericType genericType = (GenericType)((BaseType)baseType.getGenerics()[0]).getGenerics()[0];
                // 对于方法上的泛型，使不使用注册表对我们没有区别，因为方法上声明的泛型只有运行时才会更改，其他时候不会更改，与此不同的是
                // 类上的字段，类上的泛型可以被子类改写，与此同时子类继承的该字段的泛型也将一同被改写

                // 使用注册表
                JavaType result0 = JavaTypeUtil.getRealType(genericType, JavaTypeUtil.getGenericRegistry(method2));
                // 不使用注册表
                JavaType result1 = JavaTypeUtil.getRealType(genericType);
                List<JavaType> javaTypes = Arrays.asList(result0, result1);

                for (JavaType javaType : javaTypes) {
                    Assert.assertTrue(javaType instanceof BaseType);
                    BaseType baseType1 = (BaseType)javaType;
                    Assert.assertEquals(baseType1.getType(), List.class);
                    Assert.assertEquals(CollectionUtil.size(baseType1.getGenerics()), 1);
                    Assert.assertTrue(baseType1.getGenerics()[0] instanceof BaseType);
                    Assert.assertEquals(((BaseType)baseType1.getGenerics()[0]).getType(), String.class);
                    Assert.assertTrue(CollectionUtil.isEmpty(((BaseType)baseType1.getGenerics()[0]).getGenerics()));
                }
            }
        }

        {
            // 使用匿名泛型类型作为getRealType的入参
            Method method2 = this.getClass().getDeclaredMethod("method3", List.class);
            // 这里应该返回的类型是List<?>
            JavaType paramType = JavaTypeUtil.createJavaType(method2.getParameters()[0].getParameterizedType());
            Assert.assertTrue(paramType instanceof BaseType);
            Assert.assertTrue(((BaseType)paramType).getGenerics()[0] instanceof GenericType);
            GenericType genericType = (GenericType)((BaseType)paramType).getGenerics()[0];
            // 这里传一个空注册表，顺便验证下这个兜底
            JavaType javaType = JavaTypeUtil.getRealType(genericType, Collections.emptyMap());
            Assert.assertTrue(javaType instanceof BaseType);
            Assert.assertEquals(((BaseType)javaType).getType(), Object.class);
            Assert.assertTrue(CollectionUtil.isEmpty(((BaseType)javaType).getGenerics()));
        }

    }

    @Test
    public void testTypeResolve() throws Exception {
        // 高级测试：方法入参使用方法上声明的泛型，这里用方法2而不是1，主要是为了好验证
        Method method2 = this.getClass().getDeclaredMethod("method2", A.class, A.class);

        {
            // 方法第一个参数验证，指定了泛型
            JavaType paramType = JavaTypeUtil.createJavaType(method2.getParameters()[0].getParameterizedType());
            // 这里应该返回的类型是A<List<List<String>>, List<String>[]>
            paramType = JavaTypeUtil.typeResolve(paramType, JavaTypeUtil.getGenericRegistry(method2));

            Assert.assertTrue(paramType instanceof BaseType);
            BaseType baseType = (BaseType)paramType;
            Assert.assertEquals(baseType.getType(), A.class);
            Assert.assertEquals(CollectionUtil.size(baseType.getGenerics()), 2);
            Assert.assertTrue(baseType.getGenerics()[0] instanceof BaseType);
            Assert.assertTrue(baseType.getGenerics()[1] instanceof CustomGenericArrayType);

            {
                // A的第一个泛型验证，实际类型是List<List<String>>
                BaseType listTType = (BaseType)baseType.getGenerics()[0];
                Assert.assertEquals(listTType.getType(), List.class);
                Assert.assertEquals(CollectionUtil.size(listTType.getGenerics()), 1);
                Assert.assertTrue(listTType.getGenerics()[0] instanceof BaseType);
                // 泛型T的类型
                BaseType tType = (BaseType)listTType.getGenerics()[0];
                Assert.assertEquals(tType.getType(), List.class);
                Assert.assertEquals(CollectionUtil.size(tType.getGenerics()), 1);
                Assert.assertTrue(tType.getGenerics()[0] instanceof BaseType);
                BaseType stringType = (BaseType)tType.getGenerics()[0];
                Assert.assertEquals(stringType.getType(), String.class);
                Assert.assertTrue(CollectionUtil.isEmpty(stringType.getGenerics()));
            }

            {
                // A的第二个泛型验证，实际类型是List<String>[]
                CustomGenericArrayType arrayType = (CustomGenericArrayType)baseType.getGenerics()[1];
                Assert.assertEquals(arrayType.getDimensions(), 1);
                Assert.assertTrue(arrayType.getComponentType() instanceof BaseType);
                BaseType arrComponentType = (BaseType)arrayType.getComponentType();
                Assert.assertEquals(arrComponentType.getType(), List.class);
                Assert.assertEquals(CollectionUtil.size(arrComponentType.getGenerics()), 1);
                Assert.assertTrue(arrComponentType.getGenerics()[0] instanceof BaseType);
                BaseType stringType = (BaseType)arrComponentType.getGenerics()[0];
                Assert.assertTrue(CollectionUtil.isEmpty(stringType.getGenerics()));
                Assert.assertEquals(stringType.getType(), String.class);
            }
        }

        {
            // 方法第二个参数验证，没有指定泛型
            // 方法第一个参数验证，指定了泛型
            JavaType paramType = JavaTypeUtil.createJavaType(method2.getParameters()[1].getParameterizedType());
            // 这里应该返回的类型是A<List<Object>, Object>
            paramType = JavaTypeUtil.typeResolve(paramType, JavaTypeUtil.getGenericRegistry(method2));

            Assert.assertTrue(paramType instanceof BaseType);
            BaseType baseType = (BaseType)paramType;
            Assert.assertEquals(baseType.getType(), A.class);
            Assert.assertEquals(CollectionUtil.size(baseType.getGenerics()), 2);
            Assert.assertTrue(baseType.getGenerics()[0] instanceof BaseType);
            Assert.assertTrue(baseType.getGenerics()[1] instanceof BaseType);

            {
                // A的第一个泛型验证，实际类型是List<String>[]
                BaseType type = (BaseType)baseType.getGenerics()[0];
                Assert.assertEquals(type.getType(), Object.class);
                Assert.assertEquals(CollectionUtil.size(type.getGenerics()), 0);
            }

            {
                // A的第二个泛型验证，实际类型是List<String>[]
                BaseType type = (BaseType)baseType.getGenerics()[1];
                Assert.assertEquals(type.getType(), Object.class);
                Assert.assertEquals(CollectionUtil.size(type.getGenerics()), 0);
            }
        }

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
