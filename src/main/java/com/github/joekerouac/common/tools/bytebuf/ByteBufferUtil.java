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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * byteBuffer工具
 * 
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ByteBufferUtil {

    /**
     * 位移使用，位移8bit
     */
    private static final int BYTE_BITS = 8;

    /**
     * 位移使用，位移16bit
     */
    private static final int SHORT_BITS = 16;

    /**
     * 定义3byte的长度：3
     */
    private static final int BYTES24 = 3;

    /**
     * 将指定数据写入byteBuffer中，并且在写入数据前先写入1byte的数据长度信息（数据长度不能超过1byte的最大值）
     * 
     * @param data
     *            要写入的数据
     * @param buffer
     *            指定ByteBuffer
     */
    public static void putBytes8(byte[] data, ByteBuffer buffer) {
        writeInt8(data.length, buffer);
        if (data.length > 0) {
            buffer.put(data);
        }
    }

    /**
     * 将指定数据写入byteBuffer中，并且在写入数据前先写入2byte的数据长度信息（数据长度不能超过2byte的最大值）
     * 
     * @param data
     *            要写入的数据
     * @param buffer
     *            指定ByteBuffer
     */
    public static void putBytes16(byte[] data, ByteBuffer buffer) {
        writeInt16(data.length, buffer);
        if (data.length > 0) {
            buffer.put(data);
        }
    }

    /**
     * 将指定数据写入byteBuffer中，并且在写入数据前先写入3byte的数据长度信息（数据长度不能超过3byte的最大值）
     * 
     * @param data
     *            要写入的数据
     * @param buffer
     *            指定ByteBuffer
     */
    public static void putBytes24(byte[] data, ByteBuffer buffer) {
        writeInt24(data.length, buffer);
        if (data.length > 0) {
            buffer.put(data);
        }
    }

    /**
     * 将指定数据写入byteBuffer中，并且在写入数据前先写入4byte的数据长度信息（数据长度不能超过4byte的最大值）
     * 
     * @param data
     *            要写入的数据
     * @param buffer
     *            指定ByteBuffer
     */
    public static void putBytes32(byte[] data, ByteBuffer buffer) {
        writeInt32(data.length, buffer);
        if (data.length > 0) {
            buffer.put(data);
        }
    }

    /**
     * 写出int数据的低8bit
     * 
     * @param data
     *            要写出的数据
     * @param buffer
     *            指定ByteBuffer
     */
    public static void writeInt8(int data, ByteBuffer buffer) {
        buffer.put((byte)data);
    }

    /**
     * 写出int数据的低16bit
     * 
     * @param data
     *            要写出的数据
     * @param buffer
     *            指定ByteBuffer
     */
    public static void writeInt16(int data, ByteBuffer buffer) {
        buffer.putShort((short)data);
    }

    /**
     * 写出int数据的低24bit
     * 
     * @param data
     *            要写出的数据
     * @param buffer
     *            指定ByteBuffer
     */
    public static void writeInt24(int data, ByteBuffer buffer) {
        buffer.put((byte)(data >>> SHORT_BITS));
        buffer.putShort((short)data);
    }

    /**
     * 写出int数据（4byte全写出）
     * 
     * @param data
     *            要写出的数据
     * @param buffer
     *            指定ByteBuffer
     */
    public static void writeInt32(int data, ByteBuffer buffer) {
        buffer.putInt(data);
    }

    /**
     * 写出long数据（8byte全写出）
     * 
     * @param data
     *            要写出的数据
     * @param buffer
     *            指定ByteBuffer
     */
    public static void writeLong(long data, ByteBuffer buffer) {
        buffer.putLong(data);
    }

    /**
     * 从ByteBuffer读取8字节数据，合并为一个int
     * 
     * @param buffer
     *            ByteBuffer
     * @return 从当前位置往后读取8字节数据，合并为int返回
     */
    public static int mergeReadInt8(ByteBuffer buffer) {
        return Byte.toUnsignedInt(buffer.get());
    }

    /**
     * 从ByteBuffer读取16字节数据，合并为一个int
     * 
     * @param buffer
     *            ByteBuffer
     * @return 从当前位置往后读取16字节数据，合并为int返回
     */
    public static int mergeReadInt16(ByteBuffer buffer) {
        return Short.toUnsignedInt(buffer.getShort());
    }

    /**
     * 从ByteBuffer读取24字节数据，合并为一个int
     * 
     * @param buffer
     *            ByteBuffer
     * @return 从当前位置往后读取24字节数据，合并为int返回
     */
    public static int mergeReadInt24(ByteBuffer buffer) {
        byte[] data = new byte[BYTES24];
        buffer.get(data);
        return Byte.toUnsignedInt(data[0]) << SHORT_BITS | Byte.toUnsignedInt(data[1]) << BYTE_BITS
            | Byte.toUnsignedInt(data[2]);
    }

    /**
     * 从ByteBuffer读取32字节数据，合并为一个int
     * 
     * @param buffer
     *            ByteBuffer
     * @return 从当前位置往后读取32字节数据，合并为int返回
     */
    public static int mergeReadInt32(ByteBuffer buffer) {
        return buffer.getInt();
    }

    /**
     * 从ByteBuffer读取64字节数据，合并为一个long
     * 
     * @param buffer
     *            ByteBuffer
     * @return 从当前位置往后读取64字节数据，合并为long返回
     */
    public static long mergeReadLong(ByteBuffer buffer) {
        return buffer.getLong();
    }

    /**
     * 从ByteBuffer中读取一个字节的长度信息，然后继续读取读取该长度的数据
     * 
     * @param buffer
     *            buffer
     * @return 数据
     */
    public static byte[] getInt8(ByteBuffer buffer) {
        return get(buffer, mergeReadInt8(buffer));
    }

    /**
     * 从ByteBuffer中读取两个字节的长度信息，然后继续读取读取该长度的数据
     * 
     * @param buffer
     *            buffer
     * @return 数据
     */
    public static byte[] getInt16(ByteBuffer buffer) {
        return get(buffer, mergeReadInt16(buffer));
    }

    /**
     * 从ByteBuffer中读取三个字节的长度信息，然后继续读取读取该长度的数据
     * 
     * @param buffer
     *            buffer
     * @return 数据
     */
    public static byte[] getInt24(ByteBuffer buffer) {
        return get(buffer, mergeReadInt24(buffer));
    }

    /**
     * 从ByteBuffer中读取四个字节的长度信息，然后继续读取读取该长度的数据
     * 
     * @param buffer
     *            buffer
     * @return 数据
     */
    public static byte[] getInt32(ByteBuffer buffer) {
        return get(buffer, mergeReadInt32(buffer));
    }

    /**
     * 从ByteBuffer中读取指定长度的数据
     * 
     * @param buffer
     *            buffer
     * @param len
     *            长度信息
     * @return 指定长度的数据
     */
    public static byte[] get(ByteBuffer buffer, int len) {
        if (len < 0) {
            throw new IllegalArgumentException("要读取的长度不能小于0");
        }
        if (len == 0) {
            return new byte[0];
        }
        byte[] data = new byte[len];
        buffer.get(data);
        return data;
    }

}
