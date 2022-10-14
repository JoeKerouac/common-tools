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

/**
 * 算法接口，所有算法都继承自该接口
 *
 * @since 1.0.0
 * @author JoeKerouac
 */
public interface AlgorithmSpi<T extends AlgorithmSpi> extends Cloneable {

    /**
     * 摘要算法类型
     */
    int DIGEST = 0;

    /**
     * 加密算法类型
     */
    int CIPHER = 1;

    /**
     * Hmac算法类型
     */
    int HMAC = 2;

    /**
     * Phash算法类型
     */
    int PHASH = 3;

    /**
     * 算法名
     *
     * @return 算法名
     */
    String name();

    /**
     * 别名
     * 
     * @return 别名
     */
    String[] alias();

    /**
     * 算法类型
     *
     * @return 算法类型
     */
    int type();

    /**
     * 克隆，默认不支持，请自行实现
     * 
     * @return 克隆结果
     * @throws CloneNotSupportedException
     *             异常
     */
    default T copy() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("不支持copy");
    }
}
