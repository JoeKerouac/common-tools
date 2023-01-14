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
package com.github.joekerouac.common.tools.poi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.joekerouac.common.tools.poi.data.StringDataWriter;

import lombok.Data;

/**
 * @author joe
 * @version 2018.06.15 14:53
 */
public class ExcelWriterTest {

    ExcelWriter<User> writer;
    List<User> list = new ArrayList<>();

    @Before
    public void init() {
        User user = new User();
        user.setAge(1);
        user.setName("JoeKerouac");
        user.setSex("男");
        list.add(user);
        writer = new ExcelWriter<>();
    }

    @Test
    public void doWriteStream() {
        // 测试写入流
        fileTest(file -> {
            try {
                FileOutputStream outputStream = new FileOutputStream(file);
                writer.writeToExcel(list, true);
                writer.write(outputStream);
            } catch (IOException e) {
                Assert.assertNull("发生IO异常", e);
            }
        });
    }

    @Test
    public void doWriteFile() {
        // 测试写入文件
        fileTest(file -> {
            try {
                writer.writeToExcel(list, true);
                writer.write(new FileOutputStream(file));
            } catch (IOException e) {
                Assert.assertNotNull("发生IO异常", e);
            }
        });
    }

    @Test
    public void doDataWriter() {
        fileTest(file -> {
            try {
                // 使用自定义的StringDataWriter替换系统默认的StringDataWriter
                writer.registerDataWriter(String.class, new StringDataWriter() {
                    @Override
                    public void write(Cell cell, String data) {
                        // 将第一行加粗设置字号为20并且颜色设置为红色
                        if (cell.getRowIndex() == 0) {
                            CellStyleAccessor accessor = CellStyleAccessor.build(cell);
                            accessor.bold(true).setFontSize((short)20).color(HSSFColor.HSSFColorPredefined.RED);
                        }
                        cell.setCellValue(data);
                    }
                });
                writer.writeToExcel(list, true);
                writer.write(new FileOutputStream(file));
            } catch (IOException e) {
                Assert.assertNotNull("发生IO异常", e);
            }
        });
    }

    private void fileTest(Consumer<File> function) {
        File file = new File("user-" + Math.random() + ".xlsx");
        try {
            Assert.assertFalse(file.exists());

            function.accept(file);

            Assert.assertTrue(file.exists());
        } finally {
            file.delete();
        }
    }

    @Data
    static class User extends People {
        @ExcelColumn("姓名")
        private String name;
        @ExcelColumn("年龄")
        private int age;
    }

    @Data
    static class People {
        @ExcelColumn("性别")
        private String sex;
    }
}
