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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务描述
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDescriptor {

    /**
     * 任务ID
     */
    private String id;

    /**
     * 任务执行时间间隔，单位毫秒，执行间隔从上一次执行完成后开始计算；
     */
    private long executeInterval;

    /**
     * 任务执行器
     */
    private Runnable task;

    /**
     * 该任务是否要合并调度,为空表示使用调度器的默认值;
     */
    private Boolean mergeScheduler;

    public TaskDescriptor(final String id, final long executeInterval, final Runnable task) {
        this(id, executeInterval, task, null);
    }
}
