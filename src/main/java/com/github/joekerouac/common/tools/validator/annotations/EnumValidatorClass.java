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
package com.github.joekerouac.common.tools.validator.annotations;

import java.util.Objects;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.github.joekerouac.common.tools.enums.EnumInterface;

/**
 * 校验字符串必须是指定枚举
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class EnumValidatorClass implements ConstraintValidator<EnumValidator, String> {

    private EnumValidator validator;

    @Override
    public void initialize(EnumValidator constraintAnnotation) {
        this.validator = constraintAnnotation;
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        Class<? extends Enum<?>> type = validator.value();

        Enum<?>[] enumConstants = type.getEnumConstants();
        boolean isEnumInterface = EnumInterface.class.isAssignableFrom(type);

        for (Enum<?> enumConstant : enumConstants) {
            if (Objects.equals(enumConstant.name(), value)) {
                return true;
            }

            if (isEnumInterface && Objects.equals(((EnumInterface)enumConstant).code(), value)) {
                return true;
            }
        }

        return false;
    }
}
