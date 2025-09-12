# ToastUtils 线程安全优化测试文档

## 问题描述
在 NetworkErrorInterceptor 中调用 ToastUtils.showLong() 时出现 NullPointerException：
```
java.lang.NullPointerException: Can't toast on a thread that has not called Looper.prepare()
```

## 根本原因
- NetworkErrorInterceptor 在网络线程（子线程）中执行
- Toast 必须在主线程（UI线程）中显示
- 原来的 ToastUtils 没有处理线程切换

## 解决方案
修改 ToastUtils 支持线程安全调用：

### 1. 添加主线程检查和切换机制
```kotlin
private val mainHandler = Handler(Looper.getMainLooper())

private fun show(context: Context, message: String, duration: Int) {
    // 检查是否在主线程
    if (Looper.myLooper() == Looper.getMainLooper()) {
        // 在主线程，直接显示
        showToastOnMainThread(context, message, duration)
    } else {
        // 在子线程，切换到主线程显示
        mainHandler.post {
            showToastOnMainThread(context, message, duration)
        }
    }
}
```

### 2. 优化的特性
- **线程安全**：支持在任何线程中调用
- **自动切换**：自动检测线程并切换到主线程
- **性能优化**：主线程直接执行，子线程才切换
- **取消操作**：cancel() 方法也支持线程安全

## 测试场景
1. **主线程调用**：ViewModel、UI组件中调用 ✅
2. **子线程调用**：NetworkErrorInterceptor、后台任务中调用 ✅
3. **协程调用**：suspend 函数中调用 ✅
4. **Retrofit 拦截器调用**：网络请求拦截器中调用 ✅

## 使用示例
```kotlin
// 在任何线程中都可以安全调用
ToastUtils.showLong(context, "网络连接失败，请检查网络设置")
```

## 验证方法
1. 在 NetworkErrorInterceptor 中断点调试
2. 触发网络异常（断网或超时）
3. 观察 Toast 是否正常显示，不再抛出异常

## 优化效果
- ✅ 解决了线程异常问题
- ✅ 支持在任何地方安全调用 Toast
- ✅ 保持了原有API的兼容性
- ✅ 提升了调试体验和错误处理的稳定性