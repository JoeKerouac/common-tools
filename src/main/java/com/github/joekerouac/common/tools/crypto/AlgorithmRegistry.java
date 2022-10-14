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
package com.github.joekerouac.common.tools.crypto;

import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.crypto.constant.CipherDesc;
import com.github.joekerouac.common.tools.crypto.exception.CryptoException;
import com.github.joekerouac.common.tools.crypto.exception.NoSuchAlgorithmException;
import com.github.joekerouac.common.tools.crypto.impl.AesCipher;
import com.github.joekerouac.common.tools.util.Assert;

/**
 * 全局算法注册器
 *
 * @since 1.0.0
 * @author JoeKerouac
 */
public class AlgorithmRegistry {

    private static final ConcurrentMap<String, AlgorithmSpi<?>> REGISTRY = new ConcurrentHashMap<>();

    static {
        {
            @SuppressWarnings("all")
            ServiceLoader<AlgorithmSpi> load = ServiceLoader.load(AlgorithmSpi.class);
            for (AlgorithmSpi<?> algorithmSpi : load) {
                for (String alias : algorithmSpi.alias()) {
                    REGISTRY.put(alias, algorithmSpi);
                }
            }
        }

        {
            for (CipherDesc value : CipherDesc.values()) {
                REGISTRY.put(value.name(), new AesCipher(value));
                REGISTRY.put("alias.cipher." + value.name(), new AesCipher(value));
            }
        }
    }

    /**
     * 查找指定算法
     *
     * @param algorithmName
     *            算法名
     * @param <T>
     *            算法实际类型
     * @return 对应的算法
     * @throws NoSuchAlgorithmException
     *             指定算法不存在时抛出异常
     */
    @SuppressWarnings("unchecked")
    public static <T extends AlgorithmSpi<T>> T newInstance(String algorithmName) throws NoSuchAlgorithmException {
        Assert.notBlank(algorithmName, "algorithmName不能为空", ExceptionProviderConst.IllegalArgumentExceptionProvider);

        // 先加载
        try {
            T algorithmSpi = (T)REGISTRY.get(algorithmName);

            if (algorithmSpi == null) {
                throw new NoSuchAlgorithmException(algorithmName);
            }

            return algorithmSpi.copy();
        } catch (CloneNotSupportedException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * 获取当前所有指定类型的算法实现的名字
     *
     * @return 所有指定类型的算法实现的名字
     */
    public static Set<String> getAllAlgorithm() {
        return REGISTRY.values().stream().map(AlgorithmSpi::name).collect(Collectors.toSet());
    }

}
