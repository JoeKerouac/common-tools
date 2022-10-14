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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.joekerouac.common.tools.constant.Const;
import com.github.joekerouac.common.tools.exception.CommonException;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.string.StringUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * linux命令行工具
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandUtil {

    /**
     * 执行shell脚本
     * 
     * @param commandConsole
     *            命令控制台
     * @param shellPath
     *            shell脚本文件的绝对路径
     */
    public static void execShell(CommandConsole commandConsole, String shellPath) {
        commandConsole.execOrThrow(StringUtils.format("sh {}", shellPath), ErrorCodeEnum.COMMAND_EXEC_ERROR,
            StringUtils.format("shell脚本执行失败，要执行的shell脚本:[{}]", shellPath));
    }

    /**
     * 创建一个软连接
     * 
     * @param commandConsole
     *            命令控制台
     * @param src
     *            源路径
     * @param dst
     *            目标路径
     */
    public static void createSoftLink(CommandConsole commandConsole, String src, String dst) {
        commandConsole.execOrThrow(StringUtils.format("ln -s {} {}", src, dst), ErrorCodeEnum.COMMAND_EXEC_ERROR,
            StringUtils.format("软连接创建失败，执行命令：[ln -s {} {}]", src, dst));
    }

    /**
     * 判断指定命令是否存在
     * 
     * @param commandConsole
     *            命令控制台
     * @param command
     *            命令
     * @return 返回true表示命令存在
     */
    public static boolean commandExist(CommandConsole commandConsole, String command) {
        return commandConsole.exec(StringUtils.format("command -v {}", command)).getExitCode() == 0;
    }

    /**
     * 安装应用
     * 
     * @param commandConsole
     *            命令控制台
     * @param app
     *            要安装的应用
     */
    public static void install(CommandConsole commandConsole, String app) {
        commandConsole.execOrThrow(StringUtils.format("yum install -y {}", app), ErrorCodeEnum.COMMAND_EXEC_ERROR,
            StringUtils.format("应用{}安装失败", app));
    }

    /**
     * 杀死指定进程
     * 
     * @param commandConsole
     *            命令控制台
     * @param pid
     *            要杀死的进程
     * @param signal
     *            信号
     */
    public static CommandResult kill(CommandConsole commandConsole, int pid, int signal) {
        // 这里不管执行失败成功，都认为成功，因为有可能是因为进程不存在失败的
        return commandConsole.exec(StringUtils.format("kill -{} {}", signal, pid));
    }

    /**
     * 获取所有pid
     * 
     * @param commandConsole
     *            命令控制台
     * @return 当前所有PID
     */
    public static int[] getPids(CommandConsole commandConsole) {
        CommandResult result = commandConsole.exec("ps -e");
        if (result.getExitCode() != 0) {
            throw new CommonException(ErrorCodeEnum.COMMAND_EXEC_ERROR,
                StringUtils.format("执行命令ps -e失败，执行结果：[{}]", result));
        }

        String msg = result.getMsg();
        String[] strs = msg.split(Const.LINE_SEPARATOR);

        Pattern pattern = Pattern.compile("([0-9]*).*");
        List<Integer> pids = new ArrayList<>();
        // 跳过第一行，第一行不包含PID
        for (int i = 1; i < strs.length; i++) {
            String str = strs[i];
            Matcher matcher = pattern.matcher(str.trim());
            if (matcher.find()) {
                String pidStr = matcher.group(1);

                if (StringUtils.isBlank(pidStr)) {
                    continue;
                }
                pids.add(Integer.parseInt(pidStr));
            }
        }

        return pids.stream().mapToInt(i -> i).toArray();
    }

    /**
     * 判断执行PID是否存在
     * 
     * @param commandConsole
     *            命令控制台
     * @param pid
     *            pid
     * @return 返回true表示指定PID存在
     */
    public static boolean pidExist(CommandConsole commandConsole, int pid) {
        if (pid < 0) {
            return false;
        }

        int[] pids = getPids(commandConsole);
        for (int i : pids) {
            if (pid == i) {
                return true;
            }
        }
        return false;
    }

}
