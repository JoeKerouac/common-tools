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

import java.lang.ref.WeakReference;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.reflect.ReflectUtil;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class SafeThreadLocalTest {

    @Test
    public void gcTest() throws Exception {
        // 注意，这个测试用例一定要最先执行，因为后续的测试用例可能会污染SafeThreadLocal导致最终的判断失败
        // 测试内存回收，GC后ThreadLocal应该释放内存了
        SafeThreadLocal<Object> threadLocal = new SafeThreadLocal<>();
        Object obj = new Object();

        run(threadLocal, obj);

        // 主动调用GC，这种操作不太稳定，但是只能这么操作验证
        System.gc();
        System.gc();
        Thread.sleep(1000);

        // 反射获取SafeThreadLocal中的内容，主要是为了验证内存是否回收
        Map<WeakReference<Thread>, Map<SafeThreadLocal<?>, Object>> context =
            ReflectUtil.getFieldValue(SafeThreadLocal.class, "CONTEXT");
        Assert.assertNotNull(context, "SafeThreadLocal内部实现可能发生变化了，请修改测试用例");
        Assert.assertTrue(context.isEmpty());
    }

    @Test(dependsOnMethods = "gcTest")
    public void baseTest() {
        // 基础测试，测试功能正确性

        SafeThreadLocal<Object> threadLocal = new SafeThreadLocal<>();
        Object obj = new Object();
        threadLocal.set(obj);
        Assert.assertEquals(threadLocal.get(), obj);
        threadLocal.remove();
        Assert.assertNull(threadLocal.get());
    }

    private void run(SafeThreadLocal<Object> threadLocal, Object obj) throws Exception {
        Thread thread = new Thread(() -> threadLocal.set(obj));
        thread.start();
        thread.join();
    }

}
