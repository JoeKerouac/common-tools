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
package com.github.joekerouac.common.tools.binary;

import java.nio.ByteBuffer;
import java.util.Random;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.github.joekerouac.common.tools.binary.annotations.Endian;
import com.github.joekerouac.common.tools.binary.annotations.Size;
import com.github.joekerouac.common.tools.string.StringUtils;

/**
 * 测试
 *
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 * @since 1.0.0
 */
public class BinaryReadUtilTest {

    @Test
    public void baseTest() throws Exception {
        byte[] data = new byte[8];
        data[0] = (byte)0xff;
        data[1] = (byte)0xfe;
        data[2] = (byte)0xfd;
        data[3] = (byte)0xfc;
        data[4] = (byte)0xfb;
        data[5] = (byte)0xfa;
        data[6] = (byte)0xf9;
        data[7] = (byte)0xf8;

        final BinaryData1 binaryData1 = BinaryReadUtil.binaryRead(ByteBuffer.wrap(data), BinaryData1.class);
        Assert.assertEquals(binaryData1.data, data);
        final BinaryData2 binaryData2 = BinaryReadUtil.binaryRead(ByteBuffer.wrap(data), BinaryData2.class);
        Assert.assertEquals(binaryData2.data, -283686952306184L);
        final BinaryData3 binaryData3 = BinaryReadUtil.binaryRead(ByteBuffer.wrap(data), BinaryData3.class);
        Assert.assertEquals(binaryData3.data1, -66052);
        Assert.assertEquals(binaryData3.data2, -67438088);
        final BinaryData4 binaryData4 = BinaryReadUtil.binaryRead(ByteBuffer.wrap(data), BinaryData4.class);
        // 大端序
        Assert.assertEquals(binaryData4.data1, Byte.toUnsignedInt(data[0]) << 24 | Byte.toUnsignedInt(data[1]) << 16
            | Byte.toUnsignedInt(data[2]) << 8 | Byte.toUnsignedInt(data[3]));
        // 小端序
        Assert.assertEquals(binaryData4.data2, Byte.toUnsignedInt(data[7]) << 24 | Byte.toUnsignedInt(data[6]) << 16
            | Byte.toUnsignedInt(data[5]) << 8 | Byte.toUnsignedInt(data[4]));

    }

    @Test
    public void randomTest() throws Exception {
        byte[] data = new byte[8];

        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            random.nextBytes(data);
            final BinaryData1 binaryData1 = BinaryReadUtil.binaryRead(ByteBuffer.wrap(data), BinaryData1.class);
            Assert.assertEquals(binaryData1.data, data);

            final BinaryData2 binaryData2 = BinaryReadUtil.binaryRead(ByteBuffer.wrap(data), BinaryData2.class);
            Assert.assertEquals(Long.toBinaryString(binaryData2.data), toBinary(data));

            final BinaryData5 binaryData5 = BinaryReadUtil.binaryRead(ByteBuffer.wrap(data), BinaryData5.class);
            Assert.assertEquals(Long.toBinaryString(binaryData5.data), toLittleEndianBinary(data));
        }
    }

    private String toLittleEndianBinary(byte[] data) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (int i = data.length - 1; i >= 0; i--) {
            byte d = data[i];
            String append = Integer.toBinaryString(Byte.toUnsignedInt(d));
            if (first && d == 0) {
                append = "";
            } else {
                if (!first && append.length() < 8) {
                    append = StringUtils.copy("0", 8 - append.length()) + append;
                }
                first = false;
            }
            sb.append(append);
        }

        return sb.toString();
    }

    private String toBinary(byte[] data) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (final byte d : data) {
            String append = Integer.toBinaryString(Byte.toUnsignedInt(d));
            if (first && d == 0) {
                append = "";
            } else {
                if (!first && append.length() < 8) {
                    append = StringUtils.copy("0", 8 - append.length()) + append;
                }
                first = false;
            }
            sb.append(append);
        }

        return sb.toString();
    }

    public static class BinaryData1 {

        @Size(8)
        private byte[] data;

    }

    public static class BinaryData2 {
        private long data;
    }

    public static class BinaryData3 {
        private int data1;
        private int data2;
    }

    @Endian(little = true)
    public static class BinaryData4 {
        @Endian
        private int data1;

        private int data2;
    }

    @Endian(little = true)
    public static class BinaryData5 {

        @Size(8)
        private long data;

    }

}
