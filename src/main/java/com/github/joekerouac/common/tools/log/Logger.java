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
package com.github.joekerouac.common.tools.log;

import java.util.function.Supplier;

/**
 * 系统内部日志记录器
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface Logger {

    /**
     * 是否允许debug级别的日志
     * 
     * @return 返回true表示允许
     */
    boolean isDebugEnabled();

    /**
     * debug日志
     * 
     * @param msg
     *            消息模板，使用{}作为占位符
     * @param args
     *            参数
     */
    void debug(String msg, Object... args);

    /**
     * 记录debug日志
     * 
     * @param e
     *            异常
     * @param msg
     *            消息模板
     * @param args
     *            模板参数
     */
    void debug(Throwable e, String msg, Object... args);

    /**
     * debug日志
     * 
     * @param msgSupplier
     *            日志提供者
     */
    void debug(Supplier<String> msgSupplier);

    /**
     * 是否允许info级别的日志
     * 
     * @return 返回true表示允许
     */
    boolean isInfoEnabled();

    /**
     * info日志
     * 
     * @param msg
     *            消息模板
     * @param args
     *            参数
     */
    void info(String msg, Object... args);

    /**
     * info日志
     * 
     * @param e
     *            异常
     * @param msg
     *            消息模板
     * @param args
     *            参数
     */
    void info(Throwable e, String msg, Object... args);

    /**
     * info日志
     * 
     * @param msgSupplier
     *            日志提供者
     */
    void info(Supplier<String> msgSupplier);

    /**
     * info日志
     * 
     * @param e
     *            异常
     * @param msgSupplier
     *            日志提供者
     */
    void info(Throwable e, Supplier<String> msgSupplier);

    /**
     * 是否允许warn级别的日志
     * 
     * @return 返回true表示允许
     */
    boolean isWarnEnabled();

    /**
     * warn日志
     * 
     * @param msg
     *            消息模板
     * @param args
     *            参数
     */
    void warn(String msg, Object... args);

    /**
     * warn日志
     * 
     * @param e
     *            异常
     * @param msg
     *            消息模板
     * @param args
     *            参数
     */
    void warn(Throwable e, String msg, Object... args);

    /**
     * warn日志
     * 
     * @param msgSupplier
     *            日志提供者
     */
    void warn(Supplier<String> msgSupplier);

    /**
     * warn日志
     * 
     * @param e
     *            异常
     * @param msgSupplier
     *            日志提供者
     */
    void warn(Throwable e, Supplier<String> msgSupplier);

    /**
     * 是否允许error级别的日志
     * 
     * @return 返回true表示允许
     */
    boolean isErrorEnabled();

    /**
     * error日志
     * 
     * @param msg
     *            消息模板
     * @param args
     *            参数
     */
    void error(String msg, Object... args);

    /**
     * error日志
     * 
     * @param e
     *            异常
     * @param msg
     *            消息模板
     * @param args
     *            参数
     */
    void error(Throwable e, String msg, Object... args);

    /**
     * error日志
     * 
     * @param msgSupplier
     *            日志提供者
     */
    void error(Supplier<String> msgSupplier);

    /**
     * error日志
     * 
     * @param e
     *            异常
     * @param msgSupplier
     *            日志提供者
     */
    void error(Throwable e, Supplier<String> msgSupplier);
}
