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

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.x509.X509V3CertificateGenerator;
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

            read = PemUtil.readFromPEM(write.getBytes());
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

            read = PemUtil.readFromPEM(write.getBytes());
            Assert.assertTrue(read instanceof PublicKey);

            read = PemUtil.read(publicKey.getEncoded(), "RSA");
            Assert.assertTrue(read instanceof PublicKey);

            read = PemUtil.read(Base64.getEncoder().encode(publicKey.getEncoded()), "RSA");
            Assert.assertTrue(read instanceof PublicKey);
        }

        {

            // 创建X.509证书生成器
            X509V3CertificateGenerator certGenerator = new X509V3CertificateGenerator();
            X500Principal issuer = new X500Principal("CN=Issuer");
            X500Principal subject = new X500Principal("CN=Subject");
            certGenerator.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()));
            certGenerator.setIssuerDN(issuer);
            certGenerator.setNotBefore(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1)));
            certGenerator.setNotAfter(new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(365)));
            certGenerator.setSubjectDN(subject);
            certGenerator.setPublicKey(keyPair.getPublic());
            certGenerator.setSignatureAlgorithm("SHA256WithRSA");

            // 使用私钥对证书进行签名
            PrivateKey privateKey = keyPair.getPrivate();
            X509Certificate certificate = certGenerator.generate(privateKey);
            String write = PemUtil.write(certificate);
            Object read = PemUtil.read(write.getBytes(), "X.509");
            Assert.assertTrue(read instanceof X509Certificate);

            read = PemUtil.readFromPEM(write.getBytes());
            Assert.assertTrue(read instanceof X509Certificate);

            read = PemUtil.read(certificate.getEncoded(), "X.509");
            Assert.assertTrue(read instanceof X509Certificate);

            read = PemUtil.read(Base64.getEncoder().encode(certificate.getEncoded()), "X.509");
            Assert.assertTrue(read instanceof X509Certificate);

        }

    }

}
