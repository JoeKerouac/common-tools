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
package com.github.joekerouac.common.tools.scheduler;

/**
 * 调度任务
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public interface SchedulerTask {

    /**
     * 启动任务
     */
    void start();

    /**
     * 停止任务
     */
    void stop();

    /**
     * 立即触发一次任务
     */
    void scheduler();

    /**
     * 本调度任务的调度间隔（上次任务结束到下次任务开始）
     * 
     * @return 调度间隔，单位毫秒
     */
    long fixedDelay();

    /**
     * 设置调度间隔
     * 
     * @param fixedDelay
     *            调度间隔，毫秒，启动前设置立即生效，启动后设置在下次调度生效
     */
    void setFixedDelay(long fixedDelay);

    /**
     * 初始调度延迟
     * 
     * @return 初始调度延迟，单位毫秒
     */
    long initialDelay();

    /**
     * 设置初始调度延迟
     * 
     * @param initialDelay
     *            初始调度延迟，单位毫秒，启动前设置有效
     */
    void setInitialDelay(long initialDelay);

}
