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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.NotNull;
import javax.validation.executable.ExecutableValidator;

import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * 校验工具类，注意，hibernate会缓存对象，所以请不要在agent和plugin中共享该实例，也不要在多个不同的classloader中共享该实例
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class ValidationServiceImpl implements ValidationService {

    /**
     * bean验证器
     */
    private final Validator validator;

    /**
     * 构造器、方法参数、方法响应验证器
     */
    private final ExecutableValidator executableValidator;

    /**
     * 校验错误消息模板
     */
    private static final String VALIDATION_ERR_MSG = "rootBeanClass:[{}], path:[{}], value:[{}], msg:[{}]";

    public ValidationServiceImpl() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
            executableValidator = validator.forExecutables();
        }
    }

    @Override
    public <T> T validate(final T bean, final Class<?>... groups) throws IllegalArgumentException {
        Assert.argNotNull(bean, "bean");
        Set<ConstraintViolation<Object>> constraintViolations = validator.validate(bean, groups);
        check(constraintViolations);
        return bean;
    }

    /**
     * 验证方法参数是否符合规则
     *
     * @param instance
     *            方法所在的类的实例
     * @param method
     *            方法实例
     * @param params
     *            参数
     */
    @Override
    public <T> T validateParameters(@NotNull T instance, @NotNull Method method, Object[] params) {
        Assert.argNotNull(instance, "instance");
        Assert.argNotNull(method, "method");
        Set<ConstraintViolation<Object>> constraintViolations =
            executableValidator.validateParameters(instance, method, params);
        check(constraintViolations);
        return instance;
    }

    /**
     * 校验构造器入参
     * 
     * @param constructor
     *            构造器
     * @param args
     *            构造器参数
     */
    @Override
    public void validateParameters(@NotNull Constructor<?> constructor, @NotNull Object[] args) {
        Assert.argNotNull(constructor, "constructor");
        Assert.argNotNull(args, "args");
        Set<ConstraintViolation<Object>> constraintViolations =
            executableValidator.validateConstructorParameters(constructor, args);
        check(constraintViolations);
    }

    /**
     * 检查是否有校验错误
     *
     * @param constraintViolations
     *            校验结果
     */
    private void check(Set<ConstraintViolation<Object>> constraintViolations) {
        Iterator<ConstraintViolation<Object>> iterator = constraintViolations.iterator();
        if (iterator.hasNext()) {
            ConstraintViolation<Object> violation = iterator.next();
            String msg = StringUtils.format(VALIDATION_ERR_MSG, violation.getRootBeanClass(),
                violation.getPropertyPath(), violation.getInvalidValue(), violation.getMessage());
            throw new IllegalArgumentException(msg);
        }
    }

}
