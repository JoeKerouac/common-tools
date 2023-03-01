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

import org.apache.hc.client5.http.async.AsyncExecChainHandler;
import org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder;
import org.apache.hc.core5.http.config.NamedElementChain;

import brave.Tracing;
import brave.http.HttpTracing;

/**
 * @author JoeKerouac
 * @date 2023-02-16 14:47
 * @since 1.0.0
 */
public class TracingHttpAsyncClientBuilder extends HttpAsyncClientBuilder {

    private final Tracing tracing;

    public TracingHttpAsyncClientBuilder(Tracing tracing) {
        this.tracing = tracing;
    }

    @Override
    protected void customizeExecChain(NamedElementChain<AsyncExecChainHandler> execChainDefinition) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        execChainDefinition.addFirst(new TracingProtocolExec(httpTracing), "tracing");
        super.customizeExecChain(execChainDefinition);
    }

}
