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
package com.github.joekerouac.common.tools.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.enums.ResourceType;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * 资源抽象定义，实现类必须包含一个仅有一个Map作为入参的构造器
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface Resource {

    /**
     * 校验参数列表与对应的参数类型是否一致
     * 
     * @param args
     *            参数列表
     * @param types
     *            参数类型列表
     */
    static void argsCheck(Object[] args, Class<?>[] types) {
        if (types == null || types.length == 0) {
            return;
        }

        Assert.assertTrue(args != null && args.length == types.length,
            StringUtils.format("参数不对，当前参数为： [{}]", args == null ? null : Arrays.toString(args)),
            ExceptionProviderConst.IllegalArgumentExceptionProvider);
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            // 这里强制校验参数不允许为null
            Assert.notNull(arg, StringUtils.format("参数[{}]为null", i),
                ExceptionProviderConst.IllegalArgumentExceptionProvider);

            Class<?> type = types[i];
            Assert.assertTrue(type.isAssignableFrom(arg.getClass()),
                StringUtils.format("当前参数[{}]类型不对，期望参数类型： [{}]， 实际参数类型： [{}]", i, type, arg.getClass()),
                ExceptionProviderConst.IllegalArgumentExceptionProvider);
        }
    }

    /**
     * 初始化，默认资源不需要初始化，但是对于某些资源是需要初始化的
     * 
     * @param args
     *            初始化参数
     */
    default void init(Object[] args) {

    }

    /**
     * 将资源转换为map，map中包含资源的必要信息，可以通过将该map注入构造器来重新完成对象构造
     * 
     * @return map
     */
    Map<String, String> toMap();

    /**
     * 资源类型
     * 
     * @return 资源类型
     */
    ResourceType type();

    /**
     * 获取resource的输入流，多次获取是同一个输入流
     *
     * @return 输入流，不允许为null
     * @throws IOException
     *             IO异常
     */
    InputStream getInputStream() throws IOException;

    /**
     * 获取resource的名字，应该是带后缀的，比如test.jar
     *
     * @return 名字
     */
    String getName();

    /**
     * 获取资源的URL
     * 
     * @return URL，允许为null
     * @throws IOException
     *             IO异常
     */
    URL getUrl() throws IOException;

}
