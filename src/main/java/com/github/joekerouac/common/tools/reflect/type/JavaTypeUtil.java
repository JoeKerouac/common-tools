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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.exception.CommonException;
import com.github.joekerouac.common.tools.reflect.ClassUtils;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * java类型工具
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JavaTypeUtil {

    /**
     * super泛型，匹配? super Object这种泛型
     */
    private static final Pattern SUPER_PATTERN = Pattern.compile("(.*) super.*");

    /**
     * extends泛型，匹配? extends Object这种泛型
     */
    private static final Pattern EXTENDS_PATTERN = Pattern.compile("(.*) extends.*");

    /**
     * 将指定类型转换为Class，例如如果类型是带泛型的Map，则将泛型信息擦除，返回Map的class对象；
     * 
     * @param type
     *            类型
     * @return class，有可能返回null
     */
    public static Class<?> toClass(Type type) {
        final JavaType javaType = createJavaType(type);
        return javaType.getRawClass();
    }

    /**
     * 根据TypeReference得出自定义类型
     *
     * @param type
     *            TypeReference
     * @return 自定义java类型说明
     */
    public static JavaType createJavaType(AbstractTypeReference<?> type) {
        return createJavaType(type.getType());
    }

    /**
     * 根据java系统类型得出自定义类型
     *
     * @param type
     *            java反射取得的类型
     * @return 自定义java类型说明
     */
    public static JavaType createJavaType(Type type) {
        return createJavaType(type, new LinkedHashMap<>());
    }

    /**
     * 根据java系统类型得出自定义类型
     *
     * @param type
     *            java反射取得的类型
     * @param bindings
     *            bindings
     * @return 自定义java类型说明
     */
    public static JavaType createJavaType(Type type, LinkedHashMap<String, JavaType> bindings) {
        // type的来源：1、从方法参数上获取；2、从字段上获取；3、从类上获取；4、从继承上获取
        if (type instanceof JavaType) {
            return (JavaType)type;
        }

        LinkedHashMap<String, JavaType> context = new LinkedHashMap<>(bindings);

        String typeName = dealName(type.getTypeName());
        JavaType javaType;
        if (type instanceof WildcardType) {
            // 该类型是不确定的泛型，即泛型为 ?，说明该泛型没有任何声明，直接使用了，类似与匿名内部类；
            WildcardType wildcardTypeImpl = (WildcardType)type;
            // 子类
            Type[] child = wildcardTypeImpl.getLowerBounds();
            // 父类
            Type[] parent = wildcardTypeImpl.getUpperBounds();
            GenericType genericType = new GenericType();
            genericType.setName(typeName);
            genericType.setBindings(new LinkedHashMap<>());
            genericType.setBindingList(new ArrayList<>());

            // child和parent不可能都为空，如果用户是使用的一个单泛型T或者?，没有明确指出他的父类或者子类，例如T extends String、
            // T super String，那么就会有一个默认的parent，值是Object
            JavaType rawType;
            if (child.length > 0) {
                rawType = createJavaType(child[0], context);
                genericType.setChild(rawType);
            } else {
                rawType = createJavaType(parent[0], context);
                genericType.setParent(rawType);
            }

            genericType.setRawType(rawType);
            javaType = genericType;
        } else if (type instanceof TypeVariable) {
            // 该类型是名字确定的泛型，例如T等，需要先声明后使用，区别于WildcardType，WildcardType类型的泛型不需要声明可以直接使用；
            TypeVariable<?> typeVariableImpl = (TypeVariable<?>)type;

            GenericType genericType = new GenericType();
            genericType.setName(typeName);
            genericType.setBindings(new LinkedHashMap<>());
            genericType.setBindingList(new ArrayList<>());

            // 先从上下文获取，获取不到再构建
            JavaType rawType = context.get(typeName);
            if (rawType == null) {
                // 需要先将该泛型放入以确定的泛型，防止后边setParent的时候出现死循环，对于这种泛型声明将会出现死循环：
                // T extends List<T>，解析List的时候由于List还有泛型，并且这个是T，如果不做处理，将会循环解析T，最终陷入
                // 死循环，所以这里在泛型解析完成前（解析parent前）先放入map防止死循环
                context.put(typeName, genericType);

                // 指定名字的泛型只能继承，不能使用关键字super，所以getBounds该方法得出的是泛型的父类型，getBounds肯定有一个值，如果
                // 用户没有指定就是Object
                rawType = createJavaType(typeVariableImpl.getBounds()[0], context);
            }
            genericType.setParent(rawType);
            genericType.setRawType(rawType);
            javaType = genericType;
        } else if (type instanceof ParameterizedType) {
            // 该类型存在泛型
            ParameterizedType parameterizedTypeImpl = (ParameterizedType)type;
            Type[] types = parameterizedTypeImpl.getActualTypeArguments();

            JavaType rawType = createJavaType(parameterizedTypeImpl.getRawType(), context);

            TypeVariable<? extends Class<?>>[] typeParameters = rawType.getRawClass().getTypeParameters();

            LinkedHashMap<String, JavaType> currentBindings = new LinkedHashMap<>();
            for (int i = 0; i < types.length; i++) {
                Type nowType = types[i];
                TypeVariable<? extends Class<?>> typeParameter = typeParameters[i];
                JavaType bindingType = createJavaType(nowType, context);
                String bindingName = dealName(typeParameter.getTypeName());
                currentBindings.put(bindingName, bindingType);
                context.put(bindingName, bindingType);
            }

            SimpleType simpleType = new SimpleType();
            simpleType.setName(typeName);
            simpleType.setRawType(rawType);
            simpleType.setBindings(currentBindings);
            simpleType.setBindingList(new ArrayList<>(currentBindings.values()));
            javaType = simpleType;
        } else if (type instanceof GenericArrayType) {
            GenericArrayType genericArrayType = (GenericArrayType)type;
            javaType = getArrayDesc(genericArrayType, context);
        } else if (type instanceof Class) {
            // 该类型是普通类型（没有泛型，本身也不是泛型参数）
            Class<?> clazz = (Class<?>)type;

            if (clazz.isArray()) {
                javaType = getArrayDesc(clazz, context);
            } else {
                TypeVariable<? extends Class<?>>[] typeParameters = clazz.getTypeParameters();
                LinkedHashMap<String, JavaType> currentBindings =
                    Arrays.stream(typeParameters).map(t -> createJavaType(t, context))
                        .collect(Collectors.toMap(JavaType::getName, t -> t, (u, v) -> {
                            throw new IllegalStateException(String.format("Duplicate key %s", u));
                        }, LinkedHashMap::new));

                SimpleType simpleType = new SimpleType();
                simpleType.setName(clazz.getName());
                simpleType.setRawClass(clazz);
                simpleType.setBindings(currentBindings);
                simpleType.setBindingList(new ArrayList<>(currentBindings.values()));

                javaType = simpleType;
            }
        } else {
            throw new CommonException(ErrorCodeEnum.UNKNOWN_EXCEPTION, "type[" + type + "]类型未知");
        }

        return javaType;
    }

    /**
     * 获取数组的说明
     *
     * @param type
     *            类型
     * @return 数组说明，如果指定类型不是数组，那么抛出异常
     */
    public static CustomGenericArrayType getArrayDesc(java.lang.reflect.Type type) {
        return getArrayDesc(type, new LinkedHashMap<>());
    }

    /**
     * 获取数组的说明
     *
     * @param type
     *            类型
     * @param resolved
     *            当前已经确定的类型，允许为空
     * @return 数组说明，如果指定类型不是数组，那么抛出异常
     */
    public static CustomGenericArrayType getArrayDesc(java.lang.reflect.Type type,
        LinkedHashMap<String, JavaType> resolved) {
        Assert.assertTrue(((type instanceof Class) && ((Class<?>)type).isArray()) || (type instanceof GenericArrayType),
            "类型必须是数组类型", ExceptionProviderConst.IllegalArgumentExceptionProvider);

        return getArrayDesc(type, 0, resolved);
    }

    /**
     * 获取数组的说明
     *
     * @param type
     *            类型
     * @param now
     *            当前的数组维度
     * @param resolved
     *            当前已经确定的类型，允许为空
     * @return 数组说明
     */
    private static CustomGenericArrayType getArrayDesc(java.lang.reflect.Type type, int now,
        LinkedHashMap<String, JavaType> resolved) {
        if (type instanceof Class) {
            if (((Class<?>)type).isArray()) {
                return getArrayDesc(((Class<?>)type).getComponentType(), now + 1, resolved);
            }
        } else if (type instanceof GenericArrayType) {
            return getArrayDesc(((GenericArrayType)type).getGenericComponentType(), now + 1, resolved);
        }

        JavaType componentType = createJavaType(type, resolved);
        LinkedHashMap<String, JavaType> bindings = getBindings(componentType);
        CustomGenericArrayType arrayDesc = new CustomGenericArrayType();
        arrayDesc.setName(type.getTypeName());
        arrayDesc.setComponentType(componentType);
        arrayDesc.setRawClass(Array.newInstance(componentType.getRawClass(), now).getClass());
        arrayDesc.setDimensions(now);
        arrayDesc.setBindings(bindings);
        arrayDesc.setBindingList(new ArrayList<>(bindings.values()));
        return arrayDesc;
    }

    private static LinkedHashMap<String, JavaType> getBindings(JavaType javaType) {
        if (javaType instanceof SimpleType) {
            return javaType.getBindings();
        } else if (javaType instanceof CustomGenericArrayType) {
            return getBindings(((CustomGenericArrayType)javaType).getComponentType());
        } else if (javaType instanceof GenericType) {
            return new LinkedHashMap<>();
        } else {
            throw new UnsupportedOperationException(
                StringUtils.format("不支持的操作类型: [{}]", javaType == null ? null : javaType.getClass()));
        }
    }

    /**
     * 获取指定类继承父类时使用的泛型列表，例如有如下类：
     * <p>
     * <code>public class Test extends U&lt;String&gt;</code>
     * <p>
     * 对该类使用该方法将会获得{@link String}对应的JavaType
     *
     * @param clazz
     *            类型
     * @return 类继承父类时使用的泛型列表
     */
    public static List<JavaType> getDeclareGenericSuperclasses(Class<?> clazz) {
        Assert.argNotNull(clazz, "clazz");

        Type genericSuperclass = clazz.getGenericSuperclass();

        // 如果不是ParameterizedType，说明类继承的时候没有使用泛型
        if (!(genericSuperclass instanceof ParameterizedType)) {
            return Collections.emptyList();
        }

        ParameterizedType parameterizedType = (ParameterizedType)genericSuperclass;
        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
        return Arrays.stream(actualTypeArguments).map(JavaTypeUtil::createJavaType).collect(Collectors.toList());
    }

    /**
     * 获取指定类上声明的泛型列表，例如有如下类
     * <p>
     * <code>public class Test&lt;T,F&gt;</code>
     * <p>
     * 对该类使用该方法将会返回T和M这两个泛型
     * 
     * @param clazz
     *            指定Class
     * @return 类上声明的泛型列表
     */
    public static List<JavaType> getDeclareGenerics(Class<?> clazz) {
        TypeVariable<? extends Class<?>>[] typeParameters = clazz.getTypeParameters();
        if (CollectionUtil.isEmpty(typeParameters)) {
            return Collections.emptyList();
        }

        return Arrays.stream(typeParameters).map(JavaTypeUtil::createJavaType).collect(Collectors.toList());
    }

    /**
     * 将原始类型的class转换为对应的包装类型
     * 
     * @param primitiveClazz
     *            原始类型
     * @return 对应的包装类型，如果传入的不是原始类型将会抛出异常
     */
    public static Class<?> boxed(Class<?> primitiveClazz) {
        Class<?> result;

        if (boolean.class.equals(primitiveClazz)) {
            result = Boolean.class;
        } else if (char.class.equals(primitiveClazz)) {
            result = Character.class;
        } else if (byte.class.equals(primitiveClazz)) {
            result = Byte.class;
        } else if (short.class.equals(primitiveClazz)) {
            result = Short.class;
        } else if (int.class.equals(primitiveClazz)) {
            result = Integer.class;
        } else if (long.class.equals(primitiveClazz)) {
            result = Long.class;
        } else if (double.class.equals(primitiveClazz)) {
            result = Double.class;
        } else if (float.class.equals(primitiveClazz)) {
            result = Float.class;
        } else {
            throw new CommonException(ErrorCodeEnum.CODE_ERROR, String.format("类型[%s]不是原始类型", primitiveClazz));
        }

        return result;
    }

    /**
     * 判断Class对象是否为指定的几种简单类型（该方法认为java自带简单类型包括java八大基本类型及其对应的封装类型、Number的子类、String、
     * Collection的子类、Map的子类、Enum、Date、Temporal（新版日期API），如果不是这些类型将会认为该类型是一个复杂类型（pojo类型））
     *
     * @param clazz
     *            Class对象，不能为null
     * @return 如果是pojo则返回<code>false</code>
     * @throws NullPointerException
     *             当传入Class对象为null时抛出该异常
     */
    public static boolean isNotPojo(Class<?> clazz) throws NullPointerException {
        Assert.argNotNull(clazz, "clazz");
        return isSimple(clazz) || Map.class.isAssignableFrom(clazz) || Collection.class.isAssignableFrom(clazz)
            || Date.class.isAssignableFrom(clazz) || Temporal.class.isAssignableFrom(clazz);
    }

    /**
     * 判断是否是八大基本类型、枚举类型、String、Number类型
     * 
     * @param clazz
     *            Class
     * @return 如果不是以上几种类型返回false
     */
    public static boolean isSimple(Class<?> clazz) {
        return isGeneralType(clazz) || isBasic(clazz) || Enum.class.isAssignableFrom(clazz)
            || String.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz);
    }

    /**
     * 判断Class对象是否为八大基本类型的封装类型
     *
     * @param clazz
     *            Class对象，不能为null
     * @return 如果是基本类型则返回<code>true</code>
     * @throws NullPointerException
     *             当传入Class对象为null时抛出该异常
     */
    public static boolean isBasic(Class<?> clazz) throws NullPointerException {
        Assert.argNotNull(clazz, "clazz");

        return Boolean.class.isAssignableFrom(clazz) || Character.class.isAssignableFrom(clazz)
            || Byte.class.isAssignableFrom(clazz) || Short.class.isAssignableFrom(clazz)
            || Integer.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz)
            || Double.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz);
    }

    /**
     * 判断指定Class是否是8大基本类型（int、short等，不包含对应的封装类型）
     *
     * @param clazz
     *            class对象
     * @return 如果是基本类型则返回<code>true</code>
     * @throws NullPointerException
     *             当传入Class对象为空时抛出该异常
     */
    public static boolean isGeneralType(Class<?> clazz) throws NullPointerException {
        Assert.argNotNull(clazz, "clazz");
        return clazz.isPrimitive();
    }

    /**
     * 判断指定数组名称是否是java八大基本类型（int[]、short[]、int[][]等，不包含对应的封装类型）
     *
     * @param clazz
     *            指定数组名称
     * @return 如果是基本类型则返回<code>true</code>
     * @throws NullPointerException
     *             当传入Class对象为空时抛出该异常
     */
    public static boolean isGeneralArrayType(Class<?> clazz) throws NullPointerException {
        Assert.argNotNull(clazz, "clazz");
        return clazz.isArray() && isGeneralType(toClass(getArrayDesc(clazz).getComponentType()));
    }

    /**
     * 将JVM类型签名转换为JVM的class对象
     * 
     * @param jvmClassName
     *            JVM类型签名，对象（除了原生类型）的签名是以L开头，英文分号;结尾，数组的签名是以[开头（一维数组就是一个[，二维就是 两个[，以此类推），后边跟的是数组类型
     * @return Class对象
     */
    public static Class<?> signToClass(String jvmClassName) {
        return signToClass(jvmClassName, ClassUtils.getDefaultClassLoader());
    }

    /**
     * 将JVM类型签名转换为JVM的class对象
     * 
     * @param jvmClassName
     *            JVM类型签名，对象（除了原生类型）的签名是以L开头，英文分号;结尾，数组的签名是以[开头（一维数组就是一个[，二维就是 两个[，以此类推），后边跟的是数组类型
     * @param classLoader
     *            ClassLoader
     * @return Class对象
     */
    public static Class<?> signToClass(String jvmClassName, ClassLoader classLoader) {
        return ClassUtils.loadClass(signToClassName(jvmClassName), classLoader);
    }

    /**
     * 将JVM类型签名转换为JVM的ClassName，例如Ljava/lang/String;
     * 
     * @param jvmClassName
     *            JVM类型签名，对象（除了原生类型）的签名是以L开头，英文分号;结尾，数组的签名是以[开头（一维数组就是一个[，二维就是 两个[，以此类推），后边跟的是数组类型
     * @return ClassName，原生类型比较特殊，是int、boolean、byte等，数组的ClassName和签名一致，所以将会原封不动返回
     */
    public static String signToClassName(String jvmClassName) {
        Assert.argNotBlank(jvmClassName, "jvmClassName");
        String packageClass = jvmClassName.trim().replaceAll("/", ".");

        String baseClass = null;
        if ("Z".equals(packageClass)) {
            baseClass = boolean.class.getName();
        } else if ("C".equals(packageClass)) {
            baseClass = char.class.getName();
        } else if ("B".equals(packageClass)) {
            baseClass = byte.class.getName();
        } else if ("S".equals(packageClass)) {
            baseClass = short.class.getName();
        } else if ("I".equals(packageClass)) {
            baseClass = int.class.getName();
        } else if ("J".equals(packageClass)) {
            baseClass = long.class.getName();
        } else if ("D".equals(packageClass)) {
            baseClass = double.class.getName();
        } else if ("F".equals(packageClass)) {
            baseClass = float.class.getName();
        } else if (packageClass.startsWith("L") && packageClass.endsWith(";")) {
            baseClass = packageClass.substring(1, packageClass.length() - 1);
        } else if (packageClass.startsWith("[")) {
            // 数组，不用管
            baseClass = packageClass;
        }

        if (baseClass == null) {
            throw new CommonException(ErrorCodeEnum.UNKNOWN_EXCEPTION,
                StringUtils.format("指定类型[{}]无法解析", packageClass));
        }

        return baseClass;
    }

    /**
     * 处理T extends XX或者T super XX这种类型的泛型名称
     * 
     * @param fullName
     *            泛型全名
     * @return 泛型的名称
     */
    private static String dealName(String fullName) {
        Matcher matcher = SUPER_PATTERN.matcher(fullName);
        String name;
        if (matcher.find()) {
            name = matcher.group(1);
        } else {
            matcher = EXTENDS_PATTERN.matcher(fullName);
            if (matcher.find()) {
                name = matcher.group(1);
            } else {
                name = fullName;
            }
        }
        return name;
    }

}
