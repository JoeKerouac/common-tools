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

import com.github.joekerouac.common.tools.crypto.HmacSpi;
import com.github.joekerouac.common.tools.crypto.PhashSpi;

/**
 * Phash算法抽象，非线程安全
 *
 * @since 1.0.0
 * @author JoeKerouac
 */
public abstract class AbstractPhash implements PhashSpi {

    /**
     * 初始化标志，true表示已经初始化
     */
    private boolean init;

    /**
     * hmac算法实现
     */
    private HmacSpi hmacSpi;

    protected AbstractPhash(HmacSpi hmacSpi) {
        this.hmacSpi = hmacSpi;
    }

    @Override
    public String hmacAlgorithm() {
        return hmacSpi.name();
    }

    @Override
    public void init(byte[] key) {
        this.init = true;
        this.hmacSpi.init(key);
    }

    @Override
    public void phash(byte[] seed, byte[] output) {
        if (!init) {
            throw new IllegalStateException("PHASH未初始化，请先初始化");
        }

        int hmacSize = hmacSpi.macSize();
        byte[] tmp;
        byte[] aBytes = null;

        /*
         * compute:
         *
         *     P_hash(secret, seed) = HMAC_hash(secret, A(1) + seed) +
         *                            HMAC_hash(secret, A(2) + seed) +
         *                            HMAC_hash(secret, A(3) + seed) + ...
         * A() is defined as:
         *
         *     A(0) = seed
         *     A(i) = HMAC_hash(secret, A(i-1))
         */
        int remaining = output.length;
        int ofs = 0;

        while (remaining > 0) {

            /*
             * 计算A()
             */
            if (aBytes == null) {
                hmacSpi.update(seed);
            } else {
                hmacSpi.update(aBytes);
            }

            aBytes = hmacSpi.doFinal();

            /*
             * 计算HMAC_hash()
             */
            hmacSpi.update(aBytes);
            hmacSpi.update(seed);
            tmp = hmacSpi.doFinal();

            int k = Math.min(hmacSize, remaining);
            for (int i = 0; i < k; i++) {
                output[ofs++] ^= tmp[i];
            }

            remaining -= k;
        }
    }

    @Override
    public String name() {
        return "Phash" + hmacSpi.hashAlgorithm();
    }

    @Override
    public PhashSpi copy() throws CloneNotSupportedException {
        AbstractPhash phash = (AbstractPhash)super.clone();
        phash.hmacSpi = hmacSpi.copy();
        phash.init = this.init;
        return phash;
    }
}
