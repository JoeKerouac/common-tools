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
package com.github.joekerouac.common.tools.net.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http.nio.support.BasicResponseConsumer;

import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.concurrent.FutureCallback;
import com.github.joekerouac.common.tools.concurrent.ResultConvertFuture;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.io.InMemoryFile;
import com.github.joekerouac.common.tools.net.http.config.IHttpConfig;
import com.github.joekerouac.common.tools.net.http.entity.StreamAsyncEntityConsumer;
import com.github.joekerouac.common.tools.net.http.exception.UnknownException;
import com.github.joekerouac.common.tools.net.http.request.IHttpMethod;
import com.github.joekerouac.common.tools.net.http.request.UploadFile;
import com.github.joekerouac.common.tools.net.http.response.IHttpResponse;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * @author JoeKerouac
 * @date 2023-07-27 15:48
 * @since 2.1.0
 */
public class HttpRequestUtil {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static final IHttpConfig DEFAULT_HTTP_CONFIG = new IHttpConfig();

    private static final String DEFAULT_MIME_TYPE =
        com.github.joekerouac.common.tools.net.http.ContentType.CONTENT_TYPE_JSON;

    /**
     * 回调空实现
     */
    private static final FutureCallback<IHttpResponse> EMPTY_CALLBACK = new FutureCallback<IHttpResponse>() {
        @Override
        public void success(IHttpResponse result) {

        }

        @Override
        public void failed(Throwable ex) {

        }

        @Override
        public void cancelled() {

        }
    };

    /**
     * 发起get请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @return 结果
     */
    public static Future<IHttpResponse> get(@NotNull CloseableHttpAsyncClient client, @NotBlank String url) {
        return request(client, url, IHttpMethod.GET, null, null, null, null, null, null, null);
    }

    /**
     * 发起get请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param headers
     *            headers
     * @return 结果
     */
    public static Future<IHttpResponse> get(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        Map<String, String> headers) {
        return request(client, url, IHttpMethod.GET, headers, null, null, null, null, null, null);
    }

    /**
     * 发起get请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param headers
     *            headers
     * @param config
     *            请求配置
     * @return 结果
     */
    public static Future<IHttpResponse> get(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        Map<String, String> headers, IHttpConfig config) {
        return request(client, url, IHttpMethod.GET, headers, null, null, null, null, config, null);
    }

    /**
     * 发起get请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param headers
     *            headers
     * @param config
     *            请求配置
     * @param futureCallback
     *            回调
     * @return 结果
     */
    public static Future<IHttpResponse> get(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        Map<String, String> headers, IHttpConfig config, FutureCallback<IHttpResponse> futureCallback) {
        return request(client, url, IHttpMethod.GET, headers, null, null, null, null, config, futureCallback);
    }

    /**
     * 发起post请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param body
     *            body
     * @return 结果
     */
    public static Future<IHttpResponse> post(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        String body) {
        return request(client, url, IHttpMethod.POST, null, body.getBytes(DEFAULT_CHARSET), DEFAULT_CHARSET.name(),
            null, null, null, null);
    }

    /**
     * 发起post请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param body
     *            body
     * @param mimeType
     *            mimeType，参考{@link com.github.joekerouac.common.tools.net.http.ContentType}
     * @return 结果
     */
    public static Future<IHttpResponse> post(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        String body, String mimeType) {
        return request(client, url, IHttpMethod.POST, null, body.getBytes(DEFAULT_CHARSET), DEFAULT_CHARSET.name(),
            null, mimeType, null, null);
    }

    /**
     * 发起post请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param headers
     *            headers
     * @param body
     *            body
     * @param mimeType
     *            mimeType，参考{@link com.github.joekerouac.common.tools.net.http.ContentType}
     * @return 结果
     */
    public static Future<IHttpResponse> post(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        Map<String, String> headers, String body, String mimeType) {
        return request(client, url, IHttpMethod.POST, headers, body.getBytes(DEFAULT_CHARSET), DEFAULT_CHARSET.name(),
            null, mimeType, null, null);
    }

    /**
     * 发起post请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param headers
     *            headers
     * @param body
     *            body
     * @param mimeType
     *            mimeType，参考{@link com.github.joekerouac.common.tools.net.http.ContentType}
     * @param config
     *            请求配置
     * @return 结果
     */
    public static Future<IHttpResponse> post(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        Map<String, String> headers, String body, String mimeType, IHttpConfig config) {
        return request(client, url, IHttpMethod.POST, headers, body.getBytes(DEFAULT_CHARSET), DEFAULT_CHARSET.name(),
            null, mimeType, config, null);
    }

    /**
     * 发起post请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param headers
     *            headers
     * @param body
     *            body
     * @param mimeType
     *            mimeType，参考{@link com.github.joekerouac.common.tools.net.http.ContentType}
     * @param config
     *            请求配置
     * @param futureCallback
     *            回调
     * @return 结果
     */
    public static Future<IHttpResponse> post(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        Map<String, String> headers, String body, String mimeType, IHttpConfig config,
        FutureCallback<IHttpResponse> futureCallback) {
        return request(client, url, IHttpMethod.POST, headers, body.getBytes(DEFAULT_CHARSET), DEFAULT_CHARSET.name(),
            null, mimeType, config, futureCallback);
    }

    /**
     * 发起post请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param headers
     *            headers
     * @param body
     *            body
     * @param files
     *            files
     * @param mimeType
     *            mimeType，参考{@link com.github.joekerouac.common.tools.net.http.ContentType}
     * @param config
     *            请求配置
     * @param futureCallback
     *            回调
     * @return 结果
     */
    public static Future<IHttpResponse> post(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        Map<String, String> headers, String body, List<UploadFile> files, String mimeType, IHttpConfig config,
        FutureCallback<IHttpResponse> futureCallback) {
        return request(client, url, IHttpMethod.POST, headers, body.getBytes(DEFAULT_CHARSET), DEFAULT_CHARSET.name(),
            files, mimeType, config, futureCallback);
    }

    /**
     * 发起post请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param body
     *            body
     * @param charset
     *            charset
     * @return 结果
     */
    public static Future<IHttpResponse> post(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        byte[] body, String charset) {
        return request(client, url, IHttpMethod.POST, null, body, charset, null, null, null, null);
    }

    /**
     * 发起post请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param body
     *            body
     * @param charset
     *            charset
     * @param mimeType
     *            mimeType，参考{@link com.github.joekerouac.common.tools.net.http.ContentType}
     * @return 结果
     */
    public static Future<IHttpResponse> post(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        byte[] body, String charset, String mimeType) {
        return request(client, url, IHttpMethod.POST, null, body, charset, null, mimeType, null, null);
    }

    /**
     * 发起post请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param headers
     *            headers
     * @param body
     *            body
     * @param charset
     *            charset
     * @param mimeType
     *            mimeType，参考{@link com.github.joekerouac.common.tools.net.http.ContentType}
     * @return 结果
     */
    public static Future<IHttpResponse> post(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        Map<String, String> headers, byte[] body, String charset, String mimeType) {
        return request(client, url, IHttpMethod.POST, headers, body, charset, null, mimeType, null, null);
    }

    /**
     * 发起post请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param headers
     *            headers
     * @param body
     *            body
     * @param charset
     *            charset
     * @param mimeType
     *            mimeType，参考{@link com.github.joekerouac.common.tools.net.http.ContentType}
     * @param config
     *            请求配置
     * @return 结果
     */
    public static Future<IHttpResponse> post(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        Map<String, String> headers, byte[] body, String charset, String mimeType, IHttpConfig config) {
        return request(client, url, IHttpMethod.POST, headers, body, charset, null, mimeType, config, null);
    }

    /**
     * 发起post请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param headers
     *            headers
     * @param body
     *            body
     * @param charset
     *            charset
     * @param mimeType
     *            mimeType，参考{@link com.github.joekerouac.common.tools.net.http.ContentType}
     * @param config
     *            请求配置
     * @param futureCallback
     *            回调
     * @return 结果
     */
    public static Future<IHttpResponse> post(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        Map<String, String> headers, byte[] body, String charset, String mimeType, IHttpConfig config,
        FutureCallback<IHttpResponse> futureCallback) {
        return request(client, url, IHttpMethod.POST, headers, body, charset, null, mimeType, config, futureCallback);
    }

    /**
     * 发起post请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param headers
     *            headers
     * @param body
     *            body
     * @param charset
     *            charset
     * @param files
     *            files
     * @param mimeType
     *            mimeType，参考{@link com.github.joekerouac.common.tools.net.http.ContentType}
     * @param config
     *            请求配置
     * @param futureCallback
     *            回调
     * @return 结果
     */
    public static Future<IHttpResponse> post(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        Map<String, String> headers, byte[] body, String charset, List<UploadFile> files, String mimeType,
        IHttpConfig config, FutureCallback<IHttpResponse> futureCallback) {
        return request(client, url, IHttpMethod.POST, headers, body, charset, files, mimeType, config, futureCallback);
    }

    /**
     * 发起请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param method
     *            method
     * @param headers
     *            headers
     * @param body
     *            body
     * @param charset
     *            charset
     * @param mimeType
     *            mimeType，参考{@link com.github.joekerouac.common.tools.net.http.ContentType}
     * @param futureCallback
     *            回调
     * @return 结果
     */
    public static Future<IHttpResponse> request(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        @NotNull IHttpMethod method, Map<String, String> headers, byte[] body, String charset, String mimeType,
        FutureCallback<IHttpResponse> futureCallback) {
        return request(client, url, method, headers, body, charset, null, mimeType, null, futureCallback);
    }

    /**
     * 发起请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param method
     *            method
     * @param headers
     *            headers
     * @param body
     *            body
     * @param charset
     *            charset
     * @param mimeType
     *            mimeType，参考{@link com.github.joekerouac.common.tools.net.http.ContentType}
     * @param config
     *            请求配置
     * @return 结果
     */
    public static Future<IHttpResponse> request(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        @NotNull IHttpMethod method, Map<String, String> headers, byte[] body, String charset, String mimeType,
        IHttpConfig config) {
        return request(client, url, method, headers, body, charset, null, mimeType, config, null);
    }

    /**
     * 发起请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param method
     *            method
     * @param headers
     *            headers
     * @param body
     *            body
     * @param charset
     *            charset
     * @param mimeType
     *            mimeType，参考{@link com.github.joekerouac.common.tools.net.http.ContentType}
     * @param config
     *            请求配置
     * @param futureCallback
     *            回调
     * @return 结果
     */
    public static Future<IHttpResponse> request(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        @NotNull IHttpMethod method, Map<String, String> headers, byte[] body, String charset, String mimeType,
        IHttpConfig config, FutureCallback<IHttpResponse> futureCallback) {
        return request(client, url, method, headers, body, charset, null, mimeType, config, futureCallback);
    }

    /**
     * 发起请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param method
     *            method
     * @param headers
     *            headers
     * @param body
     *            body
     * @param charset
     *            charset
     * @param files
     *            files
     * @param mimeType
     *            mimeType，参考{@link com.github.joekerouac.common.tools.net.http.ContentType}
     * @param futureCallback
     *            回调
     * @return 结果
     */
    public static Future<IHttpResponse> request(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        @NotNull IHttpMethod method, Map<String, String> headers, byte[] body, String charset, List<UploadFile> files,
        String mimeType, FutureCallback<IHttpResponse> futureCallback) {
        return request(client, url, method, headers, body, charset, files, mimeType, null, futureCallback);
    }

    /**
     * 发起请求
     *
     * @param client
     *            client
     * @param url
     *            url
     * @param method
     *            method
     * @param headers
     *            headers
     * @param body
     *            body
     * @param charset
     *            charset
     * @param files
     *            files
     * @param mimeType
     *            mimeType，参考{@link com.github.joekerouac.common.tools.net.http.ContentType}
     * @param config
     *            请求配置
     * @return 结果
     */
    public static Future<IHttpResponse> request(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        @NotNull IHttpMethod method, Map<String, String> headers, byte[] body, String charset, List<UploadFile> files,
        String mimeType, IHttpConfig config) {
        return request(client, url, method, headers, body, charset, files, mimeType, config, null);
    }

    /**
     * 发起请求
     * 
     * @param client
     *            client
     * @param url
     *            url
     * @param method
     *            method
     * @param headers
     *            headers
     * @param body
     *            body
     * @param charset
     *            charset
     * @param files
     *            files
     * @param mimeType
     *            mimeType，参考{@link com.github.joekerouac.common.tools.net.http.ContentType}
     * @param config
     *            请求配置
     * @param futureCallback
     *            回调
     * @return 结果
     */
    public static Future<IHttpResponse> request(@NotNull CloseableHttpAsyncClient client, @NotBlank String url,
        @NotNull IHttpMethod method, Map<String, String> headers, byte[] body, String charset, List<UploadFile> files,
        String mimeType, IHttpConfig config, FutureCallback<IHttpResponse> futureCallback) {
        Assert.argNotNull(client, "client");
        Assert.argNotBlank(url, "url");
        Assert.argNotNull(method, "method");
        Assert.assertTrue(CollectionUtil.isEmpty(files) || (body == null || body.length == 0), "当前上传文件时不支持指定文本数据",
            ExceptionProviderConst.UnsupportedOperationExceptionProvider);
        IHttpConfig httpConfig = config == null ? DEFAULT_HTTP_CONFIG : config;

        Assert.assertTrue(body == null || StringUtils.isNotBlank(charset), "body不为空时charset也不能为空",
            ExceptionProviderConst.IllegalArgumentExceptionProvider);

        FutureCallback<IHttpResponse> callback = futureCallback == null ? EMPTY_CALLBACK : futureCallback;

        AsyncRequestProducer requestProducer = buildSimpleHttpRequest(url, method.name(), headers, body, charset, files,
            StringUtils.getOrDefault(mimeType, DEFAULT_MIME_TYPE), httpConfig);

        BasicResponseConsumer<InMemoryFile> responseConsumer =
            new BasicResponseConsumer<>(new StreamAsyncEntityConsumer(httpConfig.getInitBufferSize(),
                httpConfig.getWriteFileOnLarge(), httpConfig.getFilter()));

        // 发起请求
        Future<Message<HttpResponse, InMemoryFile>> future = client.execute(requestProducer, responseConsumer,
            new org.apache.hc.core5.concurrent.FutureCallback<Message<HttpResponse, InMemoryFile>>() {
                @Override
                public void completed(Message<HttpResponse, InMemoryFile> message) {
                    IHttpResponse response = new IHttpResponse(message);
                    callback.success(response);
                    callback.complete(response, null, 0);
                }

                @Override
                public void failed(Exception ex) {
                    callback.failed(ex);
                    callback.complete(null, ex, 1);
                }

                @Override
                public void cancelled() {
                    callback.cancelled();
                    callback.complete(null, null, 2);
                }
            });

        return new ResultConvertFuture<>(future, IHttpResponse::new);
    }

    /**
     * 构建请求处理器
     * 
     * @param url
     *            url
     * @param method
     *            method
     * @param headers
     *            headers
     * @param body
     *            body
     * @param charset
     *            charset
     * @param files
     *            files
     * @param mimeType
     *            mime type
     * @param config
     *            config
     * @return 异步请求处理器
     */
    public static AsyncRequestProducer buildSimpleHttpRequest(String url, String method, Map<String, String> headers,
        byte[] body, String charset, List<UploadFile> files, String mimeType, IHttpConfig config) {
        HttpUriRequestBase httpRequest = new HttpUriRequestBase(method, URI.create(url));

        // 请求通用配置
        RequestConfig requestConfig =
            RequestConfig.custom().setConnectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .setResponseTimeout(config.getSocketTimeout(), TimeUnit.MILLISECONDS)
                .setConnectionRequestTimeout(config.getConnectionRequestTimeout(), TimeUnit.MILLISECONDS).build();
        httpRequest.setConfig(requestConfig);

        if (headers != null) {
            headers.forEach(httpRequest::addHeader);
        }

        final ByteBuffer buffer;
        if (CollectionUtil.isNotEmpty(files)) {
            // 如果要上传的文件不为空，则使用MultipartEntity
            MultipartEntityBuilder entityBuilder =
                MultipartEntityBuilder.create().setCharset(Charset.forName(Charset.defaultCharset().name()));

            files
                .forEach(file -> entityBuilder.addBinaryBody(file.getName(), file.getFile(),
                    org.apache.hc.core5.http.ContentType.create(file.getContentType(),
                        StringUtils.getOrDefault(file.getCharset(), Charset.defaultCharset().name())),
                    file.getFileName()));

            HttpEntity httpEntity = entityBuilder.build();
            // 这里有可能内存溢出，后续修改
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                httpEntity.writeTo(outputStream);
                buffer = ByteBuffer.wrap(outputStream.toByteArray());
            } catch (IOException e) {
                throw new UnknownException("请求体读取失败，可能是上传文件的文件流读取失败", e);
            }
        } else if (body != null && body.length > 0) {
            // 使用普通StringEntity
            buffer = ByteBuffer.wrap(body);
        } else {
            buffer = null;
        }

        int contentLen = buffer == null ? 0 : buffer.limit();
        ContentType contentType = ContentType.create(mimeType, charset);

        return new BasicRequestProducer(httpRequest, buffer == null ? null : new AsyncEntityProducer() {

            private final AtomicReference<Exception> exception = new AtomicReference<>(null);

            @Override
            public boolean isRepeatable() {
                return true;
            }

            @Override
            public void failed(final Exception cause) {
                if (exception.compareAndSet(null, cause)) {
                    releaseResources();
                }
            }

            @Override
            public long getContentLength() {
                return contentLen;
            }

            @Override
            public String getContentType() {
                return contentType.toString();
            }

            @Override
            public String getContentEncoding() {
                return null;
            }

            @Override
            public boolean isChunked() {
                return false;
            }

            @Override
            public Set<String> getTrailerNames() {
                return null;
            }

            @Override
            public int available() {
                return Integer.MAX_VALUE;
            }

            @Override
            public void produce(final DataStreamChannel channel) throws IOException {
                // org.apache.hc.core5.http.impl.nio.AbstractHttp1StreamDuplexer.streamOutput
                if (buffer.hasRemaining()) {
                    channel.write(buffer);
                }

                if (!buffer.hasRemaining()) {
                    channel.endStream();
                }
            }

            @Override
            public void releaseResources() {
                buffer.clear();
            }
        });
    }

}
