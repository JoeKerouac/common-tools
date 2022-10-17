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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import com.github.joekerouac.common.tools.collection.CollectionUtil;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.file.FileUtils;
import com.github.joekerouac.common.tools.io.IOUtils;
import com.github.joekerouac.common.tools.reflect.type.AbstractTypeReference;
import com.github.joekerouac.common.tools.resource.exception.ResourceNotExistException;
import com.github.joekerouac.common.tools.resource.impl.ClassPathResource;
import com.github.joekerouac.common.tools.util.Assert;
import com.github.joekerouac.common.tools.util.JsonUtil;

import lombok.CustomLog;

/**
 * 地区工具
 *
 * @author JoeKerouac
 * @date 2022-10-17 17:35
 * @since 2.0.0
 */
@CustomLog
public class AreaUtil {

    /**
     * 存放区域文件的文件后缀
     */
    private static String AREA_FILE_SUFFIX = ".area.json";

    /**
     * 所有年份的区域集合，key是年份，value是当年最新区域集合
     */
    private static TreeMap<String, Map<String, Area>> ALL_AREA_MAP = new TreeMap<>();

    /**
     * 清除缓存
     */
    public static void clearCache() {
        ALL_AREA_MAP.clear();
    }

    /**
     * 获取区域说明
     * 
     * @param code
     *            区域代码
     * @param date
     *            日期，因为同一个代码在不同时期可能对应的区域名不同，所以需要传入日期
     * @return 区域说明
     */
    public static Area getArea(String code, String date) {
        Assert.argNotBlank(code, "code");
        Area area = getArea(date).get(code);
        if (area == null) {
            return null;
        }

        return area.copy();
    }

    /**
     * 获取指定时间最新的区域说明
     * 
     * @param date
     *            日期，因为同一个代码在不同时期可能对应的区域名不同，所以需要传入日期，格式为yyyy.MM.dd
     * @return 区域说明
     */
    public static Map<String, Area> getArea(String date) {
        if (ALL_AREA_MAP.isEmpty()) {
            init();
        }

        Assert.argNotBlank(date, "date");
        Assert.assertTrue(Pattern.matches("[0-9]{4}\\.[0-9]{2}\\.[0-9]{2}", date), "日期格式不对",
            ExceptionProviderConst.IllegalArgumentExceptionProvider);

        String descDate = ALL_AREA_MAP.firstKey();

        // 计算当前时间下最新的区划代码
        for (String arg : ALL_AREA_MAP.keySet()) {
            if (arg.compareTo(date) >= 0) {
                break;
            }
            descDate = arg;
        }

        Map<String, Area> cache = ALL_AREA_MAP.get(descDate);
        if (CollectionUtil.isEmpty(cache)) {
            return Collections.emptyMap();
        }
        Map<String, Area> result = new HashMap<>();
        cache.forEach((key, value) -> result.put(key, value.copy()));
        return result;
    }

    /**
     * 获取指定区域所在的省份
     * 
     * @param area
     *            区域
     * @param areaMap
     *            所有区域集合
     * @return 区域所在的省份，有可能为空，为空说明区域集合不全或者区域有问题
     */
    public static Area getProvince(Area area, Map<String, Area> areaMap) {
        Assert.argNotNull(area, "area");
        Assert.argNotNull(areaMap, "areaMap");
        Area province = area;
        while (province != null && !Area.DEFAULT.equals(area.getParent())) {
            province = areaMap.get(area.getParent());
        }
        return province;
    }

    /**
     * 获取指定区域全称
     * 
     * @param area
     *            区域
     * @param areaMap
     *            区域集合
     * @return 区域全称
     */
    public static String getFullName(Area area, Map<String, Area> areaMap) {
        Assert.argNotNull(area, "area");
        Assert.argNotNull(areaMap, "areaMap");
        Area nowArea = area;
        List<String> names = new ArrayList<>();
        while (nowArea != null) {
            names.add(nowArea.getName());
            nowArea = areaMap.get(nowArea.getParent());
        }

        StringBuilder nameBuilder = new StringBuilder();
        for (int i = names.size() - 1; i >= 0; i++) {
            nameBuilder.append(names.get(i));
        }

        return nameBuilder.toString();
    }

    /**
     * 初始化
     */
    private static void init() {
        try {
            // 注意：请运行测试程序将所有地区信息拉下来，然后放到class path的area目录中
            ClassPathResource resource = new ClassPathResource("area");
            File file = new File(resource.getUrl().getFile());
            List<File> areaFiles =
                FileUtils.listFiles(file, f -> f.isDirectory() || f.getName().endsWith(AREA_FILE_SUFFIX));

            for (File areaFile : areaFiles) {
                Map<String, Area> map = read(areaFile);
                ALL_AREA_MAP.put(areaFile.getName().replace(AREA_FILE_SUFFIX, ""), map);
            }
        } catch (ResourceNotExistException e) {
            throw new IllegalStateException("当前类路径上不存在area目录，请创建，并将行政区划表放入");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从文件中读取区域信息
     * 
     * @param areaFile
     *            文件
     * @return 文件中的区域信息
     * @throws IOException
     *             IO异常
     */
    private static Map<String, Area> read(File areaFile) throws IOException {
        byte[] data = IOUtils.read(new FileInputStream(areaFile), true);
        Map<String, Area> map = JsonUtil.read(data, new AbstractTypeReference<Map<String, Area>>() {});
        Map<String, Area> allMap = new HashMap<>();
        map.values().forEach(area -> putArea(allMap, area));
        return allMap;
    }

    /**
     * 将区域放入所有区域map，同时递归将该区域的子区域也放入
     * 
     * @param areaMap
     *            区域map
     * @param area
     *            区域
     */
    private static void putArea(Map<String, Area> areaMap, Area area) {
        areaMap.put(area.getCode(), area);
        if (!CollectionUtil.isEmpty(area.getChildList())) {
            area.getChildList().forEach(childArea -> putArea(areaMap, childArea));
        }
    }
}
