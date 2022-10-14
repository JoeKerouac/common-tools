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
package org.slf4j.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.Marker;

import com.github.joekerouac.common.tools.codec.xml.Dom4JXmlCodec;
import com.github.joekerouac.common.tools.date.DateUtil;
import com.github.joekerouac.common.tools.log.AbstractSlf4jLoggerAdaptor;
import com.github.joekerouac.common.tools.log.LogLevel;
import com.github.joekerouac.common.tools.net.http.IHttpClient;
import com.github.joekerouac.common.tools.reflect.ReflectUtil;
import com.github.joekerouac.common.tools.reflect.bean.BeanUtils;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * 测试用例使用ILoggerFactory实现，主要是为测试用例提供日志输出
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class TestILoggerFactory implements ILoggerFactory {

    /**
     * 日志级别
     */
    private static final Map<String, LogLevel> LEVEL_MAP = new HashMap<>();

    private final Map<String, SystemPrintLogger> loggers = new ConcurrentHashMap<>();

    static {
        LEVEL_MAP.put(IHttpClient.class.getPackage().getName(), LogLevel.WARN);
        LEVEL_MAP.put(BeanUtils.class.getName(), LogLevel.WARN);
        LEVEL_MAP.put(ReflectUtil.class.getName(), LogLevel.WARN);
        LEVEL_MAP.put(Dom4JXmlCodec.class.getName(), LogLevel.WARN);
        LEVEL_MAP.put("org.apache.hc.", LogLevel.WARN);
    }

    @Override
    public Logger getLogger(String name) {
        return loggers.compute(name, (key, logger) -> {
            if (logger == null) {
                return new SystemPrintLogger(name);
            }
            return logger;
        });
    }

    public static class SystemPrintLogger extends AbstractSlf4jLoggerAdaptor {

        private final String name;

        private LogLevel minLevel;

        public SystemPrintLogger(String name) {
            this.name = name;
            for (Map.Entry<String, LogLevel> entry : LEVEL_MAP.entrySet()) {
                if (name.startsWith(entry.getKey())) {
                    minLevel = entry.getValue();
                    break;
                }
            }

            if (minLevel == null) {
                minLevel = LogLevel.TRACE;
            }
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        protected boolean isLevelEnable(Marker marker, LogLevel level) {
            return level.getLevel() >= minLevel.getLevel();
        }

        @Override
        protected void log(Marker marker, LogLevel level, Throwable e, String msg, Object... args) {
            String print = StringUtils.format("{} [{}] {}", DateUtil.getFormatDate(new Date(), DateUtil.BASE),
                level.name(), StringUtils.format(msg, args));

            if (!isLevelEnable(marker, level)) {
                return;
            }

            if (level == LogLevel.ERROR) {
                System.err.println(print);
            } else {
                System.out.println(print);
            }
            if (e != null) {
                e.printStackTrace();
            }
        }

    }
}
