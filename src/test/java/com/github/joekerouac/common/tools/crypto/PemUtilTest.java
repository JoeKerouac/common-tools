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

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author JoeKerouac
 * @date 2023-09-01 10:57
 * @since 2.1.0
 */
public class PemUtilTest {

    @Test
    public void test() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        {

            PrivateKey privateKey = keyPair.getPrivate();
            String write = PemUtil.write(privateKey);
            Object read = PemUtil.read(write.getBytes(), "RSA");
            Assert.assertTrue(read instanceof PrivateKey);

            read = PemUtil.read(write.getBytes());
            Assert.assertTrue(read instanceof PrivateKey);

            read = PemUtil.read(privateKey.getEncoded(), "RSA");
            Assert.assertTrue(read instanceof PrivateKey);

            read = PemUtil.read(Base64.getEncoder().encode(privateKey.getEncoded()), "RSA");
            Assert.assertTrue(read instanceof PrivateKey);
        }

        {
            PublicKey publicKey = keyPair.getPublic();
            String write = PemUtil.write(publicKey);
            Object read = PemUtil.read(write.getBytes(), "RSA");
            Assert.assertTrue(read instanceof PublicKey);

            read = PemUtil.read(write.getBytes());
            Assert.assertTrue(read instanceof PublicKey);

            read = PemUtil.read(publicKey.getEncoded(), "RSA");
            Assert.assertTrue(read instanceof PublicKey);

            read = PemUtil.read(Base64.getEncoder().encode(publicKey.getEncoded()), "RSA");
            Assert.assertTrue(read instanceof PublicKey);
        }

    }

}
