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
public class HmacSHA384 extends AbstractHmac {

    public HmacSHA384() {
        super(new DigestSHA384(), 48, 128);
    }

    @Override
    public String[] alias() {
        return new String[] {"alias.hmac.sha384", "alias.hmac.sha-384", "alias.hmac.SHA384", "alias.hmac.SHA-384",
            "alias.hmac.hmacSHA384", "alias.hmac.HmacSHA384", "alias.hmac.hmac-sha384", "alias.hmac.Hmac-SHA384"};
    }

}
