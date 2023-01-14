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
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.async.methods.SimpleResponseConsumer;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.StandardAuthScheme;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;
import org.apache.hc.client5.http.protocol.RedirectStrategy;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.AsyncRequestProducer;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.nio.support.BasicRequestProducer;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.util.TimeValue;

import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.concurrent.FutureCallback;
import com.github.joekerouac.common.tools.concurrent.ResultConvertFuture;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.log.Logger;
import com.github.joekerouac.common.tools.net.http.config.AbstractHttpConfig;
import com.github.joekerouac.common.tools.net.http.config.HttpProxy;
import com.github.joekerouac.common.tools.net.http.config.IHttp1config;
import com.github.joekerouac.common.tools.net.http.config.IHttpClientConfig;
import com.github.joekerouac.common.tools.net.http.cookie.Cookie;
import com.github.joekerouac.common.tools.net.http.cookie.CookieStore;
import com.github.joekerouac.common.tools.net.http.cookie.CookieUtil;
import com.github.joekerouac.common.tools.net.http.cookie.impl.CookieStoreImpl;
import com.github.joekerouac.common.tools.net.http.exception.UnknownException;
import com.github.joekerouac.common.tools.net.http.request.IHttpPost;
import com.github.joekerouac.common.tools.net.http.response.IHttpResponse;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.thread.NamedThreadFactory;
import com.github.joekerouac.common.tools.thread.UncaughtExceptionHandlerThreadFactory;
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
     * 连接验证间隔，单位秒，如果连接超过该间隔没有活动，需要重新验证连接然后才能给用户
     */
    private static final int VALIDATE_INTERVAL = 60;

    /**
     * 不自动重定向
     */
    private static final RedirectStrategy NO_REDIRECT;

    static {
        NO_REDIRECT = new RedirectStrategy() {

            @Override
            public boolean isRedirected(HttpRequest request, HttpResponse response,
                org.apache.hc.core5.http.protocol.HttpContext context) throws HttpException {
                return false;
            }

            @Override
            public URI getLocationURI(HttpRequest request, HttpResponse response,
                org.apache.hc.core5.http.protocol.HttpContext context) throws HttpException {
                return null;
            }
        };
    }

    /**
     * client ID，用来唯一区分HttpClient
     */
    private String id;

    /**
     * HttpClient
     */
    private CloseableHttpAsyncClient httpClient;

    /**
     * cookie store ，存储cookie
     */
    private CookieStore cookieStore;

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
        CookieStore cookieStoreNotNull = cookieStore == null ? new CookieStoreImpl() : cookieStore;
        SSLContext sslContextNotNull = sslcontext == null ? SSLContexts.createSystemDefault() : sslcontext;
        this.logger = logger == null ? LOGGER : logger;
        Supplier<HttpAsyncClientBuilder> builderSupplier = httpAsyncClientBuilderSupplier == null
            ? HTTP_ASYNC_CLIENT_BUILDER_SUPPLIER : httpAsyncClientBuilderSupplier;
        this.init(this.config, cookieStoreNotNull, sslContextNotNull, noRedirect, builderSupplier);
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

        // 发起请求
        Future<SimpleHttpResponse> future = httpClient.execute(requestProducer, SimpleResponseConsumer.create(),
            new org.apache.hc.core5.concurrent.FutureCallback<SimpleHttpResponse>() {
                @Override
                public void completed(SimpleHttpResponse result) {
                    IHttpResponse response = new IHttpResponse(result);
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
        HttpUriRequestBase httpRequest = new HttpUriRequestBase(request.getMethod(), URI.create(request.getUrl()));

        // 请求通用配置
        AbstractHttpConfig config = request.getHttpConfig() == null ? this.config : request.getHttpConfig();
        httpRequest.setConfig(buildRequestConfig(config));
        request.getHeaders().forEach(httpRequest::addHeader);

        // 如果是post请求，则需要设置请求体
        if (request instanceof IHttpPost) {
            IHttpPost httpPost = (IHttpPost)request;

            if (!CollectionUtil.isEmpty(httpPost.getFiles())) {
                // 如果要上传的文件不为空，则使用MultipartEntity
                MultipartEntityBuilder entityBuilder =
                    MultipartEntityBuilder.create().setCharset(Charset.forName(request.getCharset()));
                httpPost.getFiles()
                    .forEach(file -> entityBuilder.addBinaryBody(file.getName(), file.getFile(),
                        ContentType.create(file.getContentType(),
                            StringUtils.getOrDefault(file.getCharset(), Charset.defaultCharset().name())),
                        file.getFileName()));
                // 将body解析到form表单
                if (StringUtils.isNotBlank(httpPost.getBody())) {
                    final String body = httpPost.getBody();
                    final String[] split = body.split("&");
                    Arrays.stream(split).forEach(kv -> {
                        final String[] kvArr = kv.split("=");
                        Assert.assertTrue(kvArr.length == 2,
                            StringUtils.format("当前上传文件不为空，body必须是form表单，当前body不符合要求：[{}]", body),
                            ExceptionProviderConst.IllegalArgumentExceptionProvider);
                        entityBuilder.addTextBody(kvArr[0], kvArr[1]);
                    });
                }

                httpRequest.setEntity(entityBuilder.build());
            } else if (StringUtils.isNotBlank(httpPost.getBody())) {
                // 使用普通StringEntity
                httpRequest.setEntity(new StringEntity(httpPost.getBody(),
                    ContentType.create(request.getContentType(), request.getCharset())));
            }
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
    private RequestConfig buildRequestConfig(AbstractHttpConfig config) {
        logger.debug("构建请求配置");
        RequestConfig requestConfig =
            RequestConfig.custom().setConnectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .setResponseTimeout(config.getSocketTimeout(), TimeUnit.MILLISECONDS)
                .setConnectionRequestTimeout(config.getConnectionRequestTimeout(), TimeUnit.MILLISECONDS).build();
        logger.debug("请求配置为：[{}]", requestConfig);
        return requestConfig;
    }

    /**
     * 初始化http client
     * 
     * @param config
     *            config
     * @param cookieStore
     *            cookie store
     * @param sslcontext
     *            ssl context
     * @param noRedirect
     *            是否自动重定向，true表示不重定向
     * @param httpAsyncClientBuilderSupplier
     *            HttpAsyncClientBuilder提供器
     */
    private void init(IHttpClientConfig config, CookieStore cookieStore, SSLContext sslcontext, boolean noRedirect,
        Supplier<HttpAsyncClientBuilder> httpAsyncClientBuilderSupplier) {
        logger.debug("正在初始化HttpClient");

        // 根据配置构建httpClient
        HttpAsyncClientBuilder builder = httpAsyncClientBuilderSupplier.get();

        ThreadFactory threadFactory = config.getThreadFactory();
        if (threadFactory == null) {
            threadFactory = new UncaughtExceptionHandlerThreadFactory(new NamedThreadFactory("http-client-io", true),
                (t, e) -> LOGGER.warn(e, "HTTP IO/回调 线程出错"));
        }
        builder.setThreadFactory(threadFactory);

        {
            // http1配置
            IHttp1config iHttp1config = config.getHttp1config();
            Http1Config http1Config = Http1Config.custom().setBufferSize(iHttp1config.getHttpSessionBufferSize())
                .setChunkSizeHint(iHttp1config.getHttpChunkSizeHint())
                .setMaxLineLength(iHttp1config.getHttpMaxLineLength())
                .setMaxHeaderCount(iHttp1config.getHttpMaxHeaderCount()).build();
            builder.setHttp1Config(http1Config);
            logger.debug("http1配置：[{}]", iHttp1config);
        }

        {
            // socket配置
            IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(config.getIoThreadCount())
                .setTcpNoDelay(config.isTcpNoDelay()).setSndBufSize(config.getSndBufSize())
                .setSoTimeout(config.getSocketTimeout(), TimeUnit.MILLISECONDS).setSoKeepAlive(config.isKeepAlive())
                .setRcvBufSize(config.getRcvBufSize()).build();
            builder.setIOReactorConfig(ioReactorConfig);
            logger.debug("socket配置：[{}]", ioReactorConfig);
        }

        {
            // 编码配置
            // MalformedInputAction: 编码格式错误时的处理方式，设置为null时默认使用REPORT；
            // UnmappableInputAction: 编码不可映射时的处理方式，设置为null时默认使用REPORT；
            // CodingErrorAction.IGNORE: 丢弃错误的输入；
            // CodingErrorAction.REPLACE: 丢弃错误的数据，并且把编码的替换值替换到输出；
            // CodingErrorAction.REPORT: 抛出异常；
            CharCodingConfig charCodingConfig = CharCodingConfig.custom().setCharset(config.getCharset())
                .setMalformedInputAction(CodingErrorAction.REPORT).setUnmappableInputAction(CodingErrorAction.REPORT)
                .build();
            builder.setCharCodingConfig(charCodingConfig);
            logger.debug("默认连接编码配置为：{}", charCodingConfig);
        }

        {
            HttpProxy proxy = config.getProxy();
            if (proxy != null) {
                HttpHost httpHost = new HttpHost(proxy.getHost(), proxy.getPort());
                builder.setProxy(httpHost);

                // 如果代理中的用户名不为空则认为代理是需要认证的，传入认证信息
                if (StringUtils.isNotBlank(proxy.getUsername())) {
                    BasicCredentialsProvider provider = new BasicCredentialsProvider();
                    provider.setCredentials(new AuthScope(httpHost),
                        new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword().toCharArray()));
                    builder.setDefaultCredentialsProvider(provider);
                }
            }

            if (noRedirect) {
                logger.debug("用户设置不重定向，客户端将不会重定向");
                builder.setRedirectStrategy(NO_REDIRECT);
            }

            if (StringUtils.isNotBlank(config.getUserAgent())) {
                builder.setUserAgent(config.getUserAgent());
                logger.debug("当前全局用户代理为：[{}]", config.getUserAgent());
            }
        }

        {
            AsyncClientConnectionManager connectionManager = buildAsyncClientConnectionManager(config, sslcontext);
            builder.setConnectionManager(connectionManager);
        }

        {
            org.apache.hc.client5.http.cookie.CookieStore httpClientCookieStore;
            if (cookieStore instanceof CookieStoreImpl) {
                httpClientCookieStore = ((CookieStoreImpl)cookieStore).getHttpClientCookieStore();
            } else {
                httpClientCookieStore = new org.apache.hc.client5.http.cookie.CookieStore() {
                    @Override
                    public void addCookie(org.apache.hc.client5.http.cookie.Cookie cookie) {
                        cookieStore.addCookie(CookieUtil.convert(cookie));
                    }

                    @Override
                    public List<org.apache.hc.client5.http.cookie.Cookie> getCookies() {
                        return cookieStore.getCookies().stream().map(CookieUtil::convert).collect(Collectors.toList());
                    }

                    @Override
                    public boolean clearExpired(Date date) {
                        return cookieStore.clearExpired(date);
                    }

                    @Override
                    public void clear() {
                        cookieStore.clear();
                    }
                };
            }
            builder.setDefaultCookieStore(httpClientCookieStore);
        }

        {
            // 全局配置
            RequestConfig defaultRequestConfig =
                RequestConfig.custom().setCookieSpec(StandardCookieSpec.RELAXED).setExpectContinueEnabled(true)
                    .setTargetPreferredAuthSchemes(Arrays.asList(StandardAuthScheme.BASIC, StandardAuthScheme.DIGEST))
                    .setProxyPreferredAuthSchemes(Collections.singletonList(StandardAuthScheme.BASIC)).build();
            builder.setDefaultRequestConfig(defaultRequestConfig);
            logger.debug("默认全局请求配置为：[{}]", defaultRequestConfig);
        }

        CloseableHttpAsyncClient httpclient = builder.build();
        httpclient.start();
        this.httpClient = httpclient;
        this.cookieStore = cookieStore;
        this.id = String.valueOf(System.currentTimeMillis());
        logger.debug("HttpClient初始化完毕");
    }

    /**
     * 构建连接池管理器
     *
     * @param config
     *            client配置
     * @param sslcontext
     *            安全上下文
     * @return 连接池管理器
     */
    private AsyncClientConnectionManager buildAsyncClientConnectionManager(IHttpClientConfig config,
        SSLContext sslcontext) {
        // tls策略
        TlsStrategy strategy = ClientTlsStrategyBuilder.create().setSslContext(sslcontext).build();

        // 自定义DNS
        DnsResolver dnsResolver = new SystemDefaultDnsResolver() {

            @Override
            public InetAddress[] resolve(final String host) throws UnknownHostException {
                if ("localhost".equalsIgnoreCase(host)) {
                    return new InetAddress[] {InetAddress.getLoopbackAddress()};
                } else {
                    return super.resolve(host);
                }
            }

        };

        PoolingAsyncClientConnectionManagerBuilder connectionManagerBuilder =
            PoolingAsyncClientConnectionManagerBuilder.create();

        // 连接池配置
        // PoolConcurrencyPolicy.STRICT: 对于最大连接数严格限制（LAX使宽松的限制，可以提高并发）
        // PoolReusePolicy.FIFO: 对于连接池里边的连接尽可能的公平复用，这样会使连接释放速度变慢，而LIFO则会使连接尽快释放
        // TimeValue.NEG_ONE_SECOND: 表示连接池中连接的最大生存时间，小于等于0表示不限生存时间
        // 暂停活动1S后验证连接
        // 设置连接池能够保存的最大连接数量以及对每个站点保持最大的连接数量
        // 当前设置：每个站点最大保持300个连接，连接池总共可以保持3000个连接
        connectionManagerBuilder.setTlsStrategy(strategy).setConnPoolPolicy(PoolReusePolicy.FIFO)
            .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
            .setValidateAfterInactivity(TimeValue.of(VALIDATE_INTERVAL, TimeUnit.SECONDS))
            .setConnectionTimeToLive(TimeValue.NEG_ONE_SECOND).setMaxConnPerRoute(config.getDefaultMaxPerRoute())
            .setMaxConnTotal(config.getMaxTotal()).setDnsResolver(dnsResolver)
            .setSchemePortResolver(new DefaultSchemePortResolver());

        return connectionManagerBuilder.build();
    }
}
