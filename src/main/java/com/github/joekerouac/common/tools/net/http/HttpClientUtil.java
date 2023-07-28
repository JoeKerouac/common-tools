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

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.StandardAuthScheme;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.DefaultSchemePortResolver;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.DefaultRoutePlanner;
import org.apache.hc.client5.http.nio.AsyncClientConnectionManager;
import org.apache.hc.client5.http.protocol.RedirectStrategy;
import org.apache.hc.client5.http.ssl.ClientTlsStrategyBuilder;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.nio.ssl.TlsStrategy;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.http.ssl.SSLContexts;

import com.github.joekerouac.common.tools.net.http.config.HttpProxy;
import com.github.joekerouac.common.tools.net.http.config.IHttp1config;
import com.github.joekerouac.common.tools.net.http.config.IHttpClientConfig;
import com.github.joekerouac.common.tools.net.http.cookie.CookieStore;
import com.github.joekerouac.common.tools.net.http.cookie.CookieUtil;
import com.github.joekerouac.common.tools.net.http.cookie.impl.CookieStoreImpl;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.thread.NamedThreadFactory;
import com.github.joekerouac.common.tools.thread.UncaughtExceptionHandlerThreadFactory;

import lombok.CustomLog;

/**
 * @author JoeKerouac
 * @date 2023-07-27 15:21
 * @since 2.1.0
 */
@CustomLog
public class HttpClientUtil {

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
     * 构建CloseableHttpAsyncClient
     *
     * @return CloseableHttpAsyncClient
     */
    public static CloseableHttpAsyncClient buildCloseableHttpAsyncClient() {
        return buildCloseableHttpAsyncClient(null, null, null, false, null);
    }

    /**
     * 根据指定配置构建CloseableHttpAsyncClient
     *
     * @param config
     *            基础配置，允许为空
     * @return CloseableHttpAsyncClient
     */
    public static CloseableHttpAsyncClient buildCloseableHttpAsyncClient(IHttpClientConfig config) {
        return buildCloseableHttpAsyncClient(config, null, null, false, null);
    }

    /**
     * 根据指定配置构建CloseableHttpAsyncClient
     *
     * @param config
     *            基础配置，允许为空
     * @param httpAsyncClientBuilderSupplier
     *            HttpAsyncClientBuilder提供器，允许为空
     * @return CloseableHttpAsyncClient
     */
    public static CloseableHttpAsyncClient buildCloseableHttpAsyncClient(IHttpClientConfig config,
        Supplier<HttpAsyncClientBuilder> httpAsyncClientBuilderSupplier) {
        return buildCloseableHttpAsyncClient(config, null, null, false, httpAsyncClientBuilderSupplier);
    }

    /**
     * 根据指定配置构建CloseableHttpAsyncClient
     *
     * @param config
     *            基础配置，允许为空
     * @param cookieStore
     *            cookie store，允许为空
     * @param sslcontext
     *            ssl context，允许为空
     * @return CloseableHttpAsyncClient
     */
    public static CloseableHttpAsyncClient buildCloseableHttpAsyncClient(IHttpClientConfig config,
        CookieStore cookieStore, SSLContext sslcontext) {
        return buildCloseableHttpAsyncClient(config, cookieStore, sslcontext, false, null);

    }

    /**
     * 根据指定配置构建CloseableHttpAsyncClient
     *
     * @param config
     *            基础配置，允许为空
     * @param cookieStore
     *            cookie store，允许为空
     * @param sslcontext
     *            ssl context，允许为空
     * @param noRedirect
     *            是否不自动重定向，true表示不重定向，允许为空
     * @return CloseableHttpAsyncClient
     */
    public static CloseableHttpAsyncClient buildCloseableHttpAsyncClient(IHttpClientConfig config,
        CookieStore cookieStore, SSLContext sslcontext, boolean noRedirect) {
        return buildCloseableHttpAsyncClient(config, cookieStore, sslcontext, false, null);
    }

    /**
     * 根据指定配置构建CloseableHttpAsyncClient
     * 
     * @param config
     *            基础配置，允许为空
     * @param cookieStore
     *            cookie store，允许为空
     * @param sslcontext
     *            ssl context，允许为空
     * @param noRedirect
     *            是否不自动重定向，true表示不重定向，允许为空
     * @param httpAsyncClientBuilderSupplier
     *            HttpAsyncClientBuilder提供器，允许为空
     * @return CloseableHttpAsyncClient
     */
    public static CloseableHttpAsyncClient buildCloseableHttpAsyncClient(IHttpClientConfig config,
        CookieStore cookieStore, SSLContext sslcontext, boolean noRedirect,
        Supplier<HttpAsyncClientBuilder> httpAsyncClientBuilderSupplier) {
        config = config == null ? new IHttpClientConfig() : config;
        cookieStore = cookieStore == null ? new CookieStoreImpl() : cookieStore;
        sslcontext = sslcontext == null ? SSLContexts.createSystemDefault() : sslcontext;
        httpAsyncClientBuilderSupplier = httpAsyncClientBuilderSupplier == null ? HTTP_ASYNC_CLIENT_BUILDER_SUPPLIER
            : httpAsyncClientBuilderSupplier;

        LOGGER.debug("正在初始化HttpClient");

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
            LOGGER.debug("http1配置：[{}]", iHttp1config);
        }

        {
            // socket配置
            IOReactorConfig ioReactorConfig = IOReactorConfig.custom().setIoThreadCount(config.getIoThreadCount())
                .setTcpNoDelay(config.isTcpNoDelay()).setSndBufSize(config.getSndBufSize())
                .setSoTimeout(config.getSocketTimeout(), TimeUnit.MILLISECONDS).setSoKeepAlive(config.isKeepAlive())
                .setRcvBufSize(config.getRcvBufSize()).build();
            builder.setIOReactorConfig(ioReactorConfig);
            LOGGER.debug("socket配置：[{}]", ioReactorConfig);
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
            LOGGER.debug("默认连接编码配置为：{}", charCodingConfig);
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
                LOGGER.debug("用户设置不重定向，客户端将不会重定向");
                builder.setRedirectStrategy(NO_REDIRECT);
            }

            if (StringUtils.isNotBlank(config.getUserAgent())) {
                builder.setUserAgent(config.getUserAgent());
                LOGGER.debug("当前全局用户代理为：[{}]", config.getUserAgent());
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
                CookieStore finalCookieStore = cookieStore;
                httpClientCookieStore = new org.apache.hc.client5.http.cookie.CookieStore() {
                    @Override
                    public void addCookie(org.apache.hc.client5.http.cookie.Cookie cookie) {
                        finalCookieStore.addCookie(CookieUtil.convert(cookie));
                    }

                    @Override
                    public List<Cookie> getCookies() {
                        return finalCookieStore.getCookies().stream().map(CookieUtil::convert)
                            .collect(Collectors.toList());
                    }

                    @Override
                    public boolean clearExpired(Date date) {
                        return finalCookieStore.clearExpired(date);
                    }

                    @Override
                    public void clear() {
                        finalCookieStore.clear();
                    }
                };
            }
            builder.setDefaultCookieStore(httpClientCookieStore);
        }

        {
            InetAddress localAddress = config.getLocalAddress();
            if (localAddress != null) {
                builder.setRoutePlanner(new DefaultRoutePlanner(DefaultSchemePortResolver.INSTANCE) {
                    @Override
                    protected InetAddress determineLocalAddress(HttpHost firstHop, HttpContext context)
                        throws HttpException {
                        return localAddress;
                    }
                });
            }
        }

        {
            // 全局配置
            RequestConfig defaultRequestConfig =
                RequestConfig.custom().setCookieSpec(StandardCookieSpec.RELAXED).setExpectContinueEnabled(true)
                    .setTargetPreferredAuthSchemes(Arrays.asList(StandardAuthScheme.BASIC, StandardAuthScheme.DIGEST))
                    .setProxyPreferredAuthSchemes(Collections.singletonList(StandardAuthScheme.BASIC)).build();
            builder.setDefaultRequestConfig(defaultRequestConfig);
            LOGGER.debug("默认全局请求配置为：[{}]", defaultRequestConfig);
        }

        LOGGER.debug("HttpClient初始化完毕");
        return builder.build();
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
    private static AsyncClientConnectionManager buildAsyncClientConnectionManager(IHttpClientConfig config,
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
