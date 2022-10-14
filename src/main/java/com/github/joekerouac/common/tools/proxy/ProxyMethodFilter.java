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

/**
 * 方法拦截器
 *
 * @author JoeKerouac
 * @since 1.0.0
 */
public interface ProxyMethodFilter {

    /**
     * 将指定方法代理到另外的一个方法
     * 
     * @param method
     *            指定方法
     * @return 代理方法，返回null表示不代理该方法
     */
    Interception filter(Method method);
}
