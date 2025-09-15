# 📁 文件上传下载功能使用指南

## 🎯 功能概述

本次为 Retrofit 网络请求框架添加了完整的文件上传下载功能，包括：

### 📤 上传功能
- **单文件上传**：支持选择单个文件进行上传
- **多文件上传**：支持批量选择多个文件上传
- **进度监控**：实时显示上传进度
- **类型支持**：支持各种文件类型（图片、文档、音频、视频等）

### 📥 下载功能
- **文件下载**：支持下载网络文件到本地
- **进度显示**：实时显示下载进度
- **自动保存**：自动保存到应用的下载目录

### 📋 文件管理
- **文件列表**：获取服务器文件列表（模拟）
- **文件信息**：查看文件详细信息
- **结果展示**：清晰展示上传下载结果

## 🏗️ 架构设计

### 1. API 接口层 (`FileApiService`)
```kotlin
interface FileApiService {
    // 单文件上传
    @Multipart
    @POST("upload/single")
    suspend fun uploadSingleFile(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody? = null
    ): ApiResponse<FileUploadResult>
    
    // 文件下载
    @Streaming
    @GET("download/{fileId}")
    suspend fun downloadFile(@Path("fileId") fileId: String): Response<ResponseBody>
    
    // 更多接口...
}
```

### 2. 数据仓库层 (`FileRepository`)
- 继承自 `BaseRepository`，复用网络异常处理
- 实现具体的文件操作逻辑
- 提供进度回调机制
- 模拟文件服务器响应（实际项目中替换为真实服务器）

### 3. ViewModel层 (`FileOperationViewModel`)
- 管理文件操作的UI状态
- 处理用户交互逻辑
- 提供进度更新和错误处理

### 4. UI界面层 (`FileOperationScreen`)
- 现代化的 Compose UI 设计
- 支持文件选择和进度显示
- 友好的用户交互体验

## 📱 使用方式

### 在 NetworkExamplesScreen 中访问
1. 打开应用主界面
2. 点击 "🌐 网络请求" 卡片
3. 选择 "文件操作" 标签页
4. 体验各种文件操作功能

### 主要功能操作
- **单文件上传**：点击"单文件上传"按钮，选择文件
- **多文件上传**：点击"多文件上传"按钮，选择多个文件
- **下载示例**：点击"下载示例"按钮，下载示例图片
- **文件列表**：点击"文件列表"按钮，查看模拟文件列表

## 🔧 技术特点

### 1. 线程安全
- 所有网络操作在IO线程执行
- Toast提示自动切换到主线程显示
- 避免界面阻塞

### 2. 进度监控
```kotlin
// 上传进度回调
repository.uploadFileWithProgress(file) { uploaded, total, percentage ->
    updateUiState { it.copy(uploadProgress = percentage) }
}.collect { result ->
    // 处理结果
}
```

### 3. 错误处理
- 统一的异常处理机制
- 友好的错误提示
- 详细的错误信息记录

### 4. 类型安全
- 使用强类型的数据模型
- Kotlin协程支持
- 类型安全的JSON序列化

## 📊 数据模型

### FileUploadResult - 上传结果
```kotlin
data class FileUploadResult(
    val fileId: String,        // 文件唯一标识
    val fileName: String,      // 文件名
    val fileSize: Long,        // 文件大小
    val contentType: String,   // MIME类型
    val uploadTime: String,    // 上传时间
    val downloadUrl: String    // 下载链接
)
```

### FileInfo - 文件信息
```kotlin
data class FileInfo(
    val fileId: String,
    val fileName: String,
    val fileSize: Long,
    val contentType: String,
    val uploadTime: String,
    val downloadUrl: String,
    val metadata: Map<String, String>? = null  // 扩展元数据
)
```

## 🎨 UI特性

### 1. 现代化设计
- Material Design 3 风格
- 卡片化布局
- 清晰的视觉层次

### 2. 交互友好
- 实时进度显示
- 操作状态反馈
- 错误提示与恢复

### 3. 信息丰富
- 文件详细信息展示
- 操作历史记录
- 使用说明指导

## 🚀 扩展能力

### 1. 服务器集成
当前使用模拟数据，实际项目中可以：
- 替换 `FileRepository` 中的模拟方法
- 配置真实的服务器端点
- 添加认证和权限控制

### 2. 功能扩展
- 支持文件预览
- 添加文件分类管理
- 实现断点续传
- 支持文件同步

### 3. 性能优化
- 大文件分片上传
- 并发下载管理
- 缓存策略优化

## 📝 使用示例

### 基本上传
```kotlin
// 在ViewModel中
fun uploadFile(file: File) {
    viewModelScope.launch {
        val result = repository.uploadSingleFile(file, "我的文件")
        when (result) {
            is ApiResult.Success -> {
                // 上传成功，更新UI
                updateUiState { 
                    it.copy(uploadResults = it.uploadResults + result.data)
                }
            }
            is ApiResult.Error -> {
                // 处理业务错误
                showError(result.message)
            }
            is ApiResult.Exception -> {
                // 处理网络异常
                showError(result.message)
            }
        }
    }
}
```

### 带进度下载
```kotlin
fun downloadWithProgress(url: String, fileName: String) {
    repository.downloadFile(url, targetFile) { downloaded, total, percentage ->
        // 更新进度
        updateProgress(percentage)
    }.collect { result ->
        // 处理下载结果
        handleDownloadResult(result)
    }
}
```

## 🔄 最佳实践

### 1. 文件大小控制
- 限制单文件大小（建议<50MB）
- 大文件使用分片上传
- 提供文件压缩选项

### 2. 用户体验
- 显示清晰的进度指示
- 提供取消操作选项
- 失败时提供重试机制

### 3. 安全考虑
- 验证文件类型和大小
- 防止恶意文件上传
- 实现访问权限控制

## 🛠️ 依赖说明

使用的主要技术栈：
- **Retrofit2**: 网络请求框架
- **OkHttp3**: HTTP客户端和文件处理
- **Kotlin协程**: 异步处理
- **Jetpack Compose**: 现代UI框架
- **Moshi**: JSON序列化

## 📱 实际效果

当前实现提供了：
- ✅ 完整的文件操作UI界面
- ✅ 模拟的上传下载功能
- ✅ 实时进度显示
- ✅ 错误处理和用户提示
- ✅ 文件信息展示
- ✅ 清晰的使用指导

用户可以通过这个功能：
- 学习文件上传下载的实现方式
- 了解进度监控的技术要点
- 体验现代Android开发的最佳实践
- 为实际项目提供代码参考

这个功能为 Retrofit 网络请求框架增加了重要的文件处理能力，展示了完整的文件操作解决方案！