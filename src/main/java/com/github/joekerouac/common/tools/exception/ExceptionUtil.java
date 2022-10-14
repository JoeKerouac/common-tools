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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class ExceptionUtil {

    /**
     * 打印异常的异常栈到字符串
     *
     * @param throwable
     *            异常
     * @return 异常栈
     */
    public static String printStack(Throwable throwable) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            throwable.printStackTrace(new PrintStream(bos, true, Charset.defaultCharset().name()));
            return bos.toString(Charset.defaultCharset().name());
        } catch (UnsupportedEncodingException e) {
            // 这里不可能发生
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过递归调用getCause来获取最底层的异常
     *
     * @param throwable
     *            上层异常，不能为null
     * @return 最底层异常
     */
    public static Throwable getRootCause(Throwable throwable) {
        if (throwable.getCause() == null) {
            return throwable;
        } else {
            return getRootCause(throwable.getCause());
        }
    }

}
