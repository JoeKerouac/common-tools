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
 * @since 1.0.0
 * @author JoeKerouac
 */
public class HmacSHA1 extends AbstractHmac {

    public HmacSHA1() {
        super(new DigestSHA1(), 20, 64);
    }

    @Override
    public String[] alias() {
        return new String[] {"alias.hmac.sha1", "alias.hmac.sha-1", "alias.hmac.SHA1", "alias.hmac.SHA-1",
            "alias.hmac.hmacSHA1", "alias.hmac.HmacSHA1", "alias.hmac.hmac-sha1", "alias.hmac.Hmac-SHA1"};
    }
}
