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
package com.github.joekerouac.common.tools.registry;

import lombok.Getter;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class RegistryException extends RuntimeException {

    /**
     * 注册表错误码，jni使用这些错误码创建异常
     */
    public static final int ERROR_SUCCESS = 0;

    public static final int ERROR_FILE_NOT_FOUND = 2;

    public static final int ERROR_ACCESS_DENIED = 5;

    public static final int ERROR_INVALID_HANDLE = 6;

    public static final int ERROR_INVALID_PARAMETER = 87;

    public static final int ERROR_CALL_NOT_IMPLEMENTED = 120;

    public static final int ERROR_INSUFFICIENT_BUFFER = 122;

    public static final int ERROR_LOCK_FAILED = 167;

    public static final int ERROR_TRANSFER_TOO_LONG = 222;

    public static final int ERROR_MORE_DATA = 234;

    public static final int ERROR_NO_MORE_ITEMS = 259;

    public static final int ERROR_BADDB = 1009;

    public static final int ERROR_BADKEY = 1010;

    public static final int ERROR_CANTOPEN = 1011;

    public static final int ERROR_CANTREAD = 1012;

    public static final int ERROR_CANTWRITE = 1013;

    public static final int ERROR_REGISTRY_RECOVERED = 1014;

    public static final int ERROR_REGISTRY_CORRUPT = 1015;

    public static final int ERROR_REGISTRY_IO_FAILED = 1016;

    public static final int ERROR_NOT_REGISTRY_FILE = 1017;

    public static final int ERROR_KEY_DELETED = 1018;

    public static final int ERROR_UNKNOWN = -1;

    @Getter
    private final int code;

    public RegistryException(String msg) {
        this(msg, ERROR_UNKNOWN);
    }

    /**
     * jni的c代码中会调用
     * 
     * @param msg
     *            错误消息
     * @param code
     *            错误码
     */
    public RegistryException(String msg, int code) {
        super(msg);
        this.code = code;
    }

}
