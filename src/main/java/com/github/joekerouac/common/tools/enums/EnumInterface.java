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
package com.github.joekerouac.common.tools.enums;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 枚举接口，所有枚举都应该继承自该接口
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface EnumInterface {

    /**
     * 枚举编码
     *
     * @return 枚举编码
     */
    String code();

    /**
     * 枚举说明
     *
     * @return 枚举说明
     */
    String desc();

    /**
     * 枚举英文名
     *
     * @return 枚举英文名
     */
    String englishName();

    /**
     * 枚举中文名
     *
     * @return 枚举中文名
     */
    default String chineseName() {
        return englishName();
    }

    /**
     * 根据code获取指定枚举
     *
     * @param code
     *            枚举code
     * @param clazz
     *            枚举类型
     * @param <M>
     *            枚举实际类型
     * @return 对应的枚举
     */
    @SuppressWarnings({"all"})
    static <M extends EnumInterface> M getByCode(String code, Class<?> clazz) {
        if (!clazz.isEnum()) {
            throw new IllegalArgumentException(String.format("类型[%s]不是枚举", clazz.getName()));
        }

        if (code == null) {
            throw new NullPointerException("code must not be null");
        }

        M[] enumInterfaces = (M[])clazz.getEnumConstants();

        for (M enumInterface : enumInterfaces) {
            if (code.equals(enumInterface.code())) {
                return enumInterface;
            }
        }

        // 兜底，使用系统自带方法转换
        return (M)Enum.valueOf((Class<? extends Enum>)clazz, code);
    }

    /**
     * 重复检查，检查枚举是否有重复值
     */
    static void duplicateCheck(Class<? extends EnumInterface> clazz) {
        if (!clazz.isEnum()) {
            throw new IllegalArgumentException(String.format("类型[%s]不是枚举", clazz.getName()));
        }

        EnumInterface[] enumInterfaces = clazz.getEnumConstants();
        if (enumInterfaces.length <= 1) {
            return;
        }

        if (Stream.of(enumInterfaces).map(EnumInterface::code).collect(Collectors.toSet())
            .size() != enumInterfaces.length) {
            throw new RuntimeException(String.format("枚举[%s]存在重复code，请检查", clazz));
        }
    }

}
