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

import java.util.Arrays;

import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * 数据库类型
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public enum DBType implements EnumInterface {

    /**
     * 注意，MariaDB的错误码与MySQL兼容
     */
    MySQL("MySQL"),

    SqlServer("SqlServer"),

    ;

    static {
        // 重复检测
        EnumInterface.duplicateCheck(DBType.class);
    }

    private final String code;
    private final String desc;
    private final String englishName;
    private final int[] duplicateKeyCodes;
    private final int[] cannotAcquireLockCodes;

    DBType(String code) {
        this.code = code;
        this.desc = code;
        this.englishName = code;
        // 详细的错误码参考resource中的sql-error-codes.xml
        switch (code) {
            case "MySQL":
                duplicateKeyCodes = new int[] {1062};
                cannotAcquireLockCodes = new int[] {1205, 3572};
                break;
            case "SqlServer":
                duplicateKeyCodes = new int[] {2601, 2627};
                cannotAcquireLockCodes = new int[] {1222};
                break;
            default:
                throw new UnsupportedOperationException(StringUtils.format("不支持的枚举类型： [{}]", code));

        }
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
     * 判断指定错误码是不是主键冲突错误码
     * 
     * @param code
     *            错误码
     * @return true表示是主键冲突错误码
     */
    public boolean isDuplicateKey(int code) {
        return Arrays.binarySearch(duplicateKeyCodes, code) >= 0;
    }

    /**
     * 判断指定错误码是不是锁失败错误码
     * 
     * @param code
     *            错误码
     * @return true表示是锁失败错误码
     */
    public boolean isCannotAcquireLock(int code) {
        return Arrays.binarySearch(cannotAcquireLockCodes, code) >= 0;
    }

}
