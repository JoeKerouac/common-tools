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
package com.github.joekerouac.common.tools.proxy;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.stream.Stream;

import com.github.joekerouac.common.tools.reflect.ReflectUtil;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * 所有代理都继承该类
 *
 * @author JoeKerouac
 * @since 1.0.0
 */
public interface ProxyParent {

    /**
     * {@link #GET_TARGET()}方法说明
     */
    MethodMetadata GET_TARGET =
        new MethodMetadata("GET_TARGET", ProxyParent.class, ReflectUtil.getMethod(ProxyParent.class, "GET_TARGET"));

    /**
     * {@link #GET_TARGET_CLASS()}方法说明
     */
    MethodMetadata GET_TARGET_CLASS = new MethodMetadata("GET_TARGET_CLASS", ProxyParent.class,
        ReflectUtil.getMethod(ProxyParent.class, "GET_TARGET_CLASS"));

    /**
     * {@link #GET_INTERFACES()}方法说明
     */
    MethodMetadata GET_INTERFACES = new MethodMetadata("GET_INTERFACES", ProxyParent.class,
        ReflectUtil.getMethod(ProxyParent.class, "GET_INTERFACES"));

    /**
     * {@link #GET_INTERCEPTION()}方法说明
     */
    MethodMetadata GET_LINKED_INTERCEPTION = new MethodMetadata("GET_INTERCEPTION", ProxyParent.class,
        ReflectUtil.getMethod(ProxyParent.class, "GET_INTERCEPTION"));

    /**
     * 获取代理的对象
     * 
     * @param <T>
     *            代理对象类型
     * @return 代理的对象，如果是直接生成的代理类的实例而不是对指定对象生成代理则返回null
     */
    @SuppressWarnings("MethodName")
    <T> T GET_TARGET();

    /**
     * 实际代理的类型
     * 
     * @param <T>
     *            实际代理的类型
     * @return 实际代理的类型的class对象
     */
    @SuppressWarnings("MethodName")
    <T> Class<T> GET_TARGET_CLASS();

    /**
     * 获取实现的接口，不会返回代理类本身
     * 
     * @return 实现的接口集合
     */
    @SuppressWarnings("MethodName")
    Class<?>[] GET_INTERFACES();

    /**
     * 获取Interception
     * 
     * @return Interception
     */
    @SuppressWarnings("MethodName")
    Interception GET_INTERCEPTION();

    /**
     * 是否可以执行，即方法是否是{@link ProxyParent}声明的
     * 
     * @param method
     *            方法
     * @return true表示可以执行，即可以调用{@link #invoke(Method, ProxyParent)}
     */
    static boolean canInvoke(Method method) {
        return Stream.of(GET_TARGET, GET_TARGET_CLASS, GET_INTERFACES, GET_LINKED_INTERCEPTION)
            .filter(m -> Objects.equals(m, MethodMetadata.build(method))).limit(1).count() > 0;
    }

    /**
     * 执行指定方法
     * 
     * @param method
     *            方法，必须是{@link ProxyParent}声明的方法
     * @param proxyParent
     *            ProxyParent实例
     * @param <T>
     *            返回值类型
     * @return 返回值
     */
    static <T> T invoke(Method method, ProxyParent proxyParent) {
        Assert.argNotNull(method, "method");
        Assert.argNotNull(proxyParent, "proxyParent");

        MethodMetadata metadata = MethodMetadata.build(method);
        if (canInvoke(method)) {
            return metadata.invoke(proxyParent);
        } else {
            throw new ProxyException(StringUtils.format("方法 [{}] 不是 ProxyParent 中声明的", method));
        }
    }

    class InternalProxyParent implements ProxyParent {

        private final Object target;

        private final Class<?> targetClass;

        private final Class<?>[] interfaces;

        private Interception interception;

        public InternalProxyParent(Object target, Class<?> targetClass, Class<?>[] interfaces,
            Interception interception) {
            this.target = target;
            this.targetClass = targetClass;
            this.interfaces = interfaces;
            this.interception = interception;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T GET_TARGET() {
            return (T)target;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Class<T> GET_TARGET_CLASS() {
            return (Class<T>)targetClass;
        }

        @Override
        public Class<?>[] GET_INTERFACES() {
            return interfaces;
        }

        @Override
        public Interception GET_INTERCEPTION() {
            return interception;
        }
    }
}
