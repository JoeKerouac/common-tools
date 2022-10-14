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
package com.github.joekerouac.common.tools.crypto.impl;

/**
 * 基于MD5的HMAC算法
 * 
 * @since 1.0.0
 * @author JoeKerouac
 */
public class HmacMD5 extends AbstractHmac {

    public HmacMD5() {
        super(new DigestMD5(), 16, 64);
    }

    @Override
    public String[] alias() {
        return new String[] {"alias.hmac.md5", "alias.hmac.MD5", "alias.hmac.hmacMd5", "alias.hmac.hmacmd5",
            "alias.hmac.HmacMD5"};
    }
}
