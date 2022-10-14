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

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.joekerouac.common.tools.util.Assert;

import lombok.CustomLog;

/**
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@CustomLog
public class ReferenceUtils {

    /**
     * 销毁引用队列
     */
    private static final ReferenceQueue<Object> QUEUE = new ReferenceQueue<>();

    /**
     * 回调MAP
     */
    private static final Map<Reference<?>, Runnable> CALLBACK_MAP = new ConcurrentHashMap<>();

    static {
        Thread thread = new Thread(() -> {
            Reference<?> lastRef = null;
            while (true) {
                try {
                    if (lastRef != null) {
                        Reference<?> ref = lastRef;
                        lastRef = null;

                        Runnable callback = CALLBACK_MAP.get(ref);
                        if (callback != null) {
                            try {
                                callback.run();
                            } catch (Throwable e) {
                                LOGGER.warn(e, "回调[{}]执行异常，忽略", callback);
                            }
                        }
                    } else {
                        lastRef = QUEUE.remove(1000);
                    }
                } catch (Throwable throwable) {
                    LOGGER.warn(throwable, "线程销毁监听线程异常，忽略");
                }
            }

        }, "对象销毁监听线程");
        thread.setDaemon(true);
        thread.setContextClassLoader(null);
        thread.start();
    }

    /**
     * 监听指定对象的销毁
     * 
     * @param obj
     *            要监听的对象
     * @param callback
     *            对象销毁后的回调，注意，请勿在回调中直接或者间接引用监听的对象！！！另外需要注意内部会缓存callback直到callback执行完毕， 这个可能会导致外部无法正确回收callback的class对象；
     */
    public static <T> void listenDestroy(T obj, Runnable callback) {
        // 注意，ListenerWeakReference不能丢，不然就触发不了回调了
        Assert.argNotNull(callback, "callback");

        CALLBACK_MAP.put(new PhantomReference<>(obj, QUEUE), callback);
    }
}
