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
 * HttpClient??????????????????????????????
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
@CustomLog
public final class IHttpClient implements AutoCloseable {

    /**
     * ???????????????
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
     * ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    private static final int VALIDATE_INTERVAL = 60;

    /**
     * ??????????????????
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
     * client ID?????????????????????HttpClient
     */
    private String id;

    /**
     * HttpClient
     */
    private CloseableHttpAsyncClient httpClient;

    /**
     * cookie store ?????????cookie
     */
    private CookieStore cookieStore;

    /**
     * ??????
     */
    private final IHttpClientConfig config;

    /**
     * ??????
     */
    private final Logger logger;

    /**
     * ??????client?????????cookieStore
     *
     * @param config
     *            client??????
     * @param cookieStore
     *            cookieStore
     * @param sslcontext
     *            sslcontext
     * @param logger
     *            ??????
     * @param noRedirect
     *            ?????????true???????????????????????????????????????
     */
    @Builder
    private IHttpClient(IHttpClientConfig config, CookieStore cookieStore, SSLContext sslcontext, boolean noRedirect,
        Logger logger) {
        this.config = config == null ? new IHttpClientConfig() : config;
        CookieStore cookieStoreNotNull = cookieStore == null ? new CookieStoreImpl() : cookieStore;
        SSLContext sslContextNotNull = sslcontext == null ? SSLContexts.createSystemDefault() : sslcontext;
        this.logger = logger == null ? LOGGER : logger;
        this.init(this.config, cookieStoreNotNull, sslContextNotNull, noRedirect);
    }

    /**
     * ??????HTTP??????
     *
     * @param request
     *            ?????????
     * @param futureCallback
     *            ????????????
     * @return ????????????future
     */
    Future<IHttpResponse> execute(AbstractIHttpRequest request, FutureCallback<IHttpResponse> futureCallback) {
        Assert.argNotNull(request, "request");
        if (LOGGER.isDebugEnabled()) {
            logger.debug("????????????????????????[{}]", request);
        }

        FutureCallback<IHttpResponse> callback = futureCallback == null ? EMPTY_CALLBACK : futureCallback;

        AsyncRequestProducer requestProducer = buildSimpleHttpRequest(request);

        // ????????????
        Future<SimpleHttpResponse> future = httpClient.execute(requestProducer, SimpleResponseConsumer.create(),
            new org.apache.hc.core5.concurrent.FutureCallback<SimpleHttpResponse>() {
                @Override
                public void completed(SimpleHttpResponse result) {
                    IHttpResponse response = new IHttpResponse(result);
                    try {
                        callback.success(response);
                    } catch (Throwable throwable) {
                        LOGGER.warn(throwable, "????????????[success]??????");
                    }

                    callback.complete(response, null, 0);
                }

                @Override
                public void failed(Exception ex) {
                    try {
                        callback.failed(ex);
                    } catch (Throwable throwable) {
                        LOGGER.warn(throwable, "????????????[failed]??????");
                    }

                    callback.complete(null, ex, 1);
                }

                @Override
                public void cancelled() {
                    try {
                        callback.cancelled();
                    } catch (Throwable throwable) {
                        LOGGER.warn(throwable, "????????????[failed]??????");
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
     * ????????????cookie
     *
     * @return ??????cookie??????
     */
    public List<Cookie> getCookies() {
        return cookieStore.getCookies();
    }

    /**
     * ????????????cookie
     *
     * @param name
     *            cookie???
     * @return cookie???????????????cookie?????????????????????null
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
     * ??????cookie
     *
     * @param cookie
     *            ????????????cookie
     */
    public void addCookie(Cookie cookie) {
        cookieStore.addCookie(cookie);
    }

    /**
     * ??????httpClient???ID
     *
     * @return client???ID
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

        // ??????????????????
        AbstractHttpConfig config = request.getHttpConfig() == null ? this.config : request.getHttpConfig();
        httpRequest.setConfig(buildRequestConfig(config));
        request.getHeaders().forEach(httpRequest::addHeader);

        // ?????????post?????????????????????????????????
        if (request instanceof IHttpPost) {
            IHttpPost httpPost = (IHttpPost)request;

            if (!CollectionUtil.isEmpty(httpPost.getFiles())) {
                // ?????????????????????????????????????????????MultipartEntity
                MultipartEntityBuilder entityBuilder =
                    MultipartEntityBuilder.create().setCharset(Charset.forName(request.getCharset()));
                httpPost.getFiles()
                    .forEach(file -> entityBuilder.addBinaryBody(file.getName(), file.getFile(),
                        ContentType.create(file.getContentType(),
                            StringUtils.getOrDefault(file.getCharset(), Charset.defaultCharset().name())),
                        file.getFileName()));
                // ???body?????????form??????
                if (StringUtils.isNotBlank(httpPost.getBody())) {
                    final String body = httpPost.getBody();
                    final String[] split = body.split("&");
                    Arrays.stream(split).forEach(kv -> {
                        final String[] kvArr = kv.split("=");
                        Assert.assertTrue(kvArr.length == 2,
                            StringUtils.format("??????????????????????????????body?????????form???????????????body??????????????????[{}]", body),
                            ExceptionProviderConst.IllegalArgumentExceptionProvider);
                        entityBuilder.addTextBody(kvArr[0], kvArr[1]);
                    });
                }

                httpRequest.setEntity(entityBuilder.build());
            } else if (StringUtils.isNotBlank(httpPost.getBody())) {
                // ????????????StringEntity
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
                throw new UnknownException("?????????????????????????????????????????????????????????????????????", e);
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
                // ????????????
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
     * ??????HttpClient????????????
     *
     * @param config
     *            ????????????
     * @return requestConfig
     */
    private RequestConfig buildRequestConfig(AbstractHttpConfig config) {
        logger.debug("??????????????????");
        RequestConfig requestConfig =
            RequestConfig.custom().setConnectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
                .setResponseTimeout(config.getSocketTimeout(), TimeUnit.MILLISECONDS)
                .setConnectionRequestTimeout(config.getConnectionRequestTimeout(), TimeUnit.MILLISECONDS).build();
        logger.debug("??????????????????[{}]", requestConfig);
        return requestConfig;
    }

    /**
     * ?????????httpClient???CookieStore
     *
     * @param config
     *            client????????????
     */
    private void init(IHttpClientConfig config, CookieStore cookieStore, SSLContext sslcontext, boolean noRedirect) {
        logger.debug("???????????????HttpClient");

        // ??????????????????httpClient
        HttpAsyncClientBuilder builder = HttpAsyncClients.custom();

        ThreadFactory threadFactory = config.getThreadFactory();
        if (threadFactory == null) {
            threadFactory = new UncaughtExceptionHandlerThreadFactory(new NamedThreadFactory("http-client-io", true),
                (t, e) -> LOGGER.warn(e, "HTTP IO/?????? ????????????"));
        }
        builder.setThreadFactory(threadFactory);

        {
            // http1??????
            IHttp1config iHttp1config = config.getHttp1config();
            Http1Config http1Config = Http1Config.custom().setBufferSize(iHttp1config.getHttpSessionBufferSize())
                .setChunkSizeHint(iHttp1config.getHttpChunkSizeHint())
                .setMaxLineLength(iHttp1config.getHttpMaxLineLength())
                .setMaxHeaderCount(iHttp1config.getHttpMaxHeaderCount()).build();
            builder.setHttp1Config(http1Config);
            logger.debug("http1?????????[{}]", iHttp1config);
        }

        {
            // socket??????
            IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(config.getIoThreadCount())
                .setTcpNoDelay(config.isTcpNoDelay()).setSndBufSize(config.getSndBufSize())
                .setSoTimeout(config.getSocketTimeout(), TimeUnit.MILLISECONDS).setSoKeepAlive(config.isKeepAlive())
                .setRcvBufSize(config.getRcvBufSize()).build();
            builder.setIOReactorConfig(ioReactorConfig);
            logger.debug("socket?????????[{}]", ioReactorConfig);
        }

        {
            // ????????????
            // MalformedInputAction: ????????????????????????????????????????????????null???????????????REPORT???
            // UnmappableInputAction: ????????????????????????????????????????????????null???????????????REPORT???
            // CodingErrorAction.IGNORE: ????????????????????????
            // CodingErrorAction.REPLACE: ?????????????????????????????????????????????????????????????????????
            // CodingErrorAction.REPORT: ???????????????
            CharCodingConfig charCodingConfig = CharCodingConfig.custom().setCharset(config.getCharset())
                .setMalformedInputAction(CodingErrorAction.REPORT).setUnmappableInputAction(CodingErrorAction.REPORT)
                .build();
            builder.setCharCodingConfig(charCodingConfig);
            logger.debug("??????????????????????????????{}", charCodingConfig);
        }

        {
            HttpProxy proxy = config.getProxy();
            if (proxy != null) {
                HttpHost httpHost = new HttpHost(proxy.getHost(), proxy.getPort());
                builder.setProxy(httpHost);

                // ??????????????????????????????????????????????????????????????????????????????????????????
                if (StringUtils.isNotBlank(proxy.getUsername())) {
                    BasicCredentialsProvider provider = new BasicCredentialsProvider();
                    provider.setCredentials(new AuthScope(httpHost),
                        new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword().toCharArray()));
                    builder.setDefaultCredentialsProvider(provider);
                }
            }

            if (noRedirect) {
                logger.debug("??????????????????????????????????????????????????????");
                builder.setRedirectStrategy(NO_REDIRECT);
            }

            if (StringUtils.isNotBlank(config.getUserAgent())) {
                builder.setUserAgent(config.getUserAgent());
                logger.debug("??????????????????????????????[{}]", config.getUserAgent());
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
            // ????????????
            RequestConfig defaultRequestConfig =
                RequestConfig.custom().setCookieSpec(StandardCookieSpec.RELAXED).setExpectContinueEnabled(true)
                    .setTargetPreferredAuthSchemes(Arrays.asList(StandardAuthScheme.BASIC, StandardAuthScheme.DIGEST))
                    .setProxyPreferredAuthSchemes(Collections.singletonList(StandardAuthScheme.BASIC)).build();
            builder.setDefaultRequestConfig(defaultRequestConfig);
            logger.debug("??????????????????????????????[{}]", defaultRequestConfig);
        }

        CloseableHttpAsyncClient httpclient = builder.build();
        httpclient.start();
        this.httpClient = httpclient;
        this.cookieStore = cookieStore;
        this.id = String.valueOf(System.currentTimeMillis());
        logger.debug("HttpClient???????????????");
    }

    /**
     * ????????????????????????
     *
     * @param config
     *            client??????
     * @param sslcontext
     *            ???????????????
     * @return ??????????????????
     */
    private AsyncClientConnectionManager buildAsyncClientConnectionManager(IHttpClientConfig config,
        SSLContext sslcontext) {
        // tls??????
        TlsStrategy strategy = ClientTlsStrategyBuilder.create().setSslContext(sslcontext).build();

        // ?????????DNS
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

        // ???????????????
        // PoolConcurrencyPolicy.STRICT: ????????????????????????????????????LAX??????????????????????????????????????????
        // PoolReusePolicy.FIFO: ???????????????????????????????????????????????????????????????????????????????????????????????????LIFO???????????????????????????
        // TimeValue.NEG_ONE_SECOND: ????????????????????????????????????????????????????????????0????????????????????????
        // ????????????1S???????????????
        // ????????????????????????????????????????????????????????????????????????????????????????????????
        // ???????????????????????????????????????300???????????????????????????????????????3000?????????
        connectionManagerBuilder.setTlsStrategy(strategy).setConnPoolPolicy(PoolReusePolicy.FIFO)
            .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT)
            .setValidateAfterInactivity(TimeValue.of(VALIDATE_INTERVAL, TimeUnit.SECONDS))
            .setConnectionTimeToLive(TimeValue.NEG_ONE_SECOND).setMaxConnPerRoute(config.getDefaultMaxPerRoute())
            .setMaxConnTotal(config.getMaxTotal()).setDnsResolver(dnsResolver)
            .setSchemePortResolver(new DefaultSchemePortResolver());

        return connectionManagerBuilder.build();
    }
}
