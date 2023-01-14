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
import java.nio.charset.StandardCharsets;

import org.bouncycastle.openssl.PEMParser;

import com.github.joekerouac.common.tools.crypto.exception.CryptoException;
import com.github.joekerouac.common.tools.io.IOUtils;

/**
 * PEM格式文件工具
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class PemUtil {

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
            return (T)parser.readPemObject();
        } catch (IOException e) {
            // 数据格式不对时抛出该异常
            throw new CryptoException("pem数据读取失败", e);
        }
    }

}
