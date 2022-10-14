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
package com.github.joekerouac.common.tools.codec;

import com.github.joekerouac.common.tools.codec.exception.SerializeException;
import com.github.joekerouac.common.tools.reflect.type.AbstractTypeReference;

/**
 * 序列化接口
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface Codec {

    /**
     * 读取数据
     * 
     * @param data
     *            数据
     * @param type
     *            数据类型Class
     * @param <T>
     *            数据实际类型
     * @throws SerializeException
     *             序列化失败应该抛出SerializeException而不是其他异常
     * @return 读取到的数据
     */
    <T> T read(byte[] data, Class<T> type) throws SerializeException;

    /**
     * 读取数据
     * 
     * @param data
     *            数据
     * @param typeReference
     *            数据类型引用
     * @param <T>
     *            数据实际类型
     * @throws SerializeException
     *             序列化失败应该抛出SerializeException而不是其他异常
     * @return 读取到的数据
     */
    <T> T read(byte[] data, AbstractTypeReference<T> typeReference) throws SerializeException;

    /**
     * 写出数据
     * 
     * @param data
     *            数据
     * @return 序列化后的数据
     */
    byte[] write(Object data);
}
