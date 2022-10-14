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

import java.util.Arrays;

import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.crypto.constant.PhashDesc;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class PhashSpiTest {

    @Test
    public void baseTest() {
        // 基础测试，只测试能不能运行，至于结果是否正确这里不进行保证

        // key，长度不限
        byte[] key = new byte[41];
        Arrays.fill(key, (byte)3);
        for (PhashDesc value : PhashDesc.values()) {
            PhashSpi instance = PhashSpi.getInstance(value);
            instance.init(key);
            instance.phash(new byte[12], new byte[48]);
        }
    }

}
