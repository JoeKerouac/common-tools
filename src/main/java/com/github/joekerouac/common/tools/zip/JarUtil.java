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
package com.github.joekerouac.common.tools.zip;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * jar工具
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public final class JarUtil {

    /**
     * 获取fat jar文件的main class
     *
     * @param jarFile
     *            fat jar文件
     * @return main class，可能会返回null
     * @throws IOException
     *             IO异常
     */
    public static String getMainClass(File jarFile) throws IOException {
        Assert.assertTrue(jarFile.exists() && jarFile.isFile(),
            StringUtils.format("jar文件 [{}] 不存在", jarFile.getAbsolutePath()),
            ExceptionProviderConst.IllegalArgumentExceptionProvider);
        JarFile jar = new JarFile(jarFile, false);
        return Optional.ofNullable(jar.getManifest()).map(Manifest::getMainAttributes)
            .map(attrs -> (String)attrs.get(Attributes.Name.MAIN_CLASS)).orElse(null);
    }

}
