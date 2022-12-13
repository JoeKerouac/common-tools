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

import java.security.SecureRandom;
import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.crypto.constant.CipherDesc;

/**
 * {@link CipherSpi}测试用例
 * 
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class CipherSpiTest {

    @Test
    public void testCipher() {
        // 基础测试，测试加解密数据是否正常
        for (CipherDesc value : CipherDesc.values()) {
            testCipher(value);
        }
    }

    /**
     * 校验cipher是否符合预期
     *
     * @param desc
     *            加密算法说明
     */
    private void testCipher(CipherDesc desc) {
        byte[] key = new byte[desc.getKeySize()];
        byte[] iv = new byte[desc.getIvLen()];

        CipherSpi cipherSpi = CipherSpi.getInstance(desc);
        // 注意，这里数据必须是3个block，因为后边用的时候写死了.....
        byte[] data = new byte[cipherSpi.getBlockSize() * 4];
        Arrays.fill(data, (byte)3);

        byte[] encryptResult = valid(desc, key, iv, CipherSpi.ENCRYPT_MODE, data);
        byte[] decryptResult = valid(desc, key, iv, CipherSpi.DECRYPT_MODE, encryptResult);

        if (!Arrays.equals(data, decryptResult)) {
            throw new RuntimeException("加解密后数据不一致");
        }

        if (desc.isGcm()) {
            cipherSpi.init(key, iv, CipherSpi.ENCRYPT_MODE);
            cipherSpi.doFinal(data);
            Throwable ex = null;
            try {
                // 对于gcm模式来说，初始化一次只能加密一次，下次加密需要重新初始化，所以这里应该抛异常
                cipherSpi.doFinal(data);
            } catch (Throwable throwable) {
                ex = throwable;
            }
            Assert.assertNotNull(ex);

            ex = null;
            try {
                // 对于gcm模式来说，每次初始化iv应该修改，所以这里应该抛出异常
                cipherSpi.init(key, iv, CipherSpi.ENCRYPT_MODE);
            } catch (Throwable throwable) {
                ex = throwable;
            }
            Assert.assertNotNull(ex);

            // 我们上边初始化的iv所有字节为0，我们将iv的第一个byte修改为1，然后重新初始化cipher，此时应该就能加密成功了
            iv[0] = 1;
            cipherSpi.init(key, iv, CipherSpi.ENCRYPT_MODE);
            cipherSpi.doFinal(data);
        }
    }

    /**
     * 对同一组数据调用不同处理方法验证结果一致，验证成功后将结果返回
     *
     * @param desc
     *            加密算法说明
     * @param key
     *            key
     * @param iv
     *            iv
     * @param mode
     *            加解密模式
     * @param data
     *            要加解密的数据
     * @return 结果
     */
    private byte[] valid(CipherDesc desc, byte[] key, byte[] iv, int mode, byte[] data) {
        byte[] result1 = updateAndDoFinal(desc, key, iv, mode, data);
        byte[] result2 = updateSplitAndDoFinal(desc, key, iv, mode, data);
        byte[] result3 = doFinal(desc, key, iv, mode, data);

        Assert.assertEquals(result1, result2, desc + "模式校验出错，当前加密模式：" + mode);
        Assert.assertEquals(result1, result3, desc + "模式校验出错，当前加密模式：" + mode);

        return result1;
    }

    /**
     * 直接调用doFinal得到结果
     *
     * @param desc
     *            加密算法说明
     * @param key
     *            key
     * @param iv
     *            iv
     * @param mode
     *            加解密模式
     * @param data
     *            要加解密的数据
     * @return 结果
     */
    private byte[] doFinal(CipherDesc desc, byte[] key, byte[] iv, int mode, byte[] data) {
        CipherSpi cipherSpi = CipherSpi.getInstance(desc);
        cipherSpi.init(key, iv, mode, new SecureRandom());
        return cipherSpi.doFinal(data);
    }

    /**
     * 先将数据一次性update到cipher，然后获取结果的方法
     *
     * @param desc
     *            加密算法说明
     * @param key
     *            key
     * @param iv
     *            iv
     * @param mode
     *            加解密模式
     * @param data
     *            要加解密的数据
     * @return 结果
     */
    private byte[] updateAndDoFinal(CipherDesc desc, byte[] key, byte[] iv, int mode, byte[] data) {
        CipherSpi cipherSpi = CipherSpi.getInstance(desc);
        cipherSpi.init(key, iv, mode, new SecureRandom());
        byte[] result = cipherSpi.update(data);
        byte[] finalResult = cipherSpi.doFinal();
        if (finalResult.length != 0) {
            byte[] b = new byte[result.length + finalResult.length];
            System.arraycopy(result, 0, b, 0, result.length);
            System.arraycopy(finalResult, 0, b, result.length, finalResult.length);
            result = b;
        }
        return result;
    }

    /**
     * 将数据拆分多组update到cipher，然后获取结果
     *
     * @param desc
     *            加密算法说明
     * @param key
     *            key
     * @param iv
     *            iv
     * @param mode
     *            加解密模式
     * @param data
     *            要加解密的数据
     * @return 结果
     */
    private byte[] updateSplitAndDoFinal(CipherDesc desc, byte[] key, byte[] iv, int mode, byte[] data) {
        CipherSpi cipherSpi = CipherSpi.getInstance(desc);
        cipherSpi.init(key, iv, mode, new SecureRandom());

        // 得到多个结果，注意，这里java自带SunJCE实现和BouncyCastleProvider有区别：
        // SunJCE实现的AES解密在这里返回的result1-result4都是空数组，最后doFinal才会返回结果；
        // BouncyCastleProvider实现的AES解密在这里分段解密的时候result1是空的，result2、result3、result4拼接起来就是结果了，最后doFinal则会返回空数组
        byte[] result1 = cipherSpi.update(Arrays.copyOfRange(data, 0, cipherSpi.getBlockSize()));
        byte[] result2 =
            cipherSpi.update(Arrays.copyOfRange(data, cipherSpi.getBlockSize(), cipherSpi.getBlockSize() * 2));
        byte[] result3 =
            cipherSpi.update(Arrays.copyOfRange(data, cipherSpi.getBlockSize() * 2, cipherSpi.getBlockSize() * 3));
        byte[] result4 = cipherSpi.update(Arrays.copyOfRange(data, cipherSpi.getBlockSize() * 3, data.length));

        byte[] result = new byte[result1.length + result2.length + result3.length + result4.length];
        System.arraycopy(result1, 0, result, 0, result1.length);
        System.arraycopy(result2, 0, result, result1.length, result2.length);
        System.arraycopy(result3, 0, result, result1.length + result2.length, result3.length);
        System.arraycopy(result4, 0, result, result1.length + result2.length + result3.length, result4.length);

        // GCM模式下这个返回不是空
        byte[] finalResult = cipherSpi.doFinal();
        if (finalResult.length != 0) {
            byte[] b = new byte[result.length + finalResult.length];
            System.arraycopy(result, 0, b, 0, result.length);
            System.arraycopy(finalResult, 0, b, result.length, finalResult.length);
            result = b;
        }
        return result;
    }
}
