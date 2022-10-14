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

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 多通道限速队列，控制多通道数据消费不能超过各自限速，同时总体不能超速，场景如下： <br/>
 * <br/>
 * 我们有多个隔离通道，每个通道都有一个自己的队列，队列中存放的是一些数据，有一个消费端，会从通道中取出数据并消费，但是我们要控制并发消费量，只有
 * 前边的数据消费完毕才能拉取新的数据消费（一般是消费端可能消费能力不足，而数据是我们这边主动推过去的，为防止消费端挂掉所以做一个限速），同时我们
 * 还想要对全局有一个限速，防止把自己搞挂了，而且我们可能随时会往队列添加待消费数据，需要消费端能实时感知到，这个场景是比较复杂的，实现起来也很困 难，而该队列就对该问题提供了一种高效的解决方案；
 *
 * <br/>
 * <br/>
 * 注意：对于指定通道，其队列中不允许有重复的数据
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public interface MultiChannelQueue<ID, T> {

    /**
     * 添加一个通道
     * 
     * @param id
     *            通道ID
     * @param size
     *            通道队列最大长度
     * @param maxConcurrency
     *            通道最大的并发量
     * @return 返回true表示添加成功，返回false表示通道已经存在，添加失败
     */
    boolean addChannel(ID id, int size, int maxConcurrency);

    /**
     * 删除一个通道，并将通道内的所有数据返回
     * 
     * @param id
     *            通道ID
     * @return 通道内的所有数据，如果为空表示通道内不存在数据或者数据消费完毕
     */
    List<T> removeChannel(ID id);

    /**
     * 阻塞获取一个数据，并将其从内部删除，同时会占用指定队列的令牌，该数据使用完毕后需要手动调用{@link #consumed(Object)}释放令牌；
     * 
     * @return 获取到的数据，肯定不是null
     * @throws InterruptedException
     *             如果阻塞过程中被中断将会抛出该异常
     */
    Pair<ID, T> take() throws InterruptedException;

    /**
     * 阻塞获取一个数据，并将其从内部删除，同时会占用指定队列的令牌，该数据使用完毕后需要手动调用{@link #consumed(Object)}释放令牌；
     * 
     * @param timeout
     *            超时时间，必须大于0
     * @param unit
     *            时间单位
     * @return 获取到的数据，超时返回null
     * @throws InterruptedException
     *             如果阻塞过程中被中断将会抛出该异常
     */
    Pair<ID, T> take(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * 阻塞添加一个数据
     * 
     * @param id
     *            对应的队列ID
     * @param data
     *            要添加的数据
     * @throws InterruptedException
     *             InterruptedException
     */
    void add(ID id, T data) throws InterruptedException;

    /**
     * 阻塞添加一个数据
     * 
     * @param id
     *            对应的队列ID
     * @param data
     *            要添加的数据
     * @param timeout
     *            超时时间，必须大于0
     * @param unit
     *            时间单位
     * @throws InterruptedException
     *             InterruptedException
     */
    boolean add(ID id, T data, long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * 移除指定通道的指定数据
     * 
     * @param id
     *            通道ID
     * @param data
     *            要移除的数据
     * @return 返回true表示移除成功，返回false表示移除失败，可能是通道不存在，也可能是指定数据不存在
     */
    boolean remove(ID id, T data);

    /**
     * 消费端调用，通知指定通道消费完一个数据
     * 
     * @param id
     *            通道ID
     */
    void consumed(ID id);

    /**
     * 清空所有通道所有数据
     */
    void clear();
}
