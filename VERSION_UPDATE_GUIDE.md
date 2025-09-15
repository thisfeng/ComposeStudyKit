# 📱 版本更新功能完整指南

## 🎯 功能概述

本项目实现了一套完整的Android应用版本更新系统，支持**自动检查更新**、**断点续传下载**、**智能安装**等核心功能。采用现代化的Jetpack Compose UI和MVVM架构，提供流畅的用户体验。

## ✨ 核心特性

### 🔍 智能版本检查
- 📡 **实时版本检查**：对接服务器API获取最新版本信息
- 🔢 **版本比较算法**：基于内部版本号（inner）智能判断是否需要更新
- ⚠️ **强制更新控制**：支持可选更新和强制更新两种模式
- 📊 **版本信息展示**：完整显示版本号、发布时间、更新说明

### 📥 断点续传下载
- 🚀 **HTTP Range支持**：实现真正的断点续传技术
- 📈 **实时进度监控**：字节级精度的下载进度显示
- 🔄 **网络中断恢复**：自动检测已下载部分，继续下载
- 📊 **下载速度显示**：实时计算和展示下载速度
- 🛡️ **文件完整性验证**：确保下载文件的完整性

### 📱 智能系统安装
- 🔧 **Android版本适配**：自动适配不同Android版本的安装方式
- 🔐 **权限智能检查**：自动检查并引导用户授予安装权限
- 📂 **FileProvider配置**：安全的文件共享机制
- 🎯 **一键安装**：下载完成后直接调用系统安装器

## 🏗️ 架构设计

### 📁 文件结构
```
├── utils/
│   └── UpdateManager.kt              # 核心更新管理器
├── data/
│   ├── api/
│   │   └── UpdateApiService.kt       # 版本更新API接口
│   └── repository/
│       └── UpdateRepository.kt       # 更新数据仓库
├── ui/
│   ├── viewmodel/
│   │   └── UpdateViewModel.kt        # 更新状态管理
│   └── screen/network/
│       └── UpdateScreen.kt           # 更新界面
└── res/xml/
    └── file_paths.xml                # FileProvider配置
```

### 🔄 数据流转
```
UI层 ← StateFlow ← ViewModel ← Repository ← UpdateManager ← Network/File
```

## 📋 API接口规范

### 检查版本接口
```
GET /a/api/app/version?channel=ANDROID&company=WingFat&serial=W3oqf7L71JG821Q&outlet=6001001
```

**响应格式：**
```json
{
    "msg": "獲取最新版本成功",
    "result": "接口請求成功",
    "code": 1,
    "singleLogin": false,
    "apiVerify": null,
    "data": {
        "versions": "3.6.78",        // 版本号字符串
        "inner": 3678,               // 内部版本号（用于比较）
        "type": "ANDROID",           // 平台类型
        "time": "2025-08-25",        // 发布时间
        "explain": "3.6.78   1、支持付款方式控制權限",  // 更新说明
        "isMust": true,              // 是否强制更新
        "downloadUrl": "https://...", // APK下载链接
        "downloadCount": 0,          // 下载次数
        "serial": "W3oqf7L71JG821Q"  // 设备序列号
    },
    "sign": null
}
```

## 🔧 核心组件详解

### 1. UpdateManager - 核心更新管理器

**位置**：`/app/src/main/java/com/thisfeng/composestudykit/update/UpdateManager.kt`

**主要功能**：
- ✅ **断点续传下载**：`downloadApk(downloadUrl): Flow<DownloadProgress>`
- ✅ **系统安装调用**：`installApk(): Boolean`
- ✅ **版本比较**：`needUpdate(serverVersionCode): Boolean`
- ✅ **文件管理**：`clearDownloadedApk()`

**技术亮点**：
```kotlin
// 断点续传实现
val existingFileSize = if (apkFile.exists()) apkFile.length() else 0L
val request = Request.Builder()
    .url(downloadUrl)
    .apply {
        if (existingFileSize > 0) {
            addHeader("Range", "bytes=$existingFileSize-")
        }
    }
    .build()
```

### 2. UpdateRepository - 数据仓库层

**位置**：`/app/src/main/java/com/thisfeng/composestudykit/update/UpdateRepository.kt`

**设计特点**：
- 🏗️ **继承BaseRepository**：复用网络异常处理逻辑
- 🚫 **无缓存策略**：版本检查不适用缓存机制
- 🔗 **UpdateManager集成**：统一的工具类调用接口

### 3. UpdateViewModel - 状态管理层

**位置**：`/app/src/main/java/com/thisfeng/composestudykit/update/UpdateViewModel.kt`

**UIState设计**：
```kotlin
data class UpdateUiState(
    val isCheckingUpdate: Boolean = false,      // 检查更新状态
    val hasUpdate: Boolean = false,             // 是否有更新
    val showUpdateDialog: Boolean = false,      // 显示更新对话框
    val versionInfo: VersionInfo? = null,       // 版本信息
    val currentVersionCode: Long = 0L,          // 当前版本号
    val currentVersionName: String = "",        // 当前版本名
    val isDownloading: Boolean = false,         // 下载状态
    val downloadProgress: UpdateManager.DownloadProgress = UpdateManager.DownloadProgress(),
    val downloadCompleted: Boolean = false,     // 下载完成状态
    val errorMessage: String? = null            // 错误信息
)
```

### 4. UpdateScreen - UI界面层

**位置**：`/app/src/main/java/com/thisfeng/composestudykit/update/UpdateScreen.kt`

**界面组件**：
- 📱 **当前版本卡片**：显示本地版本信息
- 🔄 **检查更新按钮**：触发版本检查
- 📊 **更新状态卡片**：显示检查结果和更新信息
- 💬 **更新对话框**：完整的更新流程交互

## 🚀 使用指南

### 快速开始

1. **进入版本更新界面**
   - 打开应用 → 网络请求案例 → 版本更新标签页

2. **检查版本更新**
   - 点击"检查更新"按钮
   - 系统自动请求服务器版本信息

3. **执行更新操作**
   - 如有新版本，自动弹出更新对话框
   - 查看更新说明，点击"立即更新"
   - 实时查看下载进度
   - 下载完成后点击"立即安装"

### 配置说明

**1. 权限配置**
```xml
<!-- APK安装权限 -->
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

**2. FileProvider配置**
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

**3. 文件路径配置**
```xml
<!-- res/xml/file_paths.xml -->
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-files-path name="download" path="download/" />
</paths>
```

### 自定义配置

**1. 修改API接口**
```kotlin
// 在UpdateApiService中修改接口地址和参数
@GET("your/api/version")
suspend fun checkVersion(
    @Query("platform") platform: String = "ANDROID",
    @Query("appId") appId: String = "your_app_id"
): ApiResponse<VersionInfo>
```

**2. 自定义下载目录**
```kotlin
// 在UpdateManager中修改下载目录
private val downloadDir: File by lazy {
    File(context.getExternalFilesDir(null), "your_download_folder").apply {
        if (!exists()) mkdirs()
    }
}
```

## 🔐 安全考虑

### 文件安全
- ✅ **FileProvider使用**：避免直接暴露文件路径
- ✅ **外部存储隔离**：使用应用专用外部存储目录
- ✅ **文件完整性验证**：下载完成后验证文件大小

### 权限管理
- ✅ **动态权限检查**：运行时检查安装权限
- ✅ **用户引导**：自动跳转到权限设置页面
- ✅ **优雅降级**：权限不足时提供友好提示

### 网络安全
- ✅ **HTTPS下载**：确保下载链接使用HTTPS协议
- ✅ **URL验证**：验证下载链接的合法性
- ✅ **异常处理**：完整的网络异常处理机制

## 🐛 故障排除

### 常见问题

**1. 下载失败**
- 检查网络连接状态
- 验证下载URL的有效性
- 确认存储空间是否充足

**2. 安装失败**
- 检查安装权限是否已授予
- 确认APK文件是否完整
- 验证应用签名是否匹配

**3. 权限被拒绝**
- 引导用户手动开启"安装未知来源应用"权限
- 在设置 → 安全 → 未知来源中开启

### 调试技巧

**1. 启用详细日志**
```kotlin
// 在UpdateManager中添加详细日志
private fun logDebug(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d("UpdateManager", message)
    }
}
```

**2. 模拟网络异常**
```kotlin
// 在测试环境中模拟网络问题
if (BuildConfig.DEBUG && shouldSimulateError) {
    throw IOException("模拟网络异常")
}
```

## 🔄 更新历史

### Version 1.1.0 (2025-09-12)
- ✅ 更新API服务器地址为 https://cloud.ablegenius.com/
- ✅ 修改测试参数为 channel=ANDROID&company=WingFat&serial=W3oqf7L71JG821Q&outlet=6001001

### Version 1.0.0 (2025-09-12)
- ✅ 实现基础版本检查功能
- ✅ 添加断点续传下载支持
- ✅ 集成系统安装调用
- ✅ 实现Material Design 3 UI
- ✅ 添加完整的错误处理机制
- ✅ 集成到NetworkExamplesScreen

## 📚 参考资料

- [Android FileProvider官方文档](https://developer.android.com/reference/androidx/core/content/FileProvider)
- [HTTP Range请求规范](https://developer.mozilla.org/en-US/docs/Web/HTTP/Range_requests)
- [Jetpack Compose官方指南](https://developer.android.com/jetpack/compose)
- [Kotlin Coroutines官方文档](https://kotlinlang.org/docs/coroutines-overview.html)

## 💡 最佳实践

### 开发建议
1. **测试驱动**：优先编写测试用例，确保功能稳定性
2. **渐进式更新**：小步快跑，逐步完善功能
3. **用户体验**：注重界面交互和错误提示的友好性
4. **性能优化**：合理使用协程，避免阻塞主线程

### 部署建议
1. **分阶段发布**：先在测试环境验证，再正式发布
2. **灰度发布**：控制更新推送范围，观察用户反馈
3. **回滚准备**：保留回滚机制，应对紧急情况
4. **监控告警**：建立完善的监控和告警机制

---

🎉 **版本更新功能现已完整集成到你的Jetpack Compose项目中，enjoy coding！**