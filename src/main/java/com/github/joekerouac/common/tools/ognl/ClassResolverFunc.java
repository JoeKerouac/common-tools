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
package com.github.joekerouac.common.tools.ognl;

import java.util.Map;

/**
 * 类型查找函数
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public interface ClassResolverFunc {

    /**
     * 根据指定类名和ognl上下文查找class
     * 
     * @param className
     *            类名
     * @param context
     *            ognl上下文
     * @return 类型
     * @throws ClassNotFoundException
     *             类型未找到
     */
    Class<?> findClass(String className, Map<Object, Object> context) throws ClassNotFoundException;

}
