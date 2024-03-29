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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
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

    private static final String CERT_NAME = "X.509";

    /**
     * 将密钥数据反序列化为对象
     *
     * @param keyData
     *            密钥数据，支持pem格式、X.509编码的公钥、PKCS#8编码的私钥以及X.509编码的公钥、PKCS#8编码的私钥的base64
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
        throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, CertificateException {
        Object read = read(keyData);

        if (read != null) {
            return (T)read;
        }

        if (CERT_NAME.equals(keyAlgorithm)) {
            try {
                CertificateFactory certificateFactory = CertificateFactory.getInstance(CERT_NAME);
                return (T)certificateFactory.generateCertificate(new ByteArrayInputStream(keyData));
            } catch (Throwable throwable) {
                // 忽略异常，继续尝试其他方案
            }

            try {
                CertificateFactory certificateFactory = CertificateFactory.getInstance(CERT_NAME);
                return (T)certificateFactory
                    .generateCertificate(new ByteArrayInputStream(Base64.getDecoder().decode(keyData)));
            } catch (Throwable throwable) {
                // 忽略异常，继续尝试其他方案
            }
        } else {
            // 如果不是pem编码的则返回null，此时可能是X.509编码的公钥，也可能是PKCS#8编码的私钥
            KeyFactory keyFactory = KeyFactory.getInstance(keyAlgorithm);
            try {
                return (T)keyFactory.generatePrivate(new PKCS8EncodedKeySpec(keyData));
            } catch (Throwable throwable) {
                // 忽略异常，继续尝试其他方案
            }

            try {
                return (T)keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyData)));
            } catch (Throwable throwable) {
                // 忽略异常，继续尝试其他方案
            }

            try {
                return (T)keyFactory.generatePublic(new X509EncodedKeySpec(keyData));
            } catch (Throwable throwable) {
                // 忽略异常，继续尝试其他方案
            }

            try {
                return (T)keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(keyData)));
            } catch (Throwable throwable) {
                // 忽略异常，继续尝试其他方案
            }

        }
        throw new IllegalArgumentException("提供的密钥信息有误，读取失败");
    }

    /**
     * 将pem编码的密钥转换为对应的密钥对象，pem编码的文件以-----BEGIN xxxx-----开头
     *
     * @param inputStream
     *            pem编码的密钥输入流
     * @param <T>
     *            密钥的实际类型
     * @return 密钥对象，根据文件内容确定返回密钥对象类型
     * @throws NoSuchAlgorithmException
     *             NoSuchAlgorithmException
     * @throws InvalidKeyException
     *             InvalidKeyException
     * @throws InvalidKeySpecException
     *             InvalidKeySpecException
     */
    public static <T> T read(InputStream inputStream)
        throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, CertificateException {
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
     * @throws NoSuchAlgorithmException
     *             NoSuchAlgorithmException
     * @throws InvalidKeyException
     *             InvalidKeyException
     * @throws InvalidKeySpecException
     *             InvalidKeySpecException
     */
    @SuppressWarnings("unchecked")
    public static <T> T read(byte[] data)
        throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, CertificateException {
        PEMParser parser = new PEMParser(new StringReader(new String(data, StandardCharsets.UTF_8)));
        Object read;
        try {
            read = parser.readObject();
        } catch (IOException e) {
            // 数据格式不对时抛出该异常
            throw new CryptoException("pem数据读取失败", e);
        }

        if (read instanceof PEMKeyPair || read instanceof SubjectPublicKeyInfo || read instanceof PrivateKeyInfo) {
            PublicKey publicKey = null;
            PrivateKey privateKey = null;

            KeyFactory keyFactory;
            try {
                if (read instanceof PEMKeyPair) {
                    PEMKeyPair pair = (PEMKeyPair)read;
                    KnownOIDs match =
                        KnownOIDs.findMatch(pair.getPrivateKeyInfo().getPrivateKeyAlgorithm().getAlgorithm().getId());
                    keyFactory = KeyFactory.getInstance(match.stdName());

                    PKCS8EncodedKeySpec pkcs8EncodedKeySpec =
                        new PKCS8EncodedKeySpec(pair.getPrivateKeyInfo().getEncoded());
                    X509EncodedKeySpec x509EncodedKeySpec =
                        new X509EncodedKeySpec(((PEMKeyPair)read).getPublicKeyInfo().getEncoded());

                    publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
                    privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
                } else if (read instanceof SubjectPublicKeyInfo) {
                    SubjectPublicKeyInfo subjectPublicKeyInfo = (SubjectPublicKeyInfo)read;
                    KnownOIDs match = KnownOIDs.findMatch(subjectPublicKeyInfo.getAlgorithm().getAlgorithm().getId());
                    keyFactory = KeyFactory.getInstance(match.stdName());

                    X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec((subjectPublicKeyInfo).getEncoded());
                    publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
                } else {
                    PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo)read;
                    KnownOIDs match =
                        KnownOIDs.findMatch(privateKeyInfo.getPrivateKeyAlgorithm().getAlgorithm().getId());
                    keyFactory = KeyFactory.getInstance(match.stdName());
                    PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyInfo.getEncoded());
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
        } else if (read instanceof X509CertificateHolder) {
            X509CertificateHolder certificateHolder = (X509CertificateHolder)read;
            byte[] certificateData;
            try {
                certificateData = certificateHolder.getEncoded();
            } catch (IOException e) {
                // 理论上不可能
                throw new RuntimeException(e);
            }
            ByteArrayInputStream inputStream = new ByteArrayInputStream(certificateData);

            CertificateFactory certificateFactory = CertificateFactory.getInstance(CERT_NAME);
            read = certificateFactory.generateCertificate(inputStream);
        }

        return (T)read;

    }

    /**
     * 将密钥或者证书写出为pem文件
     * 
     * @param key
     *            要写出的数据，可以是rsa公私钥，也可以是证书
     * @return pem文件，例如：以-----BEGIN PRIVATE KEY-----开头
     */
    public static String write(Object key) {
        String type;
        byte[] encode;
        if (key instanceof PublicKey) {
            type = "PUBLIC KEY";
            encode = ((PublicKey)key).getEncoded();
        } else if (key instanceof PrivateKey) {
            type = "PRIVATE KEY";
            encode = ((PrivateKey)key).getEncoded();
        } else if (key instanceof Certificate) {
            type = "CERTIFICATE";
            try {
                encode = ((Certificate)key).getEncoded();
            } catch (CertificateEncodingException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new UnsupportedOperationException(StringUtils.format("不支持的密钥类型： [{}]", key.getClass()));
        }

        StringWriter stringWriter = new StringWriter();
        PemWriter pemWriter = new PemWriter(stringWriter);
        try {
            pemWriter.writeObject(new PemObject(type, encode));
            pemWriter.flush();
            pemWriter.close();
        } catch (IOException e) {
            // 不可能走到这里
            throw new RuntimeException(e);
        }
        return stringWriter.toString();
    }

}
