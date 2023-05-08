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
package com.github.joekerouac.common.tools.string;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import javax.validation.constraints.NotNull;

import org.slf4j.helpers.MessageFormatter;

import com.github.joekerouac.common.tools.util.Assert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StringUtils {

    /**
     * 将指定字符集的数据转换为另外一个字符集
     *
     * @param data
     *            数据
     * @param charset
     *            当前数据字符集
     * @param resultCharset
     *            结果数据集
     * @return utf8字符集的数据
     */
    public static byte[] convert(@NotNull byte[] data, @NotNull Charset charset, @NotNull Charset resultCharset) {
        byte[] result = data;
        if (!resultCharset.equals(charset)) {
            CharBuffer decode = charset.decode(ByteBuffer.wrap(data));
            result = resultCharset.encode(decode).array();
        }
        return result;
    }

    /**
     * 查找第count个指定code在str中的位置，例如str是123132412，code是3，count是2，那么返回的index是4；
     * 
     * @param str
     *            字符串
     * @param code
     *            要搜寻的代码
     * @param count
     *            需要搜寻第几个
     * @return 第count个指定code在str中的位置，例如str是123132412，code是3，count是2，那么返回的index是4；如果str中不包含第count个code那么返回负值；
     */
    public static int indexOf(String str, String code, int count) {
        int from = 0;
        int number = count - 1;
        while (number > 0) {
            from = str.indexOf(code, from);
            if (from >= str.length()) {
                return -2;
            }

            if (from < 0) {
                return from;
            }

            from += 1;
            number--;
        }

        return str.indexOf(code, from);
    }

    /**
     * 将目标字符串重复count次返回
     * 
     * @param str
     *            目标字符串
     * @param count
     *            次数
     * @return 目标字符串重复count次结果，例如目标字符串是test，count是2，则返回testtest，如果count是3则返回testtesttest
     */
    public static String copy(String str, int count) {
        if (str == null) {
            throw new NullPointerException("原始字符串不能为null");
        }

        if (count <= 0) {
            throw new IllegalArgumentException("次数必须大于0");
        }

        if (count == 1) {
            return str;
        }

        if (count == 2) {
            return str + str;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * 将首字母小写
     *
     * @param arg
     *            指定字符串
     * @return 首字母小写后的字符串
     */
    public static String toFirstLowerCase(String arg) {
        return arg.substring(0, 1).toLowerCase() + arg.substring(1);
    }

    /**
     * 将首字母大写
     *
     * @param arg
     *            指定字符串
     * @return 首字母大写后的字符串
     */
    public static String toFirstUpperCase(String arg) {
        return arg.substring(0, 1).toUpperCase() + arg.substring(1);
    }

    /**
     * 判断String是否是null或者空
     * 
     * @param string
     *            字符串
     * @return 返回true表示字符串是null或者是空
     */
    public static boolean isBlank(String string) {
        return string == null || string.trim().isEmpty();
    }

    /**
     * 判断String是否是null或者空
     * 
     * @param string
     *            字符串
     * @return 返回true表示字符串是null或者是空
     */
    public static boolean isNotBlank(String string) {
        return !isBlank(string);
    }

    /**
     * 如果源数据为空，则返回默认数据
     * 
     * @param src
     *            源数据
     * @param defaultStr
     *            默认字符串
     * @return 如果源数据为空，则返回默认数据
     */
    public static String getOrDefault(String src, String defaultStr) {
        return isBlank(src) ? defaultStr : src;
    }

    /**
     * 格式化字符串，使用{}作为占位符
     * 
     * @param msg
     *            字符串模板
     * @param args
     *            模板参数
     * @return 格式化后的字符串
     */
    public static String format(String msg, Object... args) {
        return MessageFormatter.arrayFormat(msg, args).getMessage();
    }

    /**
     * 将字符串前后的指定数据去除
     * 
     * @param data
     *            字符串
     * @param trim
     *            要去除的数据
     * @return 处理后的数据，例如数据是123abc123，trim是123，那么处理完毕后返回abc
     */
    public static String trim(String data, String trim) {
        Assert.argNotBlank(data, "data");
        Assert.argNotBlank(trim, "trim");

        String result = data;
        while (result.startsWith(trim)) {
            result = result.substring(trim.length());
        }

        while (result.endsWith(trim)) {
            result = result.substring(0, result.length() - trim.length());
        }

        return result;
    }

    /**
     * 将字符串前后的指定数据去除
     *
     * @param data
     *            字符串
     * @param trim
     *            要去除的数据
     * @return 处理后的数据，例如数据是123abc123，trim是123，那么处理完毕后返回abc
     */
    public static String trim(String data, char trim) {
        return trim(data, Character.toString(trim));
    }

    /**
     * 求两个字符串的最大公共子序列的长度
     *
     * @param arg0
     *            字符串1
     * @param arg1
     *            字符串2
     * @return 两个字符串的最大公共子序列的长度，例：
     *         <ul>
     *         <li>123456和456789的lcs为3</li>
     *         <li>123456和256789的lcs为3</li>
     *         <li>123456和556489的lcs为2</li>
     *         </ul>
     */

    public static long lcs(String arg0, String arg1) {
        if (arg0 == null || arg1 == null) {
            return 0;
        }
        return lcs(arg0, arg1, 0, 0);
    }

    /**
     * 求两个字符串的最大公共子序列的长度
     *
     * @param arg0
     *            字符串1
     * @param arg1
     *            字符串2
     * @param i
     *            字符串1的当前位置指针
     * @param j
     *            字符串2的当前位置指针
     * @return 两个字符串的最大公共子序列的长度
     */
    private static long lcs(String arg0, String arg1, int i, int j) {
        if (arg0.length() == i || arg1.length() == j) {
            return 0;
        }

        if (arg0.charAt(i) == arg1.charAt(j)) {
            return 1 + lcs(arg0, arg1, i + 1, j + 1);
        } else {
            return Math.max(lcs(arg0, arg1, i + 1, j), lcs(arg0, arg1, i, j + 1));
        }
    }

}
