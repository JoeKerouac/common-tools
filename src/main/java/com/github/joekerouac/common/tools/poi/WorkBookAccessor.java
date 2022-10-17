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
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * @author JoeKerouac
 * @date 2022-10-17 19:09
 * @since 2.0.0
 */
public class WorkBookAccessor {

    private final Workbook workbook;

    /**
     * 当前工作sheet
     */
    private volatile int sheetIndex;

    public WorkBookAccessor(Workbook workbook) {
        this(workbook, 0);
    }

    public WorkBookAccessor(Workbook workbook, int sheetIndex) {
        this.workbook = workbook;
        this.sheetIndex = sheetIndex;
    }

    /**
     * 更改当前使用的sheet
     *
     * @param sheetIndex
     *            sheet-index
     */
    public void useSheet(int sheetIndex) {
        this.sheetIndex = sheetIndex;
    }

    /**
     * 获取指定sheet的指定行列单元格
     *
     * @param column
     *            单元格所在列
     * @param row
     *            单元格所在行
     * @return 指定行列的单元格
     */
    public Cell getCell(int column, int row) {
        return this.workbook.getSheetAt(sheetIndex).getRow(row).getCell(column);
    }

    /**
     * 获取指定单元格的Style访问器
     *
     * @param column
     *            单元格所在列
     * @param row
     *            单元格所在行
     * @return 指定行列的单元格的style访问器
     */
    public CellStyleAccessor getCellStyleAccessor(int column, int row) {
        return CellStyleAccessor.build(getCell(column, row));
    }

    /**
     * 将同一行的多列合并
     *
     * @param row
     *            指定行
     * @param firstCol
     *            要合并的第一列
     * @param lastCol
     *            要合并的最后一列
     */
    public void mergedRowRegion(int row, int firstCol, int lastCol) {
        mergedRegion(row, row, firstCol, lastCol);
    }

    /**
     * 将同一列的多行合并
     *
     * @param column
     *            指定列
     * @param firstRow
     *            要合并的第一列
     * @param lastRow
     *            要合并的最后一列
     */
    public void mergedColumnRegion(int column, int firstRow, int lastRow) {
        mergedRegion(firstRow, lastRow, column, column);
    }

    /**
     * 合并指定sheet指定区域的单元格
     *
     * @param firstRow
     *            要合并的第一行
     * @param lastRow
     *            要合并的最后一行
     * @param firstCol
     *            要合并的第一列
     * @param lastCol
     *            要合并的最后一列
     */
    public void mergedRegion(int firstRow, int lastRow, int firstCol, int lastCol) {
        mergedRegion(workbook.getSheetAt(sheetIndex), firstRow, lastRow, firstCol, lastCol);
    }

    /**
     * 合并指定sheet指定区域的单元格
     *
     * @param sheetName
     *            sheet名字
     * @param firstRow
     *            要合并的第一行
     * @param lastRow
     *            要合并的最后一行
     * @param firstCol
     *            要合并的第一列
     * @param lastCol
     *            要合并的最后一列
     */
    public void mergedRegion(String sheetName, int firstRow, int lastRow, int firstCol, int lastCol) {
        mergedRegion(workbook.getSheet(sheetName), firstRow, lastRow, firstCol, lastCol);
    }

    /**
     * 合并指定sheet指定区域的单元格
     *
     * @param sheet
     *            sheet
     * @param firstRow
     *            要合并的第一行
     * @param lastRow
     *            要合并的最后一行
     * @param firstCol
     *            要合并的第一列
     * @param lastCol
     *            要合并的最后一列
     */
    private void mergedRegion(Sheet sheet, int firstRow, int lastRow, int firstCol, int lastCol) {
        sheet.addMergedRegion(new CellRangeAddress(firstRow, lastRow, firstCol, lastCol));
    }
}
