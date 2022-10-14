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
package com.github.joekerouac.common.tools.registry;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * 注册表数据的抽象表示
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class RegistryValue {

    /*
     * 注册表类型声明，详细说明请参考官方文档：https://docs.microsoft.com/zh-cn/windows/win32/sysinfo/registry-value-types
     */

    public static final int REG_NONE = 0;

    /**
     * String
     */
    public static final int REG_SZ = 1;

    /**
     * String
     */
    public static final int REG_EXPAND_SZ = 2;

    /**
     * byte[]
     */
    public static final int REG_BINARY = 3;

    /**
     * int
     */
    public static final int REG_DWORD = 4;

    /**
     * int(little endian)
     */
    public static final int REG_DWORD_LITTLE_ENDIAN = 4;

    /**
     * int(big endian)
     */
    public static final int REG_DWORD_BIG_ENDIAN = 5;

    /**
     * 没有处理
     */
    public static final int REG_LINK = 6;

    /**
     * String[]
     */
    public static final int REG_MULTI_SZ = 7;

    /**
     * 没有处理
     */
    public static final int REG_RESOURCE_LIST = 8;

    /**
     * 没有处理
     */
    public static final int REG_FULL_RESOURCE_DESCRIPTOR = 9;

    /**
     * 没有处理
     */
    public static final int REG_RESOURCE_REQUIREMENTS_LIST = 10;

    /**
     * 注册表数据类型对应的Java类型
     */
    private static final Class<?>[] DATA_TYPE = new Class[11];

    static {
        DATA_TYPE[REG_SZ] = String.class;
        DATA_TYPE[REG_EXPAND_SZ] = String.class;
        DATA_TYPE[REG_BINARY] = byte[].class;
        DATA_TYPE[REG_DWORD] = Integer.class;
        DATA_TYPE[REG_DWORD_BIG_ENDIAN] = Integer.class;
        DATA_TYPE[REG_MULTI_SZ] = String[].class;
    }

    /**
     * jni中c代码会用到该字段
     */
    protected final int type;

    protected final String name;

    protected final RegistryKey key;

    /**
     * 数据，不同类型的数据类型也不同
     */
    private Object data;

    public RegistryValue(RegistryKey key, String name, int type) {
        Assert.notNull(key, "key 不能为空", ExceptionProviderConst.IllegalStateExceptionProvider);
        Assert.notBlank(name, "name 不能为空", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.notNull(DATA_TYPE[type], "当前不支持的type:" + type, ExceptionProviderConst.IllegalArgumentExceptionProvider);
        this.key = key;
        this.name = name;
        this.type = type;
    }

    public RegistryKey getKey() {
        return this.key;
    }

    public String getName() {
        return this.name;
    }

    public int getType() {
        return this.type;
    }

    /**
     * jni中c代码调用
     *
     * @param data
     *            数据
     */
    public void setData(Object data) {
        Assert.notNull(data, "数据不能为null", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.assertTrue(data.getClass().equals(DATA_TYPE[type]),
            StringUtils.format("当前注册表类型为：[{}:{}]，传入数据类型为：[{}]，类型不匹配", type, DATA_TYPE[type], data.getClass()),
            ExceptionProviderConst.IllegalArgumentExceptionProvider);
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    @Override
    public String toString() {
        return "[type=" + this.type + ",name=" + this.name + "]";
    }

}
