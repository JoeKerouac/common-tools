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

import java.util.Arrays;

/**
 * Base32
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class Base32 {

    private static final char[] ALPHABET = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '2', '3', '4', '5', '6', '7'};

    private static final byte[] DECODE_TABLE;

    static {
        DECODE_TABLE = new byte[128];
        Arrays.fill(DECODE_TABLE, (byte)0xFF);

        for (int i = 0; i < ALPHABET.length; i++) {
            DECODE_TABLE[ALPHABET[i]] = (byte)i;
            if (i < 24) {
                DECODE_TABLE[Character.toLowerCase(ALPHABET[i])] = (byte)i;
            }
        }
    }

    /**
     * 将二进制数据base32编码为字符串
     * 
     * @param data
     *            二进制数据
     * @return base32编码的字符串
     */
    public static String encode(byte[] data) {
        char[] chars = new char[((data.length * 8) / 5) + ((data.length % 5) != 0 ? 1 : 0)];

        for (int i = 0, j = 0, index = 0; i < chars.length; i++) {
            if (index > 3) {
                int b = data[j] & (0xFF >> index);
                index = (index + 5) % 8;
                b <<= index;

                if (j < data.length - 1) {
                    b |= (data[j + 1] & 0xFF) >> (8 - index);
                }

                chars[i] = ALPHABET[b];
                j++;
            } else {
                chars[i] = ALPHABET[((data[j] >> (8 - (index + 5))) & 0x1F)];
                index = (index + 5) % 8;

                if (index == 0) {
                    j++;
                }
            }
        }

        return new String(chars);
    }

    /**
     * 将base32字符串解析为byte数组
     * 
     * @param s
     *            base32字符串
     * @return 解析后的数据
     */
    public static byte[] decode(String s) {
        char[] stringData = s.toCharArray();
        byte[] data = new byte[(stringData.length * 5) / 8];

        for (int i = 0, j = 0, index = 0; i < stringData.length; i++) {
            int val;
            try {
                val = DECODE_TABLE[stringData[i]];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new IllegalStateException("Illegal character");
            }

            if (index <= 3) {
                index = (index + 5) % 8;
                if (index == 0) {
                    data[j++] |= val;
                } else {
                    data[j] |= val << (8 - index);
                }
            } else {
                index = (index + 5) % 8;
                data[j++] |= (val >> index);
                if (j < data.length) {
                    data[j] |= val << (8 - index);
                }
            }

        }

        return data;
    }

}
