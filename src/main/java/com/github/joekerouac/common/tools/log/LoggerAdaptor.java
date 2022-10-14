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
 * Logger适配器，降低Logger实现复杂度，只需要继承该接口即可
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface LoggerAdaptor extends Logger {

    /**
     * 实际日志输出
     * 
     * @param level
     *            日志级别
     * @param e
     *            异常，可以为null
     * @param msg
     *            消息模板
     * @param args
     *            模板参数
     */
    void log(LogLevel level, Throwable e, String msg, Object... args);

    /**
     * 实际日志输出
     * 
     * @param level
     *            日志级别
     * @param e
     *            异常，可以为null
     * @param msgSupplier
     *            消息提供器
     */
    default void log(LogLevel level, Throwable e, Supplier<String> msgSupplier) {
        if (isEnabled(level)) {
            log(level, e, msgSupplier.get());
        }
    }

    /**
     * 是否允许指定级别的日志
     * 
     * @param level
     *            日志级别
     * @return 返回true表示允许指定级别的日志
     */
    boolean isEnabled(LogLevel level);

    @Override
    default boolean isDebugEnabled() {
        return isEnabled(LogLevel.DEBUG);
    }

    @Override
    default void debug(String msg, Object... args) {
        log(LogLevel.DEBUG, null, msg, args);
    }

    @Override
    default void debug(Throwable e, String msg, Object... args) {
        log(LogLevel.DEBUG, e, msg, args);
    }

    @Override
    default void debug(Supplier<String> msgSupplier) {
        log(LogLevel.DEBUG, null, msgSupplier);
    }

    @Override
    default boolean isInfoEnabled() {
        return isEnabled(LogLevel.INFO);
    }

    @Override
    default void info(String msg, Object... args) {
        log(LogLevel.INFO, null, msg, args);
    }

    @Override
    default void info(Throwable e, String msg, Object... args) {
        log(LogLevel.INFO, e, msg, args);
    }

    @Override
    default void info(Supplier<String> msgSupplier) {
        log(LogLevel.INFO, null, msgSupplier);
    }

    @Override
    default void info(Throwable e, Supplier<String> msgSupplier) {
        log(LogLevel.INFO, e, msgSupplier);
    }

    @Override
    default boolean isWarnEnabled() {
        return isEnabled(LogLevel.WARN);
    }

    @Override
    default void warn(String msg, Object... args) {
        log(LogLevel.WARN, null, msg, args);
    }

    @Override
    default void warn(Throwable e, String msg, Object... args) {
        log(LogLevel.WARN, e, msg, args);
    }

    @Override
    default void warn(Supplier<String> msgSupplier) {
        log(LogLevel.WARN, null, msgSupplier);
    }

    @Override
    default void warn(Throwable e, Supplier<String> msgSupplier) {
        log(LogLevel.WARN, e, msgSupplier);
    }

    @Override
    default boolean isErrorEnabled() {
        return isEnabled(LogLevel.ERROR);
    }

    @Override
    default void error(String msg, Object... args) {
        log(LogLevel.ERROR, null, msg, args);
    }

    @Override
    default void error(Throwable e, String msg, Object... args) {
        log(LogLevel.ERROR, e, msg, args);
    }

    @Override
    default void error(Supplier<String> msgSupplier) {
        log(LogLevel.ERROR, null, msgSupplier);
    }

    @Override
    default void error(Throwable e, Supplier<String> msgSupplier) {
        log(LogLevel.ERROR, e, msgSupplier);
    }
}
