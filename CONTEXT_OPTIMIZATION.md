# Context 传递优化最佳实践

## 🔍 **问题分析**

### **当前实现的问题**
```kotlin
MainActivity → RealApiViewModel(context) → WanAndroidRepository(context) → DataStoreCacheManager(context) → DataStore
```

**存在的问题：**
- ❌ **内存泄漏风险**：Repository 长期持有 Activity Context
- ❌ **生命周期不匹配**：DataStore 应该使用全局 Context
- ❌ **架构不清晰**：Context 传递链过长
- ❌ **测试困难**：依赖具体的 Activity Context

## 🛠️ **优化方案对比**

### **方案一：使用 Application Context（简单改进）**

```kotlin
/**
 * 真实 API ViewModel - 使用统一的 UIState 设计
 * 🔧 Context 优化：使用 Application Context 避免内存泄漏
 */
class RealApiViewModel(private val context: Context) : ViewModel() {

    // 使用 Application Context 避免内存泄漏和生命周期问题
    // DataStore 应该使用全局 Context，不依赖具体的 Activity
    private val repository = WanAndroidRepository(context.applicationContext)
    
    // ... 其他代码保持不变
}
```

**优势：**
- ✅ **最小改动**：只需修改一行代码
- ✅ **立即生效**：解决内存泄漏问题
- ✅ **向后兼容**：不影响现有调用方式

**使用方式：**
```kotlin
// UI 层调用保持不变
val viewModel: RealApiViewModel = remember { RealApiViewModel(context) }
```

### **方案二：全局 Application 获取（推荐）**

```kotlin
/**
 * 真实 API ViewModel - 优化版本
 * 🔧 架构优化：不再依赖外部传入的 Context
 */
class RealApiViewModelV2 : ViewModel() {

    // 直接使用全局 Application，不依赖外部 Context 传递
    private val repository = WanAndroidRepository(AppGlobals.getApplication())
    
    // ... 其他代码
}
```

**优势：**
- ✅ **架构更清晰**：ViewModel 不依赖外部 Context
- ✅ **更易测试**：可以独立创建 ViewModel
- ✅ **符合原则**：单一职责，依赖倒置
- ✅ **通用性强**：适用于组件化项目

**使用方式：**
```kotlin
// UI 层调用更简洁
val viewModel: RealApiViewModelV2 = remember { RealApiViewModelV2() }
```

### **方案三：Repository 层优化（进阶）**

还可以进一步优化 Repository 层，使用单例模式：

```kotlin
/**
 * Repository 单例管理器
 */
object RepositoryManager {
    
    @Volatile
    private var instance: WanAndroidRepository? = null
    
    fun getInstance(): WanAndroidRepository {
        return instance ?: synchronized(this) {
            instance ?: WanAndroidRepository(AppGlobals.getApplication()).also { instance = it }
        }
    }
}

/**
 * ViewModel 进一步简化
 */
class RealApiViewModelV3 : ViewModel() {
    
    // 使用单例 Repository，更符合数据层设计
    private val repository = RepositoryManager.getInstance()
    
    // ... 其他代码
}
```

## 🎯 **AppGlobals 工具类详解**

### **实现特点**
- ✅ **线程安全**：使用 `@Volatile` + `synchronized` 双重检查
- ✅ **反射获取**：一次反射，永久缓存
- ✅ **组件化友好**：不依赖具体 Application 类名
- ✅ **异常处理**：完整的错误处理机制

### **核心代码**
```kotlin
object AppGlobals {
    
    @Volatile
    private var application: Application? = null
    
    fun getApplication(): Application {
        if (application == null) {
            synchronized(this) {
                if (application == null) {
                    try {
                        application = Class.forName("android.app.ActivityThread")
                            .getMethod("currentApplication")
                            .invoke(null) as Application
                    } catch (e: Exception) {
                        e.printStackTrace()
                        throw RuntimeException("Failed to get Application instance", e)
                    }
                }
            }
        }
        return application!!
    }
}
```

### **使用场景**
- 🎯 **数据存储**：DataStore、数据库初始化
- 🎯 **网络配置**：Retrofit、OkHttp 全局配置
- 🎯 **工具类**：需要 Context 的工具类
- 🎯 **组件化**：跨模块的 Context 获取

## 📊 **方案对比表**

| 特性 | 当前方案 | 方案一 | 方案二 | 方案三 |
|------|----------|--------|--------|--------|
| 内存安全 | ❌ | ✅ | ✅ | ✅ |
| 架构清晰度 | ❌ | ⚠️ | ✅ | ✅ |
| 测试友好性 | ❌ | ⚠️ | ✅ | ✅ |
| 改动成本 | - | 很低 | 低 | 中等 |
| 长期维护性 | ❌ | ⚠️ | ✅ | ✅ |

## 🚀 **推荐实施步骤**

### **第一步：立即修复（方案一）**
修改现有 ViewModel，使用 `context.applicationContext`

### **第二步：架构升级（方案二）**
1. 创建 `AppGlobals` 工具类
2. 创建 `RealApiViewModelV2`
3. 逐步迁移 UI 层调用

### **第三步：深度优化（方案三）**
根据项目需要，考虑 Repository 单例化

## 🔧 **适用场景建议**

### **小型项目 → 方案一**
- 快速修复，成本最低
- 适合学习项目、原型项目

### **中型项目 → 方案二**
- 架构清晰，易于维护
- 适合商业项目、长期维护项目

### **大型项目 → 方案三**
- 完整的架构设计
- 适合企业级项目、组件化项目

## 💡 **最佳实践总结**

1. **DataStore 应该使用 Application Context**
2. **ViewModel 不应该直接依赖 Activity Context**
3. **使用全局 Application 获取更符合架构原则**
4. **反射获取 Application 是组件化的通用方案**
5. **考虑单例模式管理全局数据层组件**

这样的架构优化不仅解决了内存泄漏问题，还让代码更加清晰、易测试、易维护！🎉