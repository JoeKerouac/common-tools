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

import java.util.Calendar;
import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class DateUtilTest {

    @Test
    public void testBase() {
        Date date = createDate();
        Date convert = DateUtil.convert(DateUtil.convert(date));
        Assert.assertEquals(date, convert);

        String baseDateStr = "2020-10-08 10:12:00";
        String shortDateStr = "2020-10-08";
        String timeDateStr = "10:12:00";
        Assert.assertEquals(DateUtil.convert(baseDateStr, DateUtil.BASE, "yyyyMMddHHmmss"), "20201008101200");
        Assert.assertEquals(DateUtil.parse(baseDateStr, DateUtil.BASE), date);
        Assert.assertEquals(DateUtil.add("2020-10-07 10:12:00", DateUtil.BASE, 1, DateUtil.DateUnit.DAY), date);
        Assert.assertEquals(
            DateUtil.add(DateUtil.parse("2020-10-07 10:12:00", DateUtil.BASE), 1, DateUtil.DateUnit.DAY), date);
        Assert.assertEquals(DateUtil.getFormatDate(date, DateUtil.BASE), baseDateStr);
        Assert.assertTrue(DateUtil.beforeNow(baseDateStr, DateUtil.BASE, DateUtil.DateUnit.SECOND));
        Assert.assertFalse(DateUtil.beforeNow(
            DateUtil.getFormatDate(DateUtil.add(new Date(), 1, DateUtil.DateUnit.DAY), DateUtil.BASE), DateUtil.BASE,
            DateUtil.DateUnit.DAY));

        Assert.assertTrue(DateUtil.isToday(new Date().getTime()));
        Assert.assertFalse(DateUtil.isToday(DateUtil.add(new Date(), 1, DateUtil.DateUnit.DAY).getTime()));
        Assert.assertTrue(DateUtil.isToday(System.currentTimeMillis()));
        Assert.assertTrue(DateUtil.isToday(DateUtil.getFormatDate(new Date(), DateUtil.BASE), DateUtil.BASE));

    }

    @Test(dataProvider = "testGetYearDayDataProvider")
    public void testGetYearDay(Date date, int day) {
        Assert.assertEquals(DateUtil.getYearDay(date), day);
    }

    @Test(dataProvider = "testGetMonthDayDataProvider")
    public void testGetMonthDay(Date date, int day) {
        Assert.assertEquals(DateUtil.getMonthDay(date), day);
    }

    @Test(dataProvider = "testCalcDateDataProvider")
    public void testCalcDate(String arg0, String arg1, String format, DateUtil.DateUnit unit, int result) {
        Assert.assertEquals(DateUtil.calc(DateUtil.parse(arg0, format), DateUtil.parse(arg1, format), unit), result);
        Assert.assertEquals(DateUtil.calc(arg0, arg1, format, unit), result);
    }

    @DataProvider(name = "testGetYearDayDataProvider", parallel = true)
    public Object[][] testGetYearDayDataProvider() {
        Object[][] result = new Object[2][2];
        Calendar calendar = Calendar.getInstance();
        // 润年
        calendar.set(Calendar.YEAR, 2020);
        result[0] = new Object[] {calendar.getTime(), 366};
        // 平年
        calendar.set(Calendar.YEAR, 2019);
        result[1] = new Object[] {calendar.getTime(), 365};
        return result;
    }

    @DataProvider(name = "testGetMonthDayDataProvider", parallel = true)
    public Object[][] testGetMonthDayDataProvider() {
        Object[][] result = new Object[13][2];
        Calendar calendar = Calendar.getInstance();
        // 平年
        calendar.set(Calendar.YEAR, 2019);
        calendar.set(Calendar.MONTH, 1);
        // 注意，这里一定要设置为每月1号，不然getTime会有问题，例如如果今天是29号，上边设置为2019年2月，因为只有28天，所以实际getTime出来是
        // 3月1号
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        result[0] = new Object[] {calendar.getTime(), 28};

        // 润年
        calendar.set(Calendar.YEAR, 2020);

        for (int i = 1; i < 13; i++) {
            calendar.set(Calendar.MONTH, i - 1);
            int day;
            if (i == 2) {
                day = 29;
            } else if (i == 1 || i == 3 || i == 5 || i == 7 || i == 8 || i == 10 || i == 12) {
                day = 31;
            } else {
                day = 30;
            }
            result[i] = new Object[] {calendar.getTime(), day};
        }

        return result;
    }

    @DataProvider(name = "testCalcDateDataProvider", parallel = true)
    public Object[][] testCalcDateDataProvider() {
        return new Object[][] {{"2020-10-01", "2020-10-01", DateUtil.SHORT, DateUtil.DateUnit.DAY, 0},
            {"2020-10-02 10", "2020-10-01 10", "yyyy-MM-dd HH", DateUtil.DateUnit.DAY, 1},
            // 则不足24小时不算，每满24小时则+1天
            {"2020-10-02 00:00:00", "2020-10-01 12:00:00", DateUtil.BASE, DateUtil.DateUnit.DAY, 0},
            {"2020-10-02 01:00:00", "2020-10-01 01:00:00", DateUtil.BASE, DateUtil.DateUnit.DAY, 1},
            {"2020-10-02 10:00:00", "2020-10-01 01:00:00", DateUtil.BASE, DateUtil.DateUnit.DAY, 1},
            {"2020-10-02 24:00:00", "2020-10-01 00:00:00", DateUtil.BASE, DateUtil.DateUnit.DAY, 2},

            // 不足60分钟的不算，超过60分钟每60分钟+1小时
            {"2020-10-01 10:00:00", "2020-10-01 09:00:01", DateUtil.BASE, DateUtil.DateUnit.HOUR, 0},
            {"2020-10-01 10:00:00", "2020-10-01 08:00:01", DateUtil.BASE, DateUtil.DateUnit.HOUR, 1},

            // 不足60秒的不算，超过60秒每60秒+1分钟
            {"2020-10-01 10:01:00", "2020-10-01 10:00:01", DateUtil.BASE, DateUtil.DateUnit.MINUTE, 0},
            {"2020-10-01 10:02:00", "2020-10-01 10:00:01", DateUtil.BASE, DateUtil.DateUnit.MINUTE, 1},

            // 不足1000毫秒的不算，超过1000毫秒每1000毫秒+1秒
            {"2020-10-01 10:00:01.100", "2020-10-01 10:00:00.200", "yyyy-MM-dd HH:mm:ss.SSS", DateUtil.DateUnit.SECOND,
                0},
            {"2020-10-01 10:00:02.100", "2020-10-01 10:00:00.200", "yyyy-MM-dd HH:mm:ss.SSS", DateUtil.DateUnit.SECOND,
                1},};
    }

    public Date createDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2020);
        // 注意，month是从0开始，而不是1，年和日都是从1开始
        calendar.set(Calendar.MONTH, 9);
        calendar.set(Calendar.DAY_OF_MONTH, 8);
        calendar.set(Calendar.HOUR_OF_DAY, 10);
        calendar.set(Calendar.MINUTE, 12);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}
