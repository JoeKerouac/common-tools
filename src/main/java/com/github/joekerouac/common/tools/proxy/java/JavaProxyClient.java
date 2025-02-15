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
package com.github.joekerouac.common.tools.proxy.java;

import java.lang.reflect.Proxy;

import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.collection.Pair;
import com.github.joekerouac.common.tools.proxy.Interception;
import com.github.joekerouac.common.tools.proxy.ParentUtil;
import com.github.joekerouac.common.tools.proxy.ProxyClient;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * 需要注意的是java原生代理客户端只支持对接口的代理，不支持对普通类或者抽象类代理，同时不支持设置代理生成的类的名字
 *
 * @author JoeKerouac
 * @since 1.0.0
 */
public class JavaProxyClient implements ProxyClient {

    @Override
    public ClientType getClientType() {
        return ClientType.JAVA;
    }

    @Override
    public Object create(Class<?>[] parent, Object proxy, ClassLoader loader, String name, Interception interception,
        Class<?>[] paramTypes, Object[] params) {
        if (!CollectionUtil.sizeEquals(params, paramTypes)) {
            throw new IllegalArgumentException("构造器参数列表paramTypes长度和实际参数params长度不一致");
        }

        Pair<Class<?>, Class<?>[]> pair = ParentUtil.setInterfaces(parent, proxy);
        Class<?> superClass = pair.getKey();
        Class<?>[] interfaces = pair.getValue();

        if (superClass != null) {
            throw new IllegalArgumentException(StringUtils.format("Java代理实现不允许指定父类: [{}]", superClass));
        }

        return Proxy.newProxyInstance(loader, interfaces,
            new MethodInterceptorAdapter(proxy, interfaces, interception));
    }

    @Override
    public Class<?> createClass(Class<?>[] parent, Object proxy, ClassLoader loader, String name,
        Interception interception) {
        // java代理返回class对象没有必要，而且构造器是一个特殊构造器，详情参照Proxy#newProxyInstance方法实现
        throw new UnsupportedOperationException("java 代理不支持对对象进行代理");
    }
}
