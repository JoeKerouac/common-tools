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
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;

/**
 * console工具
 *
 * @author JoeKerouac
 * @date 2022-11-13 20:40
 * @since 2.0.0
 */
public class ConsoleUtil {

    /**
     * 从控制台获取命令并且处理
     *
     * @param consumer
     *            处理控制台获取的命令，当输入的是exit时系统退出
     */
    public static void start(Consumer<String> consumer) {
        try {
            Console console = System.console();
            CustomReader reader;

            if (console != null) {
                System.out.println("Console对象存在，使用Console对象");
                reader = console::readLine;
            } else {
                System.out.println("Console对象不存在，使用Reader");
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                reader = bufferedReader::readLine;
            }
            work(reader, consumer);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("系统异常，即将退出");
            System.exit(1);
        }
    }

    private static void work(CustomReader reader, Consumer<String> consumer) throws IOException {
        while (true) {
            String data = reader.readLine();
            if ("exit".equals(data)) {
                System.out.println("系统即将退出");
                System.exit(0);
            }
            consumer.accept(data);
        }
    }

    private interface CustomReader {
        String readLine() throws IOException;
    }

}
