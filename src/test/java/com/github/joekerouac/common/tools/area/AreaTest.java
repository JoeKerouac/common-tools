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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.github.joekerouac.common.tools.concurrent.FutureCallback;
import com.github.joekerouac.common.tools.constant.ExceptionProviderConst;
import com.github.joekerouac.common.tools.io.IOUtils;
import com.github.joekerouac.common.tools.net.http.IHttpClient;
import com.github.joekerouac.common.tools.net.http.config.IHttpClientConfig;
import com.github.joekerouac.common.tools.net.http.request.IHttpGet;
import com.github.joekerouac.common.tools.net.http.response.IHttpResponse;
import com.github.joekerouac.common.tools.string.StringUtils;
import com.github.joekerouac.common.tools.util.Assert;
import com.github.joekerouac.common.tools.util.JsonUtil;

/**
 * 拉取区划信息
 *
 * @author JoeKerouac
 * @date 2022-10-17 18:20
 * @since 2.0.0
 */
public class AreaTest {

    private static final String BASE_DIR = "/root/area/";

    /**
     * 执行该方法即可拉取行政区划信息
     */
    public void pull() {
        Map<String, String> map = new HashMap<>();
        File dir = new File(BASE_DIR);
        if (!dir.exists()) {
            Assert.assertTrue(dir.mkdirs(), "目录创建失败", ExceptionProviderConst.IllegalStateExceptionProvider);
        }

        map.put("1980.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708040959.html");
        map.put("1981.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708041004.html");
        map.put("1982.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/1980-2000/201707141125.html");
        map.put("1983.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708160821.html");
        map.put("1984.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220856.html");
        map.put("1985.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220858.html");
        map.put("1986.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220859.html");
        map.put("1987.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220902.html");
        map.put("1988.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220903.html");
        map.put("1989.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708041017.html");
        map.put("1990.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708041018.html");
        map.put("1991.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708041020.html");
        map.put("1992.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220910.html");
        map.put("1993.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708041023.html");
        map.put("1994.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220911.html");
        map.put("1995.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220913.html");
        map.put("1996.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220914.html");
        map.put("1997.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220916.html");
        map.put("1998.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220918.html");
        map.put("1999.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220921.html");
        map.put("2000.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220923.html");
        map.put("2001.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220925.html");
        map.put("2002.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220927.html");
        map.put("2003.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220928.html");
        map.put("2004.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220930.html");
        map.put("2005.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220935.html");
        map.put("2006.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220936.html");
        map.put("2007.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220939.html");
        map.put("2008.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220941.html");
        map.put("2009.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220943.html");
        map.put("2010.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201708220946.html");
        map.put("2011.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201707271552.html");
        map.put("2012.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/201713/201707271556.html");
        map.put("2013.12.31.area.json", "http://files2.mca.gov.cn/cws/201404/20140404125552372.htm");
        map.put("2014.12.31.area.json", "http://files2.mca.gov.cn/cws/201502/20150225163817214.html");
        map.put("2015.12.31.area.json", "http://www.mca.gov.cn/article/sj/tjbz/a/2015/201706011127.html");
        map.put("2016.12.31.area.json", "http://www.mca.gov.cn/article/sj/xzqh/1980/201705/201705311652.html");
        map.put("2017.12.31.area.json", "http://www.mca.gov.cn/article/sj/xzqh/1980/201803/201803131454.html");
        map.put("2018.12.31.area.json", "http://www.mca.gov.cn/article/sj/xzqh/1980/201903/201903011447.html");
        map.put("2019.12.31.area.json", "https://www.mca.gov.cn/article/sj/xzqh/1980/2019/202002281436.html");
        map.put("2020.12.31.area.json", "https://www.mca.gov.cn/article/sj/xzqh/2020/20201201.html");

        IHttpClientConfig config = new IHttpClientConfig();
        config.setMaxTotal(10);
        config.setDefaultMaxPerRoute(10);
        config.setSocketTimeout(5000);
        IHttpClient client = IHttpClient.builder().config(config).build();
        List<Future> futures = new ArrayList<>();
        map.forEach((fileName, url) -> {
            IHttpGet get = IHttpGet.builder(url, client).build();
            Future<IHttpResponse> exec = get.exec(new FutureCallback<IHttpResponse>() {
                @Override
                public void complete(IHttpResponse result, Throwable ex, int status) {
                    if (ex == null && status == 0) {
                        parseAndWrite(url, result.getResult(), fileName);
                    } else {
                        System.err.printf("%s 获取失败了%n", url);
                        if (ex != null) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
            futures.add(exec);
        });
        futures.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }

    private static void parseAndWrite(String url, String html, String fileName) {
        Map<String, String> codeMap = parse(html);
        if (codeMap.size() == 0) {
            System.err.println("从URL[" + url + "]处没有获取到数据");
            return;
        }
        Map<String, Area> allArea = new HashMap<>();

        codeMap.forEach((code, name) -> {
            Area area = new Area();
            allArea.put(code, area);

            area.setCode(code);
            area.setName(name);

            if (code.endsWith("0000")) {
                area.setParent("000000");
            } else if (code.endsWith("00")) {
                String parent = code.substring(0, 2) + "0000";
                if (codeMap.containsKey(parent)) {
                    area.setParent(parent);
                } else {
                    area.setParent("000000");
                }
            } else {
                String parent;

                if (codeMap.containsKey(parent = code.substring(0, 4) + "00")) {
                    area.setParent(parent);
                } else if (codeMap.containsKey(parent = code.substring(0, 2) + "0000")) {
                    area.setParent(parent);
                } else {
                    area.setParent("000000");
                }
            }
        });

        allArea.forEach((code, area) -> {
            if (!area.getParent().equals("000000")) {
                Area parent = allArea.get(area.getParent());
                List<Area> childList = parent.getChildList();
                if (childList == null) {
                    childList = new ArrayList<>();
                    parent.setChildList(childList);
                }
                childList.add(area);
            }
        });

        List<String> remove = allArea.values().stream().filter(area -> !area.getParent().equals("000000"))
            .map(Area::getCode).collect(Collectors.toList());
        remove.forEach(allArea::remove);

        byte[] data = JsonUtil.write(allArea);
        // 持久化保存
        try (OutputStream out = new FileOutputStream(new File(BASE_DIR, fileName))) {
            System.out.println("保存：" + BASE_DIR + fileName);
            IOUtils.write(out, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从网页解析区划代码
     * 
     * @param html
     *            网页
     * @return 区划代码集合
     */
    private static Map<String, String> parse(String html) {
        // code和name的映射
        Map<String, String> codeMap = new HashMap<>();
        boolean start = false;
        int codeIndex = -1;
        int nameIndex = -1;
        Document doc = Jsoup.parse(html);
        Element element = doc.getElementsByTag("tbody").get(0);
        Elements trs = element.getElementsByTag("tr");

        // 遍历解析
        for (Element tr : trs) {
            Elements tds = tr.getElementsByTag("td");
            if (tds.size() == 0) {
                tds = tr.getElementsByTag("th");
            }

            if (start) {
                // 先进行越界判断
                if (tds.size() > codeIndex && tds.size() > nameIndex) {
                    String code = tds.get(codeIndex).text();
                    String name = tds.get(nameIndex).text();
                    // code必须是6位数字，并且名字不能为空
                    if (code.trim().length() == 6 && Pattern.matches("[0-9]+", code) && StringUtils.isNotBlank(name)) {
                        codeMap.put(code.trim(), name.trim());
                    }
                }
            } else {
                for (int i = 0; i < tds.size(); i++) {
                    Element td = tds.get(i);
                    if (td.text().equals("行政区划代码")) {
                        codeIndex = i;
                        start = true;
                    } else if (td.text().equals("单位名称") || td.text().equals("行政区划名称")) {
                        // 老版本的叫单位名称，新版的叫行政区划名称
                        nameIndex = i;
                        start = true;
                    }
                }
            }
        }
        return codeMap;
    }

}
