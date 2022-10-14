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
package com.github.joekerouac.common.tools.console;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.constant.Const;
import com.github.joekerouac.common.tools.log.Logger;
import com.github.joekerouac.common.tools.log.LoggerFactory;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class CommandConsoleTest {

    private static final Logger log = LoggerFactory.getLogger(CommandConsoleTest.class);

    @Test
    public void test() {
        File file = new File(Const.USER_DIR + Const.FILE_SEPARATOR + "CommandConsoleTest");
        Assert.assertFalse(file.exists());
        CommandConsole console = CommandConsoleFactory.create(new File(Const.USER_DIR), log);
        console.exec("mkdir CommandConsoleTest");
        Assert.assertTrue(file.exists());
        if (Const.IS_WINDOWS) {
            console.exec("rm CommandConsoleTest");
        } else {
            console.exec("rm -rf CommandConsoleTest");
        }
        Assert.assertFalse(file.exists());
    }

}
