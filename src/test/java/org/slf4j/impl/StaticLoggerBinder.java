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

import org.slf4j.ILoggerFactory;
import org.slf4j.spi.LoggerFactoryBinder;

/**
 * slf4j绑定器，包名、类名必须是固定的
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

    public static final String REQUESTED_API_VERSION = "1.0.0";

    private static final StaticLoggerBinder STATIC_LOGGER_BINDER = new StaticLoggerBinder();

    private TestILoggerFactory loggerFactory = new TestILoggerFactory();

    public static StaticLoggerBinder getSingleton() {
        return STATIC_LOGGER_BINDER;
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return loggerFactory.getClass().getName();
    }
}
