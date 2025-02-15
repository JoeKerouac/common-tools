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
package com.github.joekerouac.common.tools.proxy.bytebuddy;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.proxy.Interception;
import com.github.joekerouac.common.tools.proxy.ProxyParent;
import com.github.joekerouac.common.tools.util.Assert;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

/**
 * 方法拦截器
 *
 * @author JoeKerouac
 * @since 1.0.0
 */
public class GeneralInterceptor {

    /**
     * 代理方法实现
     */
    private final Interception interception;

    /**
     * target，可以为空，为空表示生成新代理，不为空表示对target代理
     */
    private final Object target;

    private final Class<?> superClass;

    private final ProxyParent proxyParent;

    public GeneralInterceptor(Interception interception, Object target, Class<?> superClass, Class<?>[] interfaces) {
        Assert.notNull(interception, "interception 不能为 null", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.notNull(interfaces, "接口不能为 null", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        this.interception = interception;
        this.target = target;
        this.superClass = superClass;
        this.proxyParent = new ProxyParent.InternalProxyParent(target, interfaces, interception);
    }

    /**
     * 拦截有实现的方法
     * 
     * @param params
     *            调用方法的参数
     * @param method
     *            被拦截的方法
     * @param callable
     *            父类调用
     * @return 执行结果
     */
    @RuntimeType
    public Object interceptClass(@AllArguments Object[] params, @Origin Method method,
        @SuperCall Callable<Object> callable) throws Throwable {
        return Interception.invokeWrap(interception, target, method, null, params, callable::call,
            proxyParent.GET_TARGET_CLASS());
    }

    /**
     * 拦截抽象方法
     * 
     * @param params
     *            调用方法的参数
     * @param method
     *            被拦截的方法
     * @return 执行结果
     */
    @RuntimeType
    public Object interceptInterface(@AllArguments Object[] params, @Origin Method method) throws Throwable {
        if (ProxyParent.canInvoke(method)) {
            return Interception.invokeWrap(interception, null, method, null, params,
                () -> ProxyParent.invoke(method, proxyParent), proxyParent.GET_TARGET_CLASS());
        } else {
            return Interception.invokeWrap(interception, target, method, null, params, null,
                proxyParent.GET_TARGET_CLASS());
        }
    }
}
