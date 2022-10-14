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

import java.nio.charset.StandardCharsets;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.crypto.constant.DigestDesc;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class DigestSpiTest {

    @Test
    public void baseTest() {
        // 基础测试，只测试能不能运行，至于结果是否正确这里不进行保证
        byte[] data = "你好呀".getBytes(StandardCharsets.UTF_8);
        for (DigestDesc value : DigestDesc.values()) {
            DigestSpi instance = DigestSpi.getInstance(value);
            instance.update(data);
            Assert.assertNotNull(instance.digest());
        }
    }
}
