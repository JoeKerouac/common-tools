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
package com.github.joekerouac.common.tools.net.http.trace.brave;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.hc.client5.http.async.AsyncExecCallback;
import org.apache.hc.client5.http.async.AsyncExecChain;
import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.nio.AsyncDataConsumer;
import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.protocol.HttpCoreContext;

import brave.Span;
import brave.Tracer;
import brave.http.HttpClientHandler;
import brave.http.HttpClientRequest;
import brave.http.HttpClientResponse;
import brave.http.HttpTracing;
import brave.sampler.SamplerFunction;

/**
 *
 * @author JoeKerouac
 * @date 2023-02-16 14:50
 * @since 1.0.0
 */
public class TracingProtocolExec implements AsyncExecChainHandler {

    final Tracer tracer;

    final SamplerFunction<brave.http.HttpRequest> httpSampler;

    final HttpClientHandler<HttpClientRequest, HttpClientResponse> handler;

    TracingProtocolExec(HttpTracing httpTracing) {
        this.tracer = httpTracing.tracing().tracer();
        this.httpSampler = httpTracing.clientRequestSampler();
        this.handler = HttpClientHandler.create(httpTracing);
    }

    @Override
    public void execute(HttpRequest request, AsyncEntityProducer entityProducer, AsyncExecChain.Scope scope,
        AsyncExecChain chain, AsyncExecCallback asyncExecCallback) throws HttpException, IOException {
        // 注意，如果发生了302重定向，那么这个会执行两次
        HttpClientContext context = scope.clientContext;
        HttpHost targetHost = scope.route.getTargetHost();
        HttpRequestWrapper requestWrapper = new HttpRequestWrapper(scope.originalRequest);
        String spanAttrName = Span.class.getName();
        Span span = (Span)context.getAttribute(spanAttrName);
        if (span == null) {
            span = tracer.nextSpan(httpSampler, requestWrapper);
            context.setAttribute(spanAttrName, span);
        }

        Span finalSpan = span;
        finalSpan.tag("http.url", requestWrapper.url());
        finalSpan.name(targetHost.getHostName());

        handler.handleSend(new HttpRequestWrapper(request), finalSpan);

        try (Tracer.SpanInScope ws = tracer.withSpanInScope(finalSpan)) {
            chain.proceed(request, entityProducer, scope, new AsyncExecCallback() {
                @Override
                public AsyncDataConsumer handleResponse(HttpResponse response, EntityDetails entityDetails)
                    throws HttpException, IOException {
                    try (Tracer.SpanInScope ws = tracer.withSpanInScope(finalSpan)) {
                        Object endpoint = context.getAttribute(HttpCoreContext.CONNECTION_ENDPOINT);
                        SocketAddress socketAddress;
                        if (endpoint instanceof EndpointDetails && (socketAddress =
                            ((EndpointDetails)endpoint).getRemoteAddress()) instanceof InetSocketAddress) {
                            InetSocketAddress inetSocketAddress = (InetSocketAddress)socketAddress;
                            finalSpan.remoteIpAndPort(inetSocketAddress.getAddress().getHostAddress(),
                                inetSocketAddress.getPort());
                        } else {
                            finalSpan.remoteIpAndPort(targetHost.getHostName(), targetHost.getPort());
                        }

                        AsyncDataConsumer consumer = asyncExecCallback.handleResponse(response, entityDetails);
                        handler.handleReceive(new HttpResponseWrapper(response), null, finalSpan);
                        return consumer;
                    }
                }

                @Override
                public void handleInformationResponse(HttpResponse response) throws HttpException, IOException {
                    asyncExecCallback.handleInformationResponse(response);
                }

                @Override
                public void completed() {
                    asyncExecCallback.completed();
                }

                @Override
                public void failed(Exception cause) {
                    try (Tracer.SpanInScope ws = tracer.withSpanInScope(finalSpan)) {
                        handler.handleReceive(null, cause, finalSpan);
                        asyncExecCallback.failed(cause);
                    }
                }
            });
        } catch (RuntimeException | HttpException | IOException e) {
            handler.handleReceive(null, e, finalSpan);
            throw e;
        }
    }

}
