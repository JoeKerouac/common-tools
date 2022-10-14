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
package com.github.joekerouac.common.tools.ognl.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.reflect.AccessorUtil;
import com.github.joekerouac.common.tools.reflect.type.JavaTypeUtil;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

import ognl.MethodAccessor;

/**
 * 方法执行函数，这是一个简单实现，不包含方法查找、入参类型转换（例如尝试将int转换为方法需要的long）等，如果需要这些能力请使用ognl自带
 * 的{@link ognl.ObjectMethodAccessor}；代价就是{@link ognl.ObjectMethodAccessor}会加方法级别的锁，也就是对于同一个方法只能串行不能并行
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class SimpleMethodAccessorFunc implements MethodAccessor {

    /**
     * 静态方法缓存
     */
    private static final Map<Class<?>, List<Method>> staticMethodCache = new ConcurrentHashMap<>();

    /**
     * 普通方法缓存
     */
    private static final Map<Class<?>, List<Method>> methodCache = new ConcurrentHashMap<>();

    @Override
    public Object callStaticMethod(final Map context, final Class targetClass, final String methodName,
        final Object[] args) {
        final List<Method> methods = staticMethodCache.compute(targetClass, (clazz, list) -> {
            if (list == null) {
                list = Arrays.stream(clazz.getDeclaredMethods()).filter(AccessorUtil::isStatic)
                    .collect(Collectors.toList());
            }
            return list;
        });

        return invoke(methods, targetClass, methodName, args);
    }

    @Override
    public Object callMethod(final Map context, final Object target, final String methodName, final Object[] args) {
        final List<Method> methods = methodCache.compute(target.getClass(), (clazz, list) -> {
            if (list == null) {
                list = Arrays.stream(clazz.getDeclaredMethods()).filter(method -> !AccessorUtil.isStatic(method))
                    .collect(Collectors.toList());
            }

            return list;
        });

        return invoke(methods, target, methodName, args);
    }

    /**
     * 执行方法
     * 
     * @param methods
     *            当前所有方法列表
     * @param target
     *            target
     * @param methodName
     *            要执行的方法名
     * @param args
     *            方法入参
     * @return 结果
     */
    private static Object invoke(List<Method> methods, Object target, String methodName, Object[] args) {
        final Method method = findBest(methods, methodName, args);
        final Object[] realArgs = convert(method, args);
        try {
            return method.invoke(target, realArgs);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(StringUtils.format("ognl表达式执行失败, [{}, {}, {}, {}, {}]", methods, target,
                methodName, method, Arrays.toString(args)), e);
        }
    }

    /**
     * 尝试将参数转换为实际的方法入参，目前仅仅对基本的数字类型做了自动转换，并没有对其他类型进行处理
     * 
     * @param method
     *            方法
     * @param args
     *            方法参数
     * @return 入参
     */
    private static Object[] convert(Method method, Object[] args) {
        Object[] realArgs = new Object[args.length];

        final Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                continue;
            }

            realArgs[i] = arg;

            Class<?> parameterType = parameterTypes[i];
            final Class<?> realType = arg.getClass();
            // 注意，参数类型不可能是原生类型，只可能是原生类型对应的包装类型或者其他类型
            if ((parameterType.isPrimitive() || Number.class.isAssignableFrom(parameterType))
                && (Number.class.isAssignableFrom(realType))) {

                // 参数声明是原生类型，转换为其包装类型后与实际入参类型一致，无需转换
                if (parameterType.isPrimitive() && JavaTypeUtil.boxed(parameterType).equals(realType)) {
                    continue;
                }

                if (parameterType.isPrimitive()) {
                    parameterType = JavaTypeUtil.boxed(parameterType);
                }

                // 这里仅尝试对几种简单类型进行转换
                if (Character.class.equals(parameterType)) {
                    realArgs[i] = ((Number)arg).byteValue();
                } else if (Byte.class.equals(parameterType)) {
                    realArgs[i] = ((Number)arg).byteValue();
                } else if (Short.class.equals(parameterType)) {
                    realArgs[i] = ((Number)arg).shortValue();
                } else if (Integer.class.equals(parameterType)) {
                    realArgs[i] = ((Number)arg).intValue();
                } else if (Long.class.equals(parameterType)) {
                    realArgs[i] = ((Number)arg).longValue();
                } else if (Double.class.equals(parameterType)) {
                    realArgs[i] = ((Number)arg).doubleValue();
                } else if (Float.class.equals(parameterType)) {
                    realArgs[i] = ((Number)arg).floatValue();
                }
            }
        }

        return realArgs;
    }

    /**
     * 从方法列表中根据方法名和入参查找匹配度最高的方法，注意，该方法是简单实现，在复杂场景下不应该使用这个
     * 
     * @param methods
     *            方法列表
     * @param methodName
     *            方法名
     * @param args
     *            方法入参
     * @return 方法
     */
    private static Method findBest(List<Method> methods, String methodName, Object[] args) {
        int maxScore = -1;
        Method result = null;
        for (final Method method : methods) {
            if (method.getName().equals(methodName) && method.getParameterCount() == args.length) {
                int score = 0;

                final Class<?>[] parameterTypes = method.getParameterTypes();
                for (int i = 0; i < parameterTypes.length; i++) {
                    Object arg = args[i];
                    if (arg == null) {
                        continue;
                    }

                    Class<?> parameterType = parameterTypes[i];
                    score += calc(parameterType, arg.getClass());
                }

                if (score > maxScore) {
                    maxScore = score;
                    result = method;
                }
            }
        }

        Assert.assertTrue(result != null,
            () -> StringUtils.format("方法列表中不包含指定方法, methods: [{}], methodName: [{}], args: [{}]", methods, methodName,
                Arrays.toString(args)),
            ExceptionProviderConst.IllegalStateExceptionProvider);

        return result;
    }

    /**
     * 计算类型匹配得分
     * 
     * @param declareParamType
     *            声明类型
     * @param realParamType
     *            实际类型
     * @return 得分
     */
    private static int calc(Class<?> declareParamType, Class<?> realParamType) {
        if (declareParamType.isPrimitive()) {
            // 封装类型特殊处理下
            return calc(JavaTypeUtil.boxed(declareParamType), realParamType);
        } else if (realParamType.equals(declareParamType) || declareParamType.isAssignableFrom(realParamType)) {
            return 100;
        } else if (Number.class.isAssignableFrom(declareParamType) && Number.class.isAssignableFrom(realParamType)) {
            // 都是数字的，可以潜在进行转换，这个也处理下
            return 50;
        }

        return 0;
    }

}
