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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.crypto.constant.ECDHKeyPair;
import com.github.joekerouac.common.tools.crypto.constant.NamedCurve;
import com.github.joekerouac.common.tools.crypto.impl.BCECDHKeyExchangeSpi;
import com.github.joekerouac.common.tools.crypto.impl.SunecECDHKeyExchangeSpi;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class ECDHKeyExchangeSpiTest {

    @Test(dataProvider = "dataProvider")
    public void baseTest(ECDHKeyExchangeSpi[] spiArray) {
        // 基础测试，测试是否可以成功交换密钥

        // 测试所有的实现
        for (ECDHKeyExchangeSpi spi : spiArray) {
            // 原生的支持依赖于JDK版本，所以不对原生的进行测试
            if (spi instanceof SunecECDHKeyExchangeSpi) {
                continue;
            }
            // 测试所有曲线类型
            for (NamedCurve curve : NamedCurve.getAllSupportCurve()) {
                ECDHKeyPair aliceKeyPair = spi.generate(curve.getId());
                ECDHKeyPair bobKeyPair = spi.generate(curve.getId());
                // alice使用自己的私钥和bob的公钥生成的密钥
                byte[] aliceKey =
                    spi.keyExchange(bobKeyPair.getPublicKey(), aliceKeyPair.getPrivateKey(), curve.getId());
                // bob使用自己的私钥和alice的公钥生成的密钥
                byte[] bobKey = spi.keyExchange(aliceKeyPair.getPublicKey(), bobKeyPair.getPrivateKey(), curve.getId());
                Assert.assertEquals(bobKey, aliceKey);
            }
        }
    }

    @DataProvider
    public Object[][] dataProvider() {
        return new Object[][] {{new BCECDHKeyExchangeSpi()}, {new SunecECDHKeyExchangeSpi()}};
    }
}
