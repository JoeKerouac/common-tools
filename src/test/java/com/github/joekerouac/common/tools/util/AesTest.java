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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.crypto.constant.CipherDesc;

/**
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class AesTest {

    @Test
    public void baseTest() {
        // 基本加解密测试
        byte[] baseData = "你好呀".getBytes(StandardCharsets.UTF_8);
        for (CipherDesc value : CipherDesc.values()) {
            Aes aes = new Aes(Aes.generateKey(value), Aes.generateIv(value), value);
            // AES固定16
            byte[] data = value.isHasPadding() ? baseData : pkcs7Padding(baseData);
            byte[] encrypt = aes.encrypt(data);
            // 对于GCM模式要测试是否可以重用
            if (value.isGcm()) {
                encrypt = aes.encrypt(data);
            }

            byte[] decrypt = aes.decrypt(encrypt);
            if (value.isGcm()) {
                decrypt = aes.decrypt(encrypt);
            }

            // 手动取消padding
            if (!value.isHasPadding()) {
                decrypt = pkcs7UnPadding(decrypt);
            }

            Assert.assertEquals(decrypt, baseData, value + "模式验证错误");
        }
    }

    private byte[] pkcs7Padding(byte[] data) {
        int newLen = (data.length / 16 + 1) * 16;
        byte[] newData = new byte[newLen];
        System.arraycopy(data, 0, newData, 0, data.length);
        Arrays.fill(newData, data.length, newLen, (byte)(newLen - data.length));
        return newData;
    }

    private byte[] pkcs7UnPadding(byte[] data) {
        return Arrays.copyOfRange(data, 0, data.length - data[data.length - 1]);
    }

}
