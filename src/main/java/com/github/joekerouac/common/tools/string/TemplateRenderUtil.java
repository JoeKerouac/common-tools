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

import com.github.joekerouac.common.tools.constant.StringConst;
import com.github.joekerouac.common.tools.reflect.bean.BeanUtils;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class TemplateRenderUtil {

    /**
     * 模板渲染
     * 
     * @param template
     *            模板，模板中的${xxx}将会被替换为context中xxx对应的值，${}这种没有任何内容的将会删除，${${xxx}}这种嵌套形式的模板将
     *            会被替换为context中key为${xxx}的值，如果没有则会置为空；暂时不支持collection、array等集合，只支持key、value格式的;
     * @param context
     *            渲染上下文，模板中可以引用上下文中的内容，可以使用 {@link BeanUtils#convertToPlaceholder(Object, String)} 函数将bean转换为上下文
     * @param enableBackslash
     *            是否允许转义符，true表示允许转义符，允许转义符后\$、\{、\}这三组字符将会被视为普通字符，开启后如果需要输出\则需要再模板中定义为\\
     * @param <T>
     *            context的value的实际类型
     * @return 渲染后的结果
     */
    public static <T> String render(String template, Map<String, T> context, boolean enableBackslash) {
        Assert.argNotBlank(template, "template");
        Assert.argNotNull(context, "context");

        char[] chars = template.toCharArray();
        StringBuilder sb = new StringBuilder();
        StringBuilder buffer = new StringBuilder();
        StringBuilder now = sb;
        int token = 0;
        int flag = 0;

        for (char aChar : chars) {
            if (flag == 1) {
                flag = 0;
                now.append(aChar);
                continue;
            }

            switch (aChar) {
                case '$':
                    if (token == 0) {
                        token = 1;
                        now = buffer;
                    } else if (token == 1) {
                        sb.append(aChar);
                        break;
                    }

                    now.append(aChar);
                    break;
                case '{':
                    if (token == 1) {
                        token = 2;
                    }

                    now.append(aChar);
                    break;
                case '}':
                    if (token == 0) {
                        now.append(aChar);
                    } else if (token == 1) {
                        sb.append(buffer.toString());
                        sb.append(aChar);
                        buffer.setLength(0);
                    } else {
                        // 开始替换内容
                        String bufferStr = buffer.toString();
                        buffer.setLength(0);

                        // 长度是2，表示用户输入的占位符是${}，大括号里边没有内容，直接删除即可
                        if (bufferStr.length() > 2) {
                            bufferStr = bufferStr.substring(2);
                            Object value = context.get(bufferStr);
                            bufferStr = value == null ? StringConst.EMPTY : value.toString();
                            sb.append(bufferStr);
                        }
                    }
                    // 只要遇到}，token肯定要回滚回0，now切换回sb
                    now = sb;
                    token = 0;
                    break;
                default:
                    if ('\\' == aChar && enableBackslash) {
                        flag = 1;
                        break;
                    }

                    if (token == 1) {
                        // $后边跟的不是'{'，中断匹配
                        sb.append(buffer.toString());
                        sb.append(aChar);
                        buffer.setLength(0);
                        token = 0;
                        now = sb;
                    } else {
                        now.append(aChar);
                    }
                    break;
            }

        }

        return sb + buffer.toString();
    }

}
