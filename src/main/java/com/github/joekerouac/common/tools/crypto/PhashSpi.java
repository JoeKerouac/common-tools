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

import com.github.joekerouac.common.tools.crypto.constant.PhashDesc;

/**
 * Phash算法，跟PRF算法的区别就是Phash算法只需要一个seed，PRF需要label+seed，只需要调用{@link #phash(byte[], byte[]) phash}方法
 * 时seed参数传入label+seed即可；
 * 
 * <p>
 * 注意：如果将该算法作为PRF算法，那么仅支持TLS1.2以及更高版本，TLS1.2以下PRF算法跟本算法有区别
 * </p>
 * <br/>
 * <p>
 * &#9; compute:<br/>
 * <br/>
 *
 * &#9;&#9; P_hash(secret, seed) = HMAC_hash(secret, A(1) + seed) +<br/>
 * &#9;&#9; HMAC_hash(secret, A(2) + seed) +<br/>
 * &#9;&#9; HMAC_hash(secret, A(3) + seed) + ...<br/>
 * &#9; A() is defined as:<br/>
 * <br/>
 *
 * &#9;&#9; A(0) = seed<br/>
 * &#9;&#9; A(i) = HMAC_hash(secret, A(i-1))<br/>
 * </p>
 *
 * @since 1.0.0
 * @author JoeKerouac
 */
public interface PhashSpi extends AlgorithmSpi<PhashSpi> {

    /**
     * 初始化
     *
     * @param key
     *            对称密钥
     */
    void init(byte[] key);

    /**
     * 计算phash，如果传入的seed是label+seed数据则是PRF算法
     * 
     * @param seed
     *            种子，长度不限
     * @param output
     *            结果输出数组，最终结果将会输出到这里，长度不固定，对于TLS1.0+来说数组长度是12
     */
    void phash(byte[] seed, byte[] output);

    /**
     * hmac算法名
     * 
     * @return hash算法名
     */
    String hmacAlgorithm();

    @Override
    default int type() {
        return AlgorithmSpi.PHASH;
    }

    static PhashSpi getInstance(PhashDesc desc) {
        return AlgorithmRegistry.newInstance("alias.phash." + desc.getAlgorithm());
    }
}
