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
package com.github.joekerouac.common.tools.validator;

import java.util.Objects;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.enums.YesOrNoEnum;
import com.github.joekerouac.common.tools.validator.annotations.EnumValidator;

import lombok.AllArgsConstructor;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class ValidationServiceTest {

    /**
     * 中文版错误消息
     */
    private static final String ERR_MSG1 = "rootBeanClass:[class " + ValidationServiceTest.class.getName()
        + "$Obj], path:[agree1], value:[A], msg:[必须是枚举类型: class " + YesOrNoEnum.class.getName() + "]";

    /**
     * 英文版错误消息
     */
    private static final String ERR_MSG2 = "rootBeanClass:[class " + ValidationServiceTest.class.getName()
        + "$Obj], path:[agree1], value:[A], msg:[must be an enum type: class " + YesOrNoEnum.class.getName() + "]";

    @Test
    public void test() {
        ValidationService validationService = new ValidationServiceImpl();
        // 校验YesOrNoEnum的两种表现形式，同时校验普通枚举和EnumInterface枚举
        Obj obj = new Obj("man", "Y", "YES");
        validationService.validate(obj);
        // 字符串为null时不校验
        obj = new Obj(null, "Y", "YES");
        validationService.validate(obj);
        // 枚举错误场景验证
        obj = new Obj("man", "A", "YES");
        IllegalArgumentException illegalArgumentException = null;
        try {
            validationService.validate(obj);
        } catch (IllegalArgumentException throwable) {
            illegalArgumentException = throwable;
        }

        Assert.assertNotNull(illegalArgumentException);
        Assert.assertTrue(Objects.equals(ERR_MSG1, illegalArgumentException.getMessage())
            || Objects.equals(ERR_MSG2, illegalArgumentException.getMessage()));
    }

    @AllArgsConstructor
    public static class Obj {

        @EnumValidator(Sex.class)
        private String sex;

        @EnumValidator(YesOrNoEnum.class)
        private String agree1;

        @EnumValidator(YesOrNoEnum.class)
        private String agree2;

    }

    private enum Sex {
        man, woman
    }

}
