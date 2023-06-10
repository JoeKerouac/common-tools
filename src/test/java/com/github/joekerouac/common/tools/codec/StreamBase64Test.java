package com.github.joekerouac.common.tools.codec;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author JoeKerouac
 * @date 2023-06-10 18:20
 * @since 2.0.3
 */
public class StreamBase64Test {

    @Test
    public void test() {
        for (int i = 0; i < 100000; i++) {
            decoderTest();
        }
    }

    public void decoderTest() {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] data = new byte[Math.max(0, Math.abs(new Random().nextInt())) % 300 + 30];
        new Random().nextBytes(data);

        byte[] encode = encoder.encode(data);

        String str = new String(encode, StandardCharsets.UTF_8);
        if (new Random().nextInt() % 2 == 1) {
            while (str.endsWith("=")) {
                str = str.substring(0, str.length() - 1);
            }
        }
        encode = str.getBytes(StandardCharsets.UTF_8);

        byte[] result;

        StreamBase64.Decoder decoder = StreamBase64.newDecoder();
        if (new Random().nextInt() % 2 == 1) {
            result = new byte[data.length];
            int offset = 0;
            int resultOffset = 0;
            while (offset < encode.length) {
                int len = Math.max(0, Math.abs(new Random().nextInt())) % 300 + 30;
                len = Math.min(len, encode.length - offset);
                resultOffset += decoder.update(encode, offset, len, result, resultOffset);
                offset += len;
            }

            byte[] bytes = decoder.doFinal();
            System.arraycopy(bytes, 0, result, resultOffset, bytes.length);
        } else {
            result = decoder.doFinal(encode);
        }

        Assert.assertArrayEquals(data, result);
    }

}
