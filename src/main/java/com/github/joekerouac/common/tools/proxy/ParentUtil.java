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

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.collection.Pair;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * @author JoeKerouac
 * @date 2025-02-14 17:28:40
 * @since 2.1.5
 */
public class ParentUtil {

    public static Pair<Class<?>, Class<?>[]> setInterfaces(Class<?>[] arr, Object proxy) {
        Class<?>[] classes = arr;
        if (proxy != null && !Modifier.isFinal(proxy.getClass().getModifiers())
            && !proxy.getClass().getName().contains("$")) {
            if (CollectionUtil.size(classes) > 0) {
                classes = CollectionUtil.addTo(proxy.getClass(), classes);
            } else {
                classes = new Class[] {proxy.getClass()};
            }
        }

        int parentSize = CollectionUtil.size(classes);
        if (parentSize <= 0) {
            throw new IllegalArgumentException(StringUtils.format("当前未指定任何父类以及接口"));
        }

        Set<Class<?>> interfaces = new HashSet<>();
        interfaces.add(ProxyParent.class);
        Class<?> superClass = null;
        for (Class<?> clazz : classes) {
            if (clazz.isInterface()) {
                interfaces.add(clazz);
            } else if (superClass != null) {
                throw new IllegalArgumentException(StringUtils.format("一个类不能有两个父类，当前父类: [{}:{}]", superClass, clazz));
            } else {
                superClass = clazz;
            }
        }

        return new Pair<>(superClass, interfaces.toArray(new Class[0]));
    }

}
