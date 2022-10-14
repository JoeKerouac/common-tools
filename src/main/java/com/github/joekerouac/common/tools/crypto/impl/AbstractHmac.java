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
package com.github.joekerouac.common.tools.crypto.impl;

import java.util.Arrays;

import com.github.joekerouac.common.tools.crypto.DigestSpi;
import com.github.joekerouac.common.tools.crypto.HmacSpi;

/**
 * 抽象Hmac算法，不同hmac的差异实际上就是摘要算法的差异，所以可以对其公共算法进行统一封装
 *
 * <p>
 * 一次初始化可以多次使用
 * </p>
 *
 * @since 1.0.0
 * @author JoeKerouac
 */
public abstract class AbstractHmac implements HmacSpi {

    /**
     * 块长度
     */
    private final int blockLen;

    /**
     * hash算法结果长度，例如SHA-256，长度就是32（256/8=32，其中256单位是bit，不是byte）
     */
    private final int hashSize;

    /**
     * 初始化标志，true表示已经初始化
     */
    private boolean init;

    /**
     * 摘要算法实现
     */
    private DigestSpi digestSpi;

    /**
     * 当前是否是第一次更新
     */
    private boolean first;

    /**
     * 对应算法中的K XOR ipad
     */
    private byte[] ipad;

    /**
     * 对应算法中的K XOR opad
     */
    private byte[] opad;

    protected AbstractHmac(DigestSpi digestSpi, int hashSize, int blockLen) {
        this.digestSpi = digestSpi;
        this.first = true;
        this.hashSize = hashSize;
        this.blockLen = blockLen;
        this.ipad = new byte[blockLen];
        this.opad = new byte[blockLen];
    }

    @Override
    public void init(byte[] key) {
        reset();

        if (key == null) {
            throw new IllegalArgumentException("key 不能为空");
        }

        byte[] keyClone = key.clone();

        // 对key生成摘要
        if (keyClone.length > this.blockLen) {
            byte[] digest = this.digestSpi.digest(keyClone);
            // 尽快将内存中的key清空，防止密钥泄漏
            Arrays.fill(keyClone, (byte)0);
            keyClone = digest;
        }

        // 根据rfc2104生成k_ipad和k_opad
        for (int i = 0; i < this.blockLen; ++i) {
            byte k = i < keyClone.length ? keyClone[i] : 0;
            this.ipad[i] = (byte)(k ^ 0x36);
            this.opad[i] = (byte)(k ^ 0x5C);
        }

        // 将内存中的数据尽快清空
        Arrays.fill(keyClone, (byte)0);
        this.init = true;
    }

    @Override
    public void update(byte[] data, int offset, int len) {
        if (!init) {
            throw new IllegalStateException("HMAC未初始化，请先初始化");
        }

        if (this.first) {
            this.digestSpi.update(this.ipad);
            this.first = false;
        }

        this.digestSpi.update(data, offset, len);
    }

    @Override
    public byte[] doFinal() {
        if (!init) {
            throw new IllegalStateException("HMAC未初始化，请先初始化");
        }

        if (this.first) {
            this.digestSpi.update(this.ipad);
        } else {
            this.first = true;
        }

        byte[] result = this.digestSpi.digest();
        this.digestSpi.update(this.opad);
        this.digestSpi.update(result);
        this.digestSpi.digest(result, 0);
        return result;
    }

    @Override
    public void reset() {
        if (!this.first) {
            this.digestSpi.reset();
            this.first = true;
        }
    }

    @Override
    public String name() {
        return "Hmac" + hashAlgorithm();
    }

    @Override
    public String hashAlgorithm() {
        return digestSpi.name();
    }

    @Override
    public AbstractHmac copy() throws CloneNotSupportedException {
        AbstractHmac hmac = (AbstractHmac)super.clone();
        hmac.digestSpi = this.digestSpi.copy();
        hmac.init = this.init;
        hmac.ipad = this.ipad.clone();
        hmac.opad = this.opad.clone();
        return hmac;
    }

    @Override
    public int macSize() {
        return hashSize;
    }

    @Override
    public int blockLen() {
        return blockLen;
    }
}
