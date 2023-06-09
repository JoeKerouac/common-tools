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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * 
 * 内存文件，当数据小于指定阈值时，使用内存缓存，当数据大于指定阈值时，将文件写出到磁盘；
 *
 * 当数据写出完毕时允许以输入流的形式获取该数据；
 * 
 * 非线程安全，允许写入和读取在不同线程
 * 
 * @author JoeKerouac
 * @date 2023-06-08 15:19
 * @since 2.0.3
 */
public class InMemoryFile {

    /**
     * 当缓存超过该值时
     */
    private final int limit;

    private OutputStream outputStream;

    private volatile File file;

    private volatile byte[] buffer;

    private volatile int index;

    private volatile boolean close;

    /**
     * 数据长度
     */
    private volatile int len;

    private volatile Charset charset;

    public InMemoryFile(int initBuffer, int limit) {
        this.limit = limit;
        this.buffer = new byte[initBuffer];
        this.index = 0;
        this.close = false;
        this.len = 0;
    }

    public void write(byte data) throws IOException {
        write(new byte[] {data}, 0, 1);
    }

    public void write(byte[] data, int offset, int len) throws IOException {
        if (data == null) {
            throw new NullPointerException();
        } else if (offset < 0 || len < 0 || len > data.length - offset) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        } else if (close) {
            throw new IOException("文件已经关闭，无法写入");
        }

        Assert.assertTrue(this.len + len > 0, "当前累计写出数据过大，无法继续写入",
            ExceptionProviderConst.UnsupportedOperationExceptionProvider);

        int writeLen = Math.min(len, buffer.length - index);
        if (writeLen < len) {
            if (limit - buffer.length + writeLen >= len) {
                // 扩容后不超过limit
                byte[] newBuffer =
                    new byte[Math.max(Math.min(buffer.length * 3 / 2, limit), len - writeLen + buffer.length)];
                System.arraycopy(buffer, 0, newBuffer, 0, index);
                System.arraycopy(data, offset, newBuffer, index, len);
                buffer = newBuffer;
                index += len;
            } else {
                // 扩容后超过limit了，将数据填满，写出到文件，然后继续填充
                if (outputStream == null) {
                    file = File.createTempFile("WriteCache", ".tmp");
                    outputStream = new FileOutputStream(file);
                }
                outputStream.write(buffer, 0, index);
                outputStream.write(data, offset, len);
                index = 0;
            }
        } else {
            System.arraycopy(data, 0, buffer, index, len);
            index += len;
        }

        this.len += len;
    }

    /**
     * 将数据刷出，如果当前文件已经映射到磁盘则将剩余未写出数据写出到磁盘
     * 
     * @throws IOException
     *             IO异常
     */
    public void flush() throws IOException {
        if (index > 0 && outputStream != null) {
            outputStream.write(buffer, 0, index);
        }

        if (outputStream != null) {
            outputStream.flush();
        }
    }

    /**
     * 关闭输出，关闭输出后可以以输入流的形式获取数据
     * 
     * @throws IOException
     *             IO异常
     */
    public void writeFinish() throws IOException {
        flush();
        if (outputStream != null) {
            outputStream.close();
        }
        close = true;
    }

    /**
     * 判断当前数据是否全在内存
     * 
     * @return 如果当前数据全在内存则返回true
     */
    public boolean inMemory() {
        return file == null;
    }

    /**
     * 以输入流的形式获取数据
     * 
     * @return 数据输入流
     * @throws IOException
     *             IO异常
     */
    public InputStream getDataAsInputStream() throws IOException {
        if (!close) {
            throw new IOException("当前输出流还未关闭，无法获取输入流");
        }

        if (file == null) {
            return new ByteArrayInputStream(buffer, 0, index);
        } else {
            return Files.newInputStream(file.toPath(), StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE);
        }
    }

    /**
     * 以数组的形式获取数据
     * 
     * @return 数据
     * @throws IOException
     *             IO异常
     */
    public byte[] getData() throws IOException {
        if (file == null) {
            if (index == buffer.length) {
                return buffer;
            } else if (index == 0) {
                return new byte[0];
            } else {
                return Arrays.copyOf(buffer, index);
            }
        } else {
            InputStream inputStream =
                Files.newInputStream(file.toPath(), StandardOpenOption.READ, StandardOpenOption.DELETE_ON_CLOSE);
            Assert.assertTrue(file.length() <= Integer.MAX_VALUE, "文件过大，不支持读取",
                ExceptionProviderConst.UnsupportedOperationExceptionProvider);
            return IOUtils.read(inputStream, (int)file.length(), true);
        }
    }

    /**
     * 获取数据长度
     * 
     * @return 数据长度
     */
    public int getLen() {
        return len;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public Charset getCharset() {
        return charset;
    }

}
