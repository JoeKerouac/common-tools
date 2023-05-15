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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

import com.github.joekerouac.common.tools.crypto.exception.CryptoException;
import com.github.joekerouac.common.tools.io.IOUtils;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * PEM格式文件工具
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class PemUtil {

    /**
     * 从pem中读取key数据
     *
     * @param keyData
     *            pem数据
     * @param keyAlgorithm
     *            key算法
     * @param <T>
     *            key类型
     * @return pem中读取到的数据
     * @throws NoSuchAlgorithmException
     *             NoSuchAlgorithmException
     * @throws InvalidKeyException
     *             InvalidKeyException
     * @throws InvalidKeySpecException
     *             InvalidKeySpecException
     */
    @SuppressWarnings("unchecked")
    public static <T> T read(byte[] keyData, String keyAlgorithm)
        throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        Object read = read(keyData);
        // 如果不是pem编码的则返回null，此时可能是X.509编码的公钥，也可能是PKCS#8编码的私钥
        if (read == null) {
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);

            try {
                read = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyData));
            } catch (Throwable throwable) {
                read = keyFactory.generatePublic(new X509EncodedKeySpec(keyData));
            }
        }

        if (read instanceof PEMKeyPair || read instanceof SubjectPublicKeyInfo || read instanceof PrivateKeyInfo) {
            PublicKey publicKey = null;
            PrivateKey privateKey = null;

            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
            try {
                if (read instanceof PEMKeyPair) {
                    PKCS8EncodedKeySpec pkcs8EncodedKeySpec =
                        new PKCS8EncodedKeySpec(((PEMKeyPair)read).getPrivateKeyInfo().getEncoded());
                    X509EncodedKeySpec x509EncodedKeySpec =
                        new X509EncodedKeySpec(((PEMKeyPair)read).getPublicKeyInfo().getEncoded());

                    publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
                    privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
                } else if (read instanceof SubjectPublicKeyInfo) {
                    X509EncodedKeySpec x509EncodedKeySpec =
                        new X509EncodedKeySpec(((SubjectPublicKeyInfo)read).getEncoded());
                    publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
                } else {
                    PKCS8EncodedKeySpec pkcs8EncodedKeySpec =
                        new PKCS8EncodedKeySpec(((PrivateKeyInfo)read).getEncoded());
                    privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
                }
            } catch (IOException e) {
                throw new RuntimeException("密钥解析失败", e);
            }

            if (publicKey != null && privateKey != null) {
                read = new KeyPair(publicKey, privateKey);
            } else {
                read = publicKey == null ? privateKey : publicKey;
            }
        }
        return (T)read;
    }

    /**
     * 将pem编码的密钥转换为对应的密钥对象，pem编码的文件以-----BEGIN xxxx-----开头
     *
     * @param inputStream
     *            pem编码的密钥输入流
     * @param <T>
     *            密钥的实际类型
     * @return 密钥对象，根据文件内容确定返回密钥对象类型
     */
    public static <T> T read(InputStream inputStream) {
        return read(IOUtils.read(inputStream, true));
    }

    /**
     * 将pem编码的密钥转换为对应的密钥对象，pem编码的文件以-----BEGIN xxxx-----开头
     * 
     * @param data
     *            pem编码的密钥
     * @param <T>
     *            密钥的实际类型
     * @return 密钥对象，根据文件内容确定返回密钥对象类型
     */
    @SuppressWarnings("unchecked")
    public static <T> T read(byte[] data) {
        PEMParser parser = new PEMParser(new StringReader(new String(data, StandardCharsets.UTF_8)));
        try {
            return (T)parser.readObject();
        } catch (IOException e) {
            // 数据格式不对时抛出该异常
            throw new CryptoException("pem数据读取失败", e);
        }
    }

    /**
     * 将密钥写出为pem文件
     * 
     * @param key
     *            密钥
     * @return pem文件，例如：以-----BEGIN PRIVATE KEY-----开头
     */
    public static String write(Key key) {
        String type;
        if (key instanceof PublicKey) {
            type = "PUBLIC KEY";
        } else if (key instanceof PrivateKey) {
            type = "PRIVATE KEY";
        } else {
            throw new UnsupportedOperationException(StringUtils.format("不支持的密钥类型： [{}]", key.getClass()));
        }

        StringWriter stringWriter = new StringWriter();
        PemWriter pemWriter = new PemWriter(stringWriter);
        try {
            pemWriter.writeObject(new PemObject(type, key.getEncoded()));
            pemWriter.flush();
            pemWriter.close();
        } catch (IOException e) {
            // 不可能走到这里
            throw new RuntimeException(e);
        }
        return stringWriter.toString();
    }

}
