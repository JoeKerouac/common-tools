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
package com.github.joekerouac.common.tools.codec;

/**
 * hex编码
 * 
 * @author JoeKerouac
 * @date 2023-06-05 11:37
 * @since 2.0.3
 */
public class HexCodec {

    private static final char[] hexDigitsLower =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final char[] hexDigitsUpper =
        {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * 将数据按照hex编码处理
     * 
     * @param data
     *            数据
     * @return hex编码，小写
     */
    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, hexDigitsLower);
    }

    /**
     * 将数据按照hex编码处理
     * 
     * @param data
     *            数据
     * @param toUpper
     *            是否转为大写，true表示转为大写
     * @return hex编码，根据toUpper参数确定是大写还是小写
     */
    public static char[] encodeHex(byte[] data, boolean toUpper) {
        return encodeHex(data, toUpper ? hexDigitsUpper : hexDigitsLower);
    }

    private static char[] encodeHex(byte[] data, char[] hexDigits) {
        int dataLen = data.length;
        char[] str = new char[dataLen * 2];
        int k = 0;
        for (int i = 0; i < dataLen; i++) {
            byte byte0 = data[i];
            str[k++] = hexDigits[byte0 >>> 4 & 0xf];
            str[k++] = hexDigits[byte0 & 0xf];
        }
        return str;
    }

}
