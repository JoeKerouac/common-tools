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

import java.math.BigInteger;
import java.security.Provider;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPrivateKeySpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.crypto.ECDHKeyExchangeSpi;
import com.github.joekerouac.common.tools.crypto.constant.NamedCurve;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * 使用bouncycastle实现的ECDH密钥交换算法
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class BCECDHKeyExchangeSpi extends AbstractECDHKeyExchangeSpi implements ECDHKeyExchangeSpi {

    private static final Map<String, X9ECParameters> CACHE = new ConcurrentHashMap<>();

    private static final Provider BC_PROVIDER = new BouncyCastleProvider();

    @Override
    protected Provider provider() {
        return BC_PROVIDER;
    }

    @Override
    protected KeySpec convertToPublicKeySpec(int curveId, byte[] publicKeyData) {
        ECDomainParameters domainParameters = getECDomainParameters(curveId);

        // 使用指定数据解析出ECPoint
        ECPoint Q = domainParameters.getCurve().decodePoint(publicKeyData);

        ECParameterSpec ecParameterSpec = internalGetECParameterSpec(curveId);
        return new ECPublicKeySpec(Q, ecParameterSpec);
    }

    @Override
    protected KeySpec convertToPrivateKeySpec(int curveId, byte[] privateKeyData) {
        ECParameterSpec ecParameterSpec = internalGetECParameterSpec(curveId);
        return new ECPrivateKeySpec(new BigInteger(privateKeyData), ecParameterSpec);
    }

    @Override
    protected AlgorithmParameterSpec getECParameterSpec(int curveId) {
        return internalGetECParameterSpec(curveId);
    }

    @Override
    protected int fieldSize(AlgorithmParameterSpec ecParamter) {
        if (!(ecParamter instanceof ECParameterSpec)) {
            throw new IllegalArgumentException("不支持的参数：" + ecParamter);
        }
        return ((ECParameterSpec)ecParamter).getCurve().getFieldSize();
    }

    /**
     * 根据curveId获取ECDomainParameters
     * 
     * @param curveId
     *            curveId
     * @return ECDomainParameters
     */
    private static ECDomainParameters getECDomainParameters(int curveId) {
        String curveName = getCurveName(curveId);

        X9ECParameters ecP = getX9ECParameters(curveName);

        return new ECDomainParameters(ecP.getCurve(), ecP.getG(), ecP.getN(), ecP.getH(), ecP.getSeed());
    }

    /**
     * 根据curveId获取ECDomainParameters
     * 
     * @param curveId
     *            curveId
     * @return ECDomainParameters
     */
    private static ECParameterSpec internalGetECParameterSpec(int curveId) {
        String curveName = getCurveName(curveId);

        X9ECParameters ecP = getX9ECParameters(curveName);

        return new ECNamedCurveParameterSpec(curveName, ecP.getCurve(), ecP.getG(), ecP.getN(), ecP.getH(),
            ecP.getSeed());
    }

    /**
     * 根据曲线ID获取曲线名
     * 
     * @param curveId
     *            曲线ID
     * @return 曲线名
     */
    private static String getCurveName(int curveId) {
        String curveName = NamedCurve.getCurveName(curveId);
        Assert.notBlank(curveName, "不支持的 curveId: " + curveId, ExceptionProviderConst.IllegalArgumentExceptionProvider);
        return curveName;
    }

    /**
     * 根据曲线名获取曲线参数
     * 
     * @param curveName
     *            曲线名
     * @return 曲线参数
     */
    private static X9ECParameters getX9ECParameters(String curveName) {
        X9ECParameters ecP = CACHE.compute(curveName, (name, parameter) -> {
            if (parameter == null) {
                return SECNamedCurves.getByName(curveName);
            } else {
                return parameter;
            }
        });

        Assert.notNull(ecP, "不支持的 curveName: " + curveName, ExceptionProviderConst.IllegalArgumentExceptionProvider);
        return ecP;
    }
}
