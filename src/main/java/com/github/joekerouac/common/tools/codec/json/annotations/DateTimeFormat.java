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
package com.github.joekerouac.common.tools.codec.json.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author JoeKerouac
 * @date 2023-05-22 11:17
 * @since 2.0.3
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface DateTimeFormat {

    /**
     * 日期格式，用于json序列化与反序列化
     * 
     * @see com.github.joekerouac.common.tools.codec.json.databind.LocalDateTimeSerializer
     * @see com.github.joekerouac.common.tools.codec.json.databind.LocalDateTimeDeserializer
     * 
     * @return 日期格式，例如yyyy-MM-dd HH:mm:ss
     */
    String value() default "";

    /**
     * 日期格式，用于json序列化，在将{@link java.time.LocalDateTime}序列化为字符串时优先使用该配置，如果该配置为空则使用{@link #value()}
     * 
     * @return 日期格式，例如yyyy-MM-dd HH:mm:ss
     */
    String serializer() default "";

    /**
     * 日期格式，用于json反序列化，在将字符串反序列化为{@link java.time.LocalDateTime}时优先使用该配置，如果该配置为空则使用{@link #value()}
     * 
     * @return 日期格式，例如yyyy-MM-dd HH:mm:ss
     */
    String deserializer() default "";

}
