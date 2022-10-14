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
package com.github.joekerouac.common.tools.net;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * http代理设置
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HttpProxySettings {

    /**
     * 代理服务器host（注意，请勿添加http://前缀，操作系统会自动添加）
     */
    @NotBlank
    private String host;

    /**
     * 代理服务器端口号
     */
    @Min(1)
    @Max(65536)
    private int port;

    /**
     * 需要跳过代理的地址，多个地址以英文分号分隔，例如：<code>192.*;127.*;10.10.10.10</code>
     */
    private String override;

}
