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

import com.github.joekerouac.common.tools.exception.CommonException;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * slf4j到系统日志的桥接
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class Slf4j2SystemLoggerBridge implements LoggerAdaptor {

    private final Logger logger;

    public Slf4j2SystemLoggerBridge(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(LogLevel level, Throwable e, String msg, Object... args) {
        if (!isEnabled(level)) {
            return;
        }

        String print = StringUtils.format(msg, args);
        switch (level) {
            case TRACE:
                logger.trace(print, e);
                break;
            case DEBUG:
                logger.debug(print, e);
                break;
            case INFO:
                logger.info(print, e);
                break;
            case WARN:
                logger.warn(print, e);
                break;
            case ERROR:
                logger.error(print, e);
                break;
            default:
                throw new CommonException(ErrorCodeEnum.UNKNOWN_EXCEPTION, String.format("未知的日志级别：%s", level.name()));
        }
    }

    @Override
    public boolean isEnabled(LogLevel level) {
        switch (level) {
            case TRACE:
                return logger.isTraceEnabled();
            case DEBUG:
                return logger.isDebugEnabled();
            case INFO:
                return logger.isInfoEnabled();
            case WARN:
                return logger.isWarnEnabled();
            case ERROR:
                return logger.isErrorEnabled();
            default:
                throw new CommonException(ErrorCodeEnum.UNKNOWN_EXCEPTION, String.format("未知的日志级别：%s", level.name()));
        }
    }
}
