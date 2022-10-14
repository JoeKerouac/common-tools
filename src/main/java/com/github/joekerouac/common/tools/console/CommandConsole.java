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
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.github.joekerouac.common.tools.enums.EnumInterface;
import com.github.joekerouac.common.tools.exception.CommonException;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * 命令行控制台
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface CommandConsole {

    /**
     * 执行命令
     * 
     * @param command
     *            要执行的命令
     * @return 执行结果
     */
    default CommandResult exec(String command) {
        return exec(command, Collections.emptyMap());
    }

    /**
     * 指定命令
     * 
     * @param command
     *            要执行的命令
     * @param env
     *            执行环境上下文
     * @return 执行结果
     */
    CommandResult exec(String command, Map<String, String> env);

    /**
     * 异步执行command
     * 
     * @param command
     *            command
     * @param executorService
     *            线程池
     * @return Future结果
     */
    default Future<CommandResult> asynExec(String command, ExecutorService executorService) {
        return executorService.submit(() -> exec(command));
    }

    /**
     * 异步执行command
     *
     * @param command
     *            command
     * @param env
     *            执行环境
     * @param executorService
     *            线程池
     * @return Future结果
     */
    default Future<CommandResult> asynExec(String command, Map<String, String> env, ExecutorService executorService) {
        return executorService.submit(() -> exec(command, env));
    }

    /**
     * 执行命令，如果命令执行失败则抛出异常
     * 
     * @param command
     *            要执行的命令
     * @param errCode
     *            抛出异常的错误码，允许为空
     * @param msg
     *            错误消息，允许为空
     * @return 执行结果
     */
    default CommandResult execOrThrow(String command, EnumInterface errCode, String msg) {
        return execOrThrow(command, Collections.emptyMap(), errCode, msg);
    }

    /**
     * 执行命令，如果命令执行失败则抛出异常
     *
     * @param command
     *            要执行的命令
     * @param env
     *            执行环境
     * @param errCode
     *            抛出异常的错误码，允许为空
     * @param msg
     *            错误消息，允许为空
     * @return 执行结果
     */
    default CommandResult execOrThrow(String command, Map<String, String> env, EnumInterface errCode, String msg) {
        CommandResult result = exec(command, env);
        if (result.getExitCode() != 0) {
            String userMsg = StringUtils.getOrDefault(msg, ErrorCodeEnum.COMMAND_EXEC_ERROR.desc());
            String finalMsg = StringUtils.format("命令执行失败，执行结果：[{}]，userMsg:[{}]", result, userMsg);
            throw new CommonException(errCode == null ? ErrorCodeEnum.COMMAND_EXEC_ERROR : errCode, finalMsg);
        }
        return result;
    }

    /**
     * 异步执行command
     * 
     * @param command
     *            command
     * @param executorService
     *            线程池
     * @return Future结果
     */
    default Future<CommandResult> asynExecOrThrow(String command, EnumInterface errCode, String msg,
        ExecutorService executorService) {
        return executorService.submit(() -> execOrThrow(command, errCode, msg));
    }

    /**
     * 异步执行command
     *
     * @param command
     *            command
     * @param env
     *            执行环境
     * @param executorService
     *            线程池
     * @return Future结果
     */
    default Future<CommandResult> asynExecOrThrow(String command, Map<String, String> env, EnumInterface errCode,
        String msg, ExecutorService executorService) {
        return executorService.submit(() -> execOrThrow(command, env, errCode, msg));
    }

    /**
     * 获取当前shell执行时的默认目录
     * 
     * @return 当前shell执行时的默认目录
     */
    File baseDir();

    /**
     * 更改shell的当前目录
     * 
     * @param baseDir
     *            要更改为的当前目录
     */
    void changeBaseDir(File baseDir);
}
