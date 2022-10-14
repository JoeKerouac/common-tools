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
package com.github.joekerouac.common.tools.concurrent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class StateFutureTest {

    @Test
    public void testNormal() throws Exception {
        // 测试正常完成
        Object result = new Object();
        StateFuture<Object> stateFuture = new StateFuture<>();
        stateFuture.startRun();

        CountDownLatch countDownLatch = new CountDownLatch(3);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicBoolean done = new AtomicBoolean(false);

        FutureCallback<Object> futureCallback = new FutureCallback<Object>() {
            @Override
            public void success(Object r) {
                counter.incrementAndGet();
                Assert.assertSame(result, r);
                countDownLatch.countDown();
            }

            @Override
            public void failed(Throwable ex) {

            }

            @Override
            public void cancelled() {

            }
        };
        new Thread(() -> {
            try {
                stateFuture.get();
                // 正常执行完毕
                done.set(true);
                countDownLatch.countDown();
            } catch (Throwable ex) {

            }

        }).start();

        // 完成前添加回调
        stateFuture.addCallback(futureCallback);
        stateFuture.done(result);
        // 完成后添加回调
        stateFuture.addCallback(futureCallback);

        // 正常情况下这里不用等
        Assert.assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS));
        // 验证回调没有重复执行
        Assert.assertEquals(counter.get(), 2);
        // 验证同步等待执行完毕
        Assert.assertTrue(done.get());
    }

    @Test
    public void testException() throws Exception {
        // 测试异常
        Throwable throwable = new RuntimeException();
        StateFuture<Object> stateFuture = new StateFuture<>();
        stateFuture.startRun();

        CountDownLatch countDownLatch = new CountDownLatch(3);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicBoolean done = new AtomicBoolean(false);

        FutureCallback<Object> futureCallback = new FutureCallback<Object>() {
            @Override
            public void success(Object r) {

            }

            @Override
            public void failed(Throwable ex) {
                counter.incrementAndGet();
                Assert.assertSame(throwable, ex);
                countDownLatch.countDown();
            }

            @Override
            public void cancelled() {

            }
        };

        new Thread(() -> {
            try {
                stateFuture.get();
            } catch (ExecutionException e) {
                // 执行异常时应该同步抛出异常
                Assert.assertSame(e.getCause(), throwable);
                done.set(true);
                countDownLatch.countDown();
            } catch (Throwable ex) {

            }

        }).start();

        // 完成前添加回调
        stateFuture.addCallback(futureCallback);
        stateFuture.exception(throwable);
        // 完成后添加回调
        stateFuture.addCallback(futureCallback);

        // 正常情况下这里不用等
        Assert.assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS));
        // 验证回调没有重复执行
        Assert.assertEquals(counter.get(), 2);
        // 验证同步等待执行完毕
        Assert.assertTrue(done.get());
    }

    @Test
    public void testCancel() throws Exception {
        // 测试cancel
        StateFuture<Object> stateFuture = new StateFuture<>();
        stateFuture.startRun();

        CountDownLatch countDownLatch = new CountDownLatch(3);
        AtomicInteger counter = new AtomicInteger(0);
        AtomicBoolean done = new AtomicBoolean(false);

        FutureCallback<Object> futureCallback = new FutureCallback<Object>() {
            @Override
            public void success(Object r) {

            }

            @Override
            public void failed(Throwable ex) {

            }

            @Override
            public void cancelled() {
                counter.incrementAndGet();
                countDownLatch.countDown();
            }
        };

        new Thread(() -> {
            try {
                stateFuture.get();
            } catch (CancellationException e) {
                // 取消后正常应该抛出异常的
                done.set(true);
                countDownLatch.countDown();
            } catch (Throwable throwable) {

            }

        }).start();

        // 完成前添加回调
        stateFuture.addCallback(futureCallback);
        stateFuture.cancel(true);
        // 完成后添加回调
        stateFuture.addCallback(futureCallback);

        // 正常情况下这里不用等
        Assert.assertTrue(countDownLatch.await(1000, TimeUnit.MILLISECONDS));
        // 验证回调没有重复执行
        Assert.assertEquals(counter.get(), 2);
        // 验证同步等待执行完毕
        Assert.assertTrue(done.get());
    }

}
