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

/**
 * 通用异常
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class CommonException extends BaseException {

    public CommonException(EnumInterface errCode) {
        super(errCode);
    }

    public CommonException(EnumInterface errCode, String message) {
        super(errCode, message);
    }

    public CommonException(EnumInterface errCode, String message, Throwable cause) {
        super(errCode, message, cause);
    }

    public CommonException(EnumInterface errCode, Throwable cause) {
        super(errCode, cause);
    }
}
