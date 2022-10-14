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
package com.github.joekerouac.common.tools.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class IOUtilsTest {

    @Test
    public void simpleTest() {
        byte[] data = new byte[] {0, 1, 2, 3, 4};

        {
            CustomByteArrayInputStream input = new CustomByteArrayInputStream(data);
            byte[] bytes = IOUtils.read(input);
            Assert.assertEquals(data, bytes);
            Assert.assertTrue(input.close);
        }

        {
            CustomByteArrayInputStream input = new CustomByteArrayInputStream(data);
            byte[] bytes = IOUtils.read(input, false);
            Assert.assertEquals(data, bytes);
            Assert.assertFalse(input.close);
        }

        {
            CustomByteArrayInputStream input = new CustomByteArrayInputStream(data);
            CustomByteArrayOutputStream out = new CustomByteArrayOutputStream();
            IOUtils.write(out, input);
            byte[] bytes = out.toByteArray();
            Assert.assertEquals(data, bytes);
            Assert.assertFalse(input.close);
            Assert.assertFalse(out.close);
            IOUtils.close(input);
            IOUtils.close(out);
            Assert.assertTrue(input.close);
            Assert.assertTrue(out.close);
        }

        {
            CustomByteArrayInputStream input = new CustomByteArrayInputStream(data);
            CustomByteArrayOutputStream out = new CustomByteArrayOutputStream();
            IOUtils.write(out, input, true);
            byte[] bytes = out.toByteArray();
            Assert.assertEquals(data, bytes);
            Assert.assertTrue(input.close);
            Assert.assertFalse(out.close);
            IOUtils.close(out);
            Assert.assertTrue(out.close);
        }

    }

    private static class CustomByteArrayInputStream extends ByteArrayInputStream {

        private boolean close = false;

        public CustomByteArrayInputStream(byte[] buf) {
            super(buf);
        }

        @Override
        public void close() throws IOException {
            super.close();
            close = true;
        }
    }

    private static class CustomByteArrayOutputStream extends ByteArrayOutputStream {

        private boolean close = false;

        @Override
        public void close() throws IOException {
            super.close();
            close = true;
        }
    }

}
