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
package com.github.joekerouac.common.tools.binary.annotations;

import java.lang.annotation.*;

/**
 * 强制指定字段长度为指定值
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Size {

    /**
     * 长度，单位byte，如果{@link #sizeField()}不为空则优先使用该{@link #sizeField()}来确定字段长度
     * 
     * @return 字段长度，如果指定的长度大于实际字段长度，那么将会忽略，注意，如果字段是数组类型，那么该长度指定的是数组长度；
     */
    int value() default 0;

    /**
     * 长度来源于前边的字段的值
     * 
     * @return 代表本字段长度的其他字段名，该字段必须比注解所在字段声明顺序靠前
     */
    String sizeField() default "";

}
