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
package com.github.joekerouac.common.tools.proxy.java;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.proxy.Interception;
import com.github.joekerouac.common.tools.proxy.ProxyClient;
import com.github.joekerouac.common.tools.proxy.ProxyClientTestHelper;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class JavaProxyClientTest {

    @Test
    public void doTest() {
        ProxyClient client = ProxyClient.getInstance(ProxyClient.ClientType.JAVA);

        Interception interception = (target, params, method, invoker) -> {
            if (method.getName().equals("say")) {
                return new Hello().hi((String)params[0]);
            } else {
                return invoker.call();
            }
        };

        Say say = client.create(Say.class, interception);

        String str = "123";
        Assert.assertEquals(say.say(str), "hi:" + str);

        ProxyClientTestHelper.doObjectMethodTest(client);
        ProxyClientTestHelper.doProxyParentMethodTest(client);
        ProxyClientTestHelper.doMultiProxy(client);
    }

    public interface Say {
        String say(String str);
    }

    class Hello {
        String hi(String str) {
            return "hi:" + str;
        }
    }

}
