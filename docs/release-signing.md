# Release 签名说明

Android 覆盖安装要求包名和签名证书保持一致。

当前仓库只提交 debug 构建配置，不提交 keystore。正式给手机长期使用前，需要生成一个私有 release keystore，并安全保存到本机/密码管理器，不要提交到 Git。

建议命令：

```bash
keytool -genkeypair \
  -v \
  -keystore ~/secure-keystores/screen-translator-overlay-release.jks \
  -alias screen-translator-overlay \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000
```

生成后再配置 Gradle release signing。以后每个正式 APK 都必须使用同一个 keystore 和 alias。
