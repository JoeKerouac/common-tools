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

import lombok.Getter;

/**
 * @author JoeKerouac
 * @date 2025-06-30 18:51:05
 * @since 2.1.6
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface NumberFormat {

    /**
     * 序列化规则
     *
     * @return 规则
     */
    Rule rule() default Rule.DEFAULT;

    @Getter
    enum Rule {

        /**
         * 默认规则，什么也不做
         */
        DEFAULT(0),

        /**
         * 规则：
         * <li>如果小数位都是0，则省略小数位，例如1.00序列化为1；</li> <i>如果小数位不全为0，则保留小数位，例如1.10序列化为1.10；</i>
         */
        AUTO_TRIM(1),

        ;

        private final int rule;

        Rule(int rule) {
            this.rule = rule;
        }
    }

}
