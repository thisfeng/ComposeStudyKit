package com.thisfeng.composestudykit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thisfeng.composestudykit.data.model.Article
import com.thisfeng.composestudykit.data.model.Banner
import com.thisfeng.composestudykit.data.repository.WanAndroidRepository
import com.thisfeng.composestudykit.network.ApiResult
import com.thisfeng.composestudykit.utils.AppGlobals
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * 不带缓存的 API 测试 ViewModel
 * 
 * 🎯 核心特性：
 * 1. 不使用缓存，直接网络请求
 * 2. 独立并发请求，单个失败不影响其他
 * 3. 渐进式 UI 更新，先到先显示
 * 4. 超时控制，避免无限等待
 * 5. 详细的性能分析数据
 */
class NoCacheApiViewModel : ViewModel() {

    private val repository = WanAndroidRepository(AppGlobals.getApplication())

    // UI 状态 - 每个数据源独立管理
    private val _uiState = MutableStateFlow(NoCacheUiState())
    val uiState: StateFlow<NoCacheUiState> = _uiState.asStateFlow()

    // 性能分析数据
    private val _performanceStats = MutableStateFlow(PerformanceStats())
    val performanceStats: StateFlow<PerformanceStats> = _performanceStats.asStateFlow()

    // 异常处理器
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        updatePerformanceStats { it.copy(unexpectedErrors = it.unexpectedErrors + 1) }
        println("❌ 协程异常: ${throwable.message}")
    }

    /**
     * 🚀 独立并发加载 - 先到先显示策略
     * 
     * 特点：
     * - 三个请求完全独立，不互相阻塞
     * - 每个请求有独立的超时控制
     * - 单个请求失败不影响其他请求
     * - 每个数据返回立即更新 UI（可能触发重组）
     */
    fun loadDataIndependently() {
        // 重置状态
        resetState()
        
        val startTime = System.currentTimeMillis()
        updatePerformanceStats { it.copy(totalStartTime = startTime) }

        // 🔥 关键点：三个独立的协程，不使用 async + await
        // 每个协程独立处理，不会相互阻塞

        // 协程1: 加载 Banner
        viewModelScope.launch(exceptionHandler) {
            loadBannerIndependently()
        }

        // 协程2: 加载文章列表  
        viewModelScope.launch(exceptionHandler) {
            loadArticlesIndependently()
        }

        // 协程3: 加载置顶文章
        viewModelScope.launch(exceptionHandler) {
            loadTopArticlesIndependently()
        }
    }

    /**
     * 独立加载 Banner 数据
     */
    private suspend fun loadBannerIndependently() {
        val startTime = System.currentTimeMillis()
        
        // 设置加载状态
        updateUiState { 
            it.copy(bannerState = RequestState.Loading("开始加载 Banner...")) 
        }

        // 带超时的网络请求
        val result = withTimeoutOrNull(10_000) { // 10秒超时
            repository.getBannersDirectly()
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        when (result) {
            is ApiResult.Success -> {
                updateUiState {
                    it.copy(
                        banners = result.data,
                        bannerState = RequestState.Success("Banner 加载成功", duration),
                        bannerCompletedAt = endTime
                    )
                }
                updatePerformanceStats { 
                    it.copy(
                        bannerDuration = duration,
                        bannerSuccess = true,
                        successfulRequests = it.successfulRequests + 1
                    ) 
                }
                println("✅ Banner 加载完成，耗时: ${duration}ms，数据量: ${result.data.size}")
            }
            
            is ApiResult.Error -> {
                updateUiState {
                    it.copy(
                        bannerState = RequestState.Error("Banner 加载失败: ${result.message}", duration),
                        bannerCompletedAt = endTime
                    )
                }
                updatePerformanceStats { 
                    it.copy(
                        bannerDuration = duration,
                        bannerSuccess = false,
                        failedRequests = it.failedRequests + 1
                    ) 
                }
                println("❌ Banner 加载失败: ${result.message}，耗时: ${duration}ms")
            }
            
            is ApiResult.Exception -> {
                updateUiState {
                    it.copy(
                        bannerState = RequestState.Error("Banner 异常: ${result.exception.message}", duration),
                        bannerCompletedAt = endTime
                    )
                }
                updatePerformanceStats { 
                    it.copy(
                        bannerDuration = duration,
                        bannerSuccess = false,
                        failedRequests = it.failedRequests + 1
                    ) 
                }
                println("❌ Banner 异常: ${result.exception.message}，耗时: ${duration}ms")
            }
            
            null -> {
                // 超时情况
                updateUiState {
                    it.copy(
                        bannerState = RequestState.Error("Banner 请求超时", duration),
                        bannerCompletedAt = endTime
                    )
                }
                updatePerformanceStats { 
                    it.copy(
                        bannerDuration = duration,
                        bannerSuccess = false,
                        timeoutRequests = it.timeoutRequests + 1
                    ) 
                }
                println("⏰ Banner 请求超时，耗时: ${duration}ms")
            }
            
            else -> {
                updateUiState {
                    it.copy(
                        bannerState = RequestState.Error("Banner 未知状态", duration),
                        bannerCompletedAt = endTime
                    )
                }
                println("❓ Banner 未知状态")
            }
        }
    }

    /**
     * 独立加载文章列表数据
     */
    private suspend fun loadArticlesIndependently() {
        val startTime = System.currentTimeMillis()
        
        updateUiState { 
            it.copy(articleState = RequestState.Loading("开始加载文章列表...")) 
        }

        val result = withTimeoutOrNull(15_000) { // 15秒超时
            repository.getArticlesDirectly(0)
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        when (result) {
            is ApiResult.Success -> {
                updateUiState {
                    it.copy(
                        articles = result.data.datas,
                        articleState = RequestState.Success("文章列表加载成功", duration),
                        articleCompletedAt = endTime
                    )
                }
                updatePerformanceStats { 
                    it.copy(
                        articleDuration = duration,
                        articleSuccess = true,
                        successfulRequests = it.successfulRequests + 1
                    ) 
                }
                println("✅ 文章列表加载完成，耗时: ${duration}ms，数据量: ${result.data.datas.size}")
            }
            
            is ApiResult.Error -> {
                updateUiState {
                    it.copy(
                        articleState = RequestState.Error("文章列表加载失败: ${result.message}", duration),
                        articleCompletedAt = endTime
                    )
                }
                updatePerformanceStats { 
                    it.copy(
                        articleDuration = duration,
                        articleSuccess = false,
                        failedRequests = it.failedRequests + 1
                    ) 
                }
                println("❌ 文章列表加载失败: ${result.message}，耗时: ${duration}ms")
            }
            
            is ApiResult.Exception -> {
                updateUiState {
                    it.copy(
                        articleState = RequestState.Error("文章列表异常: ${result.exception.message}", duration),
                        articleCompletedAt = endTime
                    )
                }
                updatePerformanceStats { 
                    it.copy(
                        articleDuration = duration,
                        articleSuccess = false,
                        failedRequests = it.failedRequests + 1
                    ) 
                }
                println("❌ 文章列表异常: ${result.exception.message}，耗时: ${duration}ms")
            }
            
            null -> {
                updateUiState {
                    it.copy(
                        articleState = RequestState.Error("文章列表请求超时", duration),
                        articleCompletedAt = endTime
                    )
                }
                updatePerformanceStats { 
                    it.copy(
                        articleDuration = duration,
                        articleSuccess = false,
                        timeoutRequests = it.timeoutRequests + 1
                    ) 
                }
                println("⏰ 文章列表请求超时，耗时: ${duration}ms")
            }
            
            else -> {
                updateUiState {
                    it.copy(
                        articleState = RequestState.Error("文章列表未知状态", duration),
                        articleCompletedAt = endTime
                    )
                }
                println("❓ 文章列表未知状态")
            }
        }
    }

    /**
     * 独立加载置顶文章数据
     */
    private suspend fun loadTopArticlesIndependently() {
        val startTime = System.currentTimeMillis()
        
        updateUiState { 
            it.copy(topArticleState = RequestState.Loading("开始加载置顶文章...")) 
        }

        val result = withTimeoutOrNull(12_000) { // 12秒超时
            repository.getTopArticlesDirectly()
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime

        when (result) {
            is ApiResult.Success -> {
                updateUiState {
                    it.copy(
                        topArticles = result.data,
                        topArticleState = RequestState.Success("置顶文章加载成功", duration),
                        topArticleCompletedAt = endTime
                    )
                }
                updatePerformanceStats { 
                    it.copy(
                        topArticleDuration = duration,
                        topArticleSuccess = true,
                        successfulRequests = it.successfulRequests + 1
                    ) 
                }
                println("✅ 置顶文章加载完成，耗时: ${duration}ms，数据量: ${result.data.size}")
            }
            
            is ApiResult.Error -> {
                updateUiState {
                    it.copy(
                        topArticleState = RequestState.Error("置顶文章加载失败: ${result.message}", duration),
                        topArticleCompletedAt = endTime
                    )
                }
                updatePerformanceStats { 
                    it.copy(
                        topArticleDuration = duration,
                        topArticleSuccess = false,
                        failedRequests = it.failedRequests + 1
                    ) 
                }
                println("❌ 置顶文章加载失败: ${result.message}，耗时: ${duration}ms")
            }
            
            is ApiResult.Exception -> {
                updateUiState {
                    it.copy(
                        topArticleState = RequestState.Error("置顶文章异常: ${result.exception.message}", duration),
                        topArticleCompletedAt = endTime
                    )
                }
                updatePerformanceStats { 
                    it.copy(
                        topArticleDuration = duration,
                        topArticleSuccess = false,
                        failedRequests = it.failedRequests + 1
                    ) 
                }
                println("❌ 置顶文章异常: ${result.exception.message}，耗时: ${duration}ms")
            }
            
            null -> {
                updateUiState {
                    it.copy(
                        topArticleState = RequestState.Error("置顶文章请求超时", duration),
                        topArticleCompletedAt = endTime
                    )
                }
                updatePerformanceStats { 
                    it.copy(
                        topArticleDuration = duration,
                        topArticleSuccess = false,
                        timeoutRequests = it.timeoutRequests + 1
                    ) 
                }
                println("⏰ 置顶文章请求超时，耗时: ${duration}ms")
            }
            
            else -> {
                updateUiState {
                    it.copy(
                        topArticleState = RequestState.Error("置顶文章未知状态", duration),
                        topArticleCompletedAt = endTime
                    )
                }
                println("❓ 置顶文章未知状态")
            }
        }
    }

    /**
     * 重置状态
     */
    private fun resetState() {
        _uiState.value = NoCacheUiState()
        _performanceStats.value = PerformanceStats()
        println("🔄 状态已重置，开始新的测试")
    }

    /**
     * 更新 UI 状态
     */
    private fun updateUiState(update: (NoCacheUiState) -> NoCacheUiState) {
        _uiState.value = update(_uiState.value)
    }

    /**
     * 更新性能统计
     */
    private fun updatePerformanceStats(update: (PerformanceStats) -> PerformanceStats) {
        _performanceStats.value = update(_performanceStats.value)
    }

    /**
     * 获取完成顺序分析
     */
    fun getCompletionOrder(): List<String> {
        val state = _uiState.value
        val completions = mutableListOf<Pair<String, Long>>()
        
        if (state.bannerCompletedAt > 0) {
            completions.add("Banner" to state.bannerCompletedAt)
        }
        if (state.articleCompletedAt > 0) {
            completions.add("文章列表" to state.articleCompletedAt)
        }
        if (state.topArticleCompletedAt > 0) {
            completions.add("置顶文章" to state.topArticleCompletedAt)
        }
        
        return completions.sortedBy { it.second }.map { it.first }
    }
}

/**
 * 不带缓存的 UI 状态
 */
data class NoCacheUiState(
    // 数据
    val banners: List<Banner> = emptyList(),
    val articles: List<Article> = emptyList(),
    val topArticles: List<Article> = emptyList(),
    
    // 各请求的状态
    val bannerState: RequestState = RequestState.Initial,
    val articleState: RequestState = RequestState.Initial,
    val topArticleState: RequestState = RequestState.Initial,
    
    // 完成时间戳（用于分析完成顺序）
    val bannerCompletedAt: Long = 0,
    val articleCompletedAt: Long = 0,
    val topArticleCompletedAt: Long = 0
)

/**
 * 请求状态
 */
sealed class RequestState {
    data object Initial : RequestState()
    data class Loading(val message: String) : RequestState()
    data class Success(val message: String, val duration: Long) : RequestState()
    data class Error(val message: String, val duration: Long) : RequestState()
}

/**
 * 性能统计数据
 */
data class PerformanceStats(
    val totalStartTime: Long = 0,
    
    // 各接口耗时
    val bannerDuration: Long = 0,
    val articleDuration: Long = 0,
    val topArticleDuration: Long = 0,
    
    // 各接口成功状态
    val bannerSuccess: Boolean = false,
    val articleSuccess: Boolean = false,
    val topArticleSuccess: Boolean = false,
    
    // 统计计数
    val successfulRequests: Int = 0,
    val failedRequests: Int = 0,
    val timeoutRequests: Int = 0,
    val unexpectedErrors: Int = 0
) {
    val totalRequests: Int get() = successfulRequests + failedRequests + timeoutRequests
    val successRate: Float get() = if (totalRequests > 0) successfulRequests.toFloat() / totalRequests else 0f
    val maxDuration: Long get() = maxOf(bannerDuration, articleDuration, topArticleDuration)
    val avgDuration: Long get() = if (totalRequests > 0) (bannerDuration + articleDuration + topArticleDuration) / totalRequests else 0
}