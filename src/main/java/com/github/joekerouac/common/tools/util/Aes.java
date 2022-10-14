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
package com.github.joekerouac.common.tools.util;

import java.security.SecureRandom;
import java.util.function.Function;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.crypto.CipherSpi;
import com.github.joekerouac.common.tools.crypto.constant.CipherDesc;

/**
 * AES加解密封装，注意，如果选择的算法没有padding，需要自己对齐数据，数据长度需要是{@link CipherSpi#getBlockSize()}的整数倍；
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class Aes {

    private final ObjectPool<CipherSpi> encryptCipherPool;

    private final ObjectPool<CipherSpi> decryptCipherPool;

    /**
     * 默认构造器
     *
     * @param key
     *            key
     * @param iv
     *            iv
     * @param desc
     *            要采用的算法说明
     */
    public Aes(byte[] key, byte[] iv, CipherDesc desc) {
        this(key, iv, desc, 30, 5);
    }

    /**
     * 默认构造器
     * 
     * @param key
     *            key
     * @param iv
     *            iv
     * @param desc
     *            要采用的算法说明
     * @param maxIdle
     *            加解密最大并发数量（例如设置为10，则加密和解密各自最大并发都是10）
     * @param minIdle
     *            加解密最小并发
     */
    public Aes(byte[] key, byte[] iv, CipherDesc desc, int maxIdle, int minIdle) {
        Assert.argNotNull(key, "key");
        Assert.argNotNull(iv, "iv");
        Assert.argNotNull(desc, "desc");
        Assert.assertTrue(desc.getKeySize() == key.length, "key格式不对",
            ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.assertTrue(desc.getIvLen() == iv.length, "iv格式不对",
            ExceptionProviderConst.IllegalArgumentExceptionProvider);

        GenericObjectPoolConfig<CipherSpi> config = new GenericObjectPoolConfig<>();
        config.setMaxIdle(maxIdle);
        config.setMaxTotal(maxIdle);
        config.setMinIdle(minIdle);

        this.encryptCipherPool = new GenericObjectPool<>(new PooledObjectFactory<CipherSpi>() {

            @Override
            public PooledObject<CipherSpi> makeObject() throws Exception {
                CipherSpi cipherSpi = CipherSpi.getInstance(desc);
                cipherSpi.init(key, iv, CipherSpi.ENCRYPT_MODE);
                return new DefaultPooledObject<>(cipherSpi);
            }

            @Override
            public void destroyObject(final PooledObject<CipherSpi> p) throws Exception {

            }

            @Override
            public boolean validateObject(final PooledObject<CipherSpi> p) {
                return true;
            }

            @Override
            public void activateObject(final PooledObject<CipherSpi> p) throws Exception {

            }

            @Override
            public void passivateObject(final PooledObject<CipherSpi> p) throws Exception {

            }
        }, config);

        this.decryptCipherPool = new GenericObjectPool<>(new PooledObjectFactory<CipherSpi>() {

            @Override
            public PooledObject<CipherSpi> makeObject() throws Exception {
                CipherSpi cipherSpi = CipherSpi.getInstance(desc);
                cipherSpi.init(key, iv, CipherSpi.DECRYPT_MODE);
                return new DefaultPooledObject<>(cipherSpi);
            }

            @Override
            public void destroyObject(final PooledObject<CipherSpi> p) throws Exception {

            }

            @Override
            public boolean validateObject(final PooledObject<CipherSpi> p) {
                return true;
            }

            @Override
            public void activateObject(final PooledObject<CipherSpi> p) throws Exception {

            }

            @Override
            public void passivateObject(final PooledObject<CipherSpi> p) throws Exception {

            }
        }, config);
    }

    /**
     * 随机生成一个指定算法的key
     * 
     * @param desc
     *            算法说明
     * @return 随机key
     */
    public static byte[] generateKey(CipherDesc desc) {
        byte[] key = new byte[desc.getKeySize()];
        new SecureRandom().nextBytes(key);
        return key;
    }

    /**
     * 随机生成一个指定算法的iv
     * 
     * @param desc
     *            算法说明
     * @return iv
     */
    public static byte[] generateIv(CipherDesc desc) {
        if (desc.getIvLen() == 0) {
            return new byte[0];
        }

        byte[] iv = new byte[desc.getIvLen()];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * 加密数据
     * 
     * @param data
     *            要加密的数据
     * @return 加密结果
     */
    public byte[] encrypt(byte[] data) {
        return runWithCipher(encryptCipherPool, cipherSpi -> cipherSpi.doFinal(data));
    }

    /**
     * 解密
     * 
     * @param data
     *            要解密的数据
     * @return 解密结果
     */
    public byte[] decrypt(byte[] data) {
        return runWithCipher(decryptCipherPool, cipherSpi -> cipherSpi.doFinal(data));
    }

    /**
     * 使用指定cipher pool提供的cipher运行函数
     * 
     * @param cipherPool
     *            cipher pool
     * @param function
     *            函数
     * @return 结果
     */
    private byte[] runWithCipher(ObjectPool<CipherSpi> cipherPool, Function<CipherSpi, byte[]> function) {
        CipherSpi cipherSpi = null;
        try {
            cipherSpi = cipherPool.borrowObject();
            try {
                return function.apply(cipherSpi);
            } finally {
                cipherPool.returnObject(cipherSpi);
            }
        } catch (Throwable throwable) {
            throw new RuntimeException("加解密过程中发生异常", throwable);
        }
    }

}
