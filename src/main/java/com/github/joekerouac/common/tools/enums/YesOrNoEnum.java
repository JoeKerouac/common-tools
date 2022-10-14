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
package com.github.joekerouac.common.tools.enums;

/**
 * yes or no枚举
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public enum YesOrNoEnum implements EnumInterface {

    YES("Y", "yes", "yes"),

    NO("N", "no", "no"),

    ;

    private String code;
    private String desc;
    private String englishName;

    static {
        // 重复检测
        EnumInterface.duplicateCheck(YesOrNoEnum.class);
    }

    YesOrNoEnum(String code, String desc, String englishName) {
        this.code = code;
        this.desc = desc;
        this.englishName = englishName;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String desc() {
        return desc;
    }

    @Override
    public String englishName() {
        return englishName;
    }

    /**
     * 判断给定字符串是不是yes
     * 
     * @param str
     *            字符串
     * @return 返回true表示给定字符串是yes
     */
    public static boolean isYes(String str) {
        return YES.code.equalsIgnoreCase(str);
    }
}
