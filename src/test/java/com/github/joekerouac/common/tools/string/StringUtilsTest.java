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

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class StringUtilsTest {

    @Test
    public void indexOfTest() {
        String msg = "[2021-07-14 17:08:00][thread-0][traceId:sdf56s4d6f8765s1df][测试]";
        String date = msg.substring(StringUtils.indexOf(msg, "[", 1) + 1, StringUtils.indexOf(msg, "]", 1));
        Assert.assertEquals(date, "2021-07-14 17:08:00");

        String traceId = msg.substring(StringUtils.indexOf(msg, "traceId:", 1) + "traceId:".length(),
            StringUtils.indexOf(msg, "]", 3));
        Assert.assertEquals(traceId, "sdf56s4d6f8765s1df");
    }

    @Test
    public void testCopy() {
        String result = StringUtils.copy("1", 3);
        Assert.assertEquals(result, "111");

        NullPointerException nullPointerException = null;
        try {
            StringUtils.copy(null, 10);
        } catch (NullPointerException e) {
            nullPointerException = e;
        }
        Assert.assertNotNull(nullPointerException);

        IllegalArgumentException illegalArgumentException = null;
        try {
            StringUtils.copy("1", 0);
        } catch (IllegalArgumentException e) {
            illegalArgumentException = e;
        }
        Assert.assertNotNull(illegalArgumentException);
    }

    @Test(dataProvider = "isBlankDataProvider")
    public void testBlank(String str, boolean isBlank) {
        Assert.assertEquals(StringUtils.isBlank(str), isBlank);
        Assert.assertEquals(StringUtils.isNotBlank(str), !isBlank);
    }

    @Test(dataProvider = "getOrDefaultDataProvider")
    public void testGetOrDefault(String str, String defaultStr, String result) {
        Assert.assertEquals(StringUtils.getOrDefault(str, defaultStr), result);
    }

    @Test(dataProvider = "formatDataProvider")
    public void testFormat(String msg, Object[] args, String result) {
        Assert.assertEquals(StringUtils.format(msg, args), result);
    }

    @Test
    public void testTrim() {
        Assert.assertEquals(StringUtils.trim("123abc123", "123"), "abc");
        Assert.assertEquals(StringUtils.trim("123abc123", "abc"), "123abc123");
        Assert.assertEquals(StringUtils.trim(" 123abc123", "123"), " 123abc");
        Assert.assertEquals(StringUtils.trim(" 123abc123 ", "123"), " 123abc123 ");
        Assert.assertEquals(StringUtils.trim("123abc123 ", "123"), "abc123 ");
    }

    @DataProvider(name = "isBlankDataProvider", parallel = true)
    public Object[][] isBlankDataProvider() {
        return new Object[][] {{null, true}, {"", true}, {" ", true}, {" \n\t", true}, {" s", false}, {" s ", false}};
    }

    @DataProvider(name = "getOrDefaultDataProvider", parallel = true)
    public Object[][] getOrDefaultDataProvider() {
        return new Object[][] {{null, "abc", "abc"}, {"", "abc", "abc"}, {" ", "abc", "abc"}, {" \n\t", "abc", "abc"},
            {" s", "abc", " s"}, {" s ", "abc", " s "}};
    }

    @DataProvider(name = "formatDataProvider", parallel = true)
    public Object[][] formatDataProvider() {
        return new Object[][] {{"-{}-", new Object[] {"123"}, "-123-"}, {"-{0}-", new Object[] {"123"}, "-{0}-"}};
    }
}
