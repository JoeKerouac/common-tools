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
package com.github.joekerouac.common.tools.impl;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.cache.impl.FixedTimeoutCache;
import com.github.joekerouac.common.tools.thread.ThreadUtil;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class FixedTimeoutCacheTest {

    @Test
    public void baseTest() {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        FixedTimeoutCache<Integer> fixedTimeoutCache = new FixedTimeoutCache<>(500, atomicInteger::getAndIncrement);

        ThreadUtil.sleep(1);
        // 先测试懒加载，如果没有懒加载此时应该是1
        Assert.assertEquals(fixedTimeoutCache.getTarget().intValue(), 0);
        // 这个多睡10毫秒，防止出现一些问题
        ThreadUtil.sleep(510, TimeUnit.MILLISECONDS);
        // 此时缓存应该更新了，应该是1了
        Assert.assertEquals(fixedTimeoutCache.getTarget().intValue(), 1);
        // 强制刷新缓存
        Assert.assertEquals(fixedTimeoutCache.refresh().intValue(), 2);
    }

}
