# 屏幕悬浮翻译

Android 悬浮窗 OCR 翻译工具。v0.1 目标是先做一个自己手机能安装、能授权、能点击悬浮球识别当前屏幕文字的 MVP。

## 当前功能

- 悬浮球显示在其他 App 上层。
- 使用 Android `MediaProjection` 获取一次当前屏幕画面。
- 使用 ML Kit 在本地做 OCR，不上传截图。
- 可配置 OpenAI 兼容翻译接口；未配置时显示 OCR 原文。
- 支持以后通过 GitHub Releases/APK 分发更新。

## 手机安装

1. 下载 `app-debug.apk` 或 release APK 到 Android 手机。
2. 打开 APK，允许对应 App 的“安装未知来源应用”。
3. 打开“屏幕悬浮翻译”。
4. 依次开启：悬浮窗权限、通知权限、屏幕捕获授权。
5. 切到要翻译的页面，点击悬浮球“译”。

## 构建

```bash
export ANDROID_HOME="$HOME/android-sdk"
export ANDROID_SDK_ROOT="$HOME/android-sdk"
./gradlew --no-daemon :app:assembleDebug
```

APK 输出位置：

```text
app/build/outputs/apk/debug/app-debug.apk
```

## 翻译接口配置

不要把 API Key 写进 Git。构建时用 Gradle 参数传入：

```bash
./gradlew --no-daemon :app:assembleDebug \
  -PtranslationBaseUrl=https://api.example.com/v1 \
  -PtranslationApiKey=YOUR_TOKEN \
  -PtranslationModel=gpt-5.5
```

接口要求兼容 OpenAI Chat Completions：

```text
POST /v1/chat/completions
```

## 更新策略

普通 APK 侧载不能静默自更新。推荐流程：

1. GitHub Release 上传新版 APK。
2. `release/latest.json` 更新最新版本号和下载地址。
3. App 后续加入“检查更新”：检测 JSON、下载 APK、跳转系统安装器。
4. 用户手动确认安装；包名和签名不变时会覆盖旧版本并保留数据。

## 固定信息

- 包名：`com.jason.screentranslator`
- 当前版本：`0.1.0`
- `versionCode`：`1`
- 最低系统：Android 8.0 / API 26
- 目标系统：Android 15 / API 35

## 后续开发优先级

1. 添加可拖拽识别区域，只 OCR 选中区域。
2. 增加 App 内设置页，输入翻译接口地址、模型和本地保存的 Key。
3. 增加“检查更新”按钮和 APK 下载/安装跳转。
4. 增加 OCR 结果缓存，避免重复翻译同一屏内容。
5. 增加 release 签名配置，固定 keystore 后正式分发。

## 隐私说明

本项目默认只在用户点击悬浮球时截取当前屏幕并 OCR。截图可能包含聊天、账号、支付等敏感内容，请避免在敏感页面使用。v0.1 不使用无障碍服务，不读取其他 App 私有数据。
