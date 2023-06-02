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

import static com.github.joekerouac.common.tools.reflect.ReflectUtil.*;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.joekerouac.common.tools.constant.StringConst;
import com.github.joekerouac.common.tools.date.DateUtil;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.exception.CommonException;
import com.github.joekerouac.common.tools.reflect.AccessorUtil;
import com.github.joekerouac.common.tools.reflect.ReflectUtil;
import com.github.joekerouac.common.tools.reflect.type.JavaTypeUtil;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.AccessLevel;
import lombok.CustomLog;
import lombok.NoArgsConstructor;

/**
 * bean工具
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@CustomLog
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanUtils {

    /**
     * 将pojo的所有字段展开映射为map，默认包含null值，例如类User中有一个map（类型Map）字段，User实例的map字段有一对KV（k1=v1, k2=v2），那么
     * 使用该方法将会返回如下map（prefix为user）：user.map.k1=v1, user.map.k2=v2
     *
     * @param pojo
     *            pojo
     * @param prefix
     *            对象在map中的前缀，允许为null
     * @return map，当pojo为null时返回空map
     */
    public static Map<String, Object> convertToPlaceholder(Object pojo, String prefix) {
        return convertToPlaceholder(pojo, prefix, null, true);
    }

    /**
     * 将pojo的所有字段展开映射为map，例如类User中有一个map（类型Map）字段，User实例的map字段有一对KV（k1=v1, k2=v2），那么
     * 使用该方法将会返回如下map（prefix为user）：user.map.k1=v1, user.map.k2=v2
     *
     * @param pojo
     *            pojo
     * @param prefix
     *            对象在map中的前缀，允许为null
     * @param map
     *            对象展开到的map，允许为null
     * @param hasNull
     *            是否包含null值，true表示包含
     * @return map，当pojo为null时返回空map
     */
    public static Map<String, Object> convertToPlaceholder(Object pojo, String prefix, Map<String, Object> map,
        boolean hasNull) {
        return convertToPlaceholder(pojo, prefix, map, hasNull, new HashSet<>());
    }

    /**
     * 将pojo的所有字段展开映射为map，例如类User中有一个map（类型Map）字段，User实例的map字段有一对KV（k1=v1, k2=v2），那么
     * 使用该方法将会返回如下map（prefix为user）：user.map.k1=v1, user.map.k2=v2
     *
     * @param pojo
     *            pojo
     * @param prefix
     *            对象在map中的前缀，允许为null
     * @param map
     *            对象展开到的map，允许为null
     * @param hasNull
     *            是否包含null值，true表示包含
     * @param set
     *            当前已经转换过的缓存，防止递归
     * @return map，当pojo为null时返回空map
     */
    private static Map<String, Object> convertToPlaceholder(Object pojo, String prefix, Map<String, Object> map,
        boolean hasNull, Set<Object> set) {
        Map<String, Object> resultMap = map;

        if (resultMap == null) {
            resultMap = new HashMap<>();
        }

        if (pojo == null) {
            if (hasNull) {
                resultMap.put(prefix, null);
            }
            return resultMap;
        }

        Class<?> pojoClass = pojo.getClass();
        LOGGER.debug("获取[{}]的字段映射", pojoClass);

        if (pojo instanceof Class) {
            resultMap.put(prefix, ((Class<?>)pojo).getName());
        } else if (JavaTypeUtil.isSimple(pojoClass)) {
            resultMap.put(prefix, pojo);
        } else if (File.class.equals(pojoClass)) {
            File file = (File)pojo;
            resultMap.put(prefix, file.getAbsolutePath());
        } else if (Date.class.equals(pojoClass)) {
            resultMap.put(prefix + StringConst.DOT + DateUtil.BASE, DateUtil.getFormatDate((Date)pojo, DateUtil.BASE));
            resultMap.put(prefix + StringConst.DOT + DateUtil.SHORT,
                DateUtil.getFormatDate((Date)pojo, DateUtil.SHORT));
            resultMap.put(prefix + StringConst.DOT + DateUtil.TIME, DateUtil.getFormatDate((Date)pojo, DateUtil.TIME));
        } else if (Map.class.isAssignableFrom(pojoClass)) {
            @SuppressWarnings("unchecked")
            Map<Object, Object> pojoMap = (Map<Object, Object>)pojo;
            Map<String, Object> finalMap = resultMap;
            pojoMap.forEach((k, v) -> {
                if (k == null) {
                    throw new CommonException(ErrorCodeEnum.UNKNOWN_EXCEPTION, "不支持null key");
                }
                convertToPlaceholder(v, StringUtils.isBlank(prefix) ? k.toString() : prefix + StringConst.DOT + k,
                    finalMap, hasNull, set);
            });
        } else if (Collection.class.isAssignableFrom(pojoClass)) {
            Collection<?> collection = (Collection<?>)pojo;
            int index = 0;
            for (Object o : collection) {
                String name = String.format("[%d]", index++);
                convertToPlaceholder(o, StringUtils.isBlank(prefix) ? name : prefix + StringConst.DOT + name, resultMap,
                    hasNull, set);
            }
        } else if (pojoClass.getName().startsWith("java.") || pojoClass.getName().startsWith("javax.")
            || pojoClass.getName().startsWith("sun.")) {
            // 其他未知系统类，不进行处理
            LOGGER.debug("类型[{}]无法处理", pojoClass);
        } else {
            LOGGER.debug("[{}]判定是pojo类，获取字段", pojoClass);

            // 防止递归
            if (set.add(pojo)) {
                PropertyEditor[] propertyDescriptors = getPropertyDescriptors(pojoClass);
                for (PropertyEditor propertyDescriptor : propertyDescriptors) {
                    // 跳过静态字段和transient字段
                    Field original = propertyDescriptor.original();
                    if (AccessorUtil.isStatic(original) || AccessorUtil.isTransient(original)) {
                        continue;
                    }

                    String name = null;
                    try {
                        Object value = propertyDescriptor.read(pojo);

                        Alias alias = propertyDescriptor.getAnnotation(Alias.class);
                        name = (alias == null || StringUtils.isBlank(alias.value())) ? propertyDescriptor.name()
                            : alias.value();
                        name = StringUtils.isBlank(prefix) ? name : prefix + StringConst.DOT + name;

                        convertToPlaceholder(value, name, resultMap, hasNull, set);
                    } catch (Throwable e) {
                        LOGGER.debug(e, StringUtils.format("字段[{}]的值处理失败，忽略字段[{}]", name, name));
                    }
                }

                set.remove(pojo);
            }
        }

        return resultMap;
    }

    /**
     * 将pojo的所有字段映射为map，默认包含null值
     *
     * @param pojo
     *            pojo
     * @param <T>
     *            value的实际类型，注意，除非你知道value是哪个确定的类型，否则请使用Object类型
     * @return map，当pojo为null时返回空map
     */
    public static <T> Map<String, T> convert(Object pojo) {
        return convert(pojo, true);
    }

    /**
     * 将pojo的所有字段映射为map
     *
     * @param pojo
     *            pojo
     * @param hasNull
     *            是否包含null值，true表示包含
     * @param <T>
     *            value的实际类型，注意，除非你知道value是哪个确定的类型，否则请使用Object类型
     * @return map，当pojo为null时返回空map
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> convert(Object pojo, boolean hasNull) {
        LOGGER.debug("获取[{}]的字段映射", pojo);
        Assert.argNotNull(pojo, "pojo");

        Field[] fields = getAllFields(pojo.getClass());
        Map<String, T> map = new HashMap<>();
        if (fields.length == 0) {
            return Collections.emptyMap();
        }
        for (Field field : fields) {
            LOGGER.debug("获取字段[{}]的值", field);
            Alias alias = field.getDeclaredAnnotation(Alias.class);

            String name = (alias == null || StringUtils.isBlank(alias.value())) ? field.getName() : alias.value();

            try {
                Object value = getFieldValue(pojo, field.getName());
                if (value == null && !hasNull) {
                    LOGGER.debug("字段[{}]值为null，当前不包含null值，忽略字段[{}]", name, name);
                    continue;
                }
                map.put(name, (T)value);
            } catch (Exception e) {
                LOGGER.debug(e, StringUtils.format("获取字段[{}]值失败，忽略字段[{}]", name, name));
            }
        }
        return map;
    }

    /**
     * 为对象的属性注入指定值，优先使用set方法
     *
     * @param obj
     *            指定对象
     * @param propName
     *            属性名称
     * @param value
     *            要注入的属性值
     * @return 如果注入成功则返回<code>true</code>，注入失败或者不存在则返回<code>false</code>
     */
    public static boolean setProperty(Object obj, String propName, Object value) {
        try {
            Field field = getField(obj, propName, true);
            if (field == null) {
                return false;
            }

            PropertyEditor propertyEditor =
                buildPropertyEditor(field, obj instanceof Class ? (Class<?>)obj : obj.getClass());

            propertyEditor.write(obj, value);
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    /**
     * 获取对象指定字段的值，优先使用get方法
     *
     * @param obj
     *            指定对象
     * @param propName
     *            要获取的字段的名称
     * @param <T>
     *            字段的类型
     * @return 该字段的值，获取失败时抛出异常
     */

    public static <T> T getProperty(Object obj, String propName) {
        Field field = getField(obj, propName, true);

        PropertyEditor propertyEditor =
            buildPropertyEditor(field, obj instanceof Class ? (Class<?>)obj : obj.getClass());

        return propertyEditor.read(obj);
    }

    /**
     * 将source中与targetClass同名的字段从source中复制到targetClass的实例中，source中的{@link Alias Alias}注解将会生效，需要注
     * 意的是source中的Alias注解不要对应dest中的多个字段，否则会发生不可预测错误
     *
     * @param targetClass
     *            要复制的目标对象的class对象
     * @param source
     *            被复制的源对象
     * @param <E>
     *            目标对象的实际类型
     * @return targetClass的实例，如果该class不能实例化将会抛出异常
     */
    public static <E> E copyFromObjToClass(Class<E> targetClass, Object source) {
        if (source == null || targetClass == null) {
            return null;
        }
        E target;
        try {
            // 没有权限访问该类或者该类（为接口、抽象类）不能实例化时将抛出异常
            target = targetClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CommonException(ErrorCodeEnum.REFLECT_SECURE_EXCEPTION,
                StringUtils.format("target[{}]生成失败，请检查代码", targetClass), e);
        }

        return copyFromObjToObj(target, source);
    }

    /**
     * 将sources中所有与dest同名的字段的值复制到dest中，如果dest中包含字段A，同时sources中多个对象都包含字段A，那么将
     * 以sources中最后一个包含字段A的对象的值为准，source中的{@link Alias Alias}注解将会生效，需要注意的是source中的Alias注解 不要对应dest中的多个字段，否则会发生不可预测错误
     *
     * @param dest
     *            目标
     * @param sources
     *            源
     * @param <E>
     *            目标对象的实际类型
     * @return 复制后的目标对象
     */
    public static <E> E copyFromMultiObjToObj(E dest, Object... sources) {
        if (dest == null || sources == null || sources.length == 0) {
            return dest;
        }

        for (Object obj : sources) {
            if (dest == obj) {
                continue;
            }
            copyFromObjToObj(dest, obj);
        }

        return dest;
    }

    /**
     * 将source中与dest的同名字段的值复制到dest中，source中的{@link Alias Alias}注解将会生效，需要注意的是source中的Alias注解 不要对应dest中的多个字段，否则会发生不可预测错误
     *
     * @param dest
     *            目标
     * @param source
     *            源
     * @param <E>
     *            目标对象的实际类型
     * @return 复制后的目标对象，如果的dest或者source有一个为null则直接返回dest，如果dest和source是同一个对象也直接返回dest
     */
    public static <E> E copyFromObjToObj(E dest, Object source) {
        if (dest == null || source == null) {
            return dest;
        }
        if (dest == source) {
            return dest;
        }

        if (source instanceof Map) {
            ((Map<?, ?>)source).forEach((key, value) -> {
                if (key instanceof String && value != null) {
                    try {
                        Field field = ReflectUtil.getField(dest, (String)key);
                        // 如果value是字段类型的子类型或者跟字段类型相同，那么设置为字段值
                        Object injectValue = value;

                        // 如果字段类型和注入值类型不一致，则尝试转换
                        if (!field.getType().isAssignableFrom(injectValue.getClass())) {
                            TypeConverter use = TypeConverterRegistry.findConverter(value.getClass(), field.getType());

                            if (use != null) {
                                injectValue = use.convert(value, field.getType());
                            }
                        }

                        // 如果此时仍然类型不一致，则停止该字段的注入
                        if (!field.getType().isAssignableFrom(injectValue.getClass())) {
                            return;
                        }

                        // 这里要使用字段属性访问器去写入，优先调用set方法，不存在set方法再直接反射注入
                        PropertyEditor propertyEditor = buildPropertyEditor(field, dest.getClass());
                        propertyEditor.write(dest, injectValue);
                    } catch (Throwable throwable) {
                        // 字段不存在或者其他异常，应该无法注入了，忽略
                    }
                }
            });

            return dest;
        }

        Class<?> sourceClass = source.getClass();
        String sourceName = sourceClass.getName();

        Field[] srcFields = getAllFields(sourceClass);

        if (srcFields.length == 0) {
            LOGGER.debug("源{}中不存在已经声明的字段", sourceName);
            return dest;
        }

        for (Field srcField : srcFields) {
            String fieldName = srcField.getName();

            // 要注入的数据
            Object srcData = getFieldValue(source, srcField);
            if (srcData == null) {
                continue;
            }

            Field targetField = getField(dest, fieldName, true, false);
            Alias alias;
            if (targetField == null && (alias = srcField.getAnnotation(Alias.class)) != null) {
                targetField = getField(dest, alias.value(), true, false);
            }

            // 目标字段不存在
            if (targetField == null) {
                continue;
            }

            Class<?> targetType = targetField.getType();

            // 如果源数据不是目标字段的字类，并且目标数据类型不是原始类型（装箱拆箱太麻烦，这里不处理），则尝试转换；
            if (!targetType.isAssignableFrom(srcData.getClass()) && !JavaTypeUtil.isGeneralType(targetType)) {
                TypeConverter converter = TypeConverterRegistry.findConverter(srcData.getClass(), targetType);
                // 找不到转换器，该字段无法注入
                if (converter == null) {
                    continue;
                }
                // 将源数据转换为目标数据类型
                srcData = converter.convert(srcData, srcField.getAnnotations(), targetType);
            }

            try {
                // 这里使用PropertyEditor的方式写入字段值，也就是优先调用set方法
                PropertyEditor propertyEditor =
                    buildPropertyEditor(targetField, dest instanceof Class ? (Class<?>)dest : dest.getClass());
                propertyEditor.write(dest, srcData);
            } catch (Throwable throwable) {
                // 忽略异常
            }

        }

        return dest;
    }

    /**
     * 将sourceList中的对象与targetClass同名的字段从source中复制到targetClass的实例中，使用前请对参数进行非空校验
     *
     * @param targetClass
     *            要复制的目标对象的class对象
     * @param sourceList
     *            被复制的源对象的数组
     * @param <S>
     *            数组中数据的实际类型
     * @param <E>
     *            目标对象的实际类型
     * @return targetClass的实例的数组
     */
    public static <E, S> List<E> copyFromMultiObjToClass(Class<E> targetClass, List<S> sourceList) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }
        List<E> list = new ArrayList<>(sourceList.size());

        for (S source : sourceList) {
            E e = copyFromObjToClass(targetClass, source);
            if (e != null) {
                list.add(e);
            }
        }
        return list;
    }

    /**
     * 获取指定Class的所有字段的编辑器
     *
     * @param clazz
     *            指定的class，不能为空
     * @return 指定class的所有字段的编辑器
     */
    public static PropertyEditor[] getPropertyDescriptors(Class<?> clazz) {
        if (clazz == null) {
            throw new CommonException(ErrorCodeEnum.CODE_ERROR, "clazz为null");
        }

        Field[] fields = getAllFields(clazz);
        PropertyEditor[] descriptors = new PropertyEditor[fields.length];

        if (fields.length == 0) {
            return descriptors;
        }

        int j = 0;
        for (Field field : fields) {
            PropertyEditor descriptor = buildPropertyEditor(field, clazz);
            descriptors[j++] = descriptor;
        }

        return descriptors;
    }

    /**
     * 构建字段编辑器
     * 
     * @param field
     *            字段
     * @param clazz
     *            字段所属class
     * @return 字段编辑器
     */
    public static PropertyEditor buildPropertyEditor(Field field, Class<?> clazz) {
        String fieldName = field.getName();

        // 首字母大写
        fieldName = StringUtils.toFirstUpperCase(fieldName);

        // 尝试获取读取方法
        Method readMethod = getMethod("get" + fieldName, clazz);

        if (readMethod == null) {
            readMethod = getMethod("is" + fieldName, clazz);
        }

        Method writeMethod = getMethod("set" + fieldName, clazz, field.getType());

        // boolean类型的字段特殊处理下，有可能boolean字段就叫isTrue，这时可能生成的set方法是setTrue，is会被删除
        if (writeMethod == null && fieldName.startsWith("Is")
            && (field.getType().equals(boolean.class) || field.getType().equals(Boolean.class))) {
            writeMethod = getMethod("set" + fieldName.substring(2), clazz, field.getType());
        }

        return new SimplePropertyEditor(field, clazz, readMethod, writeMethod);
    }

    /**
     * 获取指定方法
     *
     * @param methodName
     *            方法名
     * @param clazz
     *            Class
     * @param args
     *            方法的参数类型
     * @return 方法名对应的方法，如果获取不到返回null
     */
    private static Method getMethod(String methodName, Class<?> clazz, Class<?>... args) {
        Method method;
        try {
            method = clazz.getMethod(methodName, args);
        } catch (NoSuchMethodException e1) {
            method = null;
        }
        return method;
    }

    public final static class SimplePropertyEditor implements PropertyEditor {

        /**
         * 原始字段
         */
        private final Field field;

        /**
         * 字段所属类
         */
        private final Class<?> owner;

        /**
         * 读取方法
         */
        private final Method readMethod;

        /**
         * 写入方法
         */
        private final Method writeMethod;

        private SimplePropertyEditor(Field field, Class<?> owner, Method readMethod, Method writeMethod) {
            Assert.argNotNull(field, "field");
            Assert.argNotNull(owner, "owner");

            this.field = field;
            this.owner = owner;
            this.readMethod = readMethod;
            this.writeMethod = writeMethod;

            ReflectUtil.allowAccess(field);

            if (readMethod != null) {
                ReflectUtil.allowAccess(readMethod);
            }

            if (writeMethod != null) {
                ReflectUtil.allowAccess(writeMethod);
            }
        }

        @Override
        public boolean hasWriteMethod() {
            return writeMethod != null;
        }

        @Override
        public boolean hasReadMethod() {
            return readMethod != null;
        }

        @Override
        public Field original() {
            return field;
        }

        @Override
        public String name() {
            return field.getName();
        }

        @Override
        public Class<?> type() {
            return field.getType();
        }

        @Override
        public Type getGenericType() {
            return field.getGenericType();
        }

        @Override
        public Class<?> owner() {
            return owner;
        }

        @Override
        public void write(Object target, Object value) {
            if (writeMethod != null) {
                ReflectUtil.invoke(target, writeMethod, value);
            } else {
                ReflectUtil.setFieldValue(target, field, value);
            }
        }

        @Override
        public <T> T read(Object target) {
            if (readMethod != null) {
                return ReflectUtil.invoke(target, readMethod);
            } else {
                return ReflectUtil.getFieldValue(target, field);
            }
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return field.getAnnotation(annotationClass);
        }
    }
}
