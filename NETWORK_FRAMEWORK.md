# Android 网络请求框架使用文档

本项目实现了一个现代化的 Android 网络请求框架，基于 **Retrofit2 + OkHttp + Moshi + Coroutines** 构建，提供了完整的网络请求解决方案。

## 🚀 框架特性

### 1. 统一的结果包装
- `ApiResult` 封装了所有网络请求的状态（Success、Error、Exception、Loading）
- 提供丰富的扩展函数，方便状态判断和数据获取
- 类型安全的响应处理

### 2. 标准化的响应格式
- `ApiResponse<T>` 用于我方标准格式的接口
- 支持第三方 API 的原始数据返回
- 自动转换响应格式为 `ApiResult`

### 3. 智能错误处理
- `NetworkErrorInterceptor` 自动处理常见的 HTTP 错误
- 统一的错误码映射和异常处理
- 支持自定义网络异常类型

### 4. 灵活的客户端配置
- 支持多种 Retrofit 实例配置
- 可配置的 OkHttp 客户端
- 支持日志拦截器和自定义拦截器

## 📁 项目结构

```
app/src/main/java/com/thisfeng/composestudykit/
├── network/                    # 网络框架核心
│   ├── ApiResult.kt           # 统一结果包装类
│   ├── ApiResponse.kt         # 标准响应格式
│   ├── BaseRepository.kt      # Repository 基类
│   ├── NetworkErrorInterceptor.kt  # 错误拦截器
│   └── RetrofitClient.kt      # Retrofit 客户端工厂
├── data/
│   ├── model/                 # 数据模型
│   │   └── Article.kt         # 示例数据模型
│   └── api/                   # API 接口定义
│       └── WanAndroidApiService.kt  # 示例 API 接口
├── ui/
│   ├── screen/                # 界面层
│   │   └── SimpleHomeScreen.kt     # 演示界面
│   └── viewmodel/             # ViewModel 层
│       └── SimpleHomeViewModel.kt  # 演示 ViewModel
└── MainActivity.kt            # 主 Activity
```

## 🔧 使用方法

### 1. 定义数据模型

```kotlin
@JsonClass(generateAdapter = true)
data class Article(
    val id: Int,
    val title: String,
    val author: String
    // ... 其他字段
)
```

### 2. 定义 API 接口

```kotlin
interface WanAndroidApiService {
    // 标准格式 API（返回 ApiResponse）
    @GET("article/list/{page}/json")
    suspend fun getArticles(@Path("page") page: Int): ApiResponse<ArticleList>
    
    // 第三方 API（直接返回原始数据）
    @GET("users/{user}/repos")
    suspend fun getUserRepos(@Path("user") user: String): List<Repository>
}
```

### 3. 创建 Repository

```kotlin
class ArticleRepository : BaseRepository() {
    
    private val apiService = RetrofitClient.createStandardRetrofit()
        .create(WanAndroidApiService::class.java)
    
    // 标准格式 API
    suspend fun getArticles(page: Int): ApiResult<ArticleList> {
        return safeApiCall { apiService.getArticles(page) }
    }
    
    // 第三方 API
    suspend fun getUserRepos(user: String): ApiResult<List<Repository>> {
        return safeRawApiCall { apiService.getUserRepos(user) }
    }
    
    // 组合多个请求
    suspend fun getHomeData(): ApiResult<HomeData> {
        return try {
            val bannerResult = getBanners()
            val articlesResult = getArticles(0)
            
            if (bannerResult.isSuccess && articlesResult.isSuccess) {
                val homeData = HomeData(
                    banners = bannerResult.getOrNull() ?: emptyList(),
                    articles = articlesResult.getOrNull()?.datas ?: emptyList()
                )
                ApiResult.Success(homeData, "数据加载成功")
            } else {
                // 返回第一个错误
                when {
                    bannerResult.isError || bannerResult.isException -> bannerResult as ApiResult<HomeData>
                    else -> articlesResult as ApiResult<HomeData>
                }
            }
        } catch (e: Exception) {
            ApiResult.Exception(e)
        }
    }
}
```

### 4. 在 ViewModel 中使用

```kotlin
class HomeViewModel : ViewModel() {
    
    private val repository = ArticleRepository()
    
    private val _articles = MutableStateFlow<ApiResult<List<Article>>>(ApiResult.Loading)
    val articles: StateFlow<ApiResult<List<Article>>> = _articles.asStateFlow()
    
    fun loadArticles() {
        viewModelScope.launch {
            _articles.value = ApiResult.Loading
            val result = repository.getArticles(0)
            _articles.value = result.map { it.datas }
        }
    }
}
```

### 5. 在 Compose UI 中使用

```kotlin
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val articles by viewModel.articles.collectAsState()
    
    when (articles) {
        is ApiResult.Loading -> {
            CircularProgressIndicator()
        }
        is ApiResult.Success -> {
            val successArticles = articles as ApiResult.Success<List<Article>>
            LazyColumn {
                items(successArticles.data) { article ->
                    ArticleItem(article = article)
                }
            }
        }
        is ApiResult.Error -> {
            val errorArticles = articles as ApiResult.Error
            ErrorMessage(
                message = errorArticles.message,
                onRetry = { viewModel.loadArticles() }
            )
        }
        is ApiResult.Exception -> {
            val exceptionArticles = articles as ApiResult.Exception
            ErrorMessage(
                message = exceptionArticles.exception.message ?: "Unknown error",
                onRetry = { viewModel.loadArticles() }
            )
        }
    }
}
```

## 🔨 扩展功能

### 1. 创建自定义 Retrofit 实例

```kotlin
val customRetrofit = RetrofitClient.createCustomRetrofit(
    baseUrl = "https://your-api.com/",
    moshi = RetrofitClient.createMoshi(),
    okHttpClient = customOkHttpClient
)
```

### 2. 添加自定义拦截器

```kotlin
class AuthInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("Authorization", "Bearer your_token")
            .build()
        return chain.proceed(request)
    }
}

// 在创建 OkHttpClient 时添加
val client = RetrofitClient.createDefaultOkHttpClient().newBuilder()
    .addInterceptor(AuthInterceptor())
    .build()
```

### 3. 处理不同的响应格式

```kotlin
// 我方标准格式
{
  "errorCode": 0,
  "errorMsg": "成功",
  "data": { ... }
}

// 第三方格式（直接返回数据）
{ "key": "value", ... }
```

## 🧪 演示项目

当前项目包含了一个完整的演示，展示框架的各种功能：

- **模拟网络请求**：展示 Loading、Success、Error 状态
- **错误处理**：演示网络错误、业务错误的统一处理  
- **UI 状态管理**：完整的状态流转和界面更新
- **协程支持**：异步网络请求的现代化处理

### 运行演示

1. 打开项目并运行
2. 点击界面上的按钮测试不同功能
3. 观察不同状态下的界面表现

## 📚 最佳实践

### 1. 错误处理
- 始终处理所有 `ApiResult` 状态
- 为用户提供友好的错误提示
- 实现合适的重试机制

### 2. 状态管理
- 使用 `StateFlow` 管理异步状态
- 在网络请求时显示加载指示器
- 合理处理状态转换

### 3. 性能优化
- 根据业务需求实现缓存策略
- 合理配置网络超时时间
- 使用协程避免阻塞主线程

### 4. 代码组织
- Repository 负责数据获取和缓存
- ViewModel 负责业务逻辑和状态管理
- UI 层只负责状态展示和用户交互

## 🔗 技术栈

- **Retrofit2**: HTTP 客户端
- **OkHttp**: 网络请求引擎
- **Moshi**: JSON 序列化/反序列化
- **Kotlin Coroutines**: 异步编程
- **Jetpack Compose**: 现代化 UI
- **ViewModel & StateFlow**: 状态管理

## 📝 扩展计划

- [ ] 集成 Hilt 依赖注入（可选）
- [ ] 添加网络缓存策略
- [ ] 支持文件上传/下载
- [ ] 添加请求重试机制
- [ ] 集成网络状态监听
- [ ] 添加更多示例和测试用例

---

**注意**: 当前版本专注于核心功能的演示，生产环境使用时建议根据具体需求进行相应的安全配置和性能优化。