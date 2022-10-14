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

import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.KeyAgreement;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.crypto.ECDHKeyExchangeSpi;
import com.github.joekerouac.common.tools.crypto.constant.ECDHKeyPair;
import com.github.joekerouac.common.tools.crypto.exception.CryptoException;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public abstract class AbstractECDHKeyExchangeSpi implements ECDHKeyExchangeSpi {

    /**
     * 根据AlgorithmParameterSpec获取fieldSize
     * 
     * @param ecParamter
     *            AlgorithmParameterSpec
     * @return fieldSize
     */
    protected abstract int fieldSize(AlgorithmParameterSpec ecParamter);

    /**
     * 获取ECParameterSpec
     * 
     * @param curveId
     *            curveId
     * @return ECParameterSpec
     */
    protected abstract AlgorithmParameterSpec getECParameterSpec(int curveId);

    /**
     * 算法提供厂商
     * 
     * @return 算法提供厂商
     */
    protected abstract Provider provider();

    /**
     * 将公钥数据转换为KeySpec
     * 
     * @param curveId
     *            curveId
     * @param publicKeyData
     *            publicKeyData
     * @return KeySpec
     */
    protected abstract KeySpec convertToPublicKeySpec(int curveId, byte[] publicKeyData);

    /**
     * 将私钥数据转换为KeySpec
     * 
     * @param curveId
     *            curveId
     * @param privateKeyData
     *            privateKeyData
     * @return KeySpec
     */
    protected abstract KeySpec convertToPrivateKeySpec(int curveId, byte[] privateKeyData);

    @Override
    public byte[] keyExchange(byte[] publicKeyData, byte[] privateKeyData, int curveId) {
        Assert.notNull(publicKeyData, "publicKeyData 不能为 null",
            ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.notNull(privateKeyData, "privateKeyData 不能为 null",
            ExceptionProviderConst.IllegalArgumentExceptionProvider);

        try {
            KeyFactory factory = KeyFactory.getInstance("EC", provider());
            PublicKey publicKey = factory.generatePublic(convertToPublicKeySpec(curveId, publicKeyData));
            PrivateKey privateKey = factory.generatePrivate(convertToPrivateKeySpec(curveId, privateKeyData));
            KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", provider());
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(publicKey, true);
            return keyAgreement.generateSecret();
        } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new CryptoException(StringUtils.format("publicKey: [{}], privateKey: [{}], curveId: [{}]",
                publicKeyData, privateKeyData, curveId), e);
        }
    }

    @Override
    public ECDHKeyPair generate(int curveId) {
        AlgorithmParameterSpec parameters = getECParameterSpec(curveId);

        try {
            KeyPairGenerator factory = KeyPairGenerator.getInstance("EC", provider());
            factory.initialize(parameters);
            KeyPair keyPair = factory.genKeyPair();
            ECDHKeyPair ecdhKeyPair = new ECDHKeyPair();
            ECPrivateKey ecPrivateKey = (ECPrivateKey)keyPair.getPrivate();
            ECPublicKey ecPublicKey = (ECPublicKey)keyPair.getPublic();

            ecdhKeyPair.setPublicKey(encodePoint(ecPublicKey.getW(), fieldSize(parameters)));
            ecdhKeyPair.setPrivateKey(ecPrivateKey.getS().toByteArray());
            return ecdhKeyPair;
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 对point进行编码
     * 
     * @param point
     *            point
     * @param fieldSize
     *            fieldSize
     * @return 编码结果
     */
    private byte[] encodePoint(ECPoint point, int fieldSize) {
        int var2 = fieldSize + 7 >> 3;
        byte[] var3 = trimZeroes(point.getAffineX().toByteArray());
        byte[] var4 = trimZeroes(point.getAffineY().toByteArray());
        if (var3.length <= var2 && var4.length <= var2) {
            byte[] var5 = new byte[1 + (var2 << 1)];
            var5[0] = 4;
            System.arraycopy(var3, 0, var5, var2 - var3.length + 1, var3.length);
            System.arraycopy(var4, 0, var5, var5.length - var4.length, var4.length);
            return var5;
        } else {
            throw new CryptoException("Point coordinates do not match field size");
        }
    }

    /**
     * 将指定数组中开头位置的0去除
     * 
     * @param array
     *            数组
     * @return 去除开头0后的数组
     */
    private byte[] trimZeroes(byte[] array) {
        int offset = 0;

        while (offset < array.length - 1 && array[offset] == 0) {
            offset++;
        }

        return offset == 0 ? array : Arrays.copyOfRange(array, offset, array.length);
    }

}
