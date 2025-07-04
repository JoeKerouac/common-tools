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
package com.github.joekerouac.common.tools.net.http;

import java.util.HashSet;
import java.util.Set;

/**
 * @since 1.0.0
 * @author JoeKerouac
 * @date 2022-10-14 14:37:00
 */
public final class ContentType {

    private static final Set<String> printableContentTypeSet = new HashSet<>();

    /**
     * text/plain
     */
    public static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";

    /**
     * text/html
     */
    public static final String CONTENT_TYPE_TEXT_HTML = "text/html";

    /**
     * json类型数据
     */
    public static final String CONTENT_TYPE_JSON = "application/json";

    /**
     * xml类型数据
     */
    public static final String CONTENT_TYPE_XML = "application/xml";

    /**
     * form表单类型数据
     */
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

    /**
     * 二进制流
     */
    public static final String CONTENT_TYPE_STREAM = "application/octet-stream";

    /**
     * 图片:bmp
     */
    public static final String CONTENT_TYPE_IMG_BMP = "image/bmp";

    /**
     * 图片:gif
     */
    public static final String CONTENT_TYPE_IMG_GIF = "image/gif";

    /**
     * 图片:jpeg
     */
    public static final String CONTENT_TYPE_IMG_JPEG = "image/jpeg";

    /**
     * 图片:png
     */
    public static final String CONTENT_TYPE_IMG_PNG = "image/png";

    /**
     * 图片:svg
     */
    public static final String CONTENT_TYPE_IMG_SVG = "image/svg+xml";

    /**
     * 图片:tiff
     */
    public static final String CONTENT_TYPE_IMG_TIFF = "image/tiff";

    static {
        printableContentTypeSet.add(CONTENT_TYPE_TEXT_PLAIN);
        printableContentTypeSet.add(CONTENT_TYPE_TEXT_HTML);
        printableContentTypeSet.add(CONTENT_TYPE_JSON);
        printableContentTypeSet.add(CONTENT_TYPE_XML);
        printableContentTypeSet.add(CONTENT_TYPE_FORM);
    }

    /**
     * 判断content type对应的body是否可打印
     * 
     * @param contentType
     *            content type
     * @return true表示可打印
     */
    public static boolean printable(String contentType) {
        for (String printableContentType : printableContentTypeSet) {
            if (contentType.contains(printableContentType)) {
                return true;
            }
        }

        return false;
    }
}
