# ApiResult 异常信息处理使用指南

## 概述
`ApiResult.Exception` 提供了健壮的异常信息处理机制，支持友好的错误提示和详细的异常分析。

## 核心特性

### 1. 友好错误信息优先
```kotlin
// 创建异常时提供友好的错误信息
val result = ApiResult.Exception(
    exception = IOException("Connection timeout"),
    message = "网络连接超时，请稍后重试"
)

// 获取错误信息 - 优先返回友好信息
val errorMsg = result.getErrorMessage() 
// 返回: "网络连接超时，请稍后重试"
```

### 2. 详细异常信息
```kotlin
// 获取包含异常类型和原因的详细信息
val details = result.getExceptionDetails()
// 返回: "网络连接超时，请稍后重试 (原因: Connection timeout)"
```

### 3. 异常类型判断
```kotlin
// 判断是否为网络连接异常
if (result.isNetworkConnectionException()) {
    // 显示网络连接相关的提示
    showNetworkSettings()
}

// 判断是否为超时异常
if (result.isTimeoutException()) {
    // 提供重试选项
    showRetryOption()
}
```

## 使用示例

### 在 ViewModel 中处理异常
```kotlin
class MyViewModel : ViewModel() {
    
    fun loadData() {
        viewModelScope.launch {
            repository.getData()
                .onSuccess { data, msg ->
                    // 处理成功情况
                    updateUiState { it.copy(data = data) }
                }
                .onError { code, message ->
                    // 处理业务错误
                    showToast("业务错误: $message")
                }
                .onException { exception, friendlyMessage ->
                    // 处理网络异常 - 显示友好提示
                    showToast(friendlyMessage)
                    
                    // 根据异常类型提供不同的解决方案
                    when {
                        isNetworkConnectionException() -> {
                            // 提示检查网络设置
                            showNetworkSettings()
                        }
                        isTimeoutException() -> {
                            // 提供重试选项
                            showRetryButton()
                        }
                        else -> {
                            // 记录详细错误信息用于调试
                            logger.error(getExceptionDetails())
                        }
                    }
                }
        }
    }
}
```

### 在 UI 中显示错误信息
```kotlin
@Composable
fun DataScreen(uiState: UiState) {
    when (val result = uiState.dataResult) {
        is ApiResult.Success -> {
            // 显示数据
            DataContent(result.data)
        }
        
        is ApiResult.Error -> {
            // 显示业务错误
            ErrorCard(
                title = "请求失败",
                message = result.message,
                actionText = "重试"
            )
        }
        
        is ApiResult.Exception -> {
            // 显示网络异常
            ErrorCard(
                title = "网络问题",
                message = result.getErrorMessage(), // 友好的错误信息
                actionText = when {
                    result.isNetworkConnectionException() -> "检查网络"
                    result.isTimeoutException() -> "重试"
                    else -> "确定"
                },
                onAction = {
                    when {
                        result.isNetworkConnectionException() -> {
                            // 打开网络设置
                            openNetworkSettings()
                        }
                        result.isTimeoutException() -> {
                            // 重新请求
                            viewModel.retry()
                        }
                        else -> {
                            // 关闭错误提示
                            viewModel.clearError()
                        }
                    }
                }
            )
        }
        
        ApiResult.Loading -> {
            LoadingIndicator()
        }
    }
}
```

## 错误信息层级

### 1. 友好提示信息 (message)
- 面向用户的中文提示
- 简洁明了，提供解决建议
- 例如："网络连接失败，请检查网络设置后重试"

### 2. 原始异常信息 (exception.message)
- 来自系统或框架的原始错误
- 主要用于开发调试
- 例如："Connection timeout after 10000ms"

### 3. 详细异常信息 (getExceptionDetails())
- 包含异常类型、友好提示和原始信息
- 用于日志记录和问题分析
- 例如："网络连接失败，请检查网络设置后重试 (原因: Connection timeout after 10000ms)"

## 最佳实践

### 1. 错误信息显示原则
- **用户界面**: 使用 `getErrorMessage()` 显示友好提示
- **Toast 提示**: 使用 `message` 属性显示简洁信息
- **日志记录**: 使用 `getExceptionDetails()` 记录完整信息

### 2. 异常处理策略
```kotlin
// 推荐的异常处理模式
result.onException { exception, friendlyMessage ->
    // 1. 显示用户友好提示
    ToastUtils.showLong(context, friendlyMessage)
    
    // 2. 根据异常类型提供解决方案
    when {
        result.isNetworkConnectionException() -> {
            // 网络问题 - 引导用户检查网络
            showNetworkTroubleshooting()
        }
        result.isTimeoutException() -> {
            // 超时问题 - 提供重试机制
            enableAutoRetry()
        }
        else -> {
            // 其他异常 - 记录详细信息
            crashlytics.recordException(exception)
            logger.error("API异常: ${result.getExceptionDetails()}")
        }
    }
    
    // 3. 更新UI状态
    updateUiState { it.copy(error = friendlyMessage) }
}
```

### 3. 错误恢复机制
```kotlin
// 自动重试机制
if (result.isTimeoutException() && retryCount < MAX_RETRY) {
    delay(RETRY_DELAY)
    retry()
} else {
    showError(result.getErrorMessage())
}

// 降级方案
if (result.isNetworkConnectionException()) {
    // 显示缓存数据
    loadFromCache()
} else {
    showError(result.getErrorMessage())
}
```

## 调试支持

### 异常分析工具
```kotlin
fun analyzeApiResult(result: ApiResult<*>) {
    when (result) {
        is ApiResult.Exception -> {
            println("=== API异常分析 ===")
            println("异常类型: ${result.exception::class.simpleName}")
            println("友好提示: ${result.message}")
            println("原始信息: ${result.exception.message}")
            println("详细信息: ${result.getExceptionDetails()}")
            println("网络连接问题: ${result.isNetworkConnectionException()}")
            println("超时问题: ${result.isTimeoutException()}")
            println("==================")
        }
    }
}
```

这种设计确保了：
- ✅ 用户看到友好的中文错误提示
- ✅ 开发者能获取详细的调试信息
- ✅ 支持基于异常类型的智能处理
- ✅ 提供完整的错误恢复机制
- ✅ 保持代码的可读性和可维护性