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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.github.joekerouac.common.tools.crypto.DigestSpi;

/**
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public abstract class AbstractDigest implements DigestSpi {

    private MessageDigest digest;

    private String algorithm;

    public AbstractDigest(String algorithm) {
        init(algorithm);
    }

    private void init(String algorithm) {
        try {
            this.algorithm = algorithm;
            this.digest = MessageDigest.getInstance(algorithm, new BouncyCastleProvider());
        } catch (NoSuchAlgorithmException e) {
            throw new com.github.joekerouac.common.tools.crypto.exception.NoSuchAlgorithmException(algorithm, e);
        }
    }

    @Override
    public void update(byte[] data, int offset, int len) {
        digest.update(data, offset, len);
    }

    @Override
    public void update(byte data) {
        digest.update(data);
    }

    @Override
    public byte[] digest() {
        return digest.digest();
    }

    @Override
    public void digest(byte[] output, int offset) {
        byte[] result = digest.digest();
        System.arraycopy(result, 0, output, offset, result.length);
    }

    @Override
    public void reset() {
        digest.reset();
    }

    @Override
    public String name() {
        return algorithm;
    }

    @Override
    public AbstractDigest copy() throws CloneNotSupportedException {
        AbstractDigest digest = (AbstractDigest)super.clone();
        digest.digest = (MessageDigest)this.digest.clone();
        digest.algorithm = this.algorithm;
        return digest;
    }
}
