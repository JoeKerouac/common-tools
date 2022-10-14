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
package com.github.joekerouac.common.tools.log;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.github.joekerouac.common.tools.exception.ExceptionUtil;
import com.github.joekerouac.common.tools.util.JsonUtil;

/**
 * 辅助打印日志使用，端点日志服务，注意，该对象是非线程安全的，一次请求创建一个
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class EndpointLogService {

    /**
     * 请求端点，对于http来说这个是http path
     */
    private static final String ENDPOINT = "_endpoint";

    /**
     * 请求参数
     */
    private static final String PARAMS = "_params";

    /**
     * 请求接收时间
     */
    private static final String ACCEPT_TIME = "_acceptTime";

    /**
     * 请求开始处理的时间
     */
    private static final String PROCESS_TIME = "_processTime";

    /**
     * 请求结束时间
     */
    private static final String FINISH_TIME = "_finishTime";

    /**
     * 请求使用时间，等于结束时间减去接收时间
     */
    private static final String USED_TIME = "_usedTime";

    /**
     * 请求是否成功，true表示成功
     */
    private static final String SUCCESS = "_success";

    /**
     * 请求结果
     */
    private static final String RESULT = "_result";

    /**
     * 请求结果编码
     */
    private static final String RESULT_CODE = "_resultCode";

    /**
     * 执行异常
     */
    private static final String THROWABLE = "_ex";

    /**
     * 附加对象
     */
    private static final String CONTEXT = "_context";

    /**
     * 日志对象
     */
    private final Logger logger;

    /**
     * 数据
     */
    private final Map<String, Object> data;

    /**
     * 附加上下文
     */
    private final Map<String, Object> context;

    /**
     * 缓存的接收请求时间，方便后边计算
     */
    private final long acceptTime;

    /**
     * 是否结束
     */
    private volatile boolean finish;

    public EndpointLogService(final String endpoint, final Object params, final Logger logger) {
        this.data = new HashMap<>();
        this.context = new HashMap<>();
        this.data.put(CONTEXT, context);
        this.logger = logger;
        this.acceptTime = System.currentTimeMillis();
        this.finish = false;
        data.put(ENDPOINT, endpoint);
        data.put(PARAMS, params);
        data.put(ACCEPT_TIME, acceptTime);
        data.put(PROCESS_TIME, acceptTime);
    }

    /**
     * 真正开始处理时调用一次，请勿重复调用
     */
    public void beginProcess() {
        // 只有未初始化时
        data.put(PROCESS_TIME, System.currentTimeMillis());
    }

    /**
     * 添加日志上下文
     * 
     * @param key
     *            key
     * @param value
     *            value
     */
    public void addContext(String key, Object value) {
        context.put(key, value);
    }

    /**
     * 结束时调用，请勿重复调用
     * 
     * @param result
     *            结果
     * @param code
     *            结果码
     * @param throwable
     *            异常，如果异常不为空则认为处理失败
     */
    public void finish(Object result, String code, Throwable throwable) {
        finish(result, code, throwable, throwable == null);
    }

    /**
     * 结束时调用，请勿重复调用
     *
     * @param result
     *            结果
     * @param code
     *            结果码
     * @param throwable
     *            异常
     * @param success
     *            显示声明是否成功
     */
    public void finish(Object result, String code, Throwable throwable, boolean success) {
        if (finish) {
            return;
        }
        finish = true;
        long finishTime = System.currentTimeMillis();
        data.put(SUCCESS, success);
        data.put(FINISH_TIME, finishTime);
        data.put(USED_TIME, finishTime - acceptTime);
        data.put(RESULT, result);
        data.put(RESULT_CODE, code);

        if (throwable != null) {
            data.put(THROWABLE, ExceptionUtil.printStack(throwable));
        }

        String msg = new String(JsonUtil.write(data), Charset.defaultCharset());
        logger.info(msg);
    }

}
