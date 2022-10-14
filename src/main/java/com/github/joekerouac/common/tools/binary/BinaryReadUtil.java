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
package com.github.joekerouac.common.tools.binary;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import com.github.joekerouac.common.tools.binary.annotations.Endian;
import com.github.joekerouac.common.tools.binary.annotations.Size;
import com.github.joekerouac.common.tools.binary.annotations.Skip;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.reflect.AccessorUtil;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * 二进制文件读取工具，将二进制文件按照指定布局读取为对象
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class BinaryReadUtil {

    public static <T> T binaryRead(ByteBuffer buffer, Class<T> clazz)
        throws IllegalAccessException, InstantiationException {
        T result = clazz.newInstance();

        final Field[] declaredFields = clazz.getDeclaredFields();

        Endian endianAnnotation = clazz.getAnnotation(Endian.class);
        final boolean globalBigEndian = endianAnnotation == null || !endianAnnotation.little();

        Map<String, Long> fields = new HashMap<>();

        for (final Field field : declaredFields) {
            // 跳过transient字段
            if (AccessorUtil.isTransient(field)) {
                continue;
            }

            field.setAccessible(true);
            final Class<?> fieldType = field.getType();
            Assert.assertTrue(
                fieldType.isPrimitive() || (fieldType.isArray() && fieldType.getComponentType().isPrimitive()),
                StringUtils.format("当前仅支持八大基本类型以及对应的一维数组，不支持 [{}] 类型", fieldType),
                ExceptionProviderConst.CodeErrorExceptionProvider);

            endianAnnotation = field.getAnnotation(Endian.class);
            boolean bigEndian = endianAnnotation == null ? globalBigEndian : !endianAnnotation.little();

            final Skip skipAnnotation = field.getAnnotation(Skip.class);
            int skip = skipAnnotation == null ? 0 : skipAnnotation.value();
            Assert.assertTrue(skip >= 0, StringUtils.format("Skip注解的值必须大于等于0，当前是：[{}]", skip),
                ExceptionProviderConst.CodeErrorExceptionProvider);
            if (skip > 0) {
                buffer.position(buffer.position() + skip);
            }

            final Size sizeAnnotation = field.getAnnotation(Size.class);
            Assert.assertTrue(sizeAnnotation != null || !fieldType.isArray(),
                StringUtils.format("字段 [{}] 为数组类型，数组必须使用 [@{}] 注解指定大小", field, Size.class.getName()),
                ExceptionProviderConst.CodeErrorExceptionProvider);

            int annotationSize = 0;
            if (sizeAnnotation != null) {
                if (StringUtils.isBlank(sizeAnnotation.sizeField())) {
                    annotationSize = sizeAnnotation.value();
                } else {
                    String fieldName = sizeAnnotation.sizeField();
                    final Long size = fields.get(fieldName);
                    Assert.notNull(size,
                        StringUtils.format("字段 [{}] 的长度声明为来源于字段 [{}]，但是在字段 [{}] 前边不存在 [{}] 字段或者字段 [{}] 是array类型", field,
                            fieldName, field, fieldName),
                        ExceptionProviderConst.CodeErrorExceptionProvider);
                    annotationSize = size.intValue();
                }
            }

            int fieldSize;

            Class<?> realType = fieldType.isArray() ? fieldType.getComponentType() : fieldType;
            if (realType == byte.class || realType == char.class) {
                fieldSize = 1;
            } else if (realType == short.class) {
                fieldSize = 2;
            } else if (realType == int.class) {
                fieldSize = 4;
            } else if (realType == long.class) {
                fieldSize = 8;
            } else if (realType == float.class || realType == double.class || realType == boolean.class) {
                // 暂时不支持float、double、boolean
                throw new UnsupportedOperationException(StringUtils.format("不支持的类型： [{}]", fieldType));
            } else {
                throw new UnsupportedOperationException(StringUtils.format("不支持的类型： [{}]", fieldType));
            }

            if (fieldType.isArray()) {
                Assert.assertTrue(annotationSize > 0, "数组长度不能小于等于0", ExceptionProviderConst.CodeErrorExceptionProvider);
                final Object array = Array.newInstance(realType, annotationSize);
                for (int i = 0; i < annotationSize; i++) {
                    if (realType == byte.class) {
                        Array.set(array, i, (byte)mergeRead(buffer, fieldSize, bigEndian));
                    } else if (realType == char.class) {
                        Array.set(array, i, (char)mergeRead(buffer, fieldSize, bigEndian));
                    } else if (realType == short.class) {
                        Array.set(array, i, (short)mergeRead(buffer, fieldSize, bigEndian));
                    } else if (realType == int.class) {
                        Array.set(array, i, (int)mergeRead(buffer, fieldSize, bigEndian));
                    } else if (realType == long.class) {
                        Array.set(array, i, mergeRead(buffer, fieldSize, bigEndian));
                    } else {
                        throw new UnsupportedOperationException(StringUtils.format("不支持的类型：[{}]", realType));
                    }

                }
                field.set(result, array);
            } else {
                int realFieldSize = sizeAnnotation == null ? fieldSize : Math.min(annotationSize, fieldSize);
                Assert.assertTrue(realFieldSize > 0, "当前计算出字段长度是小于等于0，可能是注解上的值写的是小于等于0的",
                    ExceptionProviderConst.CodeErrorExceptionProvider);

                final long fieldValue = mergeRead(buffer, realFieldSize, bigEndian);
                fields.put(field.getName(), fieldValue);

                if (realType == byte.class) {
                    field.set(result, (byte)fieldValue);
                } else if (realType == char.class) {
                    field.set(result, (char)fieldValue);
                } else if (realType == short.class) {
                    field.set(result, (short)fieldValue);
                } else if (realType == int.class) {
                    field.set(result, (int)fieldValue);
                } else if (realType == long.class) {
                    field.set(result, fieldValue);
                } else {
                    throw new UnsupportedOperationException(StringUtils.format("不支持的类型：[{}]", realType));
                }
            }
        }

        return result;
    }

    /**
     * 从指定数据的指定起始位置读取指定长度，将其拼装为一个long类型的数据
     * 
     * @param buffer
     *            数据buffer
     * @param len
     *            结果数据长度
     * @param bigEndian
     *            是否大端序，true表示是大端序
     * @return 数据
     */
    public static long mergeRead(ByteBuffer buffer, int len, boolean bigEndian) {
        if (len == 1) {
            return buffer.get();
        } else {
            long value = 0;

            for (int i = 0; i < len; i++) {
                long v = Byte.toUnsignedLong(buffer.get());
                if (bigEndian) {
                    value = v << 8 * (len - i - 1) | value;
                } else {
                    value = v << 8 * i | value;
                }
            }
            return value;
        }
    }

}
