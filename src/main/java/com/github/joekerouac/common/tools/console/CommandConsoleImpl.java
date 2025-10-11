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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.github.joekerouac.common.tools.constant.Const;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.exception.CommonException;
import com.github.joekerouac.common.tools.exception.ExceptionUtil;
import com.github.joekerouac.common.tools.log.Logger;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * 基本命令控制台实现，可以多次使用
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class CommandConsoleImpl implements CommandConsole {

    private final Logger logger;

    /**
     * 环境变量
     */
    private final Map<String, String> env;

    /**
     * 命令行字符集
     */
    private final String charset;

    /**
     * 当前执行目录
     */
    private File baseDir;

    CommandConsoleImpl(Map<String, String> env, File baseDir, Logger logger, String charset) {
        Assert.argNotNull(env, "env");
        Assert.argNotNull(baseDir, "baseDir");
        Assert.argNotNull(logger, "logger");

        this.env = env;
        this.baseDir = baseDir;
        this.logger = logger;
        String defaultCharset = Const.IS_WINDOWS ? "GBK" : "UTF-8";
        this.charset = StringUtils.getOrDefault(charset, defaultCharset);
    }

    @Override
    public CommandResult exec(String command, Map<String, String> env) {
        try {
            logger.debug("准备执行命令[{}]", command);
            Process process = createProcess(command, env);

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), charset));
            BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream(), charset));

            AtomicReference<String> msgRef = new AtomicReference<>();
            AtomicReference<String> errorMsgRef = new AtomicReference<>();
            Thread msgThread = new Thread(() -> msgRef.set(read(reader)));
            Thread errorMsgThread = new Thread(() -> errorMsgRef.set(read(errReader)));

            msgThread.start();
            errorMsgThread.start();
            try {
                // 等待执行结束
                process.waitFor();
            } catch (InterruptedException e) {
                msgThread.interrupt();
                errorMsgThread.interrupt();
                throw new CommonException(ErrorCodeEnum.UNKNOWN_EXCEPTION, e);
            }

            try {
                msgThread.join();
            } catch (InterruptedException e) {
                errorMsgThread.interrupt();
                throw new RuntimeException(e);
            }

            try {
                errorMsgThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // 读取结果
            String msg = msgRef.get();
            String errMsg = errorMsgRef.get();

            CommandResult result = new CommandResult();
            result.setExitCode(process.exitValue());
            result.setMsg(msg);
            result.setErrMsg(errMsg);
            result.setCommand(command);
            logger.debug("命令[{}]执行结果:[{}]", command, result);
            return result;
        } catch (IOException e) {
            throw new CommonException(ErrorCodeEnum.IO_EXCEPTION, StringUtils.format("命令[{}]执行异常", command), e);
        }
    }

    @Override
    public File baseDir() {
        return baseDir;
    }

    @Override
    public void changeBaseDir(File baseDir) {
        Assert.argNotNull(baseDir, "baseDir");

        this.baseDir = baseDir;
    }

    /**
     * 从BufferedReader中读取内容
     * 
     * @param reader
     *            reader
     * @return 读取到的内容
     */
    private String read(BufferedReader reader) {
        StringBuilder msgBuilder = new StringBuilder();
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                msgBuilder.append(line);
                msgBuilder.append("\n");
            }
        } catch (Throwable throwable) {
            msgBuilder.append(ExceptionUtil.printStack(throwable));
        }

        return msgBuilder.toString();
    }

    /**
     * 构建一个Process
     * 
     * @param command
     *            要执行的命令
     * @param env
     *            额外的环境
     * @return Process
     * @throws IOException
     *             IO异常
     */
    private Process createProcess(String command, Map<String, String> env) throws IOException {
        ProcessBuilder builder = new ProcessBuilder();
        List<String> cmdArr = new ArrayList<>();

        // windows平台使用powershell执行命令
        if (Const.IS_WINDOWS) {
            cmdArr.add("powershell");
            cmdArr.add("/c");
            cmdArr.add(command);
        } else {
            cmdArr.add("/bin/bash");
            cmdArr.add("-c");
            cmdArr.add(command);
        }

        // 当前使用的env，全局env作为默认值，优先使用用户主动传入的
        Map<String, String> usedEnv = new HashMap<>(this.env);
        usedEnv.putAll(env);

        builder.directory(baseDir);
        builder.environment().putAll(usedEnv);
        builder.command(cmdArr);
        return builder.start();
    }

}
