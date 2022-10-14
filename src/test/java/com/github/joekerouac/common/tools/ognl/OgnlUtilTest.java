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

import java.util.*;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class OgnlUtilTest {

    @Test
    public void invokeMethodTest() {
        Map<Object, Object> context = new HashMap<>();
        context.put("a", 1);
        context.put("b", 2);

        // 测试调用root的成员方法
        {
            // 使用context中的两个数字调用root对象的add方法
            Object expression = OgnlUtil.parseExpression("add(#a, #b)");
            // 注意，这里root要传入Add类的对象
            Object result = OgnlUtil.exec(expression, context, new Add());
            Assert.assertEquals(result, 3);
        }

        // 测试调用静态方法
        {
            // 使用context中的两个数字调用root对象的add方法
            Object expression = OgnlUtil.parseExpression("@" + OgnlUtilTest.class.getName() + "@add(#a, #b)");
            Object result = OgnlUtil.exec(expression, context, context);
            Assert.assertEquals(result, 3);
        }
    }

    @Test(dataProvider = "datasource")
    public void baseTest(String expressionStr, Object expectResult, Map<Object, Object> context, Object root) {
        // 基本表达式测试，数据源参照注解定义

        Object expression = OgnlUtil.parseExpression(expressionStr);
        Object exec = OgnlUtil.exec(expression, context, root);
        Assert.assertEquals(exec, expectResult);
    }

    @DataProvider
    public Object[][] datasource() {
        // #root固定引用root对象（不是context的root）
        // context中的对象使用#引用，root对象中的字段不用#引用；

        // 作为root对象使用
        User rootUser = new User("JoeKerouac", 20);
        // 作为context中的user对象
        User contextUser = new User("context", 1);
        Map<String, Object> root = Collections.singletonMap("user", rootUser);
        Map<Object, Object> ognlContext = Collections.singletonMap("user", contextUser);

        Map<String, Object> map = new HashMap<>();
        map.put("123", "100");
        map.put("age", 100);
        map.put("id1", "abc");
        map.put("id2", "abc");

        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");

        return new Object[][] {
            // 从context中取值
            {"#user", contextUser, ognlContext, root},
            //
            {"#user.name", contextUser.getName(), ognlContext, root},
            //
            {"#user.age", contextUser.getAge(), ognlContext, root},

            //
            {"user", rootUser, ognlContext, root},
            //
            {"user.name", rootUser.getName(), ognlContext, root},
            //
            {"user.age", rootUser.getAge(), ognlContext, root},

            // 使用中括号从map中取值
            {"['user']", rootUser, ognlContext, root},
            //
            {"['user'].name", rootUser.getName(), ognlContext, root},
            //
            {"['user'].age", rootUser.getAge(), ognlContext, root},

            // #root引用的是根对象
            {"#root.user", rootUser, ognlContext, root},
            //
            {"#root.user.name", rootUser.getName(), ognlContext, root},
            //
            {"#root.user.age", rootUser.getAge(), ognlContext, root},

            // 数字key必须要用中括号，并且使用单引号表示这是一个数字，同时需要注意，因为123对应的是字符串，所以这里实际上是字符串相加
            {"['123'] + 10", "10010", ognlContext, map},
            // 英文key可以不用中括号，甚至不用单引号
            {"age + 10", 110, ognlContext, map},
            // 比较两个key对应的value的值
            {"id1 eq id2", true, ognlContext, map},
            // 比较key对应的value和指定value，注意，abc是字符串常量，在这里需要使用单引号
            {"id1 eq 'abc'", true, ognlContext, map},

            // 取list的第一个数据
            {"[0]", list.get(0), ognlContext, list},
            // 取list第二个数据
            {"[1]", list.get(1), ognlContext, list},
            // 取list的size
            {"size()", list.size(), ognlContext, list},
            // 取list的size
            {"#root.size", list.size(), ognlContext, list},
            // 取list的size
            {"#root.size()", list.size(), ognlContext, list},
            // 调用String.startWith方法
            {"id1.startsWith('ab')", true, ognlContext, map},
            // 调用String.startWith方法
            {"id1.startsWith('ac')", false, ognlContext, map},

        };
    }

    private static int add(int arg0, int arg1) {
        return arg0 + arg1;
    }

    private static class Add {

        private int add(int arg0, int arg1) {
            return arg0 + arg1;
        }

    }

}
