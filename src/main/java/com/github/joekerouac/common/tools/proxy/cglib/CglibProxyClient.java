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
package com.github.joekerouac.common.tools.proxy.cglib;

import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.collection.Pair;
import com.github.joekerouac.common.tools.proxy.Interception;
import com.github.joekerouac.common.tools.proxy.ParentUtil;
import com.github.joekerouac.common.tools.proxy.ProxyClient;
import com.github.joekerouac.common.tools.string.StringUtils;

import net.sf.cglib.proxy.Enhancer;

/**
 * cglib实现的代理客户端
 *
 * @author JoeKerouac
 * @since 1.0.0
 */
public class CglibProxyClient implements ProxyClient {

    @Override
    public Object create(Class<?>[] parent, Object proxy, ClassLoader loader, String name, Interception interception,
        Class<?>[] paramTypes, Object[] params) {
        if (!CollectionUtil.sizeEquals(params, paramTypes)) {
            throw new IllegalArgumentException("构造器参数列表paramTypes长度和实际参数params长度不一致");
        }

        Enhancer enhancer = createEnhancer(parent, proxy, loader, name, interception);
        if (CollectionUtil.isEmpty(paramTypes)) {
            return enhancer.create();
        } else {
            return enhancer.create(paramTypes, params);
        }
    }

    /**
     * 构建指定对象的代理Class，稍后可以通过反射构建该class的实例，对象的类必须是公共的，同时代理方法也必须是公共的
     * <p>
     * 注意：生成的class通过反射调用构造器创建对象的时候，构造器中调用的方法不会被拦截！！！
     * </p>
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
     * @return 代理class
     */
    @Override
    public Class<?> createClass(Class<?>[] parent, Object proxy, ClassLoader loader, String name,
        Interception interception) {
        return createEnhancer(parent, proxy, loader, name, interception).createClass();
    }

    private Enhancer createEnhancer(Class<?>[] parent, Object proxy, ClassLoader loader, String name,
        Interception interception) {
        Enhancer enhancer = new Enhancer();
        Pair<Class<?>, Class<?>[]> pair = ParentUtil.setInterfaces(parent, proxy);
        Class<?> superClass = pair.getKey();
        Class<?>[] interfaces = pair.getValue();

        enhancer.setSuperclass(superClass);
        enhancer.setInterfaces(interfaces);
        enhancer.setClassLoader(loader);
        enhancer.setCallback(new MethodInterceptorAdapter(interception, proxy, superClass, interfaces));
        if (StringUtils.isNotBlank(name)) {
            enhancer.setNamingPolicy((s, s1, o, predicate) -> name);
        }
        return enhancer;
    }

    @Override
    public ClientType getClientType() {
        return ClientType.CGLIB;
    }
}
