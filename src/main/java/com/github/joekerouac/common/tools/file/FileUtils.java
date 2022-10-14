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
package com.github.joekerouac.common.tools.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.exception.CommonException;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.io.IOUtils;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 文件工具类
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {

    /**
     * 创建指定目录
     *
     * @param path
     *            目录名
     * @return 目录
     */
    public static String mkdir(String path) {
        File file = new File(path);
        if (!file.exists()) {
            Assert.assertTrue(file.mkdirs(), String.format("目录[%s]创建失败", file.getAbsolutePath()),
                ExceptionProviderConst.IllegalStateExceptionProvider);
        }
        return path;
    }

    /**
     * 校验文件的访问权限，如果没有权限访问则会抛出异常
     * 
     * @param file
     *            文件
     * @param mustDirectory
     *            该文件是否必须是目录，true表示是
     */
    public static void checkFileAccess(File file, boolean mustDirectory) {
        if (file.exists()) {
            if (mustDirectory && !file.isDirectory()) {
                throw new CommonException(ErrorCodeEnum.FILE_ACCESS_EXCEPTION,
                    String.format("路径[%s]不是一个目录而是一个文件", file.getAbsolutePath()));
            }

            if (!file.canRead() || !file.canWrite()) {
                throw new CommonException(ErrorCodeEnum.FILE_ACCESS_EXCEPTION,
                    String.format("对路径[%s]没有读写权限", file.getAbsolutePath()));
            }
        } else if (file.getParentFile() != null) {
            checkFileAccess(file.getParentFile(), mustDirectory);
        } else {
            throw new CommonException(ErrorCodeEnum.FILE_ACCESS_EXCEPTION,
                String.format("对路径[%s]没有读写权限，同时该文件也没有父文件了", file.getAbsolutePath()));
        }
    }

    /**
     * 删除指定文件/目录
     * 
     * @param file
     *            要删除的文件/目录，如果是目录，会递归删除里边的所有文件
     * @return 删除失败的文件
     */
    public static List<File> deleteFile(File file) {
        if (file == null || !file.exists()) {
            return Collections.emptyList();
        }

        if (file.isFile() || CollectionUtil.isEmpty(file.listFiles())) {
            return file.delete() ? Collections.emptyList() : Collections.singletonList(file);
        }

        File[] files = file.listFiles();
        files = files == null ? new File[0] : files;

        // 删除失败的文件集合
        List<File> failed = new ArrayList<>();
        // 循环删除子文件
        for (int i = 0; i < files.length; i++) {
            File nowFile = files[i];
            failed.addAll(deleteFile(nowFile));
        }

        if (!file.delete()) {
            failed.add(file);
        }
        return failed;
    }

    /**
     * 清空目录
     * 
     * @param file
     *            目录
     * @return 删除失败的文件
     */
    public static List<File> clearDir(File file) {
        Assert.argNotNull(file, "file");
        Assert.assertTrue(!file.exists() || file.isDirectory(), "要清空的目录如果存在必须是目录",
            ExceptionProviderConst.IllegalArgumentExceptionProvider);

        if (!file.exists()) {
            return Collections.emptyList();
        }

        List<File> failed = new ArrayList<>();
        File[] files = file.listFiles();
        if (!CollectionUtil.isEmpty(files)) {
            for (File f : files) {
                failed.addAll(deleteFile(f));
            }
        }

        return failed;
    }

    /**
     * 遍历文件夹
     * 
     * @param file
     *            文件夹
     * @return 文件夹中所有文件（不包含本文件夹）
     */
    public static List<File> listFiles(File file) {
        if (!file.exists() || file.isFile()) {
            return new ArrayList<>(0);
        }

        File[] files = file.listFiles();

        if (files == null || files.length == 0) {
            return new ArrayList<>(0);
        }

        List<File> list = new ArrayList<>();

        for (File nowFile : files) {
            // 将子文件（可能是文件，也可能是目录）添加进来
            list.add(nowFile);
            // 如果子文件是目录，则继续遍历
            if (nowFile.isDirectory()) {
                // 如果是目录，继续循环放入该目录中的所有文件
                list.addAll(listFiles(nowFile));
            }
        }

        return list;
    }

    /**
     * 将源文件/源文件夹中所有内容（不包含源文件夹）移动到目标文件夹中
     *
     * @param src
     *            源文件/文件夹
     * @param dstPath
     *            目标路径
     * @param dstName
     *            目标文件名（允许为空），移动文件夹时不使用该参数，移动文件时使用该参数作为目标文件名，如果为空则使用源文件名
     * @param msg
     *            错误消息
     */
    public static void move(String src, String dstPath, String dstName, String msg) {
        moveOrCopy(src, dstPath, dstName, msg, false);
    }

    /**
     * 将源文件/源文件夹中所有内容移动到目标文件夹中
     *
     * @param src
     *            如果src指向的是文件，会将文件移动到指定目录，如果src指向的是目录，会递归将该目录中的所有文件移动到指定目录中
     * @param dstPath
     *            目标文件夹（必须是文件夹）
     * @param msg
     *            失败时抛出异常的msg
     */
    public static void copy(String src, String dstPath, String msg) {
        copy(src, dstPath, null, msg);
    }

    /**
     * 将源文件/源文件夹中所有内容移动到目标文件夹中
     *
     * @param src
     *            如果src指向的是文件，会将文件移动到指定目录，如果src指向的是目录，会递归将该目录中的所有文件移动到指定目录中
     * @param dstPath
     *            目标文件夹（必须是文件夹）
     * @param dstName
     *            目标文件名，允许为空
     * @param msg
     *            失败时抛出异常的msg
     */
    public static void copy(String src, String dstPath, String dstName, String msg) {
        moveOrCopy(src, dstPath, dstName, msg, true);
    }

    /**
     * 将源文件/源文件夹中所有内容（不包含源文件夹）移动到目标文件夹中
     *
     * @param src
     *            如果src指向的是文件，会将文件移动到指定目录，如果src指向的是目录，会递归将该目录中的所有文件移动到指定目录中
     * @param dstPath
     *            目标文件夹（必须是文件夹）
     * @param dstName
     *            目标文件名，允许为空，为空时使用源文件名
     * @param msg
     *            失败时抛出异常的msg
     * @param copy
     *            是否是copy，true表示copy，false表示move
     */
    private static void moveOrCopy(String src, String dstPath, String dstName, String msg, boolean copy) {
        File srcDir = new File(src);
        // 源文件夹不存在的时候
        if (!srcDir.exists()) {
            return;
        }

        File dstDir = new File(dstPath);
        if (!dstDir.exists()) {
            Assert.assertTrue(dstDir.mkdirs(), msg, ExceptionProviderConst.IllegalStateExceptionProvider);
        } else if (dstDir.isFile()) {
            throw new CommonException(ErrorCodeEnum.CODE_ERROR,
                StringUtils.format("要将[{}]移动到[{}]，但是目标[{}]是一个文件而不是目录", src, dstPath, dstPath));
        }

        if (srcDir.isFile()) {
            moveOrCopyFile(src, dstPath, dstName, msg, copy);
            return;
        }

        List<File> list = FileUtils.listFiles(srcDir);
        list.forEach(file -> {
            // 获取文件相对于srcDir的相对路径
            String relativePath = file.getAbsolutePath().substring(srcDir.getAbsolutePath().length());
            File newFile = new File(dstDir.getAbsolutePath() + relativePath);

            // 如果是文件夹，就创建文件夹，如果是文件，就移动到指定目录
            if (file.isDirectory()) {
                if (!newFile.exists()) {
                    Assert.assertTrue(newFile.mkdirs(), msg, ExceptionProviderConst.IllegalStateExceptionProvider);
                }
            } else {
                moveOrCopyFile(file.getAbsolutePath(), newFile.getParentFile().getAbsolutePath(), null, msg, copy);
            }
        });
    }

    /**
     * 将源文件移动或复制到目标文件夹中
     *
     * @param src
     *            源文件，必须是单个文件
     * @param dstPath
     *            目标文件夹（外部必须保证是文件夹，该方法会无脑将该路径作为文件夹使用）
     * @param dstName
     *            目标文件名，允许为空，为空时使用源文件名
     * @param msg
     *            失败时抛出异常的msg
     * @param copy
     *            是否是copy，true表示copy，false表示move
     */
    private static void moveOrCopyFile(String src, String dstPath, String dstName, String msg, boolean copy) {
        File dstDir = new File(dstPath);
        if (!dstDir.exists()) {
            Assert.assertTrue(dstDir.mkdirs(), msg, ExceptionProviderConst.IllegalStateExceptionProvider);
        }

        File srcFile = new File(src);

        Assert.assertTrue(srcFile.isFile(), "只能处理文件，不能复制目录，当前路径：" + src,
            ExceptionProviderConst.IllegalStateExceptionProvider);

        try (FileOutputStream out =
            new FileOutputStream(new File(dstDir, StringUtils.getOrDefault(dstName, srcFile.getName())))) {
            IOUtils.write(out, new FileInputStream(src), true);
        } catch (IOException e) {
            throw new CommonException(ErrorCodeEnum.IO_EXCEPTION,
                StringUtils.format("文件[{}]复制到目录[{}]的过程中发生异常", src, dstPath), e);
        }

        if (!copy) {
            Assert.assertTrue(srcFile.delete(), String.format("文件[%s]删除失败", src),
                ExceptionProviderConst.IllegalStateExceptionProvider);
        }
    }

}
