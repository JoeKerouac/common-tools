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

import org.apache.poi.ss.usermodel.Cell;

/**
 * excel单元格的DataWriter，用于往单元格写入数据，可以自己定制，例如要加入样式的时候可以自己定制
 *
 * @author JoeKerouac
 * @date 2022-10-17 19:09
 * @since 2.0.0
 */
public interface ExcelDataWriter<T> {
    /**
     * 将数据写入单元格
     *
     * @param cell
     *            单元格
     * @param data
     *            要写入的数据
     */
    void write(Cell cell, T data);

    /**
     * 数据类型是否可写
     *
     * @param type
     *            数据类型
     * @return 返回true表示可写
     */
    boolean writeable(Class<?> type);
}
