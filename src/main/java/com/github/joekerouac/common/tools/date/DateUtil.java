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
package com.github.joekerouac.common.tools.date;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.exception.CommonException;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtil {

    /**
     * formatter缓存
     */
    private final static Map<String, DateTimeFormatter> FORMATTER_CACHE = new HashMap<>();

    /**
     * 常用格式化yyyy-MM-dd HH:mm:ss
     */
    public final static String BASE = "yyyy-MM-dd HH:mm:ss";

    /**
     * 常用格式化yyyy-MM-dd
     */
    public final static String SHORT = "yyyy-MM-dd";

    /**
     * 常用格式化HH:mm:ss
     */
    public final static String TIME = "HH:mm:ss";

    /**
     * 将时间戳转换为LocalDateTime（使用系统默认时区）
     * 
     * @param epochMilli
     *            时间戳
     * @return LocalDateTime
     */
    public static LocalDateTime fromEpochMilli(long epochMilli) {
        Instant instant = Instant.ofEpochMilli(epochMilli);
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     * 将LocalDateTime转换为Date
     * 
     * @param localDateTime
     *            LocalDateTime
     * @return Date
     */
    public static Date convert(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 将Date转换为LocalDateTime
     * 
     * @param date
     *            Date
     * @return LocalDateTime
     */
    public static LocalDateTime convert(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 将一种格式的日期转换为另一种格式
     *
     * @param date
     *            日期字符串
     * @param format
     *            日期对应的格式
     * @param newFormat
     *            要转换的新格式
     * @return 新格式的日期
     */
    public static String convert(String date, String format, String newFormat) {
        Assert.argNotBlank(date, "date");
        Assert.argNotBlank(format, "format");
        Assert.argNotBlank(newFormat, "newFormat");

        return getFormatDate(parse(date, format), newFormat);
    }

    /**
     * 获取指定年份的天数（润年跟平年不一样）
     *
     * @param date
     *            指定年份
     * @return 指定年份对应的天数
     */
    public static int getYearDay(Date date) {
        Assert.argNotNull(date, "date");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
    }

    /**
     * 获取指定月份的天数（有的月份31天，有的30天....）
     *
     * @param date
     *            指定月份
     * @return 该月份的天数
     */
    public static int getMonthDay(Date date) {
        Assert.argNotNull(date, "date");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    /**
     * 将指定日期字符串按照指定格式转换为日期对象（如果传入的时间没有当前时分秒信息或者年月日信息则默认填充当前时间）
     *
     * @param date
     *            格式化日期字符串
     * @param format
     *            日期字符串的格式
     * @return 格式化日期字符串对应的日期对象
     * @throws DateUtilException
     *             格式错误时返回该异常
     */
    public static Date parse(String date, String format) {
        Assert.argNotBlank(date, "date");
        Assert.argNotBlank(format, "format");

        LocalDateTime time = parseToLocalDateTime(date, format);
        // 返回对应日期
        return toDate(time);
    }

    /**
     * 计算arg0-arg1的时间差，如果精度是天，则不足24小时不算，每满24小时则+1，例如两个日期差25小时，则差返回1天（精度为日），其他精度等同
     *
     * @param arg0
     *            arg0
     * @param arg1
     *            arg1
     * @param dateUnit
     *            返回结果的单位，只能是日以及日以下的精度，年月不行
     * @return arg0-arg1的时间差，精确到指定的单位（field）
     */
    public static long calc(Date arg0, Date arg1, DateUnit dateUnit) {
        Assert.argNotNull(arg0, "arg0");
        Assert.argNotNull(arg1, "arg1");
        Assert.argNotNull(dateUnit, "dateUnit");
        return calc(arg0.toInstant(), arg1.toInstant(), dateUnit);
    }

    /**
     * 计算arg0-arg1的时间差，如果精度是天，则不足24小时不算，每满24小时则+1，例如两个日期差25小时，则差返回1天（精度为日），其他精度等同
     *
     * @param arg0
     *            日期字符串
     * @param arg1
     *            日期字符串
     * @param format
     *            日期字符串的格式
     * @param dateUnit
     *            返回结果的单位，只能是日以及日以下的精度，年月不行
     * @return arg0-arg1的时间差，精确到指定的单位（field），出错时返回-1
     */
    public static long calc(String arg0, String arg1, String format, DateUnit dateUnit) {
        Assert.argNotNull(arg0, "arg0");
        Assert.argNotNull(arg1, "arg1");
        Assert.argNotBlank(format, "format");
        Assert.argNotNull(dateUnit, "dateUnit");

        try {
            return calc(parseToLocalDateTime(arg0, format), parseToLocalDateTime(arg1, format), dateUnit);
        } catch (Exception e) {
            String msgTemp = "日期计算出错，可能是日期格式有误？当前计算参数：[{}],[{}],[{}],[{}]";
            throw new CommonException(ErrorCodeEnum.UNKNOWN_EXCEPTION,
                StringUtils.format(msgTemp, arg0, arg1, format, dateUnit), e);
        }
    }

    /**
     * 计算arg0-arg1的时间差，如果精度是天，则不足24小时不算，每满24小时则+1，例如两个日期差25小时，则差返回1天（精度为日），其他精度等同
     *
     * @param arg0
     *            arg0
     * @param arg1
     *            arg1
     * @param dateUnit
     *            返回结果的单位，只能是日以及日以下的精度，年月不行
     * @return arg0-arg1的时间差，精确到指定的单位（field）
     */
    public static long calc(LocalDateTime arg0, LocalDateTime arg1, DateUnit dateUnit) {
        Assert.argNotNull(arg0, "arg0");
        Assert.argNotNull(arg1, "arg1");
        Assert.argNotNull(dateUnit, "dateUnit");
        return calc(arg0.atZone(ZoneId.systemDefault()).toInstant(), arg1.atZone(ZoneId.systemDefault()).toInstant(),
            dateUnit);
    }

    /**
     * 计算arg0-arg1的时间差，如果精度是天，则不足24小时不算，每满24小时则+1，例如两个日期差25小时，则差返回1天（精度为日），其他精度等同
     *
     * @param arg0
     *            arg0
     * @param arg1
     *            arg1
     * @param dateUnit
     *            返回结果的单位，只能是日以及日以下的精度，年月不行
     * @return arg0-arg1的时间差，精确到指定的单位（field），出错时返回-1
     */
    private static long calc(Temporal arg0, Temporal arg1, DateUnit dateUnit) {
        return dateUnit.getJdkUnit().between(arg1, arg0);
    }

    /**
     * 将指定日期增加指定时长
     *
     * @param date
     *            指定日期
     * @param format
     *            指定日期字符串的格式
     * @param amount
     *            时长
     * @param dateUnit
     *            单位
     * @return 增加后的日期
     */
    public static Date add(String date, String format, int amount, DateUnit dateUnit) {
        Assert.argNotBlank(date, "date");
        Assert.argNotBlank(format, "format");
        Assert.argNotNull(dateUnit, "dateUnit");

        LocalDateTime localDateTime = parseToLocalDateTime(date, format);
        localDateTime = localDateTime.plus(amount, dateUnit.getJdkUnit());
        return toDate(localDateTime);
    }

    /**
     * 将指定日期加上指定的时长
     *
     * @param date
     *            指定的日期
     * @param amount
     *            时长
     * @param dateUnit
     *            单位
     * @return 增加指定时长后的日期
     */
    public static Date add(Date date, int amount, DateUnit dateUnit) {
        Assert.argNotNull(date, "date");
        Assert.argNotNull(dateUnit, "dateUnit");

        return Date.from(date.toInstant().plus(amount, dateUnit.getJdkUnit()));
    }

    /**
     * 获取指定日期的指定格式的字符串
     *
     * @param date
     *            指定日期
     * @param format
     *            日期格式
     * @return 指定日期的指定格式的字符串
     */
    public static String getFormatDate(Date date, String format) {
        Assert.argNotNull(date, "date");
        Assert.argNotBlank(format, "format");

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        return dateTimeFormatter.format(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
    }

    /**
     * 获取指定日期的指定格式的字符串
     *
     * @param date
     *            指定日期
     * @param format
     *            日期格式
     * @return 指定日期的指定格式的字符串
     */
    public static String getFormatDate(LocalDateTime date, String format) {
        Assert.argNotNull(date, "date");
        Assert.argNotBlank(format, "format");

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        return dateTimeFormatter.format(date);
    }

    /**
     * 判断指定日期是否在当前时间之前，精确到指定单位
     *
     * @param date
     *            指定日期
     * @param format
     *            指定日期的格式
     * @param dateUnit
     *            精确单位（例如传入年就是精确到年）
     * @return 如果指定日期在当前时间之前返回<code>true</code>
     */
    public static boolean beforeNow(String date, String format, DateUnit dateUnit) {
        Assert.argNotBlank(date, "date");
        Assert.argNotBlank(format, "format");
        Assert.argNotNull(dateUnit, "dateUnit");

        return calc(new Date(), parse(date, format), dateUnit) > 0;
    }

    /**
     * 查询时间是否在今日
     *
     * @param time
     *            时间戳
     * @return 如果时间戳是今天的则返回<code>true</code>
     */
    public static boolean isToday(long time) {
        return isToday(new Date(time));
    }

    /**
     * 查询时间是否在今日
     *
     * @param date
     *            时间字符串
     * @param format
     *            时间字符串的格式
     * @return 如果指定日期对象在今天则返回<code>true</code>
     */
    public static boolean isToday(String date, String format) {
        return isToday(parse(date, format));
    }

    /**
     * 查询时间是否在今日
     *
     * @param time
     *            日期对象
     * @return 如果指定日期对象在今天则返回<code>true</code>
     */
    public static boolean isToday(Date time) {
        String now = getFormatDate(new Date(), SHORT);
        String target = getFormatDate(time, SHORT);
        return now.equals(target);
    }

    /**
     * 从指定日期字符串获取LocalDateTime对象
     *
     * @param date
     *            日期字符串
     * @param format
     *            日期字符串格式
     * @return LocalDateTime对象
     */
    public static LocalDateTime parseToLocalDateTime(String date, String format) {
        DateTimeFormatter formatter = FORMATTER_CACHE.get(format);
        // 优先从缓存取，取不到创建一个，不用加锁
        if (formatter == null) {
            formatter = DateTimeFormatter.ofPattern(format);
            FORMATTER_CACHE.put(format, formatter);
        }

        TemporalAccessor accessor = formatter.parse(date);
        LocalDateTime time;

        // 判断日期类型，新版日期类将时间分为年月日（LocalDate）、时分秒（LocalTime）、年月日时分秒（LocalDateTime）三种类型
        if (accessor.isSupported(ChronoField.DAY_OF_YEAR) && accessor.isSupported(ChronoField.SECOND_OF_DAY)) {
            time = LocalDateTime.from(accessor);
        } else if (accessor.isSupported(ChronoField.SECOND_OF_DAY)) {
            LocalTime localTime = LocalTime.from(accessor);
            time = localTime.atDate(LocalDate.now());
        } else if (accessor.isSupported(ChronoField.DAY_OF_YEAR)) {
            LocalDate localDate = LocalDate.from(accessor);
            time = localDate.atTime(LocalTime.now());
        } else {
            throw new DateUtilException("日期类解析异常，时间为：" + date + "；格式为：" + format);
        }
        return time;
    }

    /**
     * 将LocalDateTime转换为Date
     * 
     * @param time
     *            LocalDateTime
     * @return Date
     */
    private static Date toDate(LocalDateTime time) {
        return Date.from(time.atZone(ZoneId.systemDefault()).toInstant());
    }

    public enum DateUnit {

        YEAR(ChronoUnit.YEARS),

        MONTH(ChronoUnit.MONTHS),

        DAY(ChronoUnit.DAYS),

        HOUR(ChronoUnit.HOURS),

        MINUTE(ChronoUnit.MINUTES),

        SECOND(ChronoUnit.SECONDS);

        @Getter
        private ChronoUnit jdkUnit;

        DateUnit(ChronoUnit unit) {
            this.jdkUnit = unit;
        }
    }

    static class DateUtilException extends RuntimeException {
        private static final long serialVersionUID = 474205378026735176L;

        public DateUtilException(String message) {
            super(message);
        }
    }

}
