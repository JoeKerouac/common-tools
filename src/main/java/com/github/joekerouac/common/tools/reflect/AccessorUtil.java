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
package com.github.joekerouac.common.tools.reflect;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 访问修饰符工具，判断访问修饰符
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccessorUtil {

    /**
     * 判断成员对象是否是transient
     * 
     * @param member
     *            成员对象
     * @return 返回true表示是transient
     */
    public static boolean isTransient(Member member) {
        return Modifier.isTransient(member.getModifiers());
    }

    /**
     * 判断成员对象是否是final的
     *
     * @param member
     *            成员对象
     * @return 返回true表示是final
     */
    public static boolean isFinal(Member member) {
        return Modifier.isFinal(member.getModifiers());
    }

    /**
     * 判断成员对象是否是public
     *
     * @param member
     *            成员对象
     * @return 返回true表示是public
     */
    public static boolean isPublic(Member member) {
        return Modifier.isPublic(member.getModifiers());
    }

    /**
     * 判断成员对象是否是protected
     *
     * @param member
     *            成员对象
     * @return 返回true表示是protected
     */
    public static boolean isProtected(Member member) {
        return Modifier.isProtected(member.getModifiers());
    }

    /**
     * 判断成员对象是否是private
     *
     * @param member
     *            成员对象
     * @return 返回true表示是protected
     */
    public static boolean isPrivate(Member member) {
        return Modifier.isPrivate(member.getModifiers());
    }

    /**
     * 判断成员对象是否是static
     * 
     * @param member
     *            成员对象
     * @return true表示是静态的
     */
    public static boolean isStatic(Member member) {
        return Modifier.isStatic(member.getModifiers());
    }

    /**
     * 判断成员对象是否是抽象的
     * 
     * @param member
     *            成员对象
     * @return 返回true表示是抽象的
     */
    public static boolean isAbstract(Member member) {
        return Modifier.isAbstract(member.getModifiers());
    }

    /**
     * 判断class是否是抽象的
     * 
     * @param clazz
     *            class
     * @return 返回true表示是抽象的
     */
    public static boolean isAbstract(Class<?> clazz) {
        return Modifier.isAbstract(clazz.getModifiers());
    }
}
