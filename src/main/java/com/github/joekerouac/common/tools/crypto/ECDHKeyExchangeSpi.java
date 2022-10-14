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

import com.github.joekerouac.common.tools.crypto.constant.ECDHKeyPair;

/**
 * ECDH密钥交换算法
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface ECDHKeyExchangeSpi {

    /**
     * 使用指定公私钥完成密钥交换密钥交换
     * 
     * @param publicKey
     *            公钥
     * @param privateKey
     *            私钥
     * @param curveId
     *            curveId
     * @return 密钥交换结果
     */
    byte[] keyExchange(byte[] publicKey, byte[] privateKey, int curveId);

    /**
     * 生成一个DH公私钥对
     * 
     * @param curveId
     *            curveId
     * @return 公私钥对
     */
    ECDHKeyPair generate(int curveId);

}
