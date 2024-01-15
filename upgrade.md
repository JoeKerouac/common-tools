# 升级文档
## v1.0.1
- 增加行政区划工具、行政区划定义；
- 增加poi工具；
- 增加身份证相关工具；
- json解析正确处理不同字符集的数据；
- json工具增加直接处理字符串的方法；

## v2.0.2
- 修改bcprov的版本（`org.bouncycastle:bcprov-jdk16:1.46` -> `org.bouncycastle:bcprov-jdk15on:1.64`），同时修改因此导致的部分不兼容API；
- 修改IHttpClient，支持外部传入自定义构建的`org.apache.hc.client5.http.impl.async.HttpAsyncClientBuilder`，以此支持`brave`（tracing，spring-cloud-sleuth默认使用brave实现trace，而brave要求代码侵入）；
- 修改excel相关api，支持分批写入，而不是一次性全部写入；

## v2.0.3
- PemUtil增加将rsa密钥以pem格式写出到文件；
- 增加`AES_256_PKCS5Padding`支持；
- 增加`com.github.joekerouac.common.tools.codec.HexCodec`，处理hex；
- IHttpClient优化，当响应过大时将响应写入本地临时文件，防止发生OOM；

## v2.1.0
- HTTP工具重构；
- 修复jdk bug导致某些场景的OOM，详情参考：https://bugs.openjdk.org/browse/JDK-8168469

## v2.1.1
- excel工具优化，支持map

## v2.1.2
- 将`sun.security.util.KnownOIDs`复制到项目中；PS: 该类是`adoptopenjdk`中的类，如果使用`Oracle JDK`可能没有该类；

## v2.1.3
- 修复http工具未正确设置`Content-Type`问题；

## v2.1.4
- 升级apache httpclient; `5.0.3` -> `5.2.1`
- httpclient支持自定义DNS
