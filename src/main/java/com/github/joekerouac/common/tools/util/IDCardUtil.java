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

import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.joekerouac.common.tools.area.Area;
import com.github.joekerouac.common.tools.area.AreaUtil;
import com.github.joekerouac.common.tools.constant.StringConst;

import lombok.CustomLog;

/**
 * 身份证工具类
 *
 * @author JoeKerouac
 * @date 2022-10-17 19:27
 * @since 2.0.0
 */
@CustomLog
public class IDCardUtil {

    /**
     * 加权表
     */
    private static int[] POWER = new int[] {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};

    /**
     * 加权因子
     */
    private static char[] DIVISOR = new char[] {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    /**
     * 身份证正则
     */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("[0-9]{17}[0-9xX]");

    /**
     * 根据出生日期随机生成一个身份证号
     *
     * @param birthday
     *            出生日期，格式为yyyyMMdd（本方法不严格验证参数准确性，只会验证长度和是否数字）
     * @return 根据指定出生日期生成的身份证号，参数格式错误时返回null
     */
    public static String create(String birthday) {
        if (!Pattern.matches("[0-9]{8}", birthday)) {
            return null;
        }
        // 身份证号
        String card = StringConst.EMPTY;

        {
            // 随机挑出一个省市
            Map<String, Area> areaMap = getAreaMap(birthday);
            int r1 = (int)(Math.random() * areaMap.size());
            int i = 0;
            for (String befor : areaMap.keySet()) {
                if (r1 == i) {
                    card += befor;
                    break;
                } else {
                    i++;
                }
            }
        }

        {
            // 拼接生日
            card += birthday;
        }

        {
            // 生成三位随机数
            int r2 = (int)(Math.random() * 899 + 100);
            card += r2;
        }

        // 生成最后一位校验码
        int mod = calcMod(card);
        char calcLast = DIVISOR[mod];
        card += calcLast;
        return card;
    }

    /**
     * 检查身份证号是否符合格式
     *
     * @param idCard
     *            身份证号
     * @return 如果身份证号符合身份证格式则返回<code>true</code>
     */
    public static boolean check(String idCard) {
        // 验证身份证格式

        Matcher matcher = ID_CARD_PATTERN.matcher(idCard);
        if (!matcher.matches()) {
            // 格式不对
            LOGGER.error("身份证格式不对{}", idCard);
            return false;
        }

        // 验证最后一位加权码
        byte[] idCardByte = idCard.getBytes(Charset.defaultCharset());
        int mod = calcMod(idCard);
        int calcLast = DIVISOR[mod];
        char last;
        if (idCardByte[17] == 'x' || idCardByte[17] == 'X') {
            last = 'X';
        } else {
            last = (char)idCardByte[17];
        }
        if (last != calcLast) {
            // 格式不对
            LOGGER.error("加权码错误{}", idCard);
            return false;
        }
        return true;
    }

    /**
     * 获取用户所属省份，可能不准（因为有些人出生后很久才上户口，此时可能行政区划代码已经更改了）
     *
     * @param idCard
     *            用户身份证号
     * @return 用户所属省份
     */
    public static String getProvince(String idCard) {
        String birthday = getBirthday(idCard);
        String code = getAreaCode(idCard);
        Map<String, Area> areaMap = getAreaMap(birthday);
        Area area = areaMap == null ? null : areaMap.get(code);

        if (area == null) {
            LOGGER.warn("地区[{}]不存在或者地区已不在最新行政区划代码中，无法获取所属省份", code);
            return null;
        }

        return AreaUtil.getProvince(area, areaMap).getName();
    }

    /**
     * 获取用户所属县市，可能不准（因为有些人出生后很久才上户口，此时可能行政区划代码已经更改了）
     *
     * @param idCard
     *            用户身份证号
     * @return 用户所属县市
     */
    public static String getArea(String idCard) {
        String code = getAreaCode(idCard);
        Map<String, Area> areaMap = getAreaMap(getBirthday(idCard));
        // 用户所属地区
        Area area = areaMap == null ? null : areaMap.get(code);

        if (area == null) {
            LOGGER.warn("地区[{}]不存在或者地区已不在最新行政区划代码中", code);
            return null;
        }

        return AreaUtil.getFullName(area, areaMap);
    }

    /**
     * 获取用于生日
     *
     * @param idCard
     *            身份证号
     * @return 生日，格式yyyyMMdd
     */
    public static String getBirthday(String idCard) {
        return idCard.substring(6, 14);
    }

    /**
     * 获取身份证的区域编码
     *
     * @param idCard
     *            身份证
     * @return 区域编码
     */
    public static String getAreaCode(String idCard) {
        return idCard.substring(0, 6);
    }

    /**
     * 获取用户性别，0是女，1是男
     *
     * @param idCard
     *            用户身份证号
     * @return 用户性别
     */
    public static int getSex(String idCard) {
        // 判断身份证用户性别
        int sexInt = Integer.parseInt(idCard.substring(16, 17));
        return sexInt % 2;
    }

    /**
     * 获取用户年龄，如果2020.01.01出生，那么到2021.01.01都返回1岁，到2021.01.02就返回2岁了
     *
     * @param idCard
     *            用户身份证号
     * @return 用户年龄
     */
    public static int getAge(String idCard) {
        int age;
        // 计算用户年龄
        int year = Integer.parseInt(idCard.substring(6, 10));
        int monthDay = Integer.parseInt(idCard.substring(10, 14));
        Calendar calendar = Calendar.getInstance();
        int nowYear = calendar.get(Calendar.YEAR);
        int nowMonthDay = calendar.get(Calendar.MONTH) * 100 + calendar.get(Calendar.DATE);
        if (nowMonthDay > monthDay) {
            age = nowYear - year + 1;
        } else {
            age = nowYear - year;

        }
        if (age < 0) {
            LOGGER.warn("身份证年龄应该大于等于0，但是实际年龄为{}", age);
        }
        return age;
    }

    /**
     * 计算校验和
     *
     * @param card
     *            身份证号，长度不得低于17位，使用前17位计算校验和
     * @return 校验和
     */
    private static int calcMod(String card) {
        // 生成最后一位校验码
        byte[] idCardByte = card.getBytes(Charset.defaultCharset());
        int sum = 0;
        for (int j = 0; j < 17; j++) {
            sum += (((int)idCardByte[j]) - 48) * POWER[j];
        }
        return sum % 11;
    }

    /**
     * 获取指定年份的区域集合
     *
     * @param birthday
     *            生日，格式：yyyyMMdd
     * @return 该生日对应的区域集合
     */
    private static Map<String, Area> getAreaMap(String birthday) {
        return AreaUtil
            .getArea(birthday.substring(0, 4) + "." + birthday.substring(4, 6) + "." + birthday.substring(6, 8));
    }

}
