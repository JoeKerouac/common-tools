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

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.exception.CommonException;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.reflect.type.JavaTypeUtil;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * class工具
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassUtils {

    /**
     * 到object后默认深度+100
     */
    private static final int OBJECT_DEEP = 100;

    /**
     * lambda表达式类名中的标识符
     */
    private static final String LAMBDA_ID = "$$Lambda$";

    /** The package separator character: '.' */
    private static final char PACKAGE_SEPARATOR = '.';

    /** The ".class" file suffix */
    private static final String CLASS_FILE_SUFFIX = ".class";

    /**
     * 常用的集合类，注意，有先后顺序，先加入的优先级较高
     */
    private static final List<Class<?>> IMPL_CLASSES = new ArrayList<>();

    static {
        // list的几个常用实现
        IMPL_CLASSES.add(ArrayList.class);
        IMPL_CLASSES.add(CopyOnWriteArrayList.class);
        // set的几个常用实现
        IMPL_CLASSES.add(HashSet.class);
        IMPL_CLASSES.add(TreeSet.class);
        IMPL_CLASSES.add(ConcurrentSkipListSet.class);
        // map的几个常用实现
        IMPL_CLASSES.add(HashMap.class);
        IMPL_CLASSES.add(TreeMap.class);
        IMPL_CLASSES.add(ConcurrentHashMap.class);
    }

    /**
     * 构建指定子类到其父类的继承链
     * 
     * @param child
     *            子类
     * @param parent
     *            父类
     * @param chain
     *            存放继承链的list
     * @param <T>
     *            当前继承链的实际类型
     * @return 构造好的继承链，第一个是子类，最后一个是父类
     */
    public static <T extends List<Class<?>>> T buildChain(Class<?> child, Class<?> parent, T chain) {
        chain.add(child);
        if (child.equals(parent)) {
            return chain;
        } else if (child.equals(Object.class)) {
            throw new CommonException(ErrorCodeEnum.CODE_ERROR,
                StringUtils.format("当前类型[{}]不是类型[{}]的子类", child, parent));
        } else {
            return buildChain(child.getSuperclass(), parent, chain);
        }
    }

    /**
     * 获取指定类的路径，如果同一个class两个jar包都提供了，该方法可以返回class以及所在的jar包的路径，主要用于分析一些极端问题
     * 
     * @param cls
     *            类
     * @return 该类的路径URL，获取失败返回null，注意，该URL可能不能调用{@link URL#openStream()}，例如如果类是lambda表达式的情况
     */
    public static URL where(final Class<?> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("null input: cls");
        }

        URL result = null;
        final String clsAsResource = cls.getName().replace('.', '/').concat(".class");
        final ProtectionDomain pd = cls.getProtectionDomain();
        if (pd != null) {
            final CodeSource cs = pd.getCodeSource();
            if (cs != null) {
                result = cs.getLocation();
            }

            if (result != null) {
                if ("file".equals(result.getProtocol())) {
                    try {
                        if (result.toExternalForm().endsWith(".jar") || result.toExternalForm().endsWith(".zip")) {
                            result = new URL("jar:".concat(result.toExternalForm()).concat("!/").concat(clsAsResource));
                        } else if (new File(result.getFile()).isDirectory()) {
                            result = new URL(result, clsAsResource);
                        }
                    } catch (MalformedURLException ignore) {
                    }
                }
            }
        }
        if (result == null) {
            final ClassLoader clsLoader = cls.getClassLoader();
            result =
                clsLoader != null ? clsLoader.getResource(clsAsResource) : ClassLoader.getSystemResource(clsAsResource);
        }

        return result;
    }

    /**
     * 获取指定class的class文件的输入流
     * 
     * @param clazz
     *            class
     * @return 对应的输入流
     */
    public static InputStream getClassAsStream(Class<?> clazz) {
        Assert.argNotNull(clazz, "clazz");
        return clazz.getResourceAsStream(getClassFileName(clazz));
    }

    /**
     * 获取class的class文件名（不包含包名，例如：String.class）
     * 
     * @param clazz
     *            the class
     * @return .class文件名
     */
    public static String getClassFileName(Class<?> clazz) {
        Assert.argNotNull(clazz, "clazz");
        String className = clazz.getName();
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        return className.substring(lastDotIndex + 1) + CLASS_FILE_SUFFIX;
    }

    /**
     * 获取默认classloader
     *
     * @return 当前默认classloader（先从当前线程上下文获取，获取不到获取加载该类的ClassLoader，还获取不到就获取系统classloader）
     */
    public static ClassLoader getDefaultClassLoader() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ClassUtils.class.getClassLoader();
            if (loader == null) {
                loader = ClassLoader.getSystemClassLoader();
            }
        }
        return loader;
    }

    /**
     * 使用给定的ClassLoader重新加载class
     *
     * @param clazz
     *            class，不能为空
     * @param loader
     *            重加加载class的ClassLoader
     * @return 有可能还是同一个class
     */
    public static Class<?> reloadClass(Class<?> clazz, ClassLoader loader) {
        Assert.argNotNull(clazz, "clazz");
        return loadClass(clazz.getName(), loader);
    }

    /**
     * 使用默认ClassLoader加载class
     *
     * @param className
     *            class名字
     * @param <T>
     *            class实际类型
     * @return class
     */
    public static <T> Class<T> loadClass(String className) {
        return loadClass(className, getDefaultClassLoader());
    }

    /**
     * 使用指定ClassLoader加载class
     *
     * @param className
     *            class名字
     * @param loader
     *            加载class的ClassLoader
     * @param <T>
     *            class实际类型
     * @return class
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> loadClass(String className, ClassLoader loader) {
        return loadClass(className, loader, 0);
    }

    /**
     * 使用指定ClassLoader加载class
     *
     * @param className
     *            class名字
     * @param loader
     *            加载class的ClassLoader
     * @param arrLen
     *            数组长度，如果不是数组类型则为0
     * @param <T>
     *            class实际类型
     * @return class
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T> loadClass(String className, ClassLoader loader, int arrLen) {
        Assert.argNotBlank(className, "className");
        Assert.argNotNull(loader, "loader");
        Class<?> baseClass;
        if ("boolean".equals(className)) {
            baseClass = boolean.class;
        } else if ("byte".equals(className)) {
            baseClass = byte.class;
        } else if ("char".equals(className)) {
            baseClass = char.class;
        } else if ("short".equals(className)) {
            baseClass = short.class;
        } else if ("int".equals(className)) {
            baseClass = int.class;
        } else if ("long".equals(className)) {
            baseClass = long.class;
        } else if ("double".equals(className)) {
            baseClass = double.class;
        } else if ("float".equals(className)) {
            baseClass = float.class;
        } else if ("void".equals(className)) {
            baseClass = void.class;
        } else if (className.startsWith("[")) {
            // 注意，如果是数组这里实际上类名是签名
            return loadClass(JavaTypeUtil.signToClassName(className.substring(1)), loader, arrLen + 1);
        } else {
            try {
                baseClass = loader.loadClass(className);
            } catch (ClassNotFoundException e) {
                throw new CommonException(ErrorCodeEnum.CLASS_NOT_FOUND, e);
            }
        }

        if (arrLen > 0) {
            return (Class<T>)Array.newInstance(baseClass, new int[arrLen]).getClass();
        } else {
            return (Class<T>)baseClass;
        }
    }

    /**
     * 获取class实例
     *
     * @param className
     *            class名字
     * @param <T>
     *            class类型
     * @return class的实例
     */
    public static <T> T getInstance(String className) {
        return getInstance(className, getDefaultClassLoader());
    }

    /**
     * 获取class实例
     *
     * @param className
     *            class名字
     * @param loader
     *            加载class的classloader
     * @param <T>
     *            class类型
     * @return class的实例
     */
    public static <T> T getInstance(String className, ClassLoader loader) {
        return getInstance(loadClass(className, loader));
    }

    /**
     * 获取class的实例
     *
     * @param clazz
     *            class
     * @param <T>
     *            class类型
     * @return Class实例
     */
    public static <T> T getInstance(Class<T> clazz) {
        return getInstance(clazz, null, null);
    }

    /**
     * 获取class的实例
     *
     * @param clazz
     *            class
     * @param paramTypes
     *            构造器参数类型
     * @param params
     *            参数
     * @param <T>
     *            class类型
     * @return Class实例
     */
    @SuppressWarnings("unchecked")
    public static <T> T getInstance(Class<T> clazz, Class<?>[] paramTypes, Object[] params) {
        Assert.assertTrue(CollectionUtil.size(paramTypes) == CollectionUtil.size(params), "参数定义长度和实际长度必须一致",
            ExceptionProviderConst.IllegalArgumentExceptionProvider);

        try {
            if ((Map.class.isAssignableFrom(clazz) || Collection.class.isAssignableFrom(clazz))
                && AccessorUtil.isAbstract(clazz)) {

                for (Class<?> impl : IMPL_CLASSES) {
                    if (clazz.isAssignableFrom(impl)) {
                        return (T)getInstance(impl, paramTypes, params);
                    }
                }

            } else if (!AccessorUtil.isAbstract(clazz)) {
                Constructor<T> constructor = clazz.getConstructor(paramTypes);
                ReflectUtil.allowAccess(constructor);
                return constructor.newInstance(params);
            }
        } catch (Exception e) {
            throw new CommonException(ErrorCodeEnum.UNKNOWN_EXCEPTION,
                String.format("获取类[%s]实例异常，可能是没有默认无参构造器", clazz.getName()), e);
        }

        throw new CommonException(ErrorCodeEnum.UNKNOWN_EXCEPTION, StringUtils.format("[{}]无法实例化", clazz));
    }

    /**
     * 计算两个类之间的距离；两个类之间的距离定义：如果类A与类B相同，则距离为0，如果类A的父类是类B，则距离是1，如果类A的父类的父类是类B，则距离是2
     *
     * @param targetType
     *            目标类
     * @param srcType
     *            要计算的类，必须与目标类相同或者是目标类的子类
     * @return srcType到targetType的距离
     */
    public static int calcGap(Class<?> targetType, Class<?> srcType) {
        Assert.assertTrue(targetType.isAssignableFrom(srcType),
            StringUtils.format("类型 [{}] 不是类型 [{}] 的子类", srcType, targetType),
            ExceptionProviderConst.IllegalArgumentExceptionProvider);
        return calcGap(targetType, srcType, 0);
    }

    /**
     * 计算两个类之间的距离；两个类之间的距离定义：如果类A与类B相同，则距离为0，如果类A的父类是类B，则距离是1，如果类A的父类的父类是类B，则距离是2
     *
     * @param targetType
     *            目标类
     * @param srcType
     *            源类
     * @param nowGap
     *            当前计算的距离
     * @return srcType到targetType的距离
     */
    private static int calcGap(Class<?> targetType, Class<?> srcType, int nowGap) {
        // 这里加一个特殊逻辑，如果targetType是Object，那么最终深度要+100，基本也就是不选用Object的了，因为所有对象都能匹配上Object
        if (srcType.equals(Object.class)) {
            // 如果calcType到这里已经变为Object.class了，说明targetType肯定是Object.class，所以不用判断targetType了，直接返回就行
            return nowGap + OBJECT_DEEP;
        } else if (srcType.equals(targetType)) {
            return nowGap;
        } else if (targetType.isInterface()) {
            // 接口要特殊处理
            int min = Integer.MIN_VALUE;

            for (Class<?> anInterface : srcType.getInterfaces()) {
                // 先判断该接口是否是指定类型，
                if (anInterface.equals(targetType)) {
                    // 如果是，直接min = 1，退出循环
                    min = 1;
                    break;
                } else if (targetType.isAssignableFrom(anInterface)) {
                    // 如果不是，则判断接口是不是目标类的子类，如果是，则递归计算，然后选用上次选出来最小的，因为有可能存在这种情况，有一个接
                    // 口A，继承接口B，B继承接口C，C继承接口D，同时B也继承接口D，那么A到D就有两个gap，一个是3，一个是2，选用小的
                    min = Math.min(min, calcGap(targetType, anInterface, nowGap));
                }
            }

            return min + nowGap;
        } else {
            return calcGap(targetType, srcType.getSuperclass(), nowGap + 1);
        }
    }

    /**
     * 断言类不是lambda表达式
     * 
     * @param clazz
     *            类
     */
    private static void checkNotLambda(Class<?> clazz) {
        if (clazz.getName().contains(LAMBDA_ID)) {
            throw new CommonException(ErrorCodeEnum.UNKNOWN_EXCEPTION, String.format("目前不支持lambda表达式[%s]", clazz));
        }
    }

}
