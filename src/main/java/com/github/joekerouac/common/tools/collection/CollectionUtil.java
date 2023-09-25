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
package com.github.joekerouac.common.tools.collection;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 常用集合工具
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectionUtil {

    /**
     * 矩阵转置（矩阵必须每行的列数都一样）（行变列，列变行）
     *
     * @param datas
     *            要转置的数据
     * @param <T>
     *            数据类型
     * @since 2.0.0
     * @return 转置后的矩阵，例如[[01,02][11,12]]会变为[[01,11],[02,12]]
     */
    public static <T> List<List<T>> matrixTransform(List<List<T>> datas) {
        if (datas.size() == 0 || datas.get(0).size() == 0) {
            return datas;
        }
        int column = datas.size();
        int row = datas.get(0).size();
        List<List<T>> newData = new ArrayList<>(row);
        for (int i = 0; i < row; i++) {
            List<T> list = new ArrayList<>(column);
            for (int j = 0; j < column; j++) {
                list.add(datas.get(j).get(i));
            }
            newData.add(list);
        }
        return newData;
    }

    /**
     * map转换，将map的value按照指定方式转换为新的value然后返回转换后的map
     *
     * @param map
     *            要转换的map
     * @param function
     *            转换函数，用于将老的value转换为新的value
     * @param <K>
     *            KEY类型
     * @param <NEW>
     *            转换后的value类型
     * @param <OLD>
     *            转换前的value类型
     * @return 转换后的map
     */
    public static <K, NEW, OLD> Map<K, NEW> convert(Map<K, OLD> map, Function<OLD, NEW> function) {
        Map<K, NEW> newMap = new HashMap<>();
        map.forEach((k, v) -> newMap.put(k, function.apply(v)));
        return newMap;
    }

    /**
     * 将数组长度加1并且将指定数据添加到数组末尾
     *
     * @param target
     *            要添加的数据
     * @param array
     *            目标数组
     * @param <T>
     *            数组类型
     * @return 新数组
     */
    public static <T> T[] addTo(T target, T[] array) {
        T[] newArray = createArray(target.getClass(), array.length + 1);
        System.arraycopy(array, 0, newArray, 0, array.length);
        newArray[array.length] = target;
        return newArray;
    }

    /**
     * 生成指定类型指定长度的一维数组
     *
     * @param arrayType
     *            数组类型
     * @param len
     *            长度
     * @return 指定类型指定长度的一维数组
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] createArray(Class<?> arrayType, int len) {
        return (T[])Array.newInstance(arrayType, len);
    }

    /**
     * 判断数组长度是否相同
     * 
     * @param arr0
     *            数组1
     * @param arr1
     *            数组2
     * @param <T>
     *            数组实际类型
     * @param <F>
     *            数组实际类型
     * @return 返回true表示数组长度一致
     */
    public static <T, F> boolean sizeEquals(T[] arr0, F[] arr1) {
        return size(arr0) == size(arr1);
    }

    /**
     * 安全判断集合是否不为空
     *
     * @param collection
     *            collection集合
     * @return 返回true表示不为空
     */
    public static boolean isNotEmpty(Collection<?> collection) {
        return size(collection) > 0;
    }

    /**
     * 安全判断数组是否不为空
     *
     * @param array
     *            数组
     * @return 返回true表示不为空
     */
    public static <T> boolean isNotEmpty(T[] array) {
        return size(array) > 0;
    }

    /**
     * 安全判断集合是否为非空
     *
     * @param map
     *            map集合
     * @return 返回true表示非空
     */
    public static boolean isNotEmpty(Map<?, ?> map) {
        return size(map) != 0;
    }

    /**
     * 安全判断集合是否为空
     *
     * @param collection
     *            collection集合
     * @return 返回true表示空
     */
    public static boolean isEmpty(Collection<?> collection) {
        return size(collection) == 0;
    }

    /**
     * 判断数组是否是空
     * 
     * @param data
     *            数组
     * @return true表示数组是空
     */
    public static boolean isEmpty(byte[] data) {
        return size(data) == 0;
    }

    /**
     * 安全判断数组是否为空
     *
     * @param array
     *            数组
     * @return 返回true表示空
     */
    public static <T> boolean isEmpty(T[] array) {
        return size(array) == 0;
    }

    /**
     * 安全判断集合是否为空
     *
     * @param map
     *            map集合
     * @return 返回true表示空
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return size(map) == 0;
    }

    /**
     * 安全的获取数组的长度
     * 
     * @param array
     *            数组
     * @return 数组的长度，为null时返回0
     */
    public static <T> int size(T[] array) {
        return array == null ? 0 : array.length;
    }

    /**
     * 安全的获取数组的长度
     * 
     * @param array
     *            数组
     * @return 数组的长度，为null时返回0
     */
    public static int size(byte[] array) {
        return array == null ? 0 : array.length;
    }

    /**
     * 安全的获取集合的长度
     * 
     * @param array
     *            集合
     * @return 集合的长度，为null时返回0
     */
    public static int size(Collection<?> array) {
        return array == null ? 0 : array.size();
    }

    /**
     * 安全的获取map的长度
     * 
     * @param array
     *            map
     * @return map的长度，为null时返回0
     */
    public static int size(Map<?, ?> array) {
        return array == null ? 0 : array.size();
    }

}
