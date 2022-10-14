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
package com.github.joekerouac.common.tools.crypto;

import com.github.joekerouac.common.tools.crypto.constant.DigestDesc;

/**
 * 消息摘要SPI
 *
 * <p>
 * 非线程安全
 * </p>
 * 
 * @since 1.0.0
 * @author JoeKerouac
 */
public interface DigestSpi extends AlgorithmSpi<DigestSpi> {

    /**
     * 更新数据
     *
     * @param data
     *            源数据
     */
    default void update(byte[] data) {
        update(data, 0, data.length);
    }

    /**
     * 更新数据
     *
     * @param data
     *            源数据
     * @param offset
     *            起始位置
     * @param len
     *            长度
     */
    void update(byte[] data, int offset, int len);

    /**
     * 更新数据
     *
     * @param data
     *            源数据
     */
    void update(byte data);

    /**
     * 对所有源数据进行摘要
     * 
     * @return 摘要结果
     */
    byte[] digest();

    /**
     * 将当前内存中数据的摘要输出到指定数组，输出数组offset后必须有足够的空间存放摘要
     *
     * @param output
     *            输出数组
     * @param offset
     *            输出起始位置，会从输出数组的该位置将摘要输出到输出数组
     */
    void digest(byte[] output, int offset);

    /**
     * 对指定数据生成摘要
     * 
     * @param data
     *            数据
     * @return 摘要
     */
    default byte[] digest(byte[] data) {
        update(data);
        return digest();
    }

    /**
     * 重置摘要，重新生成摘要
     */
    void reset();

    @Override
    default int type() {
        return AlgorithmSpi.DIGEST;
    }

    static DigestSpi getInstance(DigestDesc desc) {
        return AlgorithmRegistry.newInstance("alias.digest." + desc.getAlgorithm());
    }
}
