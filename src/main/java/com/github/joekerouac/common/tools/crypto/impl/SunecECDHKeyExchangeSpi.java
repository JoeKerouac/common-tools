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
import java.security.spec.*;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.crypto.ECDHKeyExchangeSpi;
import com.github.joekerouac.common.tools.crypto.constant.NamedCurve;
import com.github.joekerouac.common.tools.crypto.exception.CryptoException;
import com.github.joekerouac.common.tools.reflect.ReflectUtil;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * 使用sunec实现的ECDH密钥交换
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class SunecECDHKeyExchangeSpi extends AbstractECDHKeyExchangeSpi implements ECDHKeyExchangeSpi {

    private static final String SUN_EC_PROVIDER = "sun.security.ec.SunEC.SunEC";

    private static final String SUN_EC_UTIL = "sun.security.util.ECUtil";

    @Override
    protected Provider provider() {
        try {
            return (Provider)Class.forName(SUN_EC_PROVIDER).newInstance();
        } catch (Throwable throwable) {
            throw new CryptoException(StringUtils.format("实例化 [{}] 失败，可能是当前使用的jdk不是Oracle的", SUN_EC_PROVIDER),
                throwable);
        }
    }

    @Override
    protected KeySpec convertToPublicKeySpec(int curveId, byte[] publicKeyData) {
        Assert.notNull(publicKeyData, "publicKey 不能为null", ExceptionProviderConst.IllegalArgumentExceptionProvider);

        ECParameterSpec parameters = (ECParameterSpec)getECParameterSpec(curveId);
        ECPoint point;
        try {
            point = ReflectUtil.invoke(Class.forName(SUN_EC_UTIL), "decodePoint",
                new Class[] {byte[].class, EllipticCurve.class}, new Object[] {publicKeyData, parameters.getCurve()});
        } catch (ClassNotFoundException e) {
            throw new CryptoException(StringUtils.format("类 [{}] 不存在，请检查当前是否是使用的Oracle JDK", SUN_EC_UTIL), e);
        }
        return new ECPublicKeySpec(point, parameters);
    }

    @Override
    protected KeySpec convertToPrivateKeySpec(int curveId, byte[] privateKeyData) {
        ECParameterSpec parameters = (ECParameterSpec)getECParameterSpec(curveId);

        return new ECPrivateKeySpec(new BigInteger(privateKeyData), parameters);
    }

    @Override
    protected AlgorithmParameterSpec getECParameterSpec(int curveId) {
        try {
            return ReflectUtil.invoke(Class.forName(SUN_EC_UTIL), "getECParameterSpec",
                new Class[] {Provider.class, String.class},
                new Object[] {provider(), NamedCurve.getCurveName(curveId)});
        } catch (ClassNotFoundException e) {
            throw new CryptoException(StringUtils.format("类 [{}] 不存在，请检查当前是否是使用的Oracle JDK", SUN_EC_UTIL), e);
        }
    }

    @Override
    protected int fieldSize(AlgorithmParameterSpec ecParamter) {
        Assert.assertTrue(ecParamter instanceof ECParameterSpec, "不支持的参数：" + ecParamter,
            ExceptionProviderConst.IllegalArgumentExceptionProvider);

        return ((ECParameterSpec)ecParamter).getCurve().getField().getFieldSize();
    }

}
