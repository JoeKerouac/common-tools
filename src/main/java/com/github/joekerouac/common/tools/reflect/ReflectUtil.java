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

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

import com.github.joekerouac.common.tools.enums.EnumInterface;
import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.constant.StringConst;
import com.github.joekerouac.common.tools.exception.BaseException;
import com.github.joekerouac.common.tools.exception.CommonException;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 反射工具，注意：本类设计上并无缓存，所以外部如果需要频繁访问某个字段、方法或者构造器，请自行缓存，否则会导致性能较低，同时内存可能也会增大，因为 每次反射获取到的字段、方法、构造器都是全新（即使是获取的同一个字段、方法、构造器）；
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectUtil {

    /**
     * 获取指定Class的指定参数构造器
     * 
     * @param type
     *            指定Class
     * @param parameterTypes
     *            构造器参数列表
     * @param <T>
     *            构造器类型
     * @return 构造器
     */
    public static <T> Constructor<T> getConstructor(Class<T> type, Class<?>... parameterTypes) {
        try {
            return type.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new CommonException(ErrorCodeEnum.CODE_ERROR, StringUtils.format("类[{}]中不存在参数列表为[{}]的构造器", type,
                parameterTypes == null ? StringConst.NULL : Arrays.toString(parameterTypes)), e);
        }
    }

    /**
     * 调用指定对象的指定无参数方法
     * 
     * @param obj
     *            指定对象，不能为空，如果要调用的方法为静态方法那么传入Class对象
     * @param methodName
     *            要调用的方法名，不能为空
     * @param <R>
     *            结果类型
     * @return 调用结果
     */
    public static <R> R invoke(Object obj, String methodName) {
        return invoke(obj, methodName, null);
    }

    /**
     * 调用指定对象的指定方法
     * 
     * @param obj
     *            指定对象，不能为空，如果要调用的方法为静态方法那么传入Class对象
     * @param methodName
     *            要调用的方法名，不能为空
     * @param parameterTypes
     *            方法参数类型，方法没有参数请传null
     * @param args
     *            参数
     * @param <R>
     *            结果类型
     * @return 调用结果
     */
    public static <R> R invoke(Object obj, String methodName, Class<?>[] parameterTypes, Object... args) {
        Assert.assertTrue(CollectionUtil.size(parameterTypes) == CollectionUtil.size(args), "方法参数类型列表必须和方法参数列表一致",
            ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Method method;
        if (obj instanceof Class) {
            method = getMethod((Class<?>)obj, methodName, true, parameterTypes);
        } else {
            method = getMethod(obj.getClass(), methodName, true, parameterTypes);
        }
        return invoke(obj, method, args);
    }

    /**
     * 调用指定对象的指定方法
     * 
     * @param obj
     *            指定对象，不能为空，如果要调用的方法为静态方法那么传入Class对象
     * @param method
     *            要调用的方法
     * @param args
     *            参数
     * @param <R>
     *            结果类型
     * @return 调用结果
     */
    @SuppressWarnings("unchecked")
    public static <R> R invoke(Object obj, Method method, Object... args) {
        try {
            if (obj instanceof Class) {
                return (R)method.invoke(null, args);
            } else {
                return (R)method.invoke(obj, args);
            }
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new CommonException(ErrorCodeEnum.REFLECT_SECURE_EXCEPTION,
                StringUtils.format("当前没有权限调用方法或者方法参数错误，方法：[{}],参数:[{}]", method, args), e);
        } catch (InvocationTargetException e) {
            Throwable realEx = e.getTargetException();
            EnumInterface code = ErrorCodeEnum.UNKNOWN_EXCEPTION;

            if (realEx instanceof BaseException) {
                code = ((BaseException)realEx).getErrCode();
            }

            throw new CommonException(code, StringUtils.format("方法执行异常，调用的方法：[{}].参数：[{}]", method, args),
                e.getTargetException());
        }
    }

    /**
     * 获取指定类型和其父类型、接口中声明的所有非抽象方法，如果对于方法M，类A中对方法M实现了M1，类B中 对方法M实现了M2，类A继承了类B，传入参数为类A，那么返回的列表中将包含M1而不包含M2
     * 
     * @param clazz
     *            指定类型
     * @return 指定类型和其父类型、接口中声明的所有方法（除了Object中声明的方法）
     */
    public static List<Method> getAllMethod(Class<?> clazz) {
        Map<String, Method> methodMap = getAllMethod(clazz, new HashMap<>());
        return new ArrayList<>(methodMap.values());
    }

    /**
     * 获取指定类的所有方法（包含父类方法）
     *
     * @param clazz
     *            Class类型
     * @param methods
     *            方法集合
     * @return 方法集合
     */
    private static Map<String, Method> getAllMethod(Class<?> clazz, Map<String, Method> methods) {
        if (clazz == null) {
            return methods;
        }

        Arrays.stream(clazz.getDeclaredMethods()).forEach(method -> {
            if (AccessorUtil.isPrivate(method) || AccessorUtil.isFinal(method) || AccessorUtil.isStatic(method)) {
                // 私有方法、final方法、static方法都是子类没办法继承覆写的，所以直接加入就行,同时因为私有方法、静态方法父类与子类可以存在同
                // 名、同参的方法的情况，所以需要用method.toGenericString()作为key而不是下边那种形式
                methods.put(method.toGenericString(), method);
            } else {
                String key = String.format("%s:%s", method.getName(), Arrays.toString(method.getParameterTypes()));

                // 如果已经存在，那么说明子类对该方法进行了继承覆写，就不用放进去了
                methods.putIfAbsent(key, method);
            }

        });

        Arrays.stream(clazz.getInterfaces()).forEach(c -> getAllMethod(c, methods));

        return getAllMethod(clazz.getSuperclass(), methods);
    }

    /**
     * 获取指定类型中指定的方法（无法获取本类未覆写过的父类方法）
     * 
     * @param clazz
     *            类型
     * @param methodName
     *            方法名
     * @param parameterTypes
     *            方法参数类型
     * @return 指定方法，获取不到时会抛出异常
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return getMethod(clazz, methodName, false, parameterTypes);
    }

    /**
     * 获取指定类型中指定的方法
     * 
     * @param clazz
     *            类型
     * @param methodName
     *            方法名
     * @param recursive
     *            是否递归查找父类方法，true表示递归查询父类方法
     * @param parameterTypes
     *            方法参数类型
     * @return 指定方法，获取不到时会抛出异常
     */
    public static Method getMethod(Class<?> clazz, String methodName, boolean recursive, Class<?>... parameterTypes) {
        Method method = getMethodIfExist(clazz, methodName, recursive, parameterTypes);

        if (method == null) {
            throw new CommonException(ErrorCodeEnum.CODE_ERROR, StringUtils.format("类[{}]中不存在方法名为[{}]、方法列表为[{}]的方法",
                clazz, methodName, parameterTypes == null ? StringConst.NULL : Arrays.toString(parameterTypes)));
        }
        return method;
    }

    /**
     * 执行方法
     * 
     * @param method
     *            要调用的方法
     * @param target
     *            方法所在Class的实例，对于静态方法要传入null或者Class对象
     * @param params
     *            调用方法的参数
     * @param <T>
     *            方法返回类型
     * @return 方法调用结果
     */
    @SuppressWarnings("unchecked")
    public static <T> T execMethod(Method method, Object target, Object... params) {
        allowAccess(method);
        try {
            return (T)method.invoke(target, params);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new CommonException(ErrorCodeEnum.REFLECT_SECURE_EXCEPTION, e);
        } catch (InvocationTargetException e) {
            throw new CommonException(ErrorCodeEnum.REFLECT_SECURE_EXCEPTION, e.getTargetException());
        }
    }

    /**
     * 获取指定类型内所有带有指定注解的方法的集合（包含父类里的方法，如果父类中的方法带有指定注解，但是子类覆写后没有那么该方法不会被添加）
     *
     * @param type
     *            指定类型
     * @param annotation
     *            指定注解
     * @return 带有指定注解的方法集合
     */
    public static List<Method> getAllAnnotationPresentMethod(Class<?> type, Class<? extends Annotation> annotation) {
        List<Method> methods = getAllMethod(type);

        if (methods.isEmpty()) {
            return Collections.emptyList();
        }

        return methods.stream().filter(method -> method.isAnnotationPresent(annotation)).collect(Collectors.toList());
    }

    /**
     * 获取指定对象中指定字段名对应的字段的值
     *
     * @param obj
     *            对象，如果要获取的字段是静态字段那么需要传入Class
     * @param fieldName
     *            字段名
     * @param <T>
     *            字段类型
     * @return 指定对象中指定字段名对应字段的值，字段不存在时抛异常
     */
    public static <T> T getFieldValue(Object obj, String fieldName) {
        Field field = getField(obj, fieldName, true);
        return getFieldValue(obj, field);
    }

    /**
     * 获取指定对象中指定字段对应的字段的值
     * 
     * @param obj
     *            对象，不能为空
     * @param field
     *            字段
     * @param <T>
     *            字段类型
     * @return 字段值
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object obj, Field field) {
        Assert.argNotNull(obj, "obj");
        Assert.argNotNull(field, "field");

        try {
            return (T)field.get(obj);
        } catch (IllegalArgumentException e) {
            throw new CommonException(ErrorCodeEnum.CODE_ERROR, e);
        } catch (IllegalAccessException e) {
            String msg = StringUtils.format("类型[{}]的字段[{}]不允许访问", obj.getClass(), field.getName());
            throw new CommonException(ErrorCodeEnum.REFLECT_SECURE_EXCEPTION, msg, e);
        }
    }

    /**
     * 设置指定对象指定对象名的字段值
     * 
     * @param obj
     *            对象，如果要设置的字段是静态字段那么请传入静态字段所在的Class对象
     * @param fieldName
     *            字段名
     * @param fieldValue
     *            字段值
     * @param <T>
     *            字段值的类型
     */
    public static <T> void setFieldValue(Object obj, String fieldName, T fieldValue) {
        Field field = getField(obj, fieldName, true);

        setFieldValue(obj, field, fieldValue);
    }

    /**
     * 设置field值
     * 
     * @param obj
     *            对象
     * @param field
     *            对象的字段
     * @param fieldValue
     *            字段值
     * @param <T>
     *            字段值泛型
     */
    public static <T> void setFieldValue(Object obj, Field field, T fieldValue) {
        Assert.argNotNull(obj, "obj");
        Assert.argNotNull(field, "field");

        try {
            field.set(obj, fieldValue);
        } catch (IllegalAccessException e) {
            String msg = StringUtils.format("类型[{}]的字段[{}]不允许设置", obj.getClass(), field.getName());
            throw new CommonException(ErrorCodeEnum.REFLECT_SECURE_EXCEPTION, msg, e);
        }
    }

    /**
     * 从指定Class中获取指定Field，并尝试将其accessible属性设置为true（能获取到父类声明的字段）
     * 
     * @param obj
     *            字段所属的对象或者class
     * @param fieldName
     *            fieldName
     * @return Field，不会为null，只会抛出异常
     */
    public static Field getField(Object obj, String fieldName) {
        return getField(obj, fieldName, true);
    }

    /**
     * 从指定Class中获取指定Field，并尝试将其accessible属性设置为true
     * 
     * @param obj
     *            字段所属的对象或者class
     * @param fieldName
     *            字段名
     * @param isRecursive
     *            是否递归获取父类中的字段，为true时表示当前类查找不到指定字段时允许递归从父类获取
     * @return 要获取的Field，不存在时抛出异常
     */
    public static Field getField(Object obj, String fieldName, boolean isRecursive) {
        return getField(obj, fieldName, isRecursive, true);
    }

    /**
     * 从指定Class中获取指定Field，并尝试将其accessible属性设置为true
     *
     * @param obj
     *            字段所属的对象或者class
     * @param fieldName
     *            字段名
     * @param isRecursive
     *            是否递归获取父类中的字段，为true时表示当前类查找不到指定字段时允许递归从父类获取
     * @param throwIfAbsent
     *            如果字段不存在是否抛出异常，true表示抛出异常，false表示不抛异常返回null
     * @return 要获取的Field，不存在时返回null
     */
    public static Field getField(Object obj, String fieldName, boolean isRecursive, boolean throwIfAbsent) {
        Assert.argNotNull(obj, "obj");
        Assert.argNotNull(fieldName, "fieldName");

        Class<?> clazz;
        if (obj instanceof Class) {
            clazz = (Class<?>)obj;
        } else {
            clazz = obj.getClass();
        }

        try {
            return allowAccess(clazz.getDeclaredField(fieldName));
        } catch (NoSuchFieldException e) {
            Class<?> superClass = clazz.getSuperclass();
            // 判断父类是否是Object
            if (superClass.equals(Object.class) || !isRecursive) {
                if (throwIfAbsent) {
                    throw new CommonException(ErrorCodeEnum.REFLECT_SECURE_EXCEPTION,
                        StringUtils.format("[{}]中不存在字段[{}]", obj, fieldName));
                } else {
                    return null;
                }
            } else {
                return getField(superClass, fieldName, isRecursive, throwIfAbsent);
            }
        }
    }

    /**
     * 获取指定Class的所有field（包含父类声明的字段）
     * 
     * @param clazz
     *            Class
     * @return 所有field数组
     */
    public static Field[] getAllFields(Class<?> clazz) {
        Assert.argNotNull(clazz, "clazz");

        List<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));

        // 查找是否存在父类，如果存在且不是Object那么查找父类的field
        Class<?> superClass = clazz.getSuperclass();

        // 遍历设置访问权限，同时加入单个field的缓存
        fields.forEach(ReflectUtil::allowAccess);

        if (superClass != null && superClass != Object.class) {
            fields.addAll(Arrays.asList(getAllFields(superClass)));
        }
        return fields.toArray(new Field[0]);
    }

    /**
     * 更改AccessibleObject的访问权限
     * 
     * @param object
     *            AccessibleObject
     * @param <T>
     *            AccessibleObject的具体类型
     * @return AccessibleObject
     */
    public static <T extends AccessibleObject> T allowAccess(T object) {
        try {
            object.setAccessible(true);
        } catch (SecurityException e) {
            throw new CommonException(ErrorCodeEnum.REFLECT_SECURE_EXCEPTION,
                StringUtils.format("无法更改[{}]的访问权限", object), e);
        }
        return object;
    }

    /**
     * 获取指定类型中指定的方法，如果不存在则返回null而不是抛出异常
     * 
     * @param clazz
     *            类型
     * @param methodName
     *            方法名
     * @param recursive
     *            是否递归查找父类方法，true表示递归查询父类方法
     * @param parameterTypes
     *            方法参数类型
     * @return 指定方法，获取不到时返回null
     */
    private static Method getMethodIfExist(Class<?> clazz, String methodName, boolean recursive,
        Class<?>... parameterTypes) {
        Assert.argNotNull(clazz, "clazz");
        Assert.argNotBlank(methodName, "methodName");

        // 优先查找本类
        Method method = null;
        try {
            return allowAccess(clazz.getDeclaredMethod(methodName, parameterTypes));
        } catch (NoSuchMethodException e) {
            // 如果不允许递归，直接抛出异常
            if (!recursive) {
                throw new CommonException(ErrorCodeEnum.CODE_ERROR, StringUtils.format("类[{}]中不存在方法名为[{}]、方法列表为[{}]的方法",
                    clazz, methodName, parameterTypes == null ? StringConst.NULL : Arrays.toString(parameterTypes)), e);
            }
        }

        // 本类不存在并且允许递归，递归查找父类
        if (clazz.getSuperclass() != null) {
            method = getMethodIfExist(clazz.getSuperclass(), methodName, true, parameterTypes);
        }

        if (method != null) {
            return method;
        }

        // 如果父类不存在或者父类中没有该方法，递归查找接口
        for (Class<?> anInterface : clazz.getInterfaces()) {
            method = getMethodIfExist(anInterface, methodName, true, parameterTypes);
            if (method != null) {
                return method;
            }
        }

        // 最后，如果都不存在，返回null
        return null;
    }

}
