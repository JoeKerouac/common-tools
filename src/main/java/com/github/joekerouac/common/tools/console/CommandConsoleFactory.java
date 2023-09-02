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
package com.github.joekerouac.common.tools.console;

import java.io.File;
import java.util.Map;

import com.github.joekerouac.common.tools.constant.Const;
import com.github.joekerouac.common.tools.log.Logger;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * CommandConsole工厂，用于创建CommandConsole
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandConsoleFactory {

    /**
     * 创建一个命令控制台
     * 
     * @param logger
     *            日志记录
     * @return 命令控制台
     */
    public static CommandConsole create(Logger logger) {
        return create(System.getenv(), new File(Const.USER_HOME), logger, null);
    }

    /**
     * 创建一个命令控制台，指定工作目录
     * 
     * @param baseDir
     *            工作目录，不能为空
     * @param logger
     *            日志记录
     * @return 命令控制台
     */
    public static CommandConsole create(File baseDir, Logger logger) {
        return create(System.getenv(), baseDir, logger, null);
    }

    /**
     * 创建一个命令控制台，指定环境变量
     * 
     * @param env
     *            环境变量，不能为空
     * @param logger
     *            日志记录
     * @return 命令控制台
     */
    public static CommandConsole create(Map<String, String> env, Logger logger) {
        return create(env, new File(Const.USER_HOME), logger, null);
    }

    /**
     * 创建一个命令控制台，指定环境变量、工作目录
     * 
     * @param env
     *            环境变量，不能为空
     * @param baseDir
     *            工作目录，不能为空
     * @param logger
     *            日志记录
     * @return 命令控制台
     */
    public static CommandConsole create(Map<String, String> env, File baseDir, Logger logger) {
        return create(env, baseDir, logger, null);
    }

    /**
     * 创建一个命令控制台，指定环境变量、工作目录
     *
     * @param env
     *            环境变量，不能为空
     * @param baseDir
     *            工作目录，不能为空
     * @param logger
     *            日志记录
     * @param charset
     *            命令行字符集
     * @return 命令控制台
     */
    public static CommandConsole create(Map<String, String> env, File baseDir, Logger logger, String charset) {
        Assert.argNotNull(env, "env");
        Assert.argNotNull(baseDir, "baseDir");

        return new CommandConsoleImpl(env, baseDir, logger, charset);
    }

}
