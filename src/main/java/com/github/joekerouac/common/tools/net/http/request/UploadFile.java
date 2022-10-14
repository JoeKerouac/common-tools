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
package com.github.joekerouac.common.tools.net.http.request;

import java.io.InputStream;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.github.joekerouac.common.tools.net.http.ContentType;

import lombok.Data;

/**
 * 上传文件
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
@Data
public class UploadFile {

    /**
     * 文件输入流
     */
    @NotNull
    private InputStream file;

    /**
     * form表单名
     */
    @NotBlank
    private String name;

    /**
     * 文件名
     */
    @NotBlank
    private String fileName;

    /**
     * 文件content type，例如image/png、application/octet-stream，可以参考{@link ContentType}
     */
    @NotBlank
    private String contentType;

    /**
     * 编码字符集，如果为空则使用当前系统默认编码字符集
     */
    private String charset;

}
