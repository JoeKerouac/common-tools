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
package com.github.joekerouac.common.tools.file.monitor;

import java.io.File;
import java.nio.file.WatchEvent;

/**
 * 文件监控回调
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface FileMonitorCallback {

    /**
     * 文件变更时触发
     * 
     * @param kind
     *            变更类型
     * @param file
     *            变更的文件
     */
    void call(WatchEvent.Kind<?> kind, File file);

    /**
     * 是否可以处理指定变更
     * 
     * @param kind
     *            变更类型
     * @return 返回true表示可以处理指定变更
     */
    boolean canDeal(WatchEvent.Kind<?> kind);

}
