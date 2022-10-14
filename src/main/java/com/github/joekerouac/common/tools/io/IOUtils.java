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
package com.github.joekerouac.common.tools.io;

import java.io.*;

import com.github.joekerouac.common.tools.exception.CommonException;
import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * IO工具
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IOUtils {

    /**
     * 将输入流剩余内容全部读取到byte数组，IO异常会被捕获，抛出一个PluginException，cause by是对应的IO异常
     * 
     * @param inputStream
     *            输入流，读取完毕会将流关闭
     * @return 输入流剩余的内容
     */
    public static byte[] read(InputStream inputStream) {
        return read(inputStream, true);
    }

    /**
     * 将输入流剩余内容全部读取到byte数组，IO异常会被捕获，抛出一个PluginException，cause by是对应的IO异常
     * 
     * @param inputStream
     *            输入流
     * @param close
     *            是否关闭，true表示读取完毕关闭流
     * @return 输入流剩余的内容
     */
    public static byte[] read(InputStream inputStream, boolean close) {
        byte[] buffer = new byte[2048];
        int len;

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }

            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new CommonException(ErrorCodeEnum.IO_EXCEPTION, e);
        } finally {
            if (close) {
                close(inputStream);
            }
        }
    }

    /**
     * 将数据写入到输出流中
     * 
     * @param outputStream
     *            输出流
     * @param data
     *            要写出的数据，不能为null
     */
    public static void write(OutputStream outputStream, byte[] data) {
        write(outputStream, data, 0, data.length);
    }

    /**
     * 将数据写入到输出流中
     * 
     * @param outputStream
     *            输出流
     * @param data
     *            要写出的数据，不能为null
     * @param offset
     *            要写出的数据的起始位置
     * @param len
     *            要写出数据的长度
     */
    public static void write(OutputStream outputStream, byte[] data, int offset, int len) {
        try {
            outputStream.write(data, offset, len);
            outputStream.flush();
        } catch (IOException e) {
            throw new CommonException(ErrorCodeEnum.IO_EXCEPTION, e);
        }
    }

    /**
     * 将输入流中的内容写入到输出流
     * 
     * @param inputStream
     *            输入流
     * @param outputStream
     *            输出流
     */
    public static void write(OutputStream outputStream, InputStream inputStream) {
        write(outputStream, inputStream, false);
    }

    /**
     * 将输入流中的内容写入到输出流
     * 
     * @param inputStream
     *            输入流
     * @param outputStream
     *            输出流
     * @param close
     *            是否关闭输入流，true表示读取完毕关闭输入流，注意，这个关闭的是输入流，不是输出流
     */
    public static void write(OutputStream outputStream, InputStream inputStream, boolean close) {
        byte[] buffer = new byte[2048];
        int len;
        try {
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
            outputStream.flush();
        } catch (IOException e) {
            throw new CommonException(ErrorCodeEnum.IO_EXCEPTION, e);
        } finally {
            if (close) {
                close(inputStream);
            }
        }
    }

    /**
     * 关闭指定资源
     * 
     * @param closeable
     *            要关闭的资源
     */
    public static void close(Closeable closeable) {
        Assert.argNotNull(closeable, "closeable");
        try {
            closeable.close();
        } catch (IOException e) {
            throw new CommonException(ErrorCodeEnum.IO_EXCEPTION, e);
        }
    }
}
