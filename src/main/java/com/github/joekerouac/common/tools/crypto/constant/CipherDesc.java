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
package com.github.joekerouac.common.tools.crypto.constant;

import lombok.Getter;

/**
 * 加密算法枚举
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public enum CipherDesc {

    AES_128("AES/CBC/NoPadding", CipherType.BLOCK, 16, 16, 0, false, 0),

    AES_128_PKCS7Padding("AES/CBC/PKCS7Padding", CipherType.BLOCK, 16, 16, 0, false, 0),

    AES_256("AES/CBC/NoPadding", CipherType.BLOCK, 32, 16, 0, false, 0),

    AES_256_PKCS7Padding("AES/CBC/PKCS7Padding", CipherType.BLOCK, 32, 16, 0, false, 0),

    AES_256_PKCS5Padding("AES/CBC/PKCS5Padding", CipherType.BLOCK, 32, 16, 0, false, 0),

    AES_128_GCM("AES/GCM/NoPadding", CipherType.AEAD, 16, 12, 4, true, 16),

    AES_256_GCM("AES/GCM/NoPadding", CipherType.AEAD, 32, 12, 4, true, 16),

    AES_128_ECB("AES/ECB/NoPadding", CipherType.BLOCK, 16, 0, 0, false, 0),

    AES_128_ECB_PKCS7Padding("AES/ECB/PKCS7Padding", CipherType.BLOCK, 16, 0, 0, false, 0),

    AES_256_ECB("AES/ECB/NoPadding", CipherType.BLOCK, 32, 0, 0, false, 0),

    AES_256_ECB_PKCS7Padding("AES/ECB/PKCS7Padding", CipherType.BLOCK, 32, 0, 0, false, 0),

    ;

    /**
     * 加密算法名
     */
    @Getter
    private final String cipherName;

    /**
     * 密钥大小，单位byte
     */
    @Getter
    private final int keySize;

    /**
     * iv大小，单位byte
     */
    @Getter
    private final int ivLen;

    /**
     * GCM模式下会大于0，表示实际的ivLen，因为对于GCM模式来说iv等于fixedIv+nonce
     */
    @Getter
    private final int fixedIvLen;

    /**
     * 加密类型
     */
    @Getter
    private final CipherType cipherType;

    /**
     * 是否是GCM模式
     */
    @Getter
    private final boolean gcm;

    /**
     * 认证数据长度，只有GCM模式才会有，GCM模式会在加密数据的最后补充上该长度的认证数据
     */
    @Getter
    private final int tagLen;

    /**
     * 是否自带padding，true表示有padding
     */
    @Getter
    private final boolean hasPadding;

    CipherDesc(String cipherName, CipherType cipherType, int keySize, int ivLen, int fixedIvLen, boolean gcm,
        int tagLen) {
        this.cipherName = cipherName;
        this.cipherType = cipherType;
        this.keySize = keySize;
        this.ivLen = ivLen;
        this.fixedIvLen = fixedIvLen;
        this.gcm = gcm;
        this.tagLen = tagLen;
        this.hasPadding = !cipherName.endsWith("NoPadding");
    }

}
