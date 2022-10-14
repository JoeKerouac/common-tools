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
package com.github.joekerouac.common.tools.collection;

import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class MultiChannelQueueTest {

    @Test
    public void baseTest() throws Exception {
        // 基础测试
        String id = "test";
        MultiChannelQueue<String, String> queue = new MultiChannelQueueImpl<>(10);
        Assert.assertTrue(queue.addChannel(id, 5, 1));
        Assert.assertFalse(queue.addChannel(id, 5, 1));
        // 校验，确保通道是根据ID判断重复的，不应该使用size和maxConcurrency
        Assert.assertFalse(queue.addChannel(id, 1, 1));
        // 因为队列还没添加数据，所以这里应该返回null
        Assert.assertNull(queue.take(1, TimeUnit.MILLISECONDS));

        queue.add(id, "data1");
        Assert.assertTrue(queue.add(id, "data2", 1, TimeUnit.MILLISECONDS));
        Assert.assertTrue(queue.add(id, "data3", 1, TimeUnit.MILLISECONDS));
        Assert.assertTrue(queue.add(id, "data4", 1, TimeUnit.MILLISECONDS));
        Assert.assertTrue(queue.add(id, "data5", 1, TimeUnit.MILLISECONDS));
        // 通道队列长度为5，所以这个添加会失败
        Assert.assertFalse(queue.add(id, "data6", 1, TimeUnit.MILLISECONDS));

        Assert.assertNotNull(queue.take());
        // 因为指定了并发数是1，上边已经获取了一个数据并且没有表示消费完毕，这里无法获取
        Assert.assertNull(queue.take(1, TimeUnit.MILLISECONDS));
        queue.consumed(id);
        Assert.assertNotNull(queue.take(1, TimeUnit.MILLISECONDS));
    }
}
