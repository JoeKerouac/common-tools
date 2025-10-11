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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.github.joekerouac.common.tools.string.StringUtils;

import lombok.CustomLog;

/**
 * 提供下载限速的工具，如果我们下载时期望下载速度小于实际带宽，则可以使用这个工具限速
 * 
 * @author JoeKerouac
 * @date 2025-01-08 16:42:41
 * @since 1.0.0
 */
@CustomLog
public class ThrottleDownloader {

    private static final long KB = 1024;

    /**
     * 下载时的缓冲区大小
     */
    private static final int BUFFER_SIZE = (int)KB * 4;

    /**
     * 以指定速率将指定输入流输出到指定输出流
     *
     * @param inputStream
     *            输入流
     * @param limit
     *            限速大小，单位kb，例如1024表示限速1M/s，注意，不应该大于实际带宽大小，大于实际带宽大小时无意义
     * @throws IOException
     *             IOException
     * @throws InterruptedException
     *             InterruptedException
     */
    public static InMemoryFile download(InputStream inputStream, int limit) throws IOException, InterruptedException {
        InMemoryFile inMemoryFile = new InMemoryFile(BUFFER_SIZE, (int)(KB * KB));
        download(inputStream, new InMemoryFileOutputStream(inMemoryFile), limit, 100, 3);
        inMemoryFile.writeFinish();
        return inMemoryFile;
    }

    /**
     * 以指定速率将指定输入流输出到指定输出流
     *
     * @param inputStream
     *            输入流
     * @param outputStream
     *            输出流
     * @param limit
     *            限速大小，单位kb，例如1024表示限速1M/s，注意，不应该大于实际带宽大小，大于实际带宽大小时无意义
     * @throws IOException
     *             IOException
     * @throws InterruptedException
     *             InterruptedException
     */
    public static void download(InputStream inputStream, OutputStream outputStream, int limit)
        throws IOException, InterruptedException {
        download(inputStream, outputStream, limit, 100, 3);
    }

    /**
     * 以指定速率将指定输入流输出到指定输出流
     * 
     * @param inputStream
     *            输入流
     * @param outputStream
     *            输出流
     * @param limit
     *            限速大小，单位kb，例如1024表示限速1M/s，注意，不应该大于实际带宽大小，大于实际带宽大小时无意义
     * @param split
     *            时间片拆分，表示将1秒拆分为多少个时间片，分别计速，100表示10毫秒下载一次，然后计速，判断是否达标（达到期望限速），这
     *            里拆分时间片，是为了防止下载流量为一个个的尖刺，尽量平缓下载流量曲线，例如：如果带宽1000M，限速1M/s，理论上1毫秒就能
     *            下载1M，如果不拆分时间片，那么下载流量曲线将会是在每秒的第一毫秒达到1000M，下载1M内容，剩余的999毫秒空闲；
     * @param window
     *            速度变更窗口，单位秒，在该窗口内速度达到指定阈值后变更
     * @throws IOException
     *             IOException
     * @throws InterruptedException
     *             InterruptedException
     */
    public static void download(InputStream inputStream, OutputStream outputStream, int limit, int split, int window)
        throws IOException, InterruptedException {
        // 保证每秒最少读取BUFFER_SIZE大小的数据
        if (limit < (BUFFER_SIZE / KB)) {
            throw new IllegalArgumentException(
                StringUtils.format("限速不能低于{}K, current: [{}]", (BUFFER_SIZE / KB), limit));
        }

        // 保证每个时间分片最少读取BUFFER_SIZE大小的数据，因为1秒只有1000毫秒，所以这里时间分片最大1000
        int maxSplit = Math.min(limit * (int)KB / BUFFER_SIZE, 1000);

        if (split > maxSplit) {
            throw new IllegalArgumentException(StringUtils.format("时间片拆分不能大于{}, current: [{}]", maxSplit, split));
        }

        // 最小512K，最大100M
        int[][] windowArr = new int[window][2];
        // 连续降速次数
        int flag1 = 0;
        // 连续增速次数
        int flag2 = 0;
        long start = System.currentTimeMillis();

        int point = 0;
        int count = 0;
        long maxRead = limit * KB / split;
        while (download(inputStream, outputStream, maxRead)) {
            // true表示当前下载速度超过预设速度
            boolean flag = false;
            long use = System.currentTimeMillis() - start;
            start = System.currentTimeMillis();
            long sleep = Math.max(1000 / split - use, 0);
            if (sleep > 0) {
                flag = true;
                Thread.sleep(sleep);
            }

            start += sleep;
            count++;

            if (count > split) {
                int threshold = window * split * 60 / 100;

                // 速度不达标的次数
                int slow = 0;
                // 速度达标的次数
                int speed = 0;

                for (int i = 0; i < window; i++) {
                    int[] arr = windowArr[i];
                    slow += arr[1];
                    speed += arr[0];
                }

                // 速度达标/速度不达标在指定窗口周期内占比超过65%时变更速度
                if (slow > threshold && split > 1) {
                    int rate = Math.max(1, split * (flag1 + 1) / 10);
                    int newSplit = Math.max(split - rate, 1);
                    windowArr = new int[window][2];
                    LOGGER.debug("当前实际下载速率过慢，合并下载时间窗, [{} -> {}], threshold: [{}], slow: [{}]", split, newSplit,
                        threshold, slow);
                    split = newSplit;
                    flag1 += 1;
                    flag2 = 0;
                } else if (speed > threshold && split < maxSplit) {
                    int rate = Math.max(1, split * (flag2 + 1) / 10);
                    int newSplit = Math.min(split + rate, maxSplit);
                    windowArr = new int[window][2];
                    LOGGER.debug("当前实际下载速率较快，拆分下载时间窗, [{} -> {}], threshold: [{}], speed: [{}]", split, newSplit,
                        threshold, speed);
                    split = newSplit;
                    flag1 = 0;
                    flag2 += 1;
                }

                count = 1;
                point += 1;
                if (point >= windowArr.length) {
                    point = 0;
                }
            }

            int[] arr = windowArr[point];
            if (flag) {
                arr[0] = arr[0] + 1;
            } else {
                arr[1] = arr[1] + 1;
            }

            maxRead = limit * KB / split;
        }
    }

    /**
     * 从输入流读取指定字节数据写入到输出流
     * 
     * @param inputStream
     *            输入流
     * @param outputStream
     *            输出流
     * @param maxRead
     *            本次最大读取数据大小
     * @return 表示是否还有更多数据可以下载，true表示有
     * @throws IOException
     *             IOException
     */
    private static boolean download(InputStream inputStream, OutputStream outputStream, long maxRead)
        throws IOException {
        byte[] buffer = new byte[(int)Math.min(maxRead, BUFFER_SIZE)];
        int len;
        // true表示输入流可能还有数据可以读取，false表示没有数据
        boolean hasMore = false;
        int readLen = 0;

        while ((len = inputStream.read(buffer)) > 0) {
            readLen += len;
            outputStream.write(buffer, 0, len);

            if ((readLen + buffer.length) > maxRead) {
                hasMore = true;
                break;
            }
        }

        if (readLen < maxRead && hasMore) {
            buffer = new byte[(int)(maxRead - readLen)];
            len = inputStream.read(buffer);
            if (len > 0) {
                outputStream.write(buffer, 0, len);
            } else {
                hasMore = false;
            }
        } else if (readLen >= maxRead) {
            hasMore = true;
        }

        return hasMore;
    }

}
