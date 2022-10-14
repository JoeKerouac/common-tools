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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.proxy.Interception;
import com.github.joekerouac.common.tools.proxy.ProxyParent;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * @author JoeKerouac
 * @since 1.0.0
 */
public class MethodInterceptorAdapter implements InvocationHandler {

    /**
     * 代理方法
     */
    private final Interception interception;

    /**
     * 代理的方法
     */
    private final Object target;

    private final ProxyParent proxyParent;

    public MethodInterceptorAdapter(Object target, Class<?> targetClass, Interception interception) {
        Assert.notNull(targetClass, "targetClass 不能为 null", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.notNull(interception, "interception 不能为 null", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        this.target = target;
        this.interception = interception;
        this.proxyParent = new ProxyParent.InternalProxyParent(target, targetClass,
            CollectionUtil.addTo(ProxyParent.class, targetClass.getInterfaces()), interception);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 构建父方法调用
        if (ProxyParent.canInvoke(method)) {
            return Interception.invokeWrap(interception, null, method, proxy, args,
                () -> ProxyParent.invoke(method, proxyParent), proxyParent.GET_TARGET_CLASS());
        } else {
            return Interception.invokeWrap(interception, target, method, proxy, args, null,
                proxyParent.GET_TARGET_CLASS());
        }
    }
}
