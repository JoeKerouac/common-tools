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
package com.github.joekerouac.common.tools.concurrent;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.joekerouac.common.tools.lock.LockTaskUtil;

import lombok.CustomLog;

/**
 * 状态机future
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@CustomLog
public class StateFuture<T> implements CallbackFuture<T> {

    /**
     * 初始化状态
     */
    private static final byte INIT = 0;

    /**
     * 执行完毕状态，包含成功和异常，不包含CANCEL状态
     */
    private static final byte COMPLETE = 1;

    /**
     * 取消状态
     */
    private static final byte CANCEL = 2;

    /**
     * 执行中状态
     */
    private static final byte RUNNING = 3;

    /**
     * 状态变更时加的锁
     */
    private final Lock lock;

    /**
     * 结束条件，执行结束（包含正常结束、执行异常、取消）的时候会发出通知
     */
    private final Condition doneCondition;

    /**
     * 待执行的回调
     */
    private final List<FutureCallback<T>> callbacks;

    /**
     * 执行结果
     */
    private volatile T obj;

    /**
     * 执行异常，如果不为空说明执行异常
     */
    private volatile Throwable throwable;

    /**
     * 状态，0表示初始，1表示完成，2表示cancel，4表示执行中，不支持cancel
     */
    private volatile byte status = 0;

    public StateFuture() {
        this.lock = new ReentrantLock();
        this.doneCondition = lock.newCondition();
        this.callbacks = new CopyOnWriteArrayList<>();
    }

    @Override
    public void addCallback(FutureCallback<T> callback) {
        if (callback(callback)) {
            return;
        }

        LockTaskUtil.runWithLock(lock, () -> {
            if (!callback(callback)) {
                callbacks.add(callback);
            }
        });
    }

    /**
     * 执行回调
     * 
     * @param callback
     *            回调
     * @return true表示回调执行，false表示当前不是终态，没有执行回调
     */
    private boolean callback(FutureCallback<T> callback) {
        if (this.status == COMPLETE) {
            int status;
            if (this.throwable != null) {
                status = 1;
                callback.failed(throwable);
            } else {
                status = 0;
                callback.success(obj);
            }
            callback.complete(obj, throwable, status);
            return true;
        } else if (this.status == CANCEL) {
            callback.cancelled();
            callback.complete(obj, throwable, 2);
            return true;
        }
        return false;
    }

    @Override
    public void removeCallback(FutureCallback<T> callback) {
        callbacks.remove(callback);
    }

    /**
     * 开始执行
     * 
     * @return 如果当前future是INIT状态，将会返回true，并将其状态置为完成，否则将什么都不做返回false
     */
    public boolean startRun() {
        return LockTaskUtil.runWithLock(lock, () -> {
            if (this.status != INIT) {
                return false;
            }

            this.status = RUNNING;
            return true;
        });
    }

    /**
     * 完成future
     * 
     * @param obj
     *            附带的数据
     * @return 如果当前future是RUNNING状态，将会返回true，并将其状态置为完成，否则将什么都不做返回false
     */
    public boolean done(T obj) {
        return LockTaskUtil.runWithLock(lock, () -> {
            if (this.status != RUNNING) {
                return false;
            }

            this.obj = obj;
            this.status = COMPLETE;
            doneCondition.signalAll();
            for (FutureCallback<T> callback : callbacks) {
                try {
                    callback(callback);
                } catch (Throwable throwable) {
                    LOGGER.error(throwable, "正常回调 [{}] 执行过程中发生异常", callback);
                }
            }
            return true;
        });
    }

    /**
     * 执行异常，结束
     * 
     * @param throwable
     *            异常
     * @return 如果当前future是RUNNING或者INIT状态，将会返回true，并将其状态置为完成，否则将什么都不做返回false
     */
    public boolean exception(Throwable throwable) {
        return LockTaskUtil.runWithLock(lock, () -> {
            // 执行过程中和初始化状态都可能超时异常
            if (this.status != RUNNING && this.status != INIT) {
                return false;
            }

            this.throwable = throwable;
            this.status = COMPLETE;
            doneCondition.signalAll();
            for (FutureCallback<T> callback : callbacks) {
                try {
                    callback(callback);
                } catch (Throwable ex) {
                    LOGGER.error(ex, "异常回调 [{}] 执行过程中发生异常", callback);
                }
            }
            return true;
        });
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return LockTaskUtil.runWithLock(lock, () -> {
            // 已经是结束状态，无法更新为cancel状态
            if (status == COMPLETE || status == CANCEL) {
                return false;
            } else {
                status = CANCEL;
                doneCondition.signalAll();
                for (FutureCallback<T> callback : callbacks) {
                    try {
                        callback(callback);
                    } catch (Throwable throwable) {
                        LOGGER.error(throwable, "cancel回调 [{}] 执行过程中发生异常", callback);
                    }
                }

                return true;
            }
        });
    }

    @Override
    public boolean isCancelled() {
        return status == CANCEL;
    }

    @Override
    public boolean isDone() {
        return status == COMPLETE || status == CANCEL;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        lock.lock();

        try {
            if (!isDone()) {
                // 理论上这里只要被唤醒，就肯定是结束了
                doneCondition.await();
            }

            return getNotWait();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        lock.lock();

        try {
            if (!isDone()) {
                if (doneCondition.await(timeout, unit)) {
                    throw new TimeoutException();
                }
            }

            return getNotWait();
        } finally {
            lock.unlock();
        }
    }

    private T getNotWait() throws ExecutionException {
        if (throwable != null) {
            throw new ExecutionException(throwable);
        }

        if (isCancelled()) {
            throw new CancellationException();
        }

        return obj;
    }
}
