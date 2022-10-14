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
package com.github.joekerouac.common.tools.net;

import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import com.github.joekerouac.common.tools.cache.Cache;
import com.github.joekerouac.common.tools.cache.impl.FixedTimeoutCache;
import com.github.joekerouac.common.tools.collection.Pair;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;
import com.github.joekerouac.common.tools.util.NumberUtil;

import lombok.CustomLog;
import lombok.Data;

/**
 * 获取本地IP使用
 *
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
@Data
@CustomLog
public final class HostInfo {

    /**
     * 本地网卡接口刷新时间，单位毫秒，每隔该时间就刷新一次
     */
    private static final long REFRESH_TIME = TimeUnit.MINUTES.toMillis(5);

    /**
     * 默认子网掩码：255.255.255.0
     */
    private static final byte[] DEFAULT_SUBNET_MASK = new byte[] {-1, -1, -1, 0};

    /**
     * IPv4地址长度
     */
    private static final int IPV4_LEN = 4;

    /**
     * 网络不可达的超时时间，单位毫秒
     */
    private static final int TIMEOUT = 100;

    /**
     * A类地址最小值:1.0.0.0
     */
    private static final long ADDRESS_A_MIN = 0x00010000;

    /**
     * A类地址最大值:126.255.255.255
     */
    private static final long ADDRESS_A_MAX = 0x7EFFFFFF;

    /**
     * B类地址最小值:128.0.0.0
     */
    private static final long ADDRESS_B_MIN = 0x80000000;

    /**
     * B类地址最大值:191.255.255.255
     */
    private static final long ADDRESS_B_MAX = 0xBFFFFFFF;

    /**
     * C类地址（内网）最小值:192.0.0.0
     */
    private static final long ADDRESS_C_MIN = 0xC0000000;

    /**
     * C类地址（内网）最大值:223.255.255.255
     */
    private static final long ADDRESS_C_MAX = 0xDFFFFFFF;

    /**
     * D类地址（多播地址）最小值:224.0.0.0
     */
    private static final long ADDRESS_D_MIN = 0xE0000000;

    /**
     * D类地址（多播地址）最大值:239.255.255.255
     */
    private static final long ADDRESS_D_MAX = 0xEFFFFFFF;

    /**
     * 本机所有网卡地址信息缓存
     */
    private static volatile Cache<List<HostInfo>> ALL_HOST_INFO_CACHE;

    /**
     * hostName
     */
    private final String hostName;

    /**
     * hostAddress
     */
    private final String hostAddress;

    /**
     * 地址的byte表示
     */
    private final byte[] address;

    /**
     * 子网掩码
     */
    private final byte[] subnetMask;

    /**
     * 地址类型
     */
    private final IpType type;

    /**
     * 子网编号
     */
    private final int subnetId;

    /**
     * 地址
     */
    private InetAddress inetAddress;

    /**
     * 该地址对应的网络接口，可能为null
     */
    private final NetworkInterface networkInterface;

    private HostInfo(InetAddress inetAddress, byte[] subnetMask, NetworkInterface networkInterface) {
        this.inetAddress = inetAddress;
        this.hostName = inetAddress.getHostName();
        this.hostAddress = inetAddress.getHostAddress();
        this.address = inetAddress.getAddress();
        this.subnetMask = subnetMask;
        this.type = getType(address);
        this.subnetId = NumberUtil.mergeToInt(address) & ~NumberUtil.mergeToInt(subnetMask);
        this.networkInterface = networkInterface;
    }

    static {
        // 初始化
        ALL_HOST_INFO_CACHE = new FixedTimeoutCache<>(TIMEOUT, HostInfo::obtainAll);
    }

    /**
     * 获取地址类型
     *
     * @param address
     *            IPv4地址
     * @return 返回地址类型
     */
    public static IpType getType(byte[] address) {
        Assert.assertTrue(address != null && address.length == IPV4_LEN,
            "地址格式不对，目前只能处理IPv4地址，当前数据：" + (address == null ? "null" : Arrays.toString(address)),
            ExceptionProviderConst.IllegalArgumentExceptionProvider);
        long target = NumberUtil.mergeToInt(address);

        if (target >= ADDRESS_A_MIN && target <= ADDRESS_A_MAX) {
            // A类地址
            return IpType.TYPE_A;
        } else if (target <= ADDRESS_B_MAX) {
            // B类地址
            return IpType.TYPE_B;
        } else if (target <= ADDRESS_C_MAX) {
            // C类地址
            return IpType.TYPE_C;
        } else if (target <= ADDRESS_D_MAX) {
            // D类地址
            return IpType.TYPE_D;
        } else {
            return IpType.TYPE_E;
        }
    }

    /**
     * 获取本机所有网卡接口对应的地址（同一个网卡多个地址的场景仅会返回一个地址）
     *
     * @return 所有网卡接口对应的地址，按照type升序排列，type小的在前边
     */
    public static List<HostInfo> getAll() {
        return ALL_HOST_INFO_CACHE.getTarget();
    }

    /**
     * 计算子网掩码
     *
     * @param interfaceAddress
     *            网络接口地址
     * @return 子网掩码
     */
    private static byte[] calcSubnetMask(InterfaceAddress interfaceAddress) {
        // 计算子网掩码
        // 子网掩码长度
        short networkPrefixLength = interfaceAddress.getNetworkPrefixLength();
        // 子网掩码
        int subnetMask = 0;
        for (int i = 0, j = 31; i < networkPrefixLength; i++, j--) {
            subnetMask = subnetMask | 1 << j;
        }

        return NumberUtil.splitToByte(subnetMask);
    }

    /**
     * 将点分十进制IPv4地址转换为byte数组，不会进行参数校验，外部注意自己校验参数
     *
     * @param ip
     *            ip
     * @return 数组
     */
    public static byte[] convert(String ip) {
        String[] data = ip.trim().split("\\.");

        byte[] result = new byte[IPV4_LEN];
        for (int i = 0; i < data.length; i++) {
            result[i] = Byte.parseByte(data[i]);
        }
        return result;
    }

    /**
     * 获取所有网卡地址信息
     *
     * @return 所有网卡地址信息（不包含IPv6网络、本地环回网络、虚拟机网络）
     */
    private static List<HostInfo> obtainAll() {
        List<HostInfo> all = new ArrayList<>();

        try {
            // 获取所有网卡接口
            Enumeration<NetworkInterface> networkInterfaceEnumeration = NetworkInterface.getNetworkInterfaces();
            List<Pair<List<InterfaceAddress>, NetworkInterface>> list = new ArrayList<>();

            while (networkInterfaceEnumeration.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaceEnumeration.nextElement();
                // 过滤掉P2P网络和本地环回网络（127.0.0.1）
                if (networkInterface.isPointToPoint() || networkInterface.isLoopback()) {
                    continue;
                }

                String displayName = StringUtils.getOrDefault(networkInterface.getDisplayName(), "");
                // 下面几个肯定是虚拟网络，过滤掉
                if (displayName.contains("Hyper-V") || displayName.contains("VirtualBox")
                    || displayName.contains("VMware")) {
                    continue;
                }

                // 获取该网卡的所有ip地址（同一个网卡允许有多个IP）
                List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();

                // 最终使用的接口地址列表
                List<InterfaceAddress> usedInterfaceAddress = new ArrayList<>();

                for (InterfaceAddress interfaceAddress : interfaceAddresses) {
                    InetAddress inetAddress = interfaceAddress.getAddress();
                    // 过滤掉ipV6
                    if (inetAddress instanceof Inet6Address) {
                        continue;
                    }

                    // 过滤掉通配地址（0.0.0.0）
                    if (inetAddress.isAnyLocalAddress()) {
                        continue;
                    }

                    usedInterfaceAddress.add(interfaceAddress);
                }

                if (usedInterfaceAddress.size() > 0) {
                    list.add(new Pair<>(usedInterfaceAddress, networkInterface));
                }
            }

            if (list.size() > 0) {
                list.sort(HostInfo::compare);

                for (Pair<List<InterfaceAddress>, NetworkInterface> pair : list) {
                    for (InterfaceAddress interfaceAddress : pair.getKey()) {
                        InetAddress inetAddress = interfaceAddress.getAddress();
                        all.add(new HostInfo(inetAddress, calcSubnetMask(interfaceAddress), pair.getValue()));
                    }
                }
            } else {
                all.add(getLocal());
            }
        } catch (Throwable e) {
            all.add(getLocal());
            LOGGER.debug(e, "获取网卡异常");
        }

        all.sort(HostInfo::compare);

        return Collections.unmodifiableList(all);
    }

    /**
     * 比较HostInfo，type小的排前边
     *
     * @param h1
     *            HostInfo
     * @param h2
     *            HostInfo
     * @return h1.type-h2.type
     */
    private static int compare(HostInfo h1, HostInfo h2) {
        return h1.type.order - h2.type.order;
    }

    private static int compare(Pair<List<InterfaceAddress>, NetworkInterface> o1,
        Pair<List<InterfaceAddress>, NetworkInterface> o2) {
        return o1.getValue().getIndex() - o2.getValue().getIndex();
    }

    /**
     * 获取机器信息
     *
     * @return 机器信息
     */
    public static HostInfo getInstance() {
        return ALL_HOST_INFO_CACHE.getTarget().get(0);
    }

    /**
     * 获取本地网络信息
     *
     * @return 本地网络信息
     */
    private static HostInfo getLocal() {
        InetAddress localhost;

        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            localhost = InetAddress.getLoopbackAddress();
        }

        return new HostInfo(localhost, DEFAULT_SUBNET_MASK, null);
    }

    /**
     * IP类型
     */
    public enum IpType {

        TYPE_A(0),

        TYPE_B(1),

        TYPE_C(2),

        TYPE_D(3),

        TYPE_E(4),

        ;

        private final int order;

        IpType(int order) {
            this.order = order;
        }

    }
}
