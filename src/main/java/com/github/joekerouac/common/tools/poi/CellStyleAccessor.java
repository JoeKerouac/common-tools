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

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * 单元格样式访问器
 *
 * @author JoeKerouac
 * @date 2022-10-17 19:09
 * @since 2.0.0
 */
public final class CellStyleAccessor {

    private Cell cell;

    private Workbook workbook;

    private CellStyle style;

    private Font font;

    private CellStyleAccessor(Cell cell) {
        this.cell = cell;
        workbook = cell.getSheet().getWorkbook();
        style = cell.getCellStyle();
        // 如果原来有style就用默认的
        if (style.getIndex() == 0) {
            // 原来没有style，创建一个
            style = workbook.createCellStyle();
        }
        cell.setCellStyle(style);
        // 原来有font就用原来的
        if (style.getFontIndex() == 0) {
            // 原来没有设置font
            font = workbook.createFont();
        } else {
            // 设置过font
            font = workbook.getFontAt(style.getFontIndex());
        }
        style.setFont(font);
    }

    /**
     * 构建单元格样式访问器，用来修改单元格样式
     *
     * @param cell
     *            单元格
     * @return CellStyleAccessor
     */
    public static CellStyleAccessor build(Cell cell) {
        return new CellStyleAccessor(cell);
    }

    /**
     * 设置字号
     *
     * @param size
     *            字号
     * @return CellStyleAccessor
     */
    public CellStyleAccessor setFontSize(short size) {
        font.setFontHeightInPoints(size);
        return this;
    }

    /**
     * 设置粗体
     *
     * @param bold
     *            是否设置粗体，true表示设置粗体
     * @return CellStyleAccessor
     */
    public CellStyleAccessor bold(boolean bold) {
        font.setBold(bold);
        return this;
    }

    /**
     * 设置单元格字体颜色
     *
     * @param color
     *            颜色
     * @return CellStyleAccessor
     */
    public CellStyleAccessor color(HSSFColor.HSSFColorPredefined color) {
        font.setColor(color.getIndex());
        return this;
    }
}
