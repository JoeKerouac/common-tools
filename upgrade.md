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

##v2.0.3
- PemUtil增加将rsa密钥以pem格式写出到文件；
- 增加`AES_256_PKCS5Padding`支持；
- 增加`com.github.joekerouac.common.tools.codec.HexCodec`，处理hex；
