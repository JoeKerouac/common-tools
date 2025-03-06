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

import java.util.Collection;

/**
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class NumberUtil {

    /**
     * 将8个byte数据合并为一个int数据，注意，数组必须包含4个及以上的byte数据，不然会报数组越界，内部没有进行越界检查；
     *
     * @param data
     *            byte数组
     * @return 合并的long数据
     */
    public static long mergeToLong(byte[] data) {
        return mergeToLong(data, 0);
    }

    /**
     * 将8个byte数据合并为一个long数据，注意，offset位置开始往后必须包含4个及以上的byte数据，不然会报数组越界，内部没有进行越界检查；
     *
     * @param data
     *            byte数据
     * @param offset
     *            byte数据起始位置
     * @return 合并为的long数据
     */
    public static long mergeToLong(byte[] data, int offset) {
        return Byte.toUnsignedLong(data[offset]) << 56 | Byte.toUnsignedLong(data[1 + offset]) << 48
            | Byte.toUnsignedLong(data[2 + offset]) << 40 | Byte.toUnsignedLong(data[3 + offset]) << 32
            | Byte.toUnsignedLong(data[4 + offset]) << 24 | Byte.toUnsignedLong(data[5 + offset]) << 16
            | Byte.toUnsignedLong(data[6 + offset]) << 8 | Byte.toUnsignedLong(data[7 + offset]);
    }

    /**
     * 将4个byte数据合并为一个int数据，注意，数组必须包含4个及以上的byte数据，不然会报数组越界，内部没有进行越界检查；
     * 
     * @param data
     *            byte数组
     * @return 合并的int数据
     */
    public static int mergeToInt(byte[] data) {
        return mergeToInt(data, 0);
    }

    /**
     * 将4个byte数据合并为一个int数据，注意，offset位置开始往后必须包含4个及以上的byte数据，不然会报数组越界，内部没有进行越界检查；
     * 
     * @param data
     *            byte数据
     * @param offset
     *            byte数据起始位置
     * @return 合并为的int数据
     */
    public static int mergeToInt(byte[] data, int offset) {
        return Byte.toUnsignedInt(data[offset]) << 24 | Byte.toUnsignedInt(data[1 + offset]) << 16
            | Byte.toUnsignedInt(data[2 + offset]) << 8 | Byte.toUnsignedInt(data[3 + offset]);
    }

    /**
     * 将2个byte数据合并为一个short数据，注意，数组必须包含2个及以上的byte数据，不然会报数组越界，内部没有进行越界检查；
     *
     * @param data
     *            byte数组
     * @return 合并的short数据
     */
    public static short mergeToShort(byte[] data) {
        return mergeToShort(data, 0);
    }

    /**
     * 将2个byte数据合并为一个short数据，注意，数组必须包含2个及以上的byte数据，不然会报数组越界，内部没有进行越界检查；
     *
     * @param data
     *            byte数据
     * @param offset
     *            byte数据起始位置
     * @return 合并为的short数据
     */
    public static short mergeToShort(byte[] data, int offset) {
        return (short)(Byte.toUnsignedInt(data[offset]) << 8 | Byte.toUnsignedInt(data[1 + offset]));
    }

    /**
     * 将一个long拆分为8个byte
     *
     * @param data
     *            long数据
     * @return 对应的8个byte
     */
    public static byte[] splitToByte(long data) {
        return splitToByte(data, new byte[8], 0);
    }

    /**
     * 将一个long拆分为8个byte
     *
     * @param data
     *            long数据
     * @param dst
     *            要写出的目标位置
     * @param offset
     *            offset
     * @return 对应的8个byte
     */
    public static byte[] splitToByte(long data, byte[] dst, int offset) {
        dst[offset] = (byte)((data >>> 56) & 0xFF);
        dst[1 + offset] = (byte)((data >>> 48) & 0xFF);
        dst[2 + offset] = (byte)((data >>> 40) & 0xFF);
        dst[3 + offset] = (byte)((data >>> 32) & 0xFF);
        dst[4 + offset] = (byte)((data >>> 24) & 0xFF);
        dst[5 + offset] = (byte)((data >>> 16) & 0XFF);
        dst[6 + offset] = (byte)((data >>> 8) & 0XFF);
        dst[7 + offset] = (byte)(data & 0XFF);
        return dst;
    }

    /**
     * 将一个int拆分为4个byte
     *
     * @param data
     *            int数据
     * @return 对应的4个byte
     */
    public static byte[] splitToByte(int data) {
        return splitToByte(data, new byte[4], 0);
    }

    /**
     * 将一个int拆分为4个byte
     * 
     * @param data
     *            int数据
     * @param dst
     *            要写出的目标位置
     * @param offset
     *            offset
     * @return 对应的4个byte
     */
    public static byte[] splitToByte(int data, byte[] dst, int offset) {
        dst[offset] = (byte)((data >>> 24) & 0xFF);
        dst[1 + offset] = (byte)((data >>> 16) & 0XFF);
        dst[2 + offset] = (byte)((data >>> 8) & 0XFF);
        dst[3 + offset] = (byte)(data & 0XFF);
        return dst;
    }

    /**
     * 将一个short拆分为2个byte
     *
     * @param data
     *            short数据
     * @return 对应的2个byte
     */
    public static byte[] splitToByte(short data) {
        return splitToByte(data, new byte[2], 0);
    }

    /**
     * 将一个short拆分为2个byte
     *
     * @param data
     *            short数据
     * @param dst
     *            要写出的目标位置
     * @param offset
     *            offset
     * @return 对应的2个byte
     */
    public static byte[] splitToByte(short data, byte[] dst, int offset) {
        dst[offset] = (byte)((data >>> 8) & 0XFF);
        dst[1 + offset] = (byte)(data & 0XFF);
        return dst;
    }

    /**
     * 求给定数字a和数字b的最大公约数
     *
     * @param a
     *            数字a
     * @param b
     *            数字b
     * @return 数字a和数字b的最大公约数
     */
    public static int gcd(int a, int b) {
        while (b != 0) {
            int temp = b;
            b = a % b;
            a = temp;
        }
        return a;
    }

    /**
     * 求给定一组数字的最大公约数
     *
     * @param numbers
     *            给定数据数组
     * @return 最大公约数
     */
    public static int gcd(int... numbers) {
        if (numbers == null || numbers.length == 0) {
            throw new IllegalArgumentException("At least one number is required");
        }

        int result = numbers[0];
        for (int i = 1; i < numbers.length; i++) {
            result = gcd(result, numbers[i]);
            // 如果当前结果是 1，提前结束
            if (result == 1) {
                return 1;
            }
        }

        return result;
    }

    /**
     * 求给定一组数字的最大公约数
     *
     * @param numbers
     *            给定数据数组
     * @return 最大公约数
     */
    public static int gcd(Collection<Integer> numbers) {
        if (numbers == null || numbers.isEmpty()) {
            throw new IllegalArgumentException("At least one number is required");
        }

        int result = 0;
        boolean first = true;
        for (Integer number : numbers) {
            if (first) {
                result = number;
                first = false;
            } else {
                result = gcd(result, number);
                // 如果当前结果是 1，提前结束
                if (result == 1) {
                    return 1;
                }
            }
        }

        return result;
    }

}
