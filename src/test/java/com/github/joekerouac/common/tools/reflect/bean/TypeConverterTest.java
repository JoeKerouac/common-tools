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
package com.github.joekerouac.common.tools.reflect.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.enums.YesOrNoEnum;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class TypeConverterTest {

    @Test(dataProvider = "dataProvider")
    public <S, T> void test(Class<T> targetType, S src) {
        TypeConverter converter = null;
        for (TypeConverter typeConverter : TypeConverterRegistry.getAllTypeConverter()) {
            if (typeConverter.test(src.getClass(), targetType)) {
                converter = typeConverter;
                break;
            }
        }

        Assert.assertNotNull(converter);
        Assert.assertNotNull(converter.convert(src, targetType));
    }

    @DataProvider(name = "dataProvider")
    public Object[][] dataProvider() {
        List<Object[]> list = new ArrayList<>();
        list.add(new Object[] {String.class, "test str"});

        list.add(new Object[] {boolean.class, "true"});
        list.add(new Object[] {Boolean.class, "true"});

        list.add(new Object[] {char.class, "t"});
        list.add(new Object[] {Character.class, "t"});

        list.add(new Object[] {byte.class, "10"});
        list.add(new Object[] {Byte.class, "10"});

        list.add(new Object[] {short.class, "10"});
        list.add(new Object[] {Short.class, "10"});

        list.add(new Object[] {int.class, "10"});
        list.add(new Object[] {Integer.class, "10"});

        list.add(new Object[] {long.class, "10"});
        list.add(new Object[] {Long.class, "10"});

        list.add(new Object[] {double.class, "10"});
        list.add(new Object[] {Double.class, "10"});

        list.add(new Object[] {float.class, "10"});
        list.add(new Object[] {Float.class, "10"});

        list.add(new Object[] {File.class, "/root/test.jar"});

        list.add(new Object[] {YesOrNoEnum.class, "YES"});
        list.add(new Object[] {YesOrNoEnum.class, "Y"});

        return list.toArray(new Object[0][]);
    }
}
