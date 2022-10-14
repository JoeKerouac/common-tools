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

import java.util.List;

/**
 * 多任务调度系统
 * 
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public interface SchedulerSystem {

    /**
     * 启动
     */
    void start();

    /**
     * 停止多任务调度系统，注意，停止任务调度系统后所有任务将会被清空
     */
    void stop();

    /**
     * 注册一个新任务，如果指定ID对应的任务已经存在，那么新任务将注册失败，并且将老任务返回
     *
     * @param taskDescriptor
     *            任务说明；
     * @return 如果存在老任务，那么将其返回，否则将我们系统的任务注册进系统
     */
    TaskDescriptor registerTask(TaskDescriptor taskDescriptor);

    /**
     * 移除指定任务
     *
     * @param id
     *            任务ID
     * @return 如果指定任务存在，那么将其移除并返回
     */
    TaskDescriptor removeTask(String id);

    /**
     * 获取当前调度系统中所有任务
     * 
     * @return 调度系统中的任务列表
     */
    List<TaskDescriptor> getAll();

    /**
     * 主动调度某个任务，如果要调度的任务不存在则抛出异常
     *
     * @param id
     *            任务ID
     */
    default void scheduler(String id) {
        scheduler(id, true);
    }

    /**
     * 主动调度某个任务
     *
     * @param id
     *            任务ID
     * @param throwIfTaskNotFound
     *            如果要调度的任务未找到是否抛出异常
     */
    void scheduler(String id, boolean throwIfTaskNotFound);

}
