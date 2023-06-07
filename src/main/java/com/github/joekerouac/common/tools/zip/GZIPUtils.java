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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.constant.Const;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.exception.CommonException;
import com.github.joekerouac.common.tools.file.FileUtils;
import com.github.joekerouac.common.tools.io.IOUtils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * GZIP工具
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GZIPUtils {

    /**
     * 将指定文件列表写入指定新建ZipFile，所有文件平铺写入，不创建目录
     * 
     * @param files
     *            文件
     * @param zipFile
     *            新建zip文件
     * @throws IOException
     *             IO异常
     */
    public static void zip(List<File> files, File zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            for (File file : files) {
                zos.putNextEntry(new ZipEntry(file.getName()));
                IOUtils.write(zos, new FileInputStream(file), true);
                zos.close();
            }
        }
    }

    /**
     * 对指定数据进行压缩
     * 
     * @param data
     *            要压缩的数据
     * @return 压缩后的数据
     */
    public static byte[] compress(byte[] data) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream, 1024)) {
            gzipOutputStream.write(data);
            gzipOutputStream.flush();
            gzipOutputStream.finish();
        } catch (IOException e) {
            // 这里不可能有IO异常
            throw new RuntimeException(e);
        }

        return outputStream.toByteArray();
    }

    /**
     * 解压缩
     *
     * @param data
     *            要解压缩的数据（需要是gzip算法压缩的）
     * @return 解压缩后的数据
     */
    public static byte[] decompress(byte[] data) {
        if (CollectionUtil.isEmpty(data)) {
            return new byte[0];
        }

        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream, 1024)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipInputStream.read(buffer, 0, buffer.length)) > 0) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            // 这里不可能会有IO异常
            throw new RuntimeException(e);
        }

        return outputStream.toByteArray();
    }

    /**
     * 解压指定文件
     *
     * @param zipFile
     *            zip文件
     * @param descDir
     *            解压目标目录
     */
    public static void unZip(File zipFile, String descDir) {
        File pathFile = new File(descDir);
        if (!pathFile.exists()) {
            FileUtils.mkdir(pathFile.getPath());
        }

        // 指定编码，否则压缩包里面不能有中文目录
        try (ZipFile zip = new ZipFile(zipFile, Const.DEFAULT_CHARSET)) {
            for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
                // zipEntry
                ZipEntry entry = entries.nextElement();
                // 输出文件
                File outFile = new File(descDir, entry.getName());

                // 先创建目录
                if (entry.isDirectory()) {
                    FileUtils.mkdir(outFile.getPath());
                    continue;
                } else {
                    FileUtils.mkdir(outFile.getParentFile().getPath());
                }

                try (InputStream in = zip.getInputStream(entry); OutputStream out = new FileOutputStream(outFile)) {
                    IOUtils.write(out, in);
                }
            }
        } catch (IOException e) {
            throw new CommonException(ErrorCodeEnum.IO_EXCEPTION, e);
        }
    }

}
