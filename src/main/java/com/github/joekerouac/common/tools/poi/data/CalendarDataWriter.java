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
package com.github.joekerouac.common.tools.poi.data;

import java.util.Calendar;

import org.apache.poi.ss.usermodel.Cell;

import com.github.joekerouac.common.tools.poi.ExcelDataWriter;

/**
 * @author JoeKerouac
 * @date 2022-10-17 19:09
 * @since 2.0.0
 */
public final class CalendarDataWriter implements ExcelDataWriter<Calendar> {

    @Override
    public void write(Cell cell, Calendar data) {
        cell.setCellValue(data);
    }

    @Override
    public boolean writeable(Class<?> type) {
        if (type != null && type.equals(Calendar.class)) {
            return true;
        }
        return false;
    }

}
