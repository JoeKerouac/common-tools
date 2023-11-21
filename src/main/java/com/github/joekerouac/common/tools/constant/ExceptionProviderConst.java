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
package com.github.joekerouac.common.tools.constant;

import com.github.joekerouac.common.tools.exception.CodeErrorException;
import com.github.joekerouac.common.tools.exception.DBException;
import com.github.joekerouac.common.tools.function.ExceptionProvider;

/**
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public final class ExceptionProviderConst {

    /**
     * RuntimeException
     */
    public static final ExceptionProvider RuntimeExceptionProvider = ((cause, msg) -> new RuntimeException(msg, cause));

    /**
     * IllegalArgumentException
     */
    public static final ExceptionProvider IllegalArgumentExceptionProvider =
        ((cause, msg) -> new IllegalArgumentException(msg, cause));

    /**
     * IllegalStateException
     */
    public static final ExceptionProvider IllegalStateExceptionProvider =
        ((cause, msg) -> new IllegalStateException(msg, cause));

    /**
     * UnsupportedOperationException
     */
    public static final ExceptionProvider UnsupportedOperationExceptionProvider =
        ((cause, msg) -> new UnsupportedOperationException(msg, cause));

    /**
     * 数据库异常
     */
    public static final ExceptionProvider DBExceptionProvider = ((cause, msg) -> new DBException(msg, cause));

    /**
     * 编码异常
     */
    public static final ExceptionProvider CodeErrorExceptionProvider =
        ((cause, msg) -> new CodeErrorException(msg, cause));

}
