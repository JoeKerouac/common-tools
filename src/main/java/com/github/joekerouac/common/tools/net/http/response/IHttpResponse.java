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
package com.github.joekerouac.common.tools.net.http.response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.hc.client5.http.async.methods.SimpleBody;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;

import com.github.joekerouac.common.tools.constant.Const;
import com.github.joekerouac.common.tools.net.http.exception.ServerException;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.JsonUtil;

import lombok.CustomLog;
import lombok.Data;

/**
 * HTTP响应，如果不使用必须关闭，如果调用过getresult方法可以不关闭
 * 
 * PS:非线程安全，并发调用有可能会导致未知错误
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@CustomLog
public class IHttpResponse {

    /**
     * 400错误码
     */
    private static final int ERR400 = 400;

    /**
     * 请求状态
     */
    private final int status;

    /**
     * 响应数据
     */
    private final byte[] data;

    /**
     * 请求中的异常，如果没有异常那么该值为空
     */
    private final ServerException exception;

    /**
     * 响应header
     */
    private final List<Header> headers;

    /**
     * 响应编码
     */
    private final String charset;

    public IHttpResponse(SimpleHttpResponse httpResponse) {
        this.status = httpResponse.getCode();
        this.headers = Arrays.asList(httpResponse.getHeaders());
        this.data = Optional.ofNullable(httpResponse.getBody()).map(SimpleBody::getBodyBytes).orElse(new byte[0]);
        this.charset = Optional.ofNullable(httpResponse.getBody()).map(SimpleBody::getContentType)
            .map(ContentType::getCharset).orElse(Const.DEFAULT_CHARSET).name();

        if (status >= ERR400) {
            ErrorResp resp = null;
            String msg = new String(this.data, Charset.forName(this.charset));

            try {
                resp = JsonUtil.read(this.data, ErrorResp.class);
            } catch (Exception e) {
                // 异常忽略
                LOGGER.info("响应4xx数据无法解析，数据为：[{}]", msg);
            }

            if (resp == null) {
                resp = new ErrorResp();
                resp.setMessage(msg);
                resp.setError(msg);
                resp.setStatus(status);
                resp.setPath("unknown");
                resp.setException("unknown");
            }

            this.exception =
                new ServerException(resp.getPath(), resp.getException(), resp.getMessage(), resp.getError(), status);
        } else {
            this.exception = null;
        }
    }

    /**
     * 获取请求头
     *
     * @param name
     *            请求头的名字
     * @return 对应的值
     */
    public List<IHeader> getHeader(String name) {
        if (name == null) {
            return Collections.emptyList();
        }

        return headers.stream().filter(header -> name.equals(header.getName())).map(IHeader::new)
            .collect(Collectors.toList());
    }

    /**
     * 获取网络异常
     * 
     * @return 网络异常
     */
    public ServerException getServerException() {
        return exception;
    }

    /**
     * 将结果转换为字符串（调用此方法后input流将会关闭）
     *
     * @return 请求结果的字符串
     */
    public String getResult() {
        return getResult(null);
    }

    /**
     * 将结果转换为指定字符集字符串（调用此方法后input流将会关闭）
     *
     * @param charset
     *            结果字符集
     * @return 请求结果的字符串
     */
    public String getResult(String charset) {
        return getResult(charset, true);
    }

    /**
     * 将结果转换为指定字符集字符串，调用此方法后input流将会关闭，无论是否发生异常（有可能因为字符集异常）
     *
     * @param charset
     *            结果字符集
     * @param force
     *            是否强制使用指定字符集而忽略服务器响应字符集，true表示强制使用指定字符集，忽略服务器响应字符集
     * @return 请求结果的字符串
     */
    public String getResult(String charset, boolean force) {
        String usedCharset = charset;
        if (!force) {
            usedCharset = getCharset();
        }

        if (StringUtils.isBlank(usedCharset)) {
            usedCharset = getCharset();
        }

        return new String(getResultAsBinary(), Charset.forName(usedCharset));
    }

    /**
     * 以流的形式获取响应
     * 
     * @return 响应流
     * @throws IOException
     *             IOException
     */
    public InputStream getResultAsStream() throws IOException {
        return new ByteArrayInputStream(getResultAsBinary());
    }

    /**
     * 以byte数组的形式获取响应数据
     * 
     * @return 响应数据的byte数组
     */
    public synchronized byte[] getResultAsBinary() {
        if (exception != null) {
            throw exception;
        }

        return this.data;
    }

    /**
     * 获取响应编码字符集
     * 
     * @return 服务器响应编码字符集
     */
    public String getCharset() {
        return charset;
    }

    /**
     * 获取响应HTTP状态
     *
     * @return http状态码
     */
    public int getStatus() {
        return this.status;
    }

    /**
     * 该对象的部分字段可能是固定的
     */
    @Data
    private static final class ErrorResp {

        /**
         * 时间戳
         */
        private String timestamp;

        /**
         * 当前http响应状态
         */
        private int status;

        /**
         * 错误说明
         */
        private String error;

        /**
         * 异常
         */
        private String exception;

        /**
         * message
         */
        private String message;

        /**
         * 路径
         */
        private String path;
    }
}
