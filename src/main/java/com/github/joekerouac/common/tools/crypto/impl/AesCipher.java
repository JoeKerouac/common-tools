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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.github.joekerouac.common.tools.constant.Const;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.crypto.CipherSpi;
import com.github.joekerouac.common.tools.crypto.constant.CipherDesc;
import com.github.joekerouac.common.tools.crypto.exception.CryptoException;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * AES加密器
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class AesCipher implements CipherSpi {

    private static final String SUN_JCE_PROVIDER = "com.sun.crypto.provider.SunJCE";

    /**
     * 实际的加密器
     */
    public final Cipher cipher;

    /**
     * 加密器提供者
     */
    private final Provider provider;

    /**
     * 加密套件说明
     */
    private final CipherDesc cipherDesc;

    /**
     * 加解密模式
     */
    private int mode = -1;

    public AesCipher(CipherDesc cipherAlgorithm) {
        this(cipherAlgorithm, Const.BC_PROVIDER);
    }

    public AesCipher(CipherDesc cipherAlgorithm, Provider provider) {
        this.cipherDesc = Objects.requireNonNull(cipherAlgorithm);
        this.provider = provider == null ? Const.BC_PROVIDER : provider;

        try {
            this.cipher = Cipher.getInstance(cipherDesc.getCipherName(), this.provider);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new com.github.joekerouac.common.tools.crypto.exception.NoSuchAlgorithmException(
                cipherAlgorithm.getCipherName(), e);
        }
    }

    @Override
    public int getMode() {
        return mode;
    }

    @Override
    public void init(byte[] key, byte[] iv, int mode, SecureRandom secureRandom) {
        // 这里是固定的
        Assert.notNull(key, "key不能为null", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.notNull(iv, "IV不能为null", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.assertTrue(mode == ENCRYPT_MODE || mode == DECRYPT_MODE, "不支持的加解密模式：" + mode,
            ExceptionProviderConst.IllegalArgumentExceptionProvider);
        this.mode = mode;
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.clone(), "AES");
        try {
            if (cipherDesc.getIvLen() == 0) {
                cipher.init(mode, secretKeySpec, (IvParameterSpec)null, secureRandom);
            } else if ((provider.getClass().getName().equals(SUN_JCE_PROVIDER)) && cipherDesc.isGcm()) {
                // 注意，SunJCE提供的加密器如果是GCM模式，需要使用GCMParameterSpec
                byte[] realIv = iv.clone();
                GCMParameterSpec parameterSpec = new GCMParameterSpec(16 * 8, realIv);
                cipher.init(mode, secretKeySpec, parameterSpec, secureRandom);
            } else {
                IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
                cipher.init(mode, secretKeySpec, ivParameterSpec, secureRandom);
            }
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new CryptoException(StringUtils.format("key: [{}], iv: [{}], mode: [{}]", key, iv, mode), e);
        }
    }

    @Override
    public int getBlockSize() {
        return cipher.getBlockSize();
    }

    @Override
    public void updateAAD(byte[] data) {
        cipher.updateAAD(data);
    }

    @Override
    public byte[] doFinal() {
        try {
            return cipher.doFinal();
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new CryptoException(StringUtils.format("block size: [{}], algorithm: [{}]", cipher.getBlockSize(),
                cipherDesc.getCipherName()), e);
        }
    }

    @Override
    public int doFinal(byte[] data, int offset, int len, byte[] result, int resultOffset) {
        try {
            return cipher.doFinal(data, offset, len, result, resultOffset);
        } catch (IllegalBlockSizeException | BadPaddingException | ShortBufferException e) {
            throw new CryptoException(StringUtils.format("data len: [{}], block size: [{}], algorithm: [{}]", len,
                cipher.getBlockSize(), cipherDesc.getCipherName()), e);
        }
    }

    @Override
    public int update(byte[] data, int offset, int len, byte[] result, int resultOffset) {
        try {
            return cipher.update(data, offset, len, result, resultOffset);
        } catch (ShortBufferException e) {
            throw new CryptoException(StringUtils.format("data len: [{}], block size: [{}], algorithm: [{}]", len,
                cipher.getBlockSize(), cipherDesc.getCipherName()), e);
        }
    }

    @Override
    public int getOutputSize(int len) {
        // 对于javax.crypto.Cipher.getOutputSize来说，如果是加密模式，那么返回加密数据长度+tagLen，如果是解密模式，返回解密数据长度-tagLen
        return cipher.getOutputSize(len);
    }

    @Override
    public int getTagLen() {
        return cipherDesc.getTagLen();
    }

    @Override
    public String name() {
        return cipherDesc.getCipherName();
    }

    @Override
    public CipherSpi copy() throws CloneNotSupportedException {
        return new AesCipher(cipherDesc);
    }

    @Override
    public Provider provider() {
        return provider;
    }

    @Override
    public String[] alias() {
        return new String[] {cipherDesc.getCipherName()};
    }
}
