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
package com.github.joekerouac.common.tools.exception;

import com.github.joekerouac.common.tools.enums.EnumInterface;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * 顶级异常
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class BaseException extends RuntimeException {

    /**
     * 当前异常的错误码
     */
    private final EnumInterface errCode;

    public BaseException(EnumInterface errCode) {
        super(toMsg(errCode, null));
        this.errCode = errCode;
    }

    public BaseException(EnumInterface errCode, String message) {
        super(toMsg(errCode, message));
        this.errCode = errCode;
    }

    public BaseException(EnumInterface errCode, String message, Throwable cause) {
        super(toMsg(errCode, message), cause);
        this.errCode = errCode;
    }

    public BaseException(EnumInterface errCode, Throwable cause) {
        super(toMsg(errCode, null), cause);
        this.errCode = errCode;
    }

    protected BaseException(EnumInterface errCode, String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(toMsg(errCode, message), cause, enableSuppression, writableStackTrace);
        this.errCode = errCode;
    }

    /**
     * 获取当前错误码
     *
     * @return 错误码
     */
    public EnumInterface getErrCode() {
        return this.errCode;
    }

    private static String toMsg(EnumInterface errCode, String msg) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(msg)) {
            sb.append("msg: ").append(msg).append(", ");
        }

        if (errCode != null) {
            sb.append("errCode: ").append(errCode.code()).append(", errCodeDesc: ").append(errCode.desc());
        }
        return sb.toString();
    }

}
