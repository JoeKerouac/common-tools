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

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import com.github.joekerouac.common.tools.enums.ErrorCodeEnum;
import com.github.joekerouac.common.tools.exception.CommonException;
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
        return read(inputStream, 4096, close);
    }

    /**
     * 将输入流剩余内容全部读取到byte数组，IO异常会被捕获，抛出一个PluginException，cause by是对应的IO异常
     *
     * @param inputStream
     *            输入流
     * @param bufferSize
     *            buffer大小
     * @param close
     *            是否关闭，true表示读取完毕关闭流
     * @return 输入流剩余的内容
     */
    public static byte[] read(InputStream inputStream, int bufferSize, boolean close) {
        byte[] buffer = new byte[bufferSize];
        int len;
        int index = 0;
        boolean first = true;
        try {
            while ((len = inputStream.read(buffer, index, buffer.length - index)) > 0) {
                index += len;
                // buffer已经写满
                if (index == buffer.length) {
                    int nextData = -1;

                    if (first) {
                        // 如果是第一次，则判断是否还有数据；PS: 这里这么判断的原因是因为外部可能精准的知道流中有多少数据，所以传入的buffer大小刚好，我们这里判断下就可以避免不必要的扩容
                        nextData = inputStream.read();
                        if (nextData < 0) {
                            // 没有更多数据了，直接break出去
                            break;
                        }

                        first = false;
                    }

                    // 不是第一次或者还有更多数据，我们扩容
                    byte[] newBuffer = new byte[(int)Math.min(buffer.length * 3L / 2, Integer.MAX_VALUE)];
                    System.arraycopy(buffer, 0, newBuffer, 0, index);
                    buffer = newBuffer;

                    if (nextData >= 0) {
                        buffer[index++] = (byte)nextData;
                    }
                }
            }

            if (index == buffer.length) {
                return buffer;
            } else if (index == 0) {
                return new byte[0];
            } else {
                return Arrays.copyOf(buffer, index);
            }
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
        byte[] buffer = new byte[4096];
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

    /**
     * 将输入流copy到memory file，如果输入流超过传入limit大小，将会写入本地临时文件
     *
     * @param stream
     *            输入流
     * @param bufferSize
     *            buffer size
     * @param limit
     *            内存中保留的最大buffer
     * @param streamFilter
     *            stream filter
     * @return 新的输入流
     * @throws IOException
     *             IO异常
     */
    public static InputStream copy(InputStream stream, int bufferSize, int limit, StreamFilter streamFilter)
        throws IOException {
        if (stream == null) {
            throw new NullPointerException("stream或者buffer不能为null");
        } else if (bufferSize <= 0 || limit <= 0 || limit < bufferSize) {
            throw new IllegalArgumentException("bufferSize、limit不能小于0，bufferSize不能大于limit");
        }

        InMemoryFile memoryFile = new InMemoryFile(bufferSize, limit, streamFilter);

        int len;
        byte[] buffer = new byte[bufferSize];

        while ((len = stream.read(buffer, 0, buffer.length)) > 0) {
            memoryFile.write(buffer, 0, len);
        }

        memoryFile.writeFinish();
        return memoryFile.getDataAsInputStream();
    }
}
