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
package com.github.joekerouac.common.tools.crypto.constant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * int类型的静态字段的字段名必须是加密套件名，值是对应的id
 * <p>
 * 密码套件说明：TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384，
 * <li>其中TLS是固定的</li>
 * <li>ECDH_RSA表示密钥交换使用ECDH算法， 使用RSA算法做签名</li>
 * <li>AES_256_GCM表示使用AES算法，密钥256位，GCM是AES的加密模式</li>
 * <li>SHA384表示MAC算法使用的hash算法</li>
 *
 * @since 1.0.0
 * @author JoeKerouac
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CipherSuite {

    private static final int TLS_NULL_WITH_NULL_NULL = 0x0000;

    private static final int TLS_RSA_WITH_NULL_MD5 = 0x0001;

    private static final int TLS_RSA_WITH_NULL_SHA = 0x0002;

    private static final int TLS_RSA_EXPORT_WITH_RC4_40_MD5 = 0x0003;

    private static final int TLS_RSA_WITH_RC4_128_MD5 = 0x0004;

    private static final int TLS_RSA_WITH_RC4_128_SHA = 0x0005;

    private static final int TLS_RSA_EXPORT_WITH_RC2_CBC_40_MD5 = 0x0006;

    private static final int TLS_RSA_WITH_IDEA_CBC_SHA = 0x0007;

    private static final int TLS_RSA_EXPORT_WITH_DES40_CBC_SHA = 0x0008;

    private static final int TLS_RSA_WITH_DES_CBC_SHA = 0x0009;

    private static final int TLS_RSA_WITH_3DES_EDE_CBC_SHA = 0x000A;

    private static final int TLS_DH_DSS_EXPORT_WITH_DES40_CBC_SHA = 0x000B;

    private static final int TLS_DH_DSS_WITH_DES_CBC_SHA = 0x000C;

    private static final int TLS_DH_DSS_WITH_3DES_EDE_CBC_SHA = 0x000D;

    private static final int TLS_DH_RSA_EXPORT_WITH_DES40_CBC_SHA = 0x000E;

    private static final int TLS_DH_RSA_WITH_DES_CBC_SHA = 0x000F;

    private static final int TLS_DH_RSA_WITH_3DES_EDE_CBC_SHA = 0x0010;

    private static final int TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA = 0x0011;

    private static final int TLS_DHE_DSS_WITH_DES_CBC_SHA = 0x0012;

    private static final int TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA = 0x0013;

    private static final int TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA = 0x0014;

    private static final int TLS_DHE_RSA_WITH_DES_CBC_SHA = 0x0015;

    private static final int TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA = 0x0016;

    private static final int TLS_DH_anon_EXPORT_WITH_RC4_40_MD5 = 0x0017;

    private static final int TLS_DH_anon_WITH_RC4_128_MD5 = 0x0018;

    private static final int TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA = 0x0019;

    private static final int TLS_DH_anon_WITH_DES_CBC_SHA = 0x001A;

    private static final int TLS_DH_anon_WITH_3DES_EDE_CBC_SHA = 0x001B;

    /*
     * RFC 3268
     */
    public static final int TLS_RSA_WITH_AES_128_CBC_SHA = 0x002F;

    public static final int TLS_DH_DSS_WITH_AES_128_CBC_SHA = 0x0030;

    public static final int TLS_DH_RSA_WITH_AES_128_CBC_SHA = 0x0031;

    public static final int TLS_DHE_DSS_WITH_AES_128_CBC_SHA = 0x0032;

    public static final int TLS_DHE_RSA_WITH_AES_128_CBC_SHA = 0x0033;

    public static final int TLS_DH_anon_WITH_AES_128_CBC_SHA = 0x0034;

    public static final int TLS_RSA_WITH_AES_256_CBC_SHA = 0x0035;

    public static final int TLS_DH_DSS_WITH_AES_256_CBC_SHA = 0x0036;

    public static final int TLS_DH_RSA_WITH_AES_256_CBC_SHA = 0x0037;

    public static final int TLS_DHE_DSS_WITH_AES_256_CBC_SHA = 0x0038;

    public static final int TLS_DHE_RSA_WITH_AES_256_CBC_SHA = 0x0039;

    public static final int TLS_DH_anon_WITH_AES_256_CBC_SHA = 0x003A;

    /*
     * RFC 4279
     */
    public static final int TLS_PSK_WITH_RC4_128_SHA = 0x008A;

    public static final int TLS_PSK_WITH_3DES_EDE_CBC_SHA = 0x008B;

    public static final int TLS_PSK_WITH_AES_128_CBC_SHA = 0x008C;

    public static final int TLS_PSK_WITH_AES_256_CBC_SHA = 0x008D;

    public static final int TLS_DHE_PSK_WITH_RC4_128_SHA = 0x008E;

    public static final int TLS_DHE_PSK_WITH_3DES_EDE_CBC_SHA = 0x008F;

    public static final int TLS_DHE_PSK_WITH_AES_128_CBC_SHA = 0x0090;

    public static final int TLS_DHE_PSK_WITH_AES_256_CBC_SHA = 0x0091;

    public static final int TLS_RSA_PSK_WITH_RC4_128_SHA = 0x0092;

    public static final int TLS_RSA_PSK_WITH_3DES_EDE_CBC_SHA = 0x0093;

    public static final int TLS_RSA_PSK_WITH_AES_128_CBC_SHA = 0x0094;

    public static final int TLS_RSA_PSK_WITH_AES_256_CBC_SHA = 0x0095;

    /*
     * RFC 4492
     */
    public static final int TLS_ECDH_ECDSA_WITH_NULL_SHA = 0xC001;

    public static final int TLS_ECDH_ECDSA_WITH_RC4_128_SHA = 0xC002;

    public static final int TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA = 0xC003;

    public static final int TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA = 0xC004;

    public static final int TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA = 0xC005;

    public static final int TLS_ECDHE_ECDSA_WITH_NULL_SHA = 0xC006;

    public static final int TLS_ECDHE_ECDSA_WITH_RC4_128_SHA = 0xC007;

    public static final int TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA = 0xC008;

    public static final int TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA = 0xC009;

    public static final int TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA = 0xC00A;

    public static final int TLS_ECDH_RSA_WITH_NULL_SHA = 0xC00B;

    public static final int TLS_ECDH_RSA_WITH_RC4_128_SHA = 0xC00C;

    public static final int TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA = 0xC00D;

    public static final int TLS_ECDH_RSA_WITH_AES_128_CBC_SHA = 0xC00E;

    public static final int TLS_ECDH_RSA_WITH_AES_256_CBC_SHA = 0xC00F;

    public static final int TLS_ECDHE_RSA_WITH_NULL_SHA = 0xC010;

    public static final int TLS_ECDHE_RSA_WITH_RC4_128_SHA = 0xC011;

    public static final int TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA = 0xC012;

    public static final int TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA = 0xC013;

    public static final int TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA = 0xC014;

    public static final int TLS_ECDH_anon_WITH_NULL_SHA = 0xC015;

    public static final int TLS_ECDH_anon_WITH_RC4_128_SHA = 0xC016;

    public static final int TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA = 0xC017;

    public static final int TLS_ECDH_anon_WITH_AES_128_CBC_SHA = 0xC018;

    public static final int TLS_ECDH_anon_WITH_AES_256_CBC_SHA = 0xC019;

    /*
     * RFC 5054
     */
    public static final int TLS_SRP_SHA_WITH_3DES_EDE_CBC_SHA = 0xC01A;

    public static final int TLS_SRP_SHA_RSA_WITH_3DES_EDE_CBC_SHA = 0xC01B;

    public static final int TLS_SRP_SHA_DSS_WITH_3DES_EDE_CBC_SHA = 0xC01C;

    public static final int TLS_SRP_SHA_WITH_AES_128_CBC_SHA = 0xC01D;

    public static final int TLS_SRP_SHA_RSA_WITH_AES_128_CBC_SHA = 0xC01E;

    public static final int TLS_SRP_SHA_DSS_WITH_AES_128_CBC_SHA = 0xC01F;

    public static final int TLS_SRP_SHA_WITH_AES_256_CBC_SHA = 0xC020;

    public static final int TLS_SRP_SHA_RSA_WITH_AES_256_CBC_SHA = 0xC021;

    public static final int TLS_SRP_SHA_DSS_WITH_AES_256_CBC_SHA = 0xC022;

    /*
     * RFC 5289
     */
    private static final int TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256 = 0xC023;

    private static final int TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384 = 0xC024;

    private static final int TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256 = 0xC025;

    private static final int TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384 = 0xC026;

    private static final int TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256 = 0xC027;

    private static final int TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384 = 0xC028;

    private static final int TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256 = 0xC029;

    private static final int TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384 = 0xC02A;

    private static final int TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256 = 0xC02B;

    private static final int TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384 = 0xC02C;

    private static final int TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256 = 0xC02D;

    private static final int TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384 = 0xC02E;

    private static final int TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256 = 0xC02F;

    private static final int TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 = 0xC030;

    private static final int TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256 = 0xC031;

    private static final int TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384 = 0xC032;

    /*
     * RFC 5746
     */
    public static final int TLS_EMPTY_RENEGOTIATION_INFO_SCSV = 0x00FF;

    private static final Map<Integer, CipherSuite> ALL_SUPPORTS = new HashMap<>();

    static {
        // 百度不支持AES256的，只能用AES128的
        ALL_SUPPORTS.put(TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
            new CipherSuite("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                MacDesc.NULL_MAC, KeyExchange.ECDHE_RSA, CipherDesc.AES_128_GCM, HashDesc.SHA256, true));
        ALL_SUPPORTS.put(TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
            new CipherSuite("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
                MacDesc.NULL_MAC, KeyExchange.ECDHE_RSA, CipherDesc.AES_256_GCM, HashDesc.SHA384, true));
    }

    /**
     * 套件名
     */
    @Getter
    private final String name;

    /**
     * 加密套件id
     */
    @EqualsAndHashCode.Include
    @Getter
    private final int suite;

    /**
     * mac算法（对应的hash算法）
     */
    @Getter
    private final MacDesc macDesc;

    /**
     * 密钥交换算法名
     */
    @Getter
    private final KeyExchange keyExchange;

    /**
     * 加密算法名
     */
    @Getter
    private final CipherDesc cipher;

    @Getter
    private final HashDesc hashDesc;

    /**
     * 是否是包含ECC算法
     */
    @Getter
    private final boolean ec;

    public CipherSuite(String name, int suite, MacDesc macDesc, KeyExchange keyExchange, CipherDesc cipher,
        HashDesc hashDesc, boolean ec) {
        this.name = name;
        this.suite = suite;
        this.macDesc = macDesc;
        this.keyExchange = keyExchange;
        this.cipher = cipher;
        this.hashDesc = hashDesc;
        this.ec = ec;
    }

    /**
     * 根据加密套件的id获取加密套件
     *
     * @param id
     *            加密套件id
     * @return 加密套件
     */
    public static CipherSuite getById(int id) {
        return ALL_SUPPORTS.get(id);
    }

    /**
     * 获取当前系统支持的所有加密套件
     *
     * @return 所有加密套件
     */
    public static List<CipherSuite> getAllSupports() {
        return new ArrayList<>(ALL_SUPPORTS.values());
    }

    @Override
    public String toString() {
        return name;
    }

}
