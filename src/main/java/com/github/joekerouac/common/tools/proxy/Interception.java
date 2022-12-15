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

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * 拦截点，拦截到方法后执行拦截点
 *
 * @author JoeKerouac
 * @since 1.0.0
 */
public interface Interception {

    /**
     * 拦截点执行，可以使用invoker对父类方法发起调用；
     * 
     * 注意，方法中需要处理Object自带方法，例如toString()，可以使用{@link MethodMetadata#isObjectMethod(Method)}来判断方法是不是object方法，如果是，则可以调用我们这里提供的invoker来处理，invoker中有默认实现，或者我们自己实现也可以；
     * 
     * @param target
     *            代理的对象（如果是静态方法那么该target为null或者对class生成的代理）
     * @param params
     *            方法调用参数
     * @param method
     *            拦截的方法
     * @param invoker
     *            父类方法调用（可能为null，为null时表示无法调用父类方法）
     * @return 拦截点执行结果
     * @throws Throwable
     *             执行异常
     */
    Object invoke(Object target, Object[] params, Method method, Invoker invoker) throws Throwable;

    /**
     * 拦截方法包装执行
     * 
     * @param interception
     *            拦截的方法的代理，不能为null
     * @param target
     *            被代理的对象，只有对指定对象代理时才会有值，其他情况为null
     * @param method
     *            被代理的方法，不能为null
     * @param realTarget
     *            代理生成的对象，可以为null（byte Buddy场景下没有）
     * @param params
     *            执行方法的参数，可以为null
     * @param superCall
     *            父类调用，可以为null
     * @param proxyClass
     *            代理的类型，不能为null
     * @return 方法执行结果
     * @throws Throwable
     *             Throwable
     */
    static Object invokeWrap(Interception interception, Object target, Method method, Object realTarget,
        Object[] params, Invoker superCall, Class<?> proxyClass) throws Throwable {
        Assert.notNull(interception, "interception 不能为 null", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.notNull(method, "method 不能为 null", ExceptionProviderConst.IllegalArgumentExceptionProvider);

        Invoker invoker;

        // 先判断对应的方法是不是object自带方法，如果是则特殊处理
        MethodMetadata methodMetadata = MethodMetadata.build(method);
        if (MethodMetadata.HASH_CODE_META.equals(methodMetadata)) {
            invoker = interception::hashCode;
        } else if (MethodMetadata.TO_STRING_META.equals(methodMetadata)) {
            invoker = () -> proxyClass.getName() + "$$Proxy";
        } else if (MethodMetadata.EQUALS_META.equals(methodMetadata)) {
            invoker = () -> {
                if (params[0] instanceof ProxyParent) {
                    ProxyParent p = (ProxyParent)params[0];
                    // 1、如果是对指定对象代理，需要先比较代理对象；
                    // 2、比较class是否一致；
                    // 3、比较interception是否一致；
                    return Objects.equals(target, p.GET_TARGET()) && proxyClass.equals(p.GET_TARGET_CLASS())
                        && interception.equals(p.GET_INTERCEPTION());
                }
                return false;
            };
        } else if (MethodMetadata.isObjectMethod(method)) {
            invoker = () -> method.invoke(interception, params);
        } else if (target == null) {
            // 如果target是null那么说明是对类生成代理，否则说明是要对指定对象进行代理
            invoker = superCall;
        } else {
            // 对指定对象生成的代理，superCall需要调用被代理的对象的方法
            invoker = () -> method.invoke(target, params);
        }

        Object invokeObj = target == null ? realTarget : target;

        return interception.invoke(invokeObj, params, method, invoker);
    }
}
