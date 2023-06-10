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
package com.github.joekerouac.common.tools.net.http.entity;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.nio.entity.AbstractBinAsyncEntityConsumer;

import com.github.joekerouac.common.tools.io.DataFilter;
import com.github.joekerouac.common.tools.io.InMemoryFile;

/**
 * @author JoeKerouac
 * @date 2023-06-08 15:01
 * @since 2.0.3
 */
public class StreamAsyncEntityConsumer extends AbstractBinAsyncEntityConsumer<InMemoryFile> {

    private InMemoryFile file;

    public StreamAsyncEntityConsumer(int initBufferSize, int writeFileOnLarge, DataFilter filter) {
        this.file = new InMemoryFile(initBufferSize, writeFileOnLarge, filter);
    }

    @Override
    protected void streamStart(ContentType contentType) throws HttpException, IOException {
        if (contentType != null) {
            file.setCharset(contentType.getCharset());
        }
    }

    @Override
    protected int capacityIncrement() {
        return Integer.MAX_VALUE;
    }

    @Override
    protected void data(ByteBuffer src, boolean endOfStream) throws IOException {
        if (src == null) {
            return;
        }
        if (src.hasArray()) {
            file.write(src.array(), src.arrayOffset() + src.position(), src.remaining());
        } else {
            int remaining = src.remaining();
            if (remaining > 0) {
                byte[] data = new byte[remaining];
                src.get(data);
                file.write(data, 0, remaining);
            }
        }
    }

    @Override
    protected InMemoryFile generateContent() throws IOException {
        // 关闭输出
        file.writeFinish();
        return file;
    }

    @Override
    public void releaseResources() {
        file = null;
    }
}
