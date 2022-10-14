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
 * 空实现，什么都不做
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class NopLogger implements Logger {

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(String msg, Object... args) {

    }

    @Override
    public void debug(Throwable e, String msg, Object... args) {

    }

    @Override
    public void debug(Supplier<String> msgSupplier) {

    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public void info(String msg, Object... args) {

    }

    @Override
    public void info(Throwable e, String msg, Object... args) {

    }

    @Override
    public void info(Supplier<String> msgSupplier) {

    }

    @Override
    public void info(Throwable e, Supplier<String> msgSupplier) {

    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void warn(String msg, Object... args) {

    }

    @Override
    public void warn(Throwable e, String msg, Object... args) {

    }

    @Override
    public void warn(Supplier<String> msgSupplier) {

    }

    @Override
    public void warn(Throwable e, Supplier<String> msgSupplier) {

    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(String msg, Object... args) {

    }

    @Override
    public void error(Throwable e, String msg, Object... args) {

    }

    @Override
    public void error(Supplier<String> msgSupplier) {

    }

    @Override
    public void error(Throwable e, Supplier<String> msgSupplier) {

    }
}
