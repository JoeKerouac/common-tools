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
 * 常用系统级错误码，区段为[0001-1000]，插件系统内如果要使用错误码请使用1000以后的
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public enum ErrorCodeEnum implements EnumInterface {

    UNKNOWN_EXCEPTION("UNKNOWN_EXCEPTION", "未知异常", "UNKNOWN_EXCEPTION"),

    NULL_POINT("NULL_POINT", "空指针", "NULL_POINT"),

    PARAM_ILLEGAL("PARAM_ILLEGAL", "参数异常，只对外部用户输入使用，如果是内部请使用其他错误码", "PARAM_ILLEGAL"),

    SERIAL_EXCEPTION("SERIAL_EXCEPTION", "序列化异常", "SERIAL_EXCEPTION"),

    ASSERT_EXCEPTION("ASSERT_EXCEPTION", "断言异常", "ASSERT_EXCEPTION"),

    ILLEGAL_STATE("ILLEGAL_STATE", "状态非法", "ILLEGAL_STATE"),

    CODE_ERROR("CODE_ERROR", "编码错误", "CODE_ERROR"),

    REFLECT_SECURE_EXCEPTION("REFLECT_SECURE_EXCEPTION", "安全限制问题，请检查本地安全策略", "REFLECT_SECURE_EXCEPTION"),

    NO_SUCH_METHOD("NO_SUCH_METHOD", "指定方法找不到", "NO_SUCH_METHOD"),

    CLASS_NOT_FOUND("CLASS_NOT_FOUND", "指定类查找不到", "CLASS_NOT_FOUND"),

    ILLEGAL_ACCESS("ILLEGAL_ACCESS", "非法访问", "ILLEGAL_ACCESS"),

    IO_EXCEPTION("IO_EXCEPTION", "io异常", "IO_EXCEPTION"),

    FILE_ACCESS_EXCEPTION("FILE_ACCESS_EXCEPTION", "文件访问异常", "FILE_ACCESS_EXCEPTION"),

    FILE_CREATE_ERROR("FILE_CREATE_ERROR", "文件创建异常", "FILE_CREATE_ERROR"),

    COMMAND_EXEC_ERROR("COMMAND_EXEC_ERROR", "控制台命令执行失败", "COMMAND_EXEC_ERROR"),

    FILE_NOT_EXIST("FILE_NOT_EXIST", "文件不存在", "FILE_NOT_EXIST"),

    INTERRUPTED("INTERRUPTED", "线程中断异常", "INTERRUPTED"),

    OGNL_ERROR("OGNL_ERROR", "OGNL 执行异常", "OGNL_ERROR"),

    NO_SUDO_PWD("NO_SUDO_PWD", "没有设置SUDO密码", "NO_SUDO_PWD");

    static {
        // 重复检测
        EnumInterface.duplicateCheck(ErrorCodeEnum.class);
    }

    private final String code;
    private final String desc;
    private final String englishName;

    ErrorCodeEnum(String code, String desc, String englishName) {
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
}
