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
package com.github.joekerouac.common.tools.db;

import java.sql.SQLException;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.joekerouac.common.tools.collection.Pair;
import com.github.joekerouac.common.tools.enums.DBType;
import com.github.joekerouac.common.tools.exception.ExceptionUtil;

/**
 * 注意，如果当前使用的不是MySQL数据库，请使用{@link #switchDbType(DBType)}来切换（注意使用完毕后切换回原来的类型）
 * 
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class SqlUtil {

    private static final ThreadLocal<DBType> DB_TYPE = new ThreadLocal<>();

    /**
     * 切换当前线程SqlUtil使用的db类型，sql util将基于该类型做判断
     * 
     * @param type
     *            类型
     */
    public static void switchDbType(DBType type) {
        DB_TYPE.set(type);
    }

    /**
     * 获取当前线程的DBType
     * 
     * @return 当前线程的DBType
     */
    public static DBType getDbType() {
        return DB_TYPE.get();
    }

    public static void clearDbType() {
        DB_TYPE.remove();
    }

    /**
     * 锁定指定资源
     * 
     * @param supplier
     *            锁定函数，里边应该是for update no wait sql的函数
     * @param <T>
     *            结果实际类型
     * @return 如果锁定失败时返回的key是false
     */
    public static <T> Pair<Boolean, T> lockNoWait(Supplier<T> supplier) {
        return lockNoWait(supplier, null, false, null);
    }

    /**
     * 锁定指定资源，如果锁定成功并且返回数据为null，则直接抛出异常
     *
     * @param supplier
     *            锁定函数，里边应该是for update no wait sql的函数
     * @param exceptionProvider
     *            异常提供器，返回数据为null时使用该异常提供器抛出异常
     * @param <T>
     *            结果实际类型
     * @return 如果锁定失败时返回的key是false
     */
    public static <T> Pair<Boolean, T> lockNoWait(Supplier<T> supplier, Supplier<RuntimeException> exceptionProvider) {
        return lockNoWait(supplier, exceptionProvider, true, null);
    }

    /**
     * 锁定指定资源，如果返回null则根据入参决定是否抛出异常
     *
     * @param supplier
     *            锁定函数，里边应该是for update no wait sql的函数
     * @param exceptionProvider
     *            异常提供器，返回数据为null并且throwIfNull为true时使用该异常提供器抛出异常
     * @param throwIfNull
     *            返回数据为null时是否抛出异常
     * @param lockExceptionDecide
     *            决策sql异常是不是因为锁失败导致的，如果是，则返回true，否则返回false，如果返回false，异常将被原封不动抛出，请注意处理；允
     *            许为null，为null时使用默认逻辑处理，将认为当前数据库是MySQL；
     * @param <T>
     *            结果实际类型
     * @return 如果锁定失败时返回的key是false
     */
    public static <T> Pair<Boolean, T> lockNoWait(Supplier<T> supplier, Supplier<RuntimeException> exceptionProvider,
        boolean throwIfNull, Function<SQLException, Boolean> lockExceptionDecide) {
        T result;
        try {
            result = supplier.get();
        } catch (RuntimeException exception) {
            Throwable rootCause = ExceptionUtil.getRootCause(exception);
            Boolean lockErr = false;
            if (rootCause instanceof SQLException) {
                if (lockExceptionDecide == null) {
                    lockErr = causeForUpdateNowaitError((SQLException)rootCause);
                } else {
                    lockErr = lockExceptionDecide.apply((SQLException)rootCause);
                }
            }
            if (lockErr == Boolean.TRUE) {
                return new Pair<>(false, null);
            }

            // 这里new一个异常，主要是为了用户处理异常逻辑出问题时准确定位异常栈
            throw new RuntimeException(exception);
        }

        if (result == null && throwIfNull) {
            throw exceptionProvider.get();
        }

        return new Pair<>(true, result);
    }

    /**
     * 判断是否是因为数据库锁失败导致的异常
     *
     * @param sqlException
     *            异常
     * @return 如果是因为锁失败导致的异常则返回true，否则返回false
     */
    public static boolean causeForUpdateNowaitError(SQLException sqlException) {
        DBType dbType = DB_TYPE.get();
        dbType = dbType == null ? DBType.MySQL : dbType;
        return dbType.isCannotAcquireLock(sqlException.getErrorCode());
    }

    /**
     * 判断是否是因为主键冲突导致的异常
     * 
     * @param sqlException
     *            异常
     * @return true表示是因为主键冲突导致的异常
     */
    public static boolean causeDuplicateKey(SQLException sqlException) {
        DBType dbType = DB_TYPE.get();
        dbType = dbType == null ? DBType.MySQL : dbType;
        return dbType.isDuplicateKey(sqlException.getErrorCode());
    }

}
