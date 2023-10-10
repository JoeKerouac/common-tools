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
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.Message;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.http.nio.support.BasicResponseConsumer;
import org.apache.hc.core5.ssl.SSLContexts;

import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.concurrent.FutureCallback;
import com.github.joekerouac.common.tools.concurrent.ResultConvertFuture;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.io.InMemoryFile;
import com.github.joekerouac.common.tools.log.Logger;
import com.github.joekerouac.common.tools.net.http.config.IHttpClientConfig;
import com.github.joekerouac.common.tools.net.http.config.IHttpConfig;
import com.github.joekerouac.common.tools.net.http.cookie.Cookie;
import com.github.joekerouac.common.tools.net.http.cookie.CookieStore;
import com.github.joekerouac.common.tools.net.http.cookie.impl.CookieStoreImpl;
import com.github.joekerouac.common.tools.net.http.entity.StreamAsyncEntityConsumer;
import com.github.joekerouac.common.tools.net.http.exception.UnknownException;
import com.github.joekerouac.common.tools.net.http.request.IHttpGenericRequest;
import com.github.joekerouac.common.tools.net.http.request.IHttpMethod;
import com.github.joekerouac.common.tools.net.http.request.IHttpPost;
import com.github.joekerouac.common.tools.net.http.request.UploadFile;
import com.github.joekerouac.common.tools.net.http.response.IHttpResponse;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;

import lombok.Builder;
import lombok.CustomLog;

/**
 * HttpClient，默认用户代理为火狐
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
@CustomLog
public final class IHttpClient implements AutoCloseable {

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

    private static final Supplier<HttpAsyncClientBuilder> HTTP_ASYNC_CLIENT_BUILDER_SUPPLIER = HttpAsyncClients::custom;

    /**
     * client ID，用来唯一区分HttpClient
     */
    private final String id;

    /**
     * HttpClient
     */
    private final CloseableHttpAsyncClient httpClient;

    /**
     * cookie store ，存储cookie
     */
    private final CookieStore cookieStore;

    /**
     * 配置
     */
    private final IHttpClientConfig config;

    /**
     * 日志
     */
    private final Logger logger;

    /**
     * 指定client配置和cookieStore
     *
     * @param config
     *            client配置
     * @param cookieStore
     *            cookieStore
     * @param sslcontext
     *            sslcontext
     * @param logger
     *            日志
     * @param noRedirect
     *            如果为true表示客户端将不会自动重定向
     */
    @Builder
    private IHttpClient(IHttpClientConfig config, CookieStore cookieStore, SSLContext sslcontext, boolean noRedirect,
        Logger logger, Supplier<HttpAsyncClientBuilder> httpAsyncClientBuilderSupplier) {
        this.config = config == null ? new IHttpClientConfig() : config;
        this.cookieStore = cookieStore == null ? new CookieStoreImpl() : cookieStore;
        SSLContext sslContextNotNull = sslcontext == null ? SSLContexts.createSystemDefault() : sslcontext;
        this.logger = logger == null ? LOGGER : logger;
        Supplier<HttpAsyncClientBuilder> builderSupplier = httpAsyncClientBuilderSupplier == null
            ? HTTP_ASYNC_CLIENT_BUILDER_SUPPLIER : httpAsyncClientBuilderSupplier;
        this.id = String.valueOf(System.currentTimeMillis());
        this.httpClient = HttpClientUtil.buildCloseableHttpAsyncClient(this.config, this.cookieStore, sslContextNotNull,
            noRedirect, builderSupplier);
        this.httpClient.start();
    }

    /**
     * 执行HTTP请求
     *
     * @param request
     *            请求体
     * @param futureCallback
     *            请求回调
     * @return 请求结果future
     */
    Future<IHttpResponse> execute(AbstractIHttpRequest request, FutureCallback<IHttpResponse> futureCallback) {
        Assert.argNotNull(request, "request");
        if (LOGGER.isDebugEnabled()) {
            logger.debug("要发送的请求为：[{}]", request);
        }

        FutureCallback<IHttpResponse> callback = futureCallback == null ? EMPTY_CALLBACK : futureCallback;

        AsyncRequestProducer requestProducer = buildSimpleHttpRequest(request);
        IHttpConfig config = request.getHttpConfig() == null ? this.config.getHttpConfig() : request.getHttpConfig();

        BasicResponseConsumer<InMemoryFile> responseConsumer =
            new BasicResponseConsumer<>(new StreamAsyncEntityConsumer(config.getInitBufferSize(),
                config.getWriteFileOnLarge(), config.getFilter()));
        // 发起请求
        Future<Message<HttpResponse, InMemoryFile>> future = httpClient.execute(requestProducer, responseConsumer,
            new org.apache.hc.core5.concurrent.FutureCallback<Message<HttpResponse, InMemoryFile>>() {
                @Override
                public void completed(Message<HttpResponse, InMemoryFile> message) {
                    IHttpResponse response = new IHttpResponse(message);
                    try {
                        callback.success(response);
                    } catch (Throwable throwable) {
                        LOGGER.warn(throwable, "异步回调[success]异常");
                    }

                    callback.complete(response, null, 0);
                }

                @Override
                public void failed(Exception ex) {
                    try {
                        callback.failed(ex);
                    } catch (Throwable throwable) {
                        LOGGER.warn(throwable, "异步回调[failed]异常");
                    }

                    callback.complete(null, ex, 1);
                }

                @Override
                public void cancelled() {
                    try {
                        callback.cancelled();
                    } catch (Throwable throwable) {
                        LOGGER.warn(throwable, "异步回调[failed]异常");
                    }

                    callback.complete(null, null, 2);
                }
            });

        return new ResultConvertFuture<>(future, IHttpResponse::new);
    }

    public CookieStore getCookieManager() {
        return this.cookieStore;
    }

    /**
     * 获取所有cookie
     *
     * @return 返回cookie列表
     */
    public List<Cookie> getCookies() {
        return cookieStore.getCookies();
    }

    /**
     * 获取指定cookie
     *
     * @param name
     *            cookie名
     * @return cookie存在时返回cookie，不存在时返回null
     */
    public Cookie getCookie(String name) {
        List<Cookie> cookies = cookieStore.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(name)) {
                return cookie;
            }
        }
        return null;
    }

    /**
     * 添加cookie
     *
     * @param cookie
     *            要添加的cookie
     */
    public void addCookie(Cookie cookie) {
        cookieStore.addCookie(cookie);
    }

    /**
     * 获取httpClient的ID
     *
     * @return client的ID
     */
    public String getId() {
        return this.id;
    }

    @Override
    public void close() throws IOException {
        httpClient.close();
    }

    private AsyncRequestProducer buildSimpleHttpRequest(AbstractIHttpRequest request) {
        String method = request.getMethod();
        HttpUriRequestBase httpRequest = new HttpUriRequestBase(method, URI.create(request.getUrl()));

        // 请求通用配置
        IHttpConfig config = request.getHttpConfig() == null ? this.config.getHttpConfig() : request.getHttpConfig();
        httpRequest.setConfig(buildRequestConfig(config));
        request.getHeaders().forEach(httpRequest::addHeader);
        httpRequest.addHeader("Content-Type",
            ContentType.create(request.getContentType(), request.getCharset()).toString());

        List<UploadFile> files = null;
        String body = null;

        // 如果是post请求，则需要设置请求体
        if (request instanceof IHttpPost) {
            IHttpPost httpPost = (IHttpPost)request;

            files = httpPost.getFiles();
            body = httpPost.getBody();
        } else if (request instanceof IHttpGenericRequest) {
            if (Objects.equals(method, IHttpMethod.POST.name()) || Objects.equals(method, IHttpMethod.PUT.name())
                || Objects.equals(method, IHttpMethod.PATCH.name())) {
                IHttpGenericRequest genericRequest = (IHttpGenericRequest)request;
                files = genericRequest.getFiles();
                body = genericRequest.getBody();
            }
        }

        if (CollectionUtil.isNotEmpty(files)) {
            // 如果要上传的文件不为空，则使用MultipartEntity
            MultipartEntityBuilder entityBuilder =
                MultipartEntityBuilder.create().setCharset(Charset.forName(request.getCharset()));
            files
                .forEach(file -> entityBuilder.addBinaryBody(file.getName(), file.getFile(),
                    ContentType.create(file.getContentType(),
                        StringUtils.getOrDefault(file.getCharset(), Charset.defaultCharset().name())),
                    file.getFileName()));
            // 将body解析到form表单
            if (StringUtils.isNotBlank(body)) {
                final String[] split = body.split("&");
                String finalBody = body;
                Arrays.stream(split).forEach(kv -> {
                    final String[] kvArr = kv.split("=");
                    Assert.assertTrue(kvArr.length == 2,
                        StringUtils.format("当前上传文件不为空，body必须是form表单，当前body不符合要求：[{}]", finalBody),
                        ExceptionProviderConst.IllegalArgumentExceptionProvider);
                    entityBuilder.addTextBody(kvArr[0], kvArr[1]);
                });
            }

            httpRequest.setEntity(entityBuilder.build());
        } else if (StringUtils.isNotBlank(body)) {
            // 使用普通StringEntity
            httpRequest
                .setEntity(new StringEntity(body, ContentType.create(request.getContentType(), request.getCharset())));
        }

        final ByteBuffer buffer;
        final HttpEntity entity = httpRequest.getEntity();
        if (entity != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            try {
                entity.writeTo(stream);
            } catch (IOException e) {
                throw new UnknownException("请求体读取失败，可能是上传文件的文件流读取失败", e);
            }
            final byte[] data = stream.toByteArray();
            buffer = ByteBuffer.wrap(data);
        } else {
            buffer = null;
        }

        return new BasicRequestProducer(httpRequest, entity == null ? null : new AsyncEntityProducer() {
            @Override
            public boolean isRepeatable() {
                return entity.isRepeatable();
            }

            @Override
            public void failed(final Exception cause) {

            }

            @Override
            public long getContentLength() {
                return entity.getContentLength();
            }

            @Override
            public String getContentType() {
                return entity.getContentType();
            }

            @Override
            public String getContentEncoding() {
                return entity.getContentEncoding();
            }

            @Override
            public boolean isChunked() {
                return entity.isChunked();
            }

            @Override
            public Set<String> getTrailerNames() {
                return entity.getTrailerNames();
            }

            @Override
            public int available() {
                return Integer.MAX_VALUE;
            }

            @Override
            public void produce(final DataStreamChannel channel) throws IOException {
                channel.write(buffer);
                // 写出完毕
                if (buffer.remaining() <= 0) {
                    channel.endStream();
                }
            }

            @Override
            public void releaseResources() {
                buffer.flip();
            }
        });
    }

    /**
     * 构建HttpClient请求配置
     *
     * @param config
     *            请求配置
     * @return requestConfig
     */
    private RequestConfig buildRequestConfig(IHttpConfig config) {
        logger.debug("构建请求配置");
        RequestConfig requestConfig =
            RequestConfig.custom().setConnectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .setResponseTimeout(config.getSocketTimeout(), TimeUnit.MILLISECONDS)
                .setConnectionRequestTimeout(config.getConnectionRequestTimeout(), TimeUnit.MILLISECONDS).build();
        logger.debug("请求配置为：[{}]", requestConfig);
        return requestConfig;
    }
}
