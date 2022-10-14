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
package com.github.joekerouac.common.tools.bytebuf;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public class ByteBufferUtilTest {

    @Test
    public void baseTest() {
        ByteBuffer buffer = ByteBuffer.allocate(512);

        // 先测试基本的写入，注意，这里正好构造的是二进制位全是1的数字，正好可以测试符号位存在数的时候行为是否正确（对于Java来说，数值的第一个bit
        // 表示符号位，1是负数，0是正数，也就是对于byte来说，实际上只有7位大小+1位符号位）
        {
            ByteBufferUtil.writeInt8(0Xff, buffer);
            buffer.position(0);
            Assert.assertEquals(buffer.get(), -1);
            buffer.position(0);

            ByteBufferUtil.writeInt16(0xffff, buffer);
            buffer.position(0);
            Assert.assertEquals(buffer.getShort(), -1);
            buffer.position(0);

            ByteBufferUtil.writeInt24(0xffffff, buffer);
            buffer.position(0);
            byte[] data = new byte[3];
            buffer.get(data);
            Assert.assertEquals(data[0], -1);
            Assert.assertEquals(data[1], -1);
            Assert.assertEquals(data[2], -1);
            buffer.position(0);

            ByteBufferUtil.writeInt32(0xffffffff, buffer);
            buffer.position(0);
            Assert.assertEquals(buffer.getInt(), -1);
            buffer.position(0);

            ByteBufferUtil.writeLong(0xffffffffffffffffL, buffer);
            buffer.position(0);
            Assert.assertEquals(buffer.getLong(), -1);
            buffer.position(0);
        }

        // 写入已经测试成功了，说明write的行为是正确的，后边就可以使用write并认为是符合预期的
        {
            ByteBufferUtil.writeInt8(0xff, buffer);
            buffer.position(0);
            Assert.assertEquals(ByteBufferUtil.mergeReadInt8(buffer), 0xff);
            buffer.position(0);

            ByteBufferUtil.writeInt16(0xffff, buffer);
            buffer.position(0);
            Assert.assertEquals(ByteBufferUtil.mergeReadInt16(buffer), 0xffff);
            buffer.position(0);

            ByteBufferUtil.writeInt24(0xffffff, buffer);
            buffer.position(0);
            Assert.assertEquals(ByteBufferUtil.mergeReadInt24(buffer), 0xffffff);
            buffer.position(0);

            ByteBufferUtil.writeInt32(0xffffffff, buffer);
            buffer.position(0);
            Assert.assertEquals(ByteBufferUtil.mergeReadInt32(buffer), 0xffffffff);
            buffer.position(0);

            ByteBufferUtil.writeLong(0xffffffffffffffffL, buffer);
            buffer.position(0);
            Assert.assertEquals(ByteBufferUtil.mergeReadLong(buffer), 0xffffffffffffffffL);
            buffer.position(0);
        }

        {
            ByteBufferUtil.writeInt8(8, buffer);
            ByteBufferUtil.writeLong(0xffffffffffffffffL, buffer);
            buffer.position(0);
            check(ByteBufferUtil.getInt8(buffer));
            buffer.position(0);

            ByteBufferUtil.writeInt16(8, buffer);
            ByteBufferUtil.writeLong(0xffffffffffffffffL, buffer);
            buffer.position(0);
            check(ByteBufferUtil.getInt16(buffer));
            buffer.position(0);

            ByteBufferUtil.writeInt24(8, buffer);
            ByteBufferUtil.writeLong(0xffffffffffffffffL, buffer);
            buffer.position(0);
            check(ByteBufferUtil.getInt24(buffer));
            buffer.position(0);

            ByteBufferUtil.writeInt32(8, buffer);
            ByteBufferUtil.writeLong(0xffffffffffffffffL, buffer);
            buffer.position(0);
            check(ByteBufferUtil.getInt32(buffer));
            buffer.position(0);

            ByteBufferUtil.writeLong(0xffffffffffffffffL, buffer);
            buffer.position(0);
            check(ByteBufferUtil.get(buffer, 8));
            buffer.position(0);
        }

        {
            // 测试put，依赖于上边的write测试，只有上边的write测试是正确的这个才会是正确的
            byte[] data = new byte[8];
            Arrays.fill(data, (byte)0xff);
            ByteBufferUtil.putBytes8(data, buffer);
            buffer.position(0);
            check(ByteBufferUtil.getInt8(buffer));
            buffer.position(0);

            ByteBufferUtil.putBytes16(data, buffer);
            buffer.position(0);
            check(ByteBufferUtil.getInt16(buffer));
            buffer.position(0);

            ByteBufferUtil.putBytes24(data, buffer);
            buffer.position(0);
            check(ByteBufferUtil.getInt24(buffer));
            buffer.position(0);

            ByteBufferUtil.putBytes32(data, buffer);
            buffer.position(0);
            check(ByteBufferUtil.getInt32(buffer));
            buffer.position(0);
        }
    }

    private void check(byte[] data) {
        for (byte datum : data) {
            Assert.assertEquals(datum, -1);
        }
    }

}
