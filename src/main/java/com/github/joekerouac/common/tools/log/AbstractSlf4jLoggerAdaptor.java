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

import org.slf4j.Logger;
import org.slf4j.Marker;

/**
 * Slf4j日志适配器，方便快速实现slf4j的logger
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public abstract class AbstractSlf4jLoggerAdaptor implements Logger {

    /**
     * 实际日志记录
     * 
     * @param marker
     *            marker
     * @param level
     *            日志级别
     * @param e
     *            异常
     * @param msg
     *            消息模板
     * @param args
     *            消息模板参数
     */
    protected abstract void log(Marker marker, LogLevel level, Throwable e, String msg, Object... args);

    /**
     * 实际日志记录
     *
     * @param marker
     *            marker
     * @param level
     *            日志级别
     * @param msg
     *            消息模板
     * @param args
     *            消息模板参数
     */
    protected void log(Marker marker, LogLevel level, String msg, Object... args) {
        log(marker, level, null, msg, args);
    }

    /**
     * 指定级别的日志是否允许
     * 
     * @param marker
     *            marker
     * @param level
     *            日志级别
     * @return true表示允许
     */
    protected abstract boolean isLevelEnable(Marker marker, LogLevel level);

    @Override
    public boolean isTraceEnabled() {
        return isTraceEnabled(null);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return isLevelEnable(marker, LogLevel.TRACE);
    }

    @Override
    public void trace(String msg) {
        trace((Marker)null, msg);
    }

    @Override
    public void trace(String format, Object arg) {
        trace((Marker)null, format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        trace((Marker)null, format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        trace((Marker)null, format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        trace((Marker)null, msg, t);
    }

    @Override
    public void trace(Marker marker, String msg) {
        log(marker, LogLevel.TRACE, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        log(marker, LogLevel.TRACE, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        log(marker, LogLevel.TRACE, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        log(marker, LogLevel.TRACE, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        log(marker, LogLevel.TRACE, t, msg);
    }

    @Override
    public boolean isDebugEnabled() {
        return isDebugEnabled(null);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return isLevelEnable(marker, LogLevel.DEBUG);
    }

    @Override
    public void debug(String msg) {
        debug((Marker)null, msg);
    }

    @Override
    public void debug(String format, Object arg) {
        debug((Marker)null, format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        debug((Marker)null, format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        debug((Marker)null, format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        debug((Marker)null, msg, t);
    }

    @Override
    public void debug(Marker marker, String msg) {
        log(marker, LogLevel.DEBUG, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        log(marker, LogLevel.DEBUG, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        log(marker, LogLevel.DEBUG, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... argArray) {
        log(marker, LogLevel.DEBUG, format, argArray);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        log(marker, LogLevel.DEBUG, t, msg);
    }

    @Override
    public boolean isInfoEnabled() {
        return isInfoEnabled(null);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return isLevelEnable(marker, LogLevel.INFO);
    }

    @Override
    public void info(String msg) {
        info((Marker)null, msg);
    }

    @Override
    public void info(String format, Object arg) {
        info((Marker)null, format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        info((Marker)null, format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        info((Marker)null, format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        info((Marker)null, msg, t);
    }

    @Override
    public void info(Marker marker, String msg) {
        log(marker, LogLevel.INFO, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        log(marker, LogLevel.INFO, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        log(marker, LogLevel.INFO, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... argArray) {
        log(marker, LogLevel.INFO, format, argArray);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        log(marker, LogLevel.INFO, t, msg);
    }

    @Override
    public boolean isWarnEnabled() {
        return isWarnEnabled(null);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return isLevelEnable(marker, LogLevel.WARN);
    }

    @Override
    public void warn(String msg) {
        warn((Marker)null, msg);
    }

    @Override
    public void warn(String format, Object arg) {
        warn((Marker)null, format, arg);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        warn((Marker)null, format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object... arguments) {
        warn((Marker)null, format, arguments);
    }

    @Override
    public void warn(String msg, Throwable t) {
        warn((Marker)null, msg, t);
    }

    @Override
    public void warn(Marker marker, String msg) {
        log(marker, LogLevel.WARN, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        log(marker, LogLevel.WARN, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        log(marker, LogLevel.WARN, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... argArray) {
        log(marker, LogLevel.WARN, format, argArray);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        log(marker, LogLevel.WARN, t, msg);
    }

    @Override
    public boolean isErrorEnabled() {
        return isErrorEnabled(null);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return isLevelEnable(marker, LogLevel.ERROR);
    }

    @Override
    public void error(String msg) {
        error((Marker)null, msg);
    }

    @Override
    public void error(String format, Object arg) {
        error((Marker)null, format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        error((Marker)null, format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        error((Marker)null, format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        error((Marker)null, msg, t);
    }

    @Override
    public void error(Marker marker, String msg) {
        log(marker, LogLevel.ERROR, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        log(marker, LogLevel.ERROR, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        log(marker, LogLevel.ERROR, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... argArray) {
        log(marker, LogLevel.ERROR, format, argArray);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        log(marker, LogLevel.ERROR, t, msg);
    }
}
