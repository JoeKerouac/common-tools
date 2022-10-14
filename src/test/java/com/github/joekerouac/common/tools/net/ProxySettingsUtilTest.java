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
package com.github.joekerouac.common.tools.net;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.constant.Const;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class ProxySettingsUtilTest {

    @Test
    public void test() throws Exception {
        if (!Const.IS_WINDOWS && !Const.IS_MAC_OS) {
            return;
        }

        HttpProxySettings settings = new HttpProxySettings("127.0.0.1", 8080, "");
        ProxySettingsUtil.setProxy(settings);

        try {
            if (Const.IS_WINDOWS) {
                HttpProxySettings currentProxySettings = ProxySettingsUtil.getCurrentProxySettings();
                // 操作系统会自动添加http://前缀，所以对比的时候要添加上前缀
                settings.setHost("http://127.0.0.1");
                Assert.assertEquals(currentProxySettings, settings);
            }
        } finally {
            ProxySettingsUtil.closeProxy();
        }
    }

}
