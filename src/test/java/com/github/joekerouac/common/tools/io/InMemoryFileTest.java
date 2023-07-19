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

import java.io.File;
import java.io.InputStream;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.reflect.ReflectUtil;

/**
 * @author JoeKerouac
 * @date 2023-07-19 13:33
 * @since 1.0.0
 */
public class InMemoryFileTest {

    @Test
    public void test1() throws Throwable {
        InMemoryFile inMemoryFile = new InMemoryFile(1, 1);
        inMemoryFile.write(new byte[2]);
        inMemoryFile.writeFinish();
        File tempFile = ReflectUtil.getFieldValue(inMemoryFile, "file");
        String tempFilePath = tempFile.getAbsolutePath();
        tempFile = null;
        InputStream dataAsInputStream = inMemoryFile.getDataAsInputStream();
        // 释放外部显式InMemoryFile的引用
        inMemoryFile = null;
        System.gc();
        Thread.sleep(100);
        // 此时InputStream中还包含InMemoryFile的引用，所以临时文件不会被释放
        Assert.assertTrue(new File(tempFilePath).exists());
        // close的时候也会释放InMemoryFile的引用
        dataAsInputStream.close();

        System.gc();
        Thread.sleep(100);
        // 由于InputStream也关闭了，InMemoryFile的引用彻底没了，临时文件此时也会被删除
        Assert.assertFalse(new File(tempFilePath).exists());
    }

    @Test
    public void test2() throws Throwable {
        InMemoryFile inMemoryFile = new InMemoryFile(1, 1);
        inMemoryFile.write(new byte[2]);
        inMemoryFile.writeFinish();
        File tempFile = ReflectUtil.getFieldValue(inMemoryFile, "file");
        String tempFilePath = tempFile.getAbsolutePath();
        tempFile = null;

        Assert.assertTrue(new File(tempFilePath).exists());
        // 主动关闭，释放临时文件
        inMemoryFile.close();
        Assert.assertFalse(new File(tempFilePath).exists());
    }

}
