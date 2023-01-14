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

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.exception.ExcelClosedException;
import com.github.joekerouac.common.tools.poi.data.*;
import com.github.joekerouac.common.tools.reflect.ReflectUtil;
import com.github.joekerouac.common.tools.string.StringUtils;

import lombok.AllArgsConstructor;
import lombok.CustomLog;

/**
 * Excel写出工具，将数据写入excel，用户可以注册自己的excel单元格数据类型处理器{@link ExcelDataWriter ExcelDataWriter}来做
 * 一些特殊处理，例如添加单元格样式等，系统默认会注册Date、Calendar、String、Number、Boolean、Enum类型的数据处理器。
 * 
 * @author JoeKerouac
 * @date 2023-01-12 19:35
 * @since 2.0.0
 */
@CustomLog
public class ExcelWriter<DATA> {

    private static final String DEFAULT_SHEET_NAME = "Sheet0";

    /**
     * 默认内存中最多多少行单元格
     */
    private static final int IN_MEMORY = 100;

    /**
     * 排序器
     */
    private static final Comparator<Field> COMPARATOR;

    /**
     * 全局默认的Excel单元格数据类型
     */
    private static final Map<Class<?>, ExcelDataWriter<?>> DEFAULT_WRITERS;

    /**
     * null数据写出函数
     */
    private static final Writer<?> NULL_WRITER = new Writer<>(new StringDataWriter(), "");

    static {
        Map<Class<?>, ExcelDataWriter<?>> writers = new HashMap<>();
        writers.put(Boolean.class, new BooleanDataWriter());
        writers.put(Calendar.class, new CalendarDataWriter());
        writers.put(Date.class, new DateDataWriter());
        writers.put(Enum.class, new EnumDataWriter());
        writers.put(Number.class, new NumberDataWriter());
        writers.put(String.class, new StringDataWriter());
        DEFAULT_WRITERS = Collections.unmodifiableMap(writers);

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
    private final Map<Class<?>, ExcelDataWriter<?>> writers = new ConcurrentHashMap<>();

    /**
     * 是否写出标题，如果为true，则每个sheet写入第一行数据前都会先写出标题
     */
    private final boolean hasTitle;

    /**
     * 是否横向写入（一列对应一个pojo，标题在第一列），默认false（一行一个pojo，标题在第一行）
     */
    private final boolean transverse;

    /**
     * work book
     */
    private final Workbook wb;

    /**
     * 标志位，false表示无法写出
     */
    private boolean flag;

    /**
     * 构建器
     *
     */
    public ExcelWriter() {
        this(IN_MEMORY, true, false);
    }

    /**
     * 构建器
     *
     * @param inMemory
     *            最多保留在内存中多少行
     */
    public ExcelWriter(int inMemory) {
        this(inMemory, true, false);
    }

    /**
     * 构建器
     *
     * @param inMemory
     *            最多保留在内存中多少行
     * @param hasTitle
     *            是否写出标题，全局配置，后续还可以分别指定
     */
    public ExcelWriter(int inMemory, boolean hasTitle) {
        this(inMemory, hasTitle, false);
    }

    /**
     * 构建器
     *
     * @param inMemory
     *            最多保留在内存中多少行
     * @param hasTitle
     *            是否写出标题，全局配置，后续还可以分别指定
     * @param transverse
     *            默认数据是按行写的，是否将数据按列写出，true表示按列写出（即竖着写）
     */
    public ExcelWriter(int inMemory, boolean hasTitle, boolean transverse) {
        this.hasTitle = hasTitle;
        this.transverse = transverse;
        this.wb = new SXSSFWorkbook(inMemory);
        this.flag = true;
    }

    /**
     * 将excel写出到指定输出流；
     * 
     * 注意：写出后输出流将会被关闭，同时excel也会被关闭，无法再写入数据
     * 
     * @param outputStream
     *            输出流
     * @throws IOException
     *             IO异常
     */
    public synchronized void write(OutputStream outputStream) throws IOException {
        if (!flag) {
            throw new ExcelClosedException("excel closed, can not write");
        }

        flag = false;

        wb.write(outputStream);

        if (wb instanceof SXSSFWorkbook) {
            ((SXSSFWorkbook)wb).dispose();
        }

        wb.close();
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
     * 将pojo集合写入excel
     *
     * @param dataList
     *            pojo集合，空元素将被忽略，集合中必须是都是同种对象
     */
    public void writeToExcel(List<DATA> dataList) {
        writeToExcel(dataList, hasTitle, transverse, DEFAULT_SHEET_NAME);
    }

    /**
     * 将pojo集合写入excel
     *
     * @param dataList
     *            pojo集合，空元素将被忽略，集合中必须是都是同种对象
     * @param sheetName
     *            数据要写入的sheet
     */
    public void writeToExcel(List<DATA> dataList, String sheetName) {
        writeToExcel(dataList, hasTitle, transverse, sheetName);
    }

    /**
     * 将pojo集合写入excel
     *
     * @param dataList
     *            pojo集合，空元素将被忽略，集合中必须是都是同种对象
     * @param hasTitle
     *            是否写入title
     */
    public void writeToExcel(List<DATA> dataList, boolean hasTitle) {
        writeToExcel(dataList, hasTitle, transverse, DEFAULT_SHEET_NAME);
    }

    /**
     * 将pojo集合写入excel
     *
     * @param dataList
     *            pojo集合，空元素将被忽略，集合中必须是都是同种对象
     * @param hasTitle
     *            是否写入title
     * @param transverse
     *            是否横向写入（一列对应一个pojo，标题在第一列），默认false（一行一个pojo，标题在第一行）
     */
    public void writeToExcel(List<DATA> dataList, boolean hasTitle, boolean transverse) {
        writeToExcel(dataList, hasTitle, transverse, DEFAULT_SHEET_NAME);
    }

    /**
     * 将pojo集合写入excel
     *
     * @param dataList
     *            pojo集合，空元素将被忽略，集合中必须是都是同种对象
     * @param hasTitle
     *            是否写入title
     * @param transverse
     *            是否横向写入（一列对应一个pojo，标题在第一列），默认false（一行一个pojo，标题在第一行）
     * @param sheetName
     *            sheetName
     */
    public void writeToExcel(List<DATA> dataList, boolean hasTitle, boolean transverse, String sheetName) {
        if (CollectionUtil.isEmpty(dataList)) {
            LOGGER.warn("给定数据集合为空");
            return;
        }

        List<DATA> newDataList = dataList.parallelStream().filter(Objects::nonNull).collect(Collectors.toList());
        if (newDataList.isEmpty()) {
            LOGGER.warn("给定数据集合里的数据全是空");
            return;
        }

        // 获取所有字段（包括父类的）
        Field[] fields = ReflectUtil.getAllFields(newDataList.get(0).getClass());

        // 过滤可以写入的字段
        List<Field> writeFields = new ArrayList<>();
        for (Field field : fields) {
            String name = field.getName();
            Class<?> type = field.getType();

            if (decide(type) == null) {
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

        List<List<Writer<?>>> writeDatas = new ArrayList<>(newDataList.size());
        for (Object dataValue : newDataList) {
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
        writeToExcel(titles, writeDatas, hasTitle, transverse, sheetName);
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
     * @param transverse
     *            是否横向写入（一列对应一个pojo，标题在第一列），默认false（一行一个pojo，标题在第一行）
     * @param sheetName
     *            sheet名，如果存在则追加写入，否则创建
     */
    @SuppressWarnings("rawtypes")
    private synchronized void writeToExcel(List<Writer<?>> titles, List<List<Writer<?>>> dataList, boolean hasTitle,
        boolean transverse, String sheetName) {
        if (!flag) {
            throw new ExcelClosedException("excel closed, can not write");
        }

        if (CollectionUtil.isEmpty(dataList)) {
            LOGGER.warn("数据为空，不写入直接返回");
            return;
        }

        LOGGER.debug("写入excel，{}标题", hasTitle ? "需要" : "不需要");

        Sheet sheet = wb.getSheet(sheetName);

        List<List<Writer<?>>> realWriteData = dataList;

        if (sheet == null) {
            sheet = wb.createSheet(sheetName);
            if (hasTitle && !CollectionUtil.isEmpty(titles)) {
                LOGGER.debug("需要标题，标题为：{}", titles);
                List<List<Writer<?>>> lists = new ArrayList<>(realWriteData.size() + 1);
                lists.add(titles);
                lists.addAll(realWriteData);
                realWriteData = lists;
            }
        }

        int rowNum = sheet.getPhysicalNumberOfRows();

        if (transverse) {
            realWriteData = CollectionUtil.matrixTransform(realWriteData);
        }

        for (int i = rowNum; i < (rowNum + realWriteData.size()); i++) {
            Row row = sheet.createRow(i);
            List<? extends Writer> columnDatas = realWriteData.get(i - rowNum);
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

        ExcelDataWriter excelDataWriter = decide(data.getClass());
        if (excelDataWriter == null) {
            throw new UnsupportedOperationException("数据[" + data + "]没有对应的ExcelDataWriter");
        }

        return new Writer(excelDataWriter, data);
    }

    /**
     * 根据类型决策出一个数据写出函数
     * 
     * @param type
     *            数据类型class
     * @param <T>
     *            实际类型
     * @return 该数据的写出函数，可能为null
     */
    @SuppressWarnings("unchecked")
    private <T> ExcelDataWriter<T> decide(Class<? extends T> type) {
        ExcelDataWriter<T> writer = (ExcelDataWriter<T>)writers.get(type);
        if (writer != null) {
            return writer;
        }

        try {
            writer = (ExcelDataWriter<T>)DEFAULT_WRITERS.get(type);
            if (writer != null) {
                return writer;
            }

            writer = (ExcelDataWriter<T>)writers.values().stream().filter(w -> w.writeable(type)).limit(1).findFirst()
                .orElse(null);

            if (writer != null) {
                return writer;
            }

            writer = (ExcelDataWriter<T>)DEFAULT_WRITERS.values().stream().filter(w -> w.writeable(type)).limit(1)
                .findFirst().orElse(null);

            return writer;
        } finally {
            if (writer != null) {
                writers.put(type, writer);
            }
        }
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
