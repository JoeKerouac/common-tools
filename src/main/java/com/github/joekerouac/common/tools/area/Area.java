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
package com.github.joekerouac.common.tools.area;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;

/**
 * 区域对象定义
 *
 * @author JoeKerouac
 * @date 2022-10-17 17:35
 * @since 2.0.0
 */
@Data
public class Area {

    /**
     * 默认区域代码，当没有父地区的时候使用该代码作为父区域代码
     */
    public static final String DEFAULT = "000000";

    /**
     * 当前地区代码
     */
    private String code;

    /**
     * 父地区代码
     */
    private String parent;

    /**
     * 地区名
     */
    private String name;

    /**
     * 子地区
     */
    private List<Area> childList;

    /**
     * 自己复制自己，deep copy
     * 
     * @return 复制结果
     */
    public Area copy() {
        Area area = new Area();
        area.code = code;
        area.parent = parent;
        area.name = name;
        area.childList = childList.stream().map(Area::copy).collect(Collectors.toList());
        return area;
    }
}
