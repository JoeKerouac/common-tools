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

/**
 * 文件变更监听服务
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface FileMonitorService {

    /**
     * 启动文件监听服务
     */
    void start();

    /**
     * 关闭文件监听服务
     */
    void stop();

    /**
     * 监听文件变更
     * 
     * @param filePath
     *            要监听的文件，不能是目录
     * @param callback
     *            监听回调
     * @param <T>
     *            回调实际类型
     * @return 添加监听是否成功，返回true表示添加成功，返回false表示当前已经存在该监听，无需重复添加
     */
    <T extends FileMonitorCallback> boolean addWatch(String filePath, T callback);

    /**
     * 移除指定callback
     * 
     * @param filePath
     *            文件路径
     * @param callback
     *            要删除的回调
     * @param <T>
     *            回调实际类型
     * @return 返回true表示删除成功，返回false表示当前没有指定回调
     */
    <T extends FileMonitorCallback> boolean removeWatch(String filePath, T callback);
}
