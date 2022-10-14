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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 系统配置key常量
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SystemPropKeyConst {

    /**
     * JAVA版本号Key
     */
    public static final String JAVA_VERSION = "java.version";

    /**
     * JVM名称
     */
    public static final String JAVA_VM_NAME = "java.vm.name";

    /**
     * java home
     */
    public static final String JAVA_HOME = "java.home";

    /**
     * java class path
     */
    public static final String JAVA_CLASS_PATH = "java.class.path";

    /**
     * java library path
     */
    public static final String JAVA_LIBRARY_PATH = "java.library.path";

    /**
     * java IO临时目录
     */
    public static final String JAVA_IO_TMPDIR = "java.io.tmpdir";

    /**
     * java扩展lib目录
     */
    public static final String JAVA_EXT_DIRS = "java.ext.dirs";

    /**
     * 当前操作系统名称
     */
    public static final String OS_NAME = "os.name";

    /**
     * 文件分隔符
     */
    public static final String FILE_SEPARATOR = "file.separator";

    /**
     * 环境变量分隔符，分隔多个环境变量使用，windows下是分号，Unix是冒号
     */
    public static final String PATH_SEPARATOR = "path.separator";

    /**
     * 行分隔符
     */
    public static final String LINE_SEPARATOR = "line.separator";

    /**
     * 当前用户名
     */
    public static final String USER_NAME = "user.name";

    /**
     * 用户目录
     */
    public static final String USER_HOME = "user.home";

    /**
     * 当前工作目录
     */
    public static final String USER_DIR = "user.dir";
}
