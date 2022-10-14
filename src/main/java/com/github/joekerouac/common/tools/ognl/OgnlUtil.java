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
package com.github.joekerouac.common.tools.ognl;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.util.Map;

import com.github.joekerouac.common.tools.reflect.AccessorUtil;
import com.github.joekerouac.common.tools.reflect.ReflectUtil;

import ognl.ClassResolver;
import ognl.MemberAccess;
import ognl.Ognl;
import ognl.OgnlException;

/**
 * Ognl工具类，Ognl中关键字参考{@link ognl.OgnlParserConstants}，详细的语法可以参考 <a
 * href=https://commons.apache.org/proper/commons-ognl/language-guide.html>apache ognl 语法</a>
 * 
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class OgnlUtil {

    /**
     * 类成员变量访问控制
     */
    private static final MemberAccess MEMBER_ACCESS = new MemberAccess() {
        @Override
        public Object setup(final Map context, final Object target, final Member member, final String propertyName) {
            if (!AccessorUtil.isPublic(member) && member instanceof AccessibleObject) {
                ReflectUtil.allowAccess((AccessibleObject)member);
            }

            return null;
        }

        @Override
        public void restore(final Map context, final Object target, final Member member, final String propertyName,
            final Object state) {
            // 这个方法其实是要还原setup方法做的操作，例如访问限制，参数中的state就是setup方法返回的值，我们这里不做任何操作
        }

        @Override
        public boolean isAccessible(final Map context, final Object target, final Member member,
            final String propertyName) {
            return AccessorUtil.isPublic(member) || (member instanceof AccessibleObject);
        }
    };

    /**
     * 解析表达式
     *
     * @param expression
     *            表达式
     * @return 表达式解析对象
     */
    public static Object parseExpression(String expression) {
        try {
            return Ognl.parseExpression(expression);
        } catch (OgnlException e) {
            throw new com.github.joekerouac.common.tools.exception.OgnlException(e);
        }
    }

    /**
     * 执行表达式
     *
     * @param expression
     *            表达式
     * @param root
     *            root对象
     * @return 表达式执行结果
     */
    public static Object exec(Object expression, Object root) {
        return exec(expression, null, root);
    }

    /**
     * 执行表达式
     *
     * @param expression
     *            使用{@link #parseExpression(String)}解析出来的表达式，不允许为空
     * @param context
     *            上下文，可以为null
     * @param root
     *            root对象，不允许为空
     * @return 表达式执行结果
     */
    public static Object exec(Object expression, Map<Object, Object> context, Object root) {
        return exec(expression, context, root, null);
    }

    /**
     * 执行表达式，目前本地简单测试结果：调用一个简单的public method一秒大概能到160W的QPS
     *
     * @param expression
     *            使用{@link #parseExpression(String)}解析出来的表达式，不允许为空
     * @param context
     *            上下文，可以为null
     * @param root
     *            root对象，不允许为空
     * @param classResolverFunc
     *            类型查找函数，允许为空
     * @return 表达式执行结果
     */
    @SuppressWarnings("unchecked")
    public static Object exec(Object expression, Map<Object, Object> context, Object root,
        ClassResolverFunc classResolverFunc) {
        ClassResolver classResolver = classResolverFunc == null ? null : (classResolverFunc::findClass);
        Map<Object, Object> ognlContext = Ognl.createDefaultContext(null, MEMBER_ACCESS, classResolver, null);

        if (context != null) {
            ognlContext.putAll(context);
        }

        try {
            // 注意，从context中取数据需要使用#{}，从root中取数据直接写字段名（如果是）
            return Ognl.getValue(expression, ognlContext, root);
        } catch (OgnlException e) {
            throw new com.github.joekerouac.common.tools.exception.OgnlException(e);
        }
    }

}
