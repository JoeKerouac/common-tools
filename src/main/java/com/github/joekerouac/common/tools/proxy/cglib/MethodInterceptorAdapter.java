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

import java.lang.reflect.Method;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.proxy.Interception;
import com.github.joekerouac.common.tools.proxy.Invoker;
import com.github.joekerouac.common.tools.proxy.ProxyParent;
import com.github.joekerouac.common.tools.util.Assert;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * cglib方法拦截适配器
 *
 * @author JoeKerouac
 * @since 1.0.0
 */
public class MethodInterceptorAdapter implements MethodInterceptor {

    /**
     * 代理方法实现
     */
    private final Interception proxy;

    /**
     * target，可以为空，为空表示生成新代理，不为空表示对target代理
     */
    private final Object target;

    private final Class<?> superClass;

    private final ProxyParent proxyParent;

    public MethodInterceptorAdapter(Interception proxy, Object target, Class<?> superClass, Class<?>[] interfaces) {
        Assert.notNull(proxy, "proxy 不能为 null", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.notNull(interfaces, "parent 不能为 null", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        this.proxy = proxy;
        this.target = target;
        this.superClass = superClass;
        this.proxyParent = new ProxyParent.InternalProxyParent(target, interfaces, proxy);
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Invoker supperCall = null;
        if (superClass != null) {
            supperCall = () -> methodProxy.invokeSuper(obj, args);
        }

        if (ProxyParent.canInvoke(method)) {
            return Interception.invokeWrap(proxy, null, method, obj, args,
                () -> ProxyParent.invoke(method, proxyParent), proxyParent.GET_TARGET_CLASS());
        } else {
            return Interception.invokeWrap(proxy, target, method, obj, args, supperCall,
                proxyParent.GET_TARGET_CLASS());
        }
    }
}
