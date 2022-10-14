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

import com.github.joekerouac.common.tools.reflect.ClassUtils;

/**
 * 代理客户端，代理的class必须是公共可访问的（如果是内部类那么必须是静态内部类），请注意：
 * <ul>
 * <li>注意java代理客户端{@link com.github.joekerouac.common.tools.proxy.java.JavaProxyClient JavaProxyClient}的特殊性</li>
 * <li>不保证多次创建的代理对象{@link Object#getClass() getClass}方法的返回值相同</li>
 * <li>代理对象的hashCode、equals方法实际会转发到代理对象的Interception上边，如果两个代理的Interception相同，那么这两个代理对象就认为是相同的;</li>
 * </ul>
 *
 * @author JoeKerouac
 * @since 1.0.0
 */
public interface ProxyClient {

    /**
     * ProxyClient的默认classloader
     */
    ProxyClassLoader DEFAULT_LOADER = new ProxyClassLoader(ProxyClient.class.getClassLoader());

    /**
     * 构建指定接口的代理Class，接口必须是公共的，同时代理方法也必须是公共的
     * 
     * @param parent
     *            指定接口
     * @param proxy
     *            方法代理
     * @param <T>
     *            代理真实类型
     * @return 代理
     */
    default <T> T create(Class<T> parent, Interception proxy) {
        return create(parent, DEFAULT_LOADER, null, proxy);
    }

    /**
     * 构建指定接口的代理Class，接口必须是公共的，同时代理方法也必须是公共的，支持多重代理
     * 
     * @param parent
     *            指定接口
     * @param proxy
     *            被代理的对象
     * @param interception
     *            方法代理
     * @param <T>
     *            代理真实类型
     * @return 代理
     */
    default <T> T create(Class<T> parent, T proxy, Interception interception) {
        return create(parent, proxy, DEFAULT_LOADER, null, interception);
    }

    /**
     * 构建指定接口的代理Class，接口必须是公共的，同时代理方法也必须是公共的
     * 
     * @param parent
     *            指定接口
     * @param name
     *            生成的对象的class名字，不一定支持（java代理不支持）
     * @param proxy
     *            方法代理
     * @param <T>
     *            代理真实类型
     * @return 代理
     */
    default <T> T create(Class<T> parent, String name, Interception proxy) {
        return create(parent, DEFAULT_LOADER, name, proxy);
    }

    /**
     * 构建指定接口的代理Class，接口必须是公共的，同时代理方法也必须是公共的，支持多重代理
     * 
     * @param parent
     *            指定接口
     * @param proxy
     *            被代理的对象
     * @param name
     *            生成的对象的class名字，不一定支持（java代理不支持）
     * @param interception
     *            方法代理
     * @param <T>
     *            代理真实类型
     * @return 代理
     */
    default <T> T create(Class<T> parent, T proxy, String name, Interception interception) {
        return create(parent, proxy, DEFAULT_LOADER, name, interception);
    }

    /**
     * 构建指定接口的代理Class，接口必须是公共的，同时代理方法也必须是公共的
     * 
     * @param parent
     *            指定接口
     * @param loader
     *            加载生成的对象的class的classloader
     * @param proxy
     *            方法代理
     * @param <T>
     *            代理真实类型
     * @return 代理
     */
    default <T> T create(Class<T> parent, ClassLoader loader, Interception proxy) {
        return create(parent, loader, null, proxy);
    }

    /**
     * 构建指定接口的代理Class，接口必须是公共的，同时代理方法也必须是公共的，支持多重代理
     * 
     * @param parent
     *            指定接口
     * @param proxy
     *            被代理的对象
     * @param loader
     *            加载生成的对象的class的classloader
     * @param interception
     *            方法代理
     * @param <T>
     *            代理真实类型
     * @return 代理
     */
    default <T> T create(Class<T> parent, T proxy, ClassLoader loader, Interception interception) {
        return create(parent, proxy, loader, null, interception);
    }

    /**
     * 构建指定接口的代理Class，接口必须是公共的，同时代理方法也必须是公共的
     * 
     * @param parent
     *            指定接口
     * @param loader
     *            加载生成的对象的class的classloader
     * @param name
     *            生成的对象的class名字，不一定支持（java代理不支持）
     * @param proxy
     *            方法代理
     * @param <T>
     *            代理真实类型
     * @return 代理
     */
    default <T> T create(Class<T> parent, ClassLoader loader, String name, Interception proxy) {
        return create(parent, null, loader, name, proxy);
    }

    /**
     * 构建指定对象的代理，对象的类必须是公共的，同时代理方法也必须是公共的，支持多重代理
     * 
     * @param parent
     *            指定接口
     * @param proxy
     *            被代理的对象
     * @param loader
     *            加载生成的对象的class的classloader
     * @param name
     *            生成的对象的class名字，不一定支持（java代理不支持）
     * @param interception
     *            方法代理
     * @param <T>
     *            代理真实类型
     * @return 代理
     */
    default <T> T create(Class<T> parent, T proxy, ClassLoader loader, String name, Interception interception) {
        return create(parent, proxy, loader, name, interception, null, null);
    }

    /**
     * 构建指定对象的代理，对象的类必须是公共的，同时代理方法也必须是公共的
     * 
     * @param parent
     *            指定接口
     * @param proxy
     *            被代理的对象
     * @param loader
     *            加载生成的对象的class的classloader
     * @param name
     *            生成的对象的class名字，不一定支持（java代理不支持）
     * @param interception
     *            方法代理
     * @param paramTypes
     *            构造器参数类型，如果构造器是无参构造器那么传null
     * @param params
     *            构造器参数，如果是无参构造器那么传null
     * @param <T>
     *            代理真实类型
     * @return 代理
     */
    <T> T create(Class<T> parent, T proxy, ClassLoader loader, String name, Interception interception,
        Class<?>[] paramTypes, Object[] params);

    /**
     * 构建指定对象的代理Class，稍后可以通过反射构建该class的实例，对象的类必须是公共的，同时代理方法也必须是公共的
     *
     * @param parent
     *            指定接口
     * @param proxy
     *            被代理的对象
     * @param loader
     *            加载生成的对象的class的classloader
     * @param name
     *            生成的对象的class名字，不一定支持（java代理不支持）
     * @param interception
     *            方法代理
     * @param <T>
     *            代理真实类型
     * @return 代理class
     */
    <T> Class<? extends T> createClass(Class<T> parent, T proxy, ClassLoader loader, String name,
        Interception interception);

    /**
     * 获取代理客户端的类型
     * 
     * @return 代理客户端类型
     */
    ClientType getClientType();

    /**
     * 获取指定类型的代理客户端
     * 
     * @param type
     *            代理客户端类型
     * @return 客户端
     */
    static ProxyClient getInstance(ClientType type) {
        return ClassUtils.getInstance(type.clientClass);
    }

    /**
     * 
     */
    enum ClientType {

        /**
         * CGLIB代理客户端
         * 
         * @see com.github.joekerouac.common.tools.proxy.cglib.CglibProxyClient
         */
        CGLIB("com.github.joekerouac.common.tools.proxy.cglib.CglibProxyClient"),

        /**
         * ByteBuddy客户端
         * 
         * @see com.github.joekerouac.common.tools.proxy.bytebuddy.ByteBuddyProxyClient
         */
        BYTE_BUDDY("com.github.joekerouac.common.tools.proxy.bytebuddy.ByteBuddyProxyClient"),

        /**
         * JAVA代理客户端
         * 
         * @see com.github.joekerouac.common.tools.proxy.java.JavaProxyClient
         */
        JAVA("com.github.joekerouac.common.tools.proxy.java.JavaProxyClient");

        private final String clientClass;

        ClientType(String clientClass) {
            this.clientClass = clientClass;
        }
    }
}
