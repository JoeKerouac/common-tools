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
package com.github.joekerouac.common.tools.reference;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class ReferenceUtilsTest {

    @Test
    public void baseTest() throws Exception {
        Object obj = new Object();
        CountDownLatch latch = new CountDownLatch(1);
        ReferenceUtils.listenDestroy(obj, latch::countDown);
        System.gc();
        Assert.assertFalse(latch.await(100, TimeUnit.MILLISECONDS));
        obj = null;
        System.gc();
        Assert.assertTrue(latch.await(100, TimeUnit.MILLISECONDS));
    }
}
