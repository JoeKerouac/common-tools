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

import java.lang.annotation.*;

/**
 * excel列注解
 *
 * @author JoeKerouac
 * @date 2022-10-17 19:09
 * @since 2.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface ExcelColumn {
    /**
     * 排序，会将所有带ExcelColumn的字段按照sort从小到大的顺序排，一样的随机排序
     *
     * @return 大小
     */
    int sort() default 100;

    /**
     * 对应的列标题，默认采用字段名
     *
     * @return 列标题
     */
    String value() default "";

    /**
     * 是否忽略
     *
     * @return true表示忽略
     */
    boolean ignore() default false;
}
