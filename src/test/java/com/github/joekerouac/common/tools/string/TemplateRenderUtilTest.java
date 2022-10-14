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
package com.github.joekerouac.common.tools.string;

import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.reflect.bean.BeanUtils;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class TemplateRenderUtilTest {

    @Test
    public void testRender() {
        String template = "用户姓名:${user.name},用户年龄:${user.age},用户性别:${user.sex};特殊情况渲染:[$$${user.name},{${{}},$}}]";
        Map<String, Object> context = BeanUtils.convertToPlaceholder(new User("JoeKerouac", 0, "man"), "user");
        String render = TemplateRenderUtil.render(template, context, true);

        Assert.assertEquals(render, "用户姓名:JoeKerouac,用户年龄:0,用户性别:man;特殊情况渲染:[$$JoeKerouac,{},$}}]");

        // 测试转义工作是否正常
        template = "\\${user.name}\\\\";
        Assert.assertEquals(TemplateRenderUtil.render(template, context, true), "${user.name}\\");
        template = "$\\{user.name}";
        Assert.assertEquals(TemplateRenderUtil.render(template, context, true), "${user.name}");
        template = "${user.name\\}";
        Assert.assertEquals(TemplateRenderUtil.render(template, context, true), "${user.name}");
        template = "\\${user.name}";
        Assert.assertEquals(TemplateRenderUtil.render(template, context, false), "\\JoeKerouac");
    }

    @Data
    @AllArgsConstructor
    private static class User {

        private String name;

        private int age;

        private String sex;
    }

}
