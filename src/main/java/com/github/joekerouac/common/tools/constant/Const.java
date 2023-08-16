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
package com.github.joekerouac.common.tools.constant;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.Provider;
import java.util.regex.Pattern;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 系统级别的常量
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Const {

    static {
        String osName = System.getProperty("os.name");
        Pattern pattern = Pattern.compile(".*?[w|W][i|I][n|N][d|D][o|O][w|W][s|S].*");

        if (osName.contains("Mac OS")) {
            IS_MAC_OS = true;
            IS_WINDOWS = false;
            CLASSPATH_SEPARATOR = ":";
        } else if (pattern.matcher(osName).matches()) {
            IS_MAC_OS = false;
            IS_WINDOWS = true;
            CLASSPATH_SEPARATOR = ";";
        } else {
            IS_MAC_OS = false;
            IS_WINDOWS = false;
            CLASSPATH_SEPARATOR = ":";
        }
    }

    /**
     * 当前系统是否是windows系统
     */
    public static final boolean IS_WINDOWS;

    /**
     * 当前系统是否是mac
     */
    public static final boolean IS_MAC_OS;

    /**
     * class path分隔符，Windows和Linux场景是不同的
     */
    public static final String CLASSPATH_SEPARATOR;

    /**
     * 默认字符集编码，注意，这里不要使用{@link Charset#defaultCharset()}，使用项目编码UTF-8
     */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    /**
     * java home
     */
    public static final String JAVA_HOME = System.getProperty(SystemPropKeyConst.JAVA_HOME);

    /**
     * java class path
     */
    public static final String JAVA_CLASS_PATH = System.getProperty(SystemPropKeyConst.JAVA_CLASS_PATH);

    /**
     * 当前操作系统名称
     */
    public static final String OS_NAME = System.getProperty(SystemPropKeyConst.OS_NAME);

    /**
     * 文件分隔符
     */
    public static final String FILE_SEPARATOR = System.getProperty(SystemPropKeyConst.FILE_SEPARATOR);

    /**
     * 环境变量分隔符，分隔多个环境变量使用，windows下是分号，Unix是冒号
     */
    public static final String PATH_SEPARATOR = System.getProperty(SystemPropKeyConst.PATH_SEPARATOR);

    /**
     * 行分隔符
     */
    public static final String LINE_SEPARATOR = System.getProperty(SystemPropKeyConst.LINE_SEPARATOR);

    /**
     * 当前用户名
     */
    public static final String USER_NAME = System.getProperty(SystemPropKeyConst.USER_NAME);

    /**
     * 用户目录
     */
    public static final String USER_HOME = System.getProperty(SystemPropKeyConst.USER_HOME);

    /**
     * 当前工作目录
     */
    public static final String USER_DIR = System.getProperty(SystemPropKeyConst.USER_DIR);

    /**
     * byte的长度
     */
    public static final int BYTE_LEN = 8;

    /**
     * short的长度
     */
    public static final int SHORT_LEN = 16;

    /**
     * int的长度
     */
    public static final int INT_LEN = 32;

    /**
     * float的长度
     */
    public static final int FLOAT_LEN = 32;

    /**
     * long的长度
     */
    public static final int LONG_LEN = 64;

    /**
     * double的长度
     */
    public static final int DOUBLE_LEN = 64;

    /**
     * 全局共享provider，防止jdk bug导致的oom
     *
     * @see <a href="https://bugs.openjdk.org/browse/JDK-8168469">JDK-8168469</a>
     */
    public static final Provider BC_PROVIDER = new BouncyCastleProvider();

}
