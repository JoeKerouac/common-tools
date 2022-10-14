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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.lock.LockTaskUtil;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * 并发安全的实现;
 * 
 * 注意：如果{@link #take()}和{@link #removeChannel(Object)}并发执行，有可能存在通道已经被删除了，但是take仍然会将对应通道的数据返回
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class MultiChannelQueueImpl<ID, T> implements MultiChannelQueue<ID, T> {

    /**
     * 存放所有通道
     */
    private final Map<ID, ChannelEntry<T>> map;

    /**
     * 全局令牌，用于全局控速
     */
    private final Semaphore semaphore;

    /**
     * 全局队列，主要为了数据消费的公平，使整个队列是FIFO的，list会经常从head删除，往tail添加，所以这里使用LinkedList
     */
    private final LinkedList<Pair<ID, T>> list;

    /**
     * 全局锁，用于操作{@link #list}的时候加锁，同时也是为了获取下边的condition
     */
    private final Lock lock;

    /**
     * 条件，添加数据和释放令牌的时候会触发通知，删除数据的时候会消费通知
     */
    private final Condition condition;

    public MultiChannelQueueImpl(int maxConcurrency) {
        this.map = new ConcurrentHashMap<>();
        this.semaphore = new Semaphore(maxConcurrency);
        // 如无必要，请勿修改这个list的实现类，因为这个list会频繁随机删除（不一定是删除头部或者尾部），所以采用LinkedList
        this.list = new LinkedList<>();
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
    }

    @Override
    public boolean addChannel(ID id, int size, int maxConcurrency) {
        Assert.argNotNull(id, "id");
        Assert.assertTrue(size > 0, "size必须大于0", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.assertTrue(maxConcurrency > 0, "maxConcurrency必须大于0",
            ExceptionProviderConst.IllegalArgumentExceptionProvider);

        // 初始化通道节点
        ChannelEntry<T> entry = new ChannelEntry<>();
        // 通道内元素正常情况下都是从head开始删除的
        entry.list = new LinkedList<>();
        entry.lock = new ReentrantLock();
        entry.condition = entry.lock.newCondition();
        entry.size = size;
        entry.token = new Semaphore(maxConcurrency);

        return map.putIfAbsent(id, entry) == null;
    }

    @Override
    public List<T> removeChannel(ID id) {
        ChannelEntry<T> remove = map.remove(id);
        if (remove == null) {
            return Collections.emptyList();
        } else {
            return remove.list;
        }
    }

    @Override
    public Pair<ID, T> take() throws InterruptedException {
        return take0(0, TimeUnit.MILLISECONDS);
    }

    @Override
    public Pair<ID, T> take(long timeout, TimeUnit unit) throws InterruptedException {
        Assert.assertTrue(timeout > 0, "timeout必须大于0", ExceptionProviderConst.IllegalArgumentExceptionProvider);
        Assert.argNotNull(unit, "unit");
        return take0(timeout, unit);
    }

    /**
     * 从队列中获取通道并发数还未达到最大并且较早放入的数据
     * 
     * @param timeout
     *            超时时间，小于等于0表示没有超时判断
     * @param unit
     *            时间单位，不能为null
     * @return 通道数据，如果指定了超时时间则可能返回null
     * @throws InterruptedException
     *             中断异常
     */
    private Pair<ID, T> take0(final long timeout, final TimeUnit unit) throws InterruptedException {
        // 需要先申请全局令牌，注意，申请令牌的时候不要加锁
        semaphore.acquire();

        // 结束时间
        long end = System.currentTimeMillis() + unit.toMillis(timeout);
        try {
            return LockTaskUtil.runInterruptedTaskWithLock(lock, () -> {
                while (true) {
                    Pair<ID, T> pair = null;

                    Iterator<Pair<ID, T>> iterator = list.iterator();

                    // 这里要有序从头到尾遍历list，确保公平
                    while (iterator.hasNext()) {
                        Pair<ID, T> p = iterator.next();

                        ChannelEntry<T> entry = map.get(p.getKey());
                        // 尝试申请通道的令牌，申请到就消费这个通道的这个数据
                        if (entry.token.tryAcquire()) {
                            pair = p;
                            // 直接删除
                            iterator.remove();
                            break;
                        }

                    }

                    // pair不等于null的时候说明成功获取到数据了，否则说明当前没有可获取的数据（可能是通道并发都达到上限导致虽然有数据但是无法获取），继续获取
                    if (pair == null) {
                        // timeout大于0，表示指定了超时时间，否则表示永久等待
                        if (timeout > 0) {
                            // 最长等待时间
                            long waitTime = end - System.currentTimeMillis();

                            // 如果当前已经没有等待时间了，直接返回null
                            if (waitTime <= 0) {
                                return null;
                            }

                            // 等待下一次通知，在队列增加数据、释放令牌的时候会通知
                            if (!condition.await(waitTime, TimeUnit.MILLISECONDS)) {
                                // 如果等到超时也没有等来通知，则直接返回null
                                return null;
                            }
                            // 等到了通知，继续循环
                        } else {
                            condition.await();
                        }
                    } else {
                        ChannelEntry<T> entry = map.get(pair.getKey());
                        // 如果这个entry已经被删除了（并发调用了删除通道）
                        if (entry != null) {
                            // 从指定队列中移除
                            Assert.assertTrue(entry.remove(pair.getValue()), "未预期异常，这里应该删除成功的",
                                ExceptionProviderConst.IllegalStateExceptionProvider);
                        }

                        return pair;
                    }
                }
            });
        } catch (InterruptedException e) {
            semaphore.release();
            throw e;
        }
    }

    @Override
    public void add(ID id, T data) throws InterruptedException {
        add0(id, data, 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean add(ID id, T data, long timeout, TimeUnit unit) throws InterruptedException {
        Assert.assertTrue(timeout > 0, ExceptionProviderConst.IllegalArgumentExceptionProvider);

        return add0(id, data, timeout, unit);
    }

    /**
     * 往通道中增加数据，如果通道队列已满则阻塞等待直到可以放入或者等到超时返回false
     * 
     * @param id
     *            id
     * @param data
     *            要放入的数据，不能为null
     * @param timeout
     *            超时时间，小于等于0的时候表示不用超时判断
     * @param unit
     *            时间单位
     * @return 如果到超时时间还没有放入成功则返回false，放入成功返回true
     * @throws InterruptedException
     *             中断异常
     */
    private boolean add0(ID id, T data, long timeout, TimeUnit unit) throws InterruptedException {
        Assert.argNotNull(id, "id");
        Assert.argNotNull(data, "data");
        Assert.argNotNull(unit, "unit");

        ChannelEntry<T> entry = map.get(id);

        if (entry == null) {
            throw new IllegalStateException("当前ID还未初始化队列,id:" + id);
        }

        // 先往通道自己的队列中添加
        if (entry.add(data, timeout, unit)) {
            // 全局队列添加数据，注意要加锁，因为list不是线程安全的变量，并且我们要调用condition
            LockTaskUtil.runInterruptedTaskWithLock(lock, () -> {
                list.add(new Pair<>(id, data));
                // 通知数据变化
                condition.signalAll();
            });
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean remove(ID id, T data) {
        ChannelEntry<T> entry = map.get(id);

        if (entry == null) {
            return false;
        }

        // 这里要加锁，要把两个数据一起移除
        return LockTaskUtil.runWithLock(lock, () -> {
            list.remove(new Pair<>(id, data));
            return entry.remove(data);
        });
    }

    @Override
    public void consumed(ID id) {
        Assert.argNotNull(id, "id");

        // 无论如何，先释放全局令牌
        semaphore.release();

        ChannelEntry<T> entry = map.get(id);
        // 如果通道节点不存在，可能是被删除了，不用管
        if (entry != null) {
            entry.token.release();
            // 通知，有令牌被释放了，只有令牌真的释放才会通知，既然该通道已经不存在了，那就不通知了，因为即使通知了后边也找不到该通道的数据了
            LockTaskUtil.runWithLock(lock, condition::signalAll);
        }
    }

    @Override
    public void clear() {
        LockTaskUtil.runWithLock(lock, () -> {
            map.values().forEach(entry -> entry.list.clear());
            map.clear();
            list.clear();
        });
    }

    private static class ChannelEntry<T> {

        /**
         * 通道的队列
         */
        List<T> list;

        /**
         * 对{@link #list}操作加的锁
         */
        Lock lock;

        /**
         * 删除{@link #list}中的数据的时候通知，往{@link #list}中添加数据的时候消费，用于限制list长度
         */
        Condition condition;

        /**
         * 指定{@link #list}的最大长度
         */
        int size;

        /**
         * 令牌
         */
        Semaphore token;

        /**
         * 往通道中增加数据，如果通道队列已满则阻塞等待直到可以放入或者等到超时返回false
         * 
         * @param data
         *            要放入的数据，不能为null
         * @param timeout
         *            超时时间，小于等于0的时候表示不用超时判断
         * @param unit
         *            时间单位
         * @return 如果到超时时间还没有放入成功则返回false，放入成功返回true
         * @throws InterruptedException
         *             中断异常
         */
        boolean add(T data, long timeout, TimeUnit unit) throws InterruptedException {
            long end = System.currentTimeMillis() + unit.toMillis(timeout);

            return LockTaskUtil.runInterruptedTaskWithLock(lock, () -> {
                while (true) {
                    if (list.size() >= size) {
                        long waitTime = end - System.currentTimeMillis();

                        // 如果等到了超时，直接返回false
                        if (waitTime <= 0 || !condition.await(waitTime, TimeUnit.MILLISECONDS)) {
                            return false;
                        }
                    } else {
                        list.add(data);
                        return true;
                    }
                }
            });
        }

        /**
         * 删除指定数据
         * 
         * @param data
         *            要删除的数据
         * @return true表示删除成功
         */
        boolean remove(T data) {
            return LockTaskUtil.runWithLock(lock, () -> {
                if (list.remove(data)) {
                    condition.signalAll();
                    return true;
                } else {
                    return false;
                }
            });
        }
    }

}
