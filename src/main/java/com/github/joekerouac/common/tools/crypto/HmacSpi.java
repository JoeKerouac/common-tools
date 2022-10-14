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

import com.github.joekerouac.common.tools.crypto.constant.HmacDesc;

/**
 * HMAC（Keyed-Hashing for Message Authentication）算法接口，消息完整性保证，保证消息不会被篡改，算法大概如下： <br/>
 * <br/>
 * <p>
 * ipad = the byte 0x36 repeated B times <br/>
 * opad = the byte 0x5C repeated B times.<br/>
 * <br/>
 *
 * 对数据text做mac计算（H表示hash算法，可以是MD5，也可以是SHA等，K表示密钥，通信双方都应持有）：<br/>
 * H(K XOR opad, H(K XOR ipad, text))
 * </p>
 * <br/>
 * <br/>
 * <p>
 * HMAC算法前提是通信双方有一个共同的密钥（相当于salt），只能保证消息完整性，但是不具备数字签名的抗否认性，也就是A发给B消息后A可以 否认发过这个消息，因为加密用的密钥双方都有，B可以很轻松造一个同样的消息出来。
 * </p>
 * <br/>
 * 相关文档可以参考：https://tools.ietf.org/html/rfc2104
 *
 * <p>
 * 非线程安全
 * </p>
 * 
 * @since 1.0.0
 * @author JoeKerouac
 */
public interface HmacSpi extends AlgorithmSpi<HmacSpi> {

    /**
     * 初始化
     *
     * @param key
     *            密钥，长度不限
     */
    void init(byte[] key);

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
     *            要更新的数据的起始位置
     * @param data
     *            长度
     */
    void update(byte[] data, int offset, int len);

    /**
     * 对所有源数据进行hmac认证加密
     * 
     * @return 认证数据
     */
    byte[] doFinal();

    /**
     * 对指定数据生成认证数据
     * 
     * @param data
     *            数据
     * @return 认证数据
     */
    default byte[] doFinal(byte[] data) {
        update(data);
        return doFinal();
    }

    /**
     * 重置内存中的认证数据，重新生成认证
     */
    void reset();

    /**
     * hmac算法块大小
     * 
     * @return hmac算法块大小
     */
    int blockLen();

    /**
     * mac算法结果长度
     * 
     * @return mac算法结果长度（实际是对应的hash算法长度，注意单位是bit不是byte）
     */
    int macSize();

    /**
     * hash算法名
     * 
     * @return hash算法名
     */
    String hashAlgorithm();

    default int type() {
        return AlgorithmSpi.HMAC;
    }

    static HmacSpi getInstance(HmacDesc desc) {
        return AlgorithmRegistry.newInstance("alias.hmac." + desc.getAlgorithm());
    }
}
