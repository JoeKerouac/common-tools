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
package com.github.joekerouac.common.tools.function;

/**
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface ExceptionProvider {

    /**
     * 创建一个新的异常
     * 
     * @param cause
     *            cause，可能为空
     * @param msg
     *            msg，不能为空
     * @return 异常
     */
    RuntimeException newRuntimeException(Throwable cause, String msg);

    /**
     * 创建一个新的异常
     * 
     * @param msg
     *            异常消息，不能为空
     * @return 异常
     */
    default RuntimeException newRuntimeException(String msg) {
        return newRuntimeException(null, msg);
    }

}
