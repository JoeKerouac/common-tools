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
package com.github.joekerouac.common.tools.registry;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.constant.Const;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class RegistryTest {

    @Test
    public void test() throws Exception {
        if (!Const.IS_WINDOWS) {
            return;
        }

        RegistryKey test = RegistryKey.HKEY_CURRENT_USER.createSubKey("test", RegistryKey.ACCESS_ALL);
        RegistryKey java = test.createSubKey("java", RegistryKey.ACCESS_ALL);
        RegistryValue value1 = new RegistryValue(java, "value1", RegistryValue.REG_SZ);
        value1.setData("value1 str");
        RegistryValue value2 = new RegistryValue(java, "value2", RegistryValue.REG_EXPAND_SZ);
        value2.setData("value2 str");
        RegistryValue value3 = new RegistryValue(java, "value3", RegistryValue.REG_BINARY);
        value3.setData(new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        RegistryValue value4 = new RegistryValue(java, "value4", RegistryValue.REG_DWORD);
        value4.setData(1);
        RegistryValue value5 = new RegistryValue(java, "value5", RegistryValue.REG_MULTI_SZ);
        value5.setData(new String[] {"str1", "str2"});

        java.setValue(value1);
        java.setValue(value2);
        java.setValue(value3);
        java.setValue(value4);
        java.setValue(value5);

        java.close();
        test.close();

        test = RegistryKey.HKEY_CURRENT_USER.openSubKey("test", RegistryKey.ACCESS_ALL);
        java = test.openSubKey("java", RegistryKey.ACCESS_ALL);
        value1 = java.getValue("value1");
        value2 = java.getValue("value2");
        value3 = java.getValue("value3");
        value4 = java.getValue("value4");
        value5 = java.getValue("value5");

        Assert.assertEquals(value1.getData(), "value1 str");
        Assert.assertEquals(value2.getData(), "value2 str");
        Assert.assertEquals(value3.getData(), new byte[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        Assert.assertEquals(value4.getData(), 1);
        Assert.assertEquals(value5.getData(), new String[] {"str1", "str2"});

        // 注意，如果想要删除一个注册表，必须把该注册表下边的所有子项都删除了才能删除，所以这里如果想删除test必须把test下边的Java删除
        test.deleteSubKey("java");
        RegistryKey.HKEY_CURRENT_USER.deleteSubKey("test");

        test.close();
        java.close();
    }

}
