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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.poi.data.*;
import com.github.joekerouac.common.tools.reflect.ReflectUtil;
import com.github.joekerouac.common.tools.string.StringUtils;

import lombok.AllArgsConstructor;
import lombok.CustomLog;

/**
 * Excel执行器，将数据写入excel，用户可以注册自己的excel单元格数据类型处理器{@link ExcelDataWriter ExcelDataWriter}来做
 * 一些特殊处理，例如添加单元格样式等，系统默认会注册Date、Calendar、String、Number、Boolean、Enum类型的数据处理器。
 *
 * @author JoeKerouac
 * @date 2022-10-17 19:09
 * @since 2.0.0
 * @see ExcelWriter
 */
@Deprecated
@CustomLog
public final class ExcelExecutor {

    /**
     * null数据写出函数
     */
    private static final Writer<?> NULL_WRITER = new Writer<>(new StringDataWriter(), "");

    /**
     * 默认内存中最多多少行单元格
     */
    private static final int IN_MEMORY = 100;

    /**
     * 排序器
     */
    private static final Comparator<Field> COMPARATOR;

    /**
     * 默认实例
     */
    private static final ExcelExecutor UTILS = new ExcelExecutor();

    static {
        COMPARATOR = (f1, f2) -> {
            ExcelColumn c1 = f1.getAnnotation(ExcelColumn.class);
            ExcelColumn c2 = f2.getAnnotation(ExcelColumn.class);
            if (c1 == null && c2 == null) {
                return f1.getName().compareTo(f2.getName());
            }

            if (c1 == null) {
                return 1;
            }

            if (c2 == null) {
                return -1;
            }
            return c1.sort() - c2.sort();
        };
    }

    /**
     * 所有的Excel单元格数据类型
     */
    private final Map<Class<?>, ExcelDataWriter<?>> writers = new HashMap<>();

    private ExcelExecutor() {
        init();
    }

    /**
     * 初始化，注册默认的DataWriter
     */
    private void init() {
        writers.put(Boolean.class, new BooleanDataWriter());
        writers.put(Calendar.class, new CalendarDataWriter());
        writers.put(Date.class, new DateDataWriter());
        writers.put(Enum.class, new EnumDataWriter());
        writers.put(Number.class, new NumberDataWriter());
        writers.put(String.class, new StringDataWriter());
    }

    /**
     * 获取POIUtils的实例
     *
     * @return POIUtils实例，多次获取的一样
     */
    public static ExcelExecutor getInstance() {
        return UTILS;
    }

    /**
     * 构建新的POIUtils
     *
     * @return 新的POIUtils，每次返回的实例都不一样
     */
    public static ExcelExecutor buildInstance() {
        return new ExcelExecutor();
    }

    /**
     * 注册一个新的excel单元格DataWriter（如果原来存在那么将会覆盖原来的DataWriter）
     *
     * @param type
     *            DataWriter处理的数据类型的Class
     * @param writer
     *            DataWriter
     * @param <T>
     *            DataWriter处理的数据类型
     * @return 如果原来存在该类型的DataWriter那么返回原来的DataWriter
     */
    public <T> ExcelDataWriter<?> registerDataWriter(Class<T> type, ExcelDataWriter<T> writer) {
        if (writer != null) {
            return writers.put(type, writer);
        } else {
            return null;
        }
    }

    /**
     * 当前系统是否包含指定类型的DataWriter
     *
     * @param type
     *            DataWriter对应的数据类型的Class
     * @param <T>
     *            DataWriter对应的数据类型
     * @return 返回true表示包含
     */
    public <T> boolean containsDataWriter(Class<T> type) {
        return writers.containsKey(type);
    }

    /**
     * 将pojo写入本地excel
     *
     * @param datas
     *            pojo数据
     * @param hasTitle
     *            是否需要标题
     * @param path
     *            excel本地路径
     * @throws IOException
     *             IO异常
     */
    public void writeToExcel(List<? extends Object> datas, boolean hasTitle, String path) throws IOException {
        writeToExcel(datas, hasTitle, path, false);
    }

    /**
     * 将pojo写入本地excel
     *
     * @param datas
     *            pojo数据
     * @param hasTitle
     *            是否需要标题
     * @param path
     *            excel本地路径
     * @param transverse
     *            是否横向写入（一列对应一个pojo，标题在第一列），默认false（一行一个pojo，标题在第一行）
     * @throws IOException
     *             IO异常
     */
    public void writeToExcel(List<? extends Object> datas, boolean hasTitle, String path, boolean transverse)
        throws IOException {
        writeToExcel(datas, hasTitle, path, IN_MEMORY, transverse);
    }

    /**
     * 将pojo写入本地excel
     *
     * @param datas
     *            pojo数据
     * @param hasTitle
     *            是否需要标题
     * @param path
     *            excel本地路径
     * @param inMemory
     *            最多保留在内存中多少行
     * @throws IOException
     *             IO异常
     */
    public void writeToExcel(List<? extends Object> datas, boolean hasTitle, String path, int inMemory)
        throws IOException {
        writeToExcel(datas, hasTitle, path, inMemory, false);
    }

    /**
     * 将pojo写入本地excel
     *
     * @param datas
     *            pojo数据
     * @param hasTitle
     *            是否需要标题
     * @param path
     *            excel本地路径
     * @param inMemory
     *            最多保留在内存中多少行
     * @param transverse
     *            是否横向写入（一列对应一个pojo，标题在第一列），默认false（一行一个pojo，标题在第一行）
     * @throws IOException
     *             IO异常
     */
    public void writeToExcel(List<? extends Object> datas, boolean hasTitle, String path, int inMemory,
        boolean transverse) throws IOException {
        writeToExcel(datas, hasTitle, new FileOutputStream(path), inMemory, transverse);
    }

    /**
     * 将数据写入excel
     *
     * @param datas
     *            要写入excel的pojo数据
     * @param hasTitle
     *            是否需要标题
     * @param outputStream
     *            输出流（该流不会关闭，需要用户手动关闭）
     * @throws IOException
     *             IO异常
     */
    public void writeToExcel(List<? extends Object> datas, boolean hasTitle, OutputStream outputStream)
        throws IOException {
        writeToExcel(datas, hasTitle, outputStream, false);
    }

    /**
     * 将数据写入excel
     *
     * @param datas
     *            要写入excel的pojo数据
     * @param hasTitle
     *            是否需要标题
     * @param outputStream
     *            输出流（该流不会关闭，需要用户手动关闭）
     * @param transverse
     *            是否横向写入（一列对应一个pojo，标题在第一列），默认false（一行一个pojo，标题在第一行）
     * @throws IOException
     *             IO异常
     */
    public void writeToExcel(List<? extends Object> datas, boolean hasTitle, OutputStream outputStream,
        boolean transverse) throws IOException {
        writeToExcel(datas, hasTitle, outputStream, IN_MEMORY, transverse);
    }

    /**
     * 将数据写入excel
     *
     * @param datas
     *            要写入excel的pojo数据
     * @param hasTitle
     *            是否需要标题
     * @param outputStream
     *            输出流（该流不会关闭，需要用户手动关闭）
     * @param inMemory
     *            最多保留在内存中多少行
     * @throws IOException
     *             IO异常
     */
    public void writeToExcel(List<? extends Object> datas, boolean hasTitle, OutputStream outputStream, int inMemory)
        throws IOException {
        writeToExcel(datas, hasTitle, outputStream, inMemory, false);
    }

    /**
     * 将数据写入excel
     *
     * @param datas
     *            要写入excel的pojo数据
     * @param hasTitle
     *            是否需要标题
     * @param outputStream
     *            输出流（该流不会关闭，需要用户手动关闭）
     * @param inMemory
     *            最多保留在内存中多少行
     * @param transverse
     *            是否横向写入（一列对应一个pojo，标题在第一列），默认false（一行一个pojo，标题在第一行）
     * @throws IOException
     *             IO异常
     */
    public void writeToExcel(List<? extends Object> datas, boolean hasTitle, OutputStream outputStream, int inMemory,
        boolean transverse) throws IOException {
        LOGGER.debug("准备将数据写入excel");
        // 这里使用SXSSFWorkbook而不是XSSFWorkbook，这样将会节省内存，但是内存中仅仅存在inMemory行数据，如果超出那么会将
        // index最小的刷新到本地，后续不能通过getRow方法获取到该行
        SXSSFWorkbook wb = new SXSSFWorkbook(inMemory);
        writeToExcel(datas, hasTitle, wb, transverse);
        LOGGER.debug("数据写入excel完毕，准备写入本地文件");
        wb.write(outputStream);
        LOGGER.debug("删除临时文件，关闭Workbook");
        wb.dispose();
        wb.close();
    }

    /**
     * 将pojo集合写入excel
     *
     * @param datas
     *            pojo集合，空元素将被忽略
     * @param hasTitle
     *            是否需要title
     * @param workbook
     *            工作簿
     * @return 写入后的工作簿
     */
    public Workbook writeToExcel(List<? extends Object> datas, boolean hasTitle, Workbook workbook) {
        return writeToExcel(datas, hasTitle, workbook, false);
    }

    /**
     * 将pojo集合写入excel（处理数据，不写入）
     *
     * @param dataList
     *            pojo集合，空元素将被忽略，集合中必须是都是同种对象
     * @param hasTitle
     *            是否需要title
     * @param workbook
     *            工作簿
     * @param transverse
     *            是否横向写入（一列对应一个pojo，标题在第一列），默认false（一行一个pojo，标题在第一行）
     * @return 写入后的工作簿
     */
    public Workbook writeToExcel(List<? extends Object> dataList, boolean hasTitle, Workbook workbook,
        boolean transverse) {
        if (CollectionUtil.isEmpty(dataList)) {
            LOGGER.warn("给定数据集合为空");
            return workbook;
        }
        List<? extends Object> datas = dataList.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());
        if (datas.isEmpty()) {
            LOGGER.warn("给定数据集合里的数据全是空");
            return workbook;
        }
        // 获取所有字段（包括父类的）
        Field[] fields = ReflectUtil.getAllFields(datas.get(0).getClass());

        // 过滤可以写入的字段
        List<Field> writeFields = new ArrayList<>();
        for (Field field : fields) {
            String name = field.getName();
            Class<?> type = field.getType();

            // 查找该字段类型的数据处理器
            List<ExcelDataWriter<?>> data =
                writers.values().stream().filter(excelData -> excelData.writeable(type)).collect(Collectors.toList());
            if (data.isEmpty()) {
                LOGGER.debug("字段[{}]不能写入", name);
            } else {
                ExcelColumn column = field.getAnnotation(ExcelColumn.class);

                if (column == null || !column.ignore()) {
                    writeFields.add(field);
                }
            }
        }

        LOGGER.debug("可写入excel的字段集合为：[{}]，对可写入excel的字段集合排序", writeFields);
        writeFields.sort(COMPARATOR);

        List<Writer<?>> titles = null;
        if (hasTitle) {
            LOGGER.info("当前需要标题列表，构建...");
            titles = new ArrayList<>(writeFields.size());
            for (Field field : writeFields) {
                ExcelColumn column = field.getAnnotation(ExcelColumn.class);
                if (column == null || StringUtils.isBlank(column.value())) {
                    titles.add(build(field.getName()));
                } else {
                    titles.add(build(column.value()));
                }
            }
        }

        List<List<Writer<?>>> writeDatas = new ArrayList<>(datas.size());
        for (Object dataValue : datas) {
            // 构建一行数据
            List<Writer<?>> columnDatas = new ArrayList<>(writeFields.size());
            // 加入
            writeDatas.add(columnDatas);
            for (Field field : writeFields) {
                try {
                    Object value = field.get(dataValue);
                    columnDatas.add(build(value));
                } catch (IllegalAccessException e) {
                    LOGGER.warn("[{}]中字段[{}]不能读取", dataValue, field.getName(), e);
                    columnDatas.add(null);
                }
            }
        }

        LOGGER.debug("要写入的数据为：[{}]", writeDatas);
        writeToExcel(titles, writeDatas, hasTitle, workbook, transverse);
        return workbook;
    }

    /**
     * 写入excel（实际处理方法，在该方法中数据将会被写入excel）
     *
     * @param titles
     *            标题列表
     * @param dataList
     *            数据列表
     * @param hasTitle
     *            是否需要标题
     * @param workbook
     *            工作簿
     * @param transverse
     *            是否横向写入（一列对应一个pojo，标题在第一列），默认false（一行一个pojo，标题在第一行）
     * @return 写入数据后的工作簿
     */
    @SuppressWarnings("rawtypes")
    private Workbook writeToExcel(List<Writer<?>> titles, List<List<Writer<?>>> dataList, boolean hasTitle,
        Workbook workbook, boolean transverse) {
        if (CollectionUtil.isEmpty(dataList)) {
            LOGGER.warn("数据为空，不写入直接返回");
            return workbook;
        }

        LOGGER.debug("写入excel，{}标题", hasTitle ? "需要" : "不需要");
        Sheet sheet = workbook.createSheet();
        int rowNum = 0;

        List<List<Writer<?>>> datas = dataList;
        if (hasTitle && !CollectionUtil.isEmpty(titles)) {
            LOGGER.debug("需要标题，标题为：{}", titles);
            List<List<Writer<?>>> lists = new ArrayList<>(datas.size() + 1);
            lists.add(titles);
            lists.addAll(datas);
            datas = lists;
        }

        if (transverse) {
            datas = CollectionUtil.matrixTransform(datas);
        }

        for (int i = rowNum; i < (rowNum + datas.size()); i++) {
            Row row = sheet.createRow(i);
            List<? extends Writer> columnDatas = datas.get(i - rowNum);
            if (CollectionUtil.isEmpty(columnDatas)) {
                continue;
            }
            for (int j = 0; j < columnDatas.size(); j++) {
                Writer<?> data = columnDatas.get(j);
                if (data == null) {
                    continue;
                }
                LOGGER.debug("写入第[{}]行第[{}]列数据[{}]", i, j, data.data);
                data.write(row.createCell(j));
            }
        }
        return workbook;
    }

    /**
     * 构建单元格数据
     *
     * @param data
     *            要写入单元格的数据
     * @return 返回不为空表示能写入，并返回单元格数据，返回空表示无法写入
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Writer<?> build(Object data) {
        if (data == null) {
            return NULL_WRITER;
        }

        Optional<ExcelDataWriter<?>> dataBuilder = writers.values().parallelStream()
            .filter(excelData -> excelData.writeable(data.getClass())).limit(1).findFirst();

        ExcelDataWriter<?> writer = dataBuilder
            .orElseThrow(() -> new UnsupportedOperationException("类型[" + data.getClass() + "]没有对应的ExcelDataWriter"));
        return new Writer(writer, data);
    }

    @AllArgsConstructor
    private static class Writer<T> {

        private final ExcelDataWriter<T> writer;

        private final T data;

        public void write(Cell cell) {
            writer.write(cell, data);
        }
    }
}
