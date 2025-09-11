package com.thisfeng.composestudykit.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thisfeng.composestudykit.cache.CacheResult
import com.thisfeng.composestudykit.cache.CacheStrategy
import com.thisfeng.composestudykit.cache.getData
import com.thisfeng.composestudykit.data.model.Article
import com.thisfeng.composestudykit.data.model.Banner
import com.thisfeng.composestudykit.data.repository.WanAndroidRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

/**
 * 真实API页面的完整UI状态
 * 统一管理页面的所有数据和状态
 */
data class RealApiUiState(
    // 页面级加载状态 - 控制整个页面的加载显示
    val isPageLoading: Boolean = false,

    // 数据集合
    val banners: List<Banner> = emptyList(),
    val articles: List<Article> = emptyList(),
    val topArticles: List<Article> = emptyList(),

    // 各数据源的加载状态和来源标识
    val bannerState: DataState = DataState.Initial,
    val articleState: DataState = DataState.Initial,
    val topArticleState: DataState = DataState.Initial,

    // 页面级错误信息
    val errorMessage: String? = null,
    val lastApiCall: String = "",

    // 缓存统计信息
    val cacheStatsInfo: String = ""
)

/**
 * 数据状态枚举 - 表示每个数据源的状态
 */
sealed class DataState {
    data object Initial : DataState()           // 初始状态
    data object Loading : DataState()           // 加载中
    data class Success(val source: String) : DataState()  // 成功(来源: 缓存/网络)
    data class Error(val message: String) : DataState()   // 错误
}

/**
 * 获取数据来源描述
 */
fun DataState.getSourceDescription(): String {
    return when (this) {
        is DataState.Success -> source
        is DataState.Error -> "错误: $message"
        is DataState.Loading -> "加载中"
        is DataState.Initial -> "未加载"
    }
}

/**
 * 真实 API ViewModel - 使用统一的 UIState 设计
 * 调用真实的 WanAndroid API，并使用 DataStore 缓存策略
 *
 * 🔧 Context 优化：使用 Application Context 避免内存泄漏
 */
class RealApiViewModel(private val context: Context) : ViewModel() {

    // 使用 Application Context 避免内存泄漏和生命周期问题
    // DataStore 应该使用全局 Context，不依赖具体的 Activity
    private val repository = WanAndroidRepository(context.applicationContext)

    // 统一的 UI 状态
    private val _uiState = MutableStateFlow(RealApiUiState())
    val uiState: StateFlow<RealApiUiState> = _uiState.asStateFlow()

    init {
        // 初始化时加载页面数据
        loadPageData()
    }

    /**
     * 加载页面数据 - 页面级加载控制
     * 这是最佳实践：等待所有接口都完成后才关闭页面加载
     */
    fun loadPageData() {
        viewModelScope.launch {
            // 开始页面级加载
            updateUiState { it.copy(isPageLoading = true, errorMessage = null) }

            // 并发加载所有数据
            val bannerDeferred = async { loadBannersInternal() }
            val articlesDeferred = async { loadArticlesInternal() }
            val topArticlesDeferred = async { loadTopArticlesInternal() }

            // 等待所有数据加载完成
            bannerDeferred.await()
            articlesDeferred.await()
            topArticlesDeferred.await()

            // 关闭页面级加载
            updateUiState {
                it.copy(
                    isPageLoading = false,
                    cacheStatsInfo = getStatsInfo()
                )
            }
        }
    }

    /**
     * 内部方法：加载 Banner 数据
     */
    private suspend fun loadBannersInternal(strategy: CacheStrategy = CacheStrategy.CACHE_FIRST) {
        // 设置 Banner 加载状态
        updateUiState { it.copy(bannerState = DataState.Loading) }

        repository.getBanners(strategy).collect { result ->
            when (result) {
                is CacheResult.FromCache -> {
                    val banners = result.data
                    updateUiState {
                        it.copy(
                            banners = banners,
                            bannerState = DataState.Success(if (result.isExpired) "过期缓存" else "缓存"),
                            lastApiCall = "Banner (来源: ${if (result.isExpired) "过期缓存" else "缓存"})"
                        )
                    }
                }

                is CacheResult.FromNetwork -> {
                    val banners = result.data
                    updateUiState {
                        it.copy(
                            banners = banners,
                            bannerState = DataState.Success("网络"),
                            lastApiCall = "Banner (来源: 网络)"
                        )
                    }
                }

                is CacheResult.Failed -> {
                    updateUiState {
                        it.copy(
                            bannerState = DataState.Error("Banner加载失败"),
                            errorMessage = "Banner加载失败"
                        )
                    }
                }

                /*  is CacheResult.Loading -> {
                      // 接口级Loading状态已在上面设置，这里不需要额外处理
                  }*/else -> {  /*可以什么都不处理 */
            }
            }
        }
    }

    /**
     * 内部方法：加载文章列表数据
     */
    private suspend fun loadArticlesInternal(
        page: Int = 0,
        strategy: CacheStrategy = CacheStrategy.CACHE_FIRST
    ) {
        // 设置文章加载状态
        updateUiState { it.copy(articleState = DataState.Loading) }

        repository.getArticles(page, strategy).collect { result ->
            when (result) {
                is CacheResult.FromCache -> {
                    val articles = result.data.datas
                    updateUiState {
                        it.copy(
                            articles = articles,
                            articleState = DataState.Success(if (result.isExpired) "过期缓存" else "缓存"),
                            lastApiCall = "文章列表 页码:$page (来源: ${if (result.isExpired) "过期缓存" else "缓存"})"
                        )
                    }
                }

                is CacheResult.FromNetwork -> {
                    val articles = result.data.datas
                    updateUiState {
                        it.copy(
                            articles = articles,
                            articleState = DataState.Success("网络"),
                            lastApiCall = "文章列表 页码:$page (来源: 网络)"
                        )
                    }
                }

                is CacheResult.Failed -> {
                    updateUiState {
                        it.copy(
                            articleState = DataState.Error("文章列表加载失败"),
                            errorMessage = "文章列表加载失败"
                        )
                    }
                }

                is CacheResult.Loading -> {
                    // 接口级Loading状态已在上面设置
                }
            }
        }
    }

    /**
     * 内部方法：加载置顶文章数据
     */
    private suspend fun loadTopArticlesInternal(strategy: CacheStrategy = CacheStrategy.CACHE_FIRST) {
        // 设置置顶文章加载状态
        updateUiState { it.copy(topArticleState = DataState.Loading) }

        repository.getTopArticles(strategy).collect { result ->
            when (result) {
                is CacheResult.FromCache -> {
                    val topArticles = result.data
                    updateUiState {
                        it.copy(
                            topArticles = topArticles,
                            topArticleState = DataState.Success(if (result.isExpired) "过期缓存" else "缓存"),
                            lastApiCall = "置顶文章 (来源: ${if (result.isExpired) "过期缓存" else "缓存"})"
                        )
                    }
                }

                is CacheResult.FromNetwork -> {
                    val topArticles = result.data
                    updateUiState {
                        it.copy(
                            topArticles = topArticles,
                            topArticleState = DataState.Success("网络"),
                            lastApiCall = "置顶文章 (来源: 网络)"
                        )
                    }
                }

                is CacheResult.Failed -> {
                    updateUiState {
                        it.copy(
                            topArticleState = DataState.Error("置顶文章加载失败"),
                            errorMessage = "置顶文章加载失败"
                        )
                    }
                }

                is CacheResult.Loading -> {
                    // 接口级Loading状态已在上面设置
                }
            }
        }
    }

    // ============ 公开方法 - 用于 UI 交互 ============

    /**
     * 刷新所有数据
     */
    fun refreshAllData() {
        loadPageData()
    }

    /**
     * 加载 Banner 数据（单独调用）
     */
    fun loadBanners(strategy: CacheStrategy = CacheStrategy.CACHE_FIRST) {
        viewModelScope.launch {
            loadBannersInternal(strategy)
        }
    }

    /**
     * 加载文章列表（单独调用）
     */
    fun loadArticles(page: Int = 0, strategy: CacheStrategy = CacheStrategy.CACHE_FIRST) {
        viewModelScope.launch {
            loadArticlesInternal(page, strategy)
        }
    }

    /**
     * 加载置顶文章（单独调用）
     */
    fun loadTopArticles(strategy: CacheStrategy = CacheStrategy.CACHE_FIRST) {
        viewModelScope.launch {
            loadTopArticlesInternal(strategy)
        }
    }

    /**
     * 仅从缓存加载数据
     */
    fun loadFromCacheOnly() {
        viewModelScope.launch {
            updateUiState { it.copy(isPageLoading = true) }

            val bannerDeferred = async { loadBannersInternal(CacheStrategy.CACHE_ONLY) }
            val articlesDeferred = async { loadArticlesInternal(0, CacheStrategy.CACHE_ONLY) }
            val topArticlesDeferred = async { loadTopArticlesInternal(CacheStrategy.CACHE_ONLY) }

            bannerDeferred.await()
            articlesDeferred.await()
            topArticlesDeferred.await()

            updateUiState { it.copy(isPageLoading = false) }
        }
    }

    /**
     * 仅从网络加载数据
     */
    fun loadFromNetworkOnly() {
        viewModelScope.launch {
            updateUiState { it.copy(isPageLoading = true) }

            val bannerDeferred = async { loadBannersInternal(CacheStrategy.NETWORK_ONLY) }
            val articlesDeferred = async { loadArticlesInternal(0, CacheStrategy.NETWORK_ONLY) }
            val topArticlesDeferred = async { loadTopArticlesInternal(CacheStrategy.NETWORK_ONLY) }

            bannerDeferred.await()
            articlesDeferred.await()
            topArticlesDeferred.await()

            updateUiState { it.copy(isPageLoading = false) }
        }
    }

    /**
     * 先显示缓存，同时更新网络数据
     */
    fun loadWithCacheAndNetwork() {
        viewModelScope.launch {
            val bannerDeferred = async { loadBannersInternal(CacheStrategy.CACHE_AND_NETWORK) }
            val articlesDeferred =
                async { loadArticlesInternal(0, CacheStrategy.CACHE_AND_NETWORK) }
            val topArticlesDeferred =
                async { loadTopArticlesInternal(CacheStrategy.CACHE_AND_NETWORK) }

            bannerDeferred.await()
            articlesDeferred.await()
            topArticlesDeferred.await()
        }
    }

    /**
     * 清除所有缓存
     */
    fun clearAllCache() {
        viewModelScope.launch {
            repository.clearAllCache()
            updateUiState { it.copy(lastApiCall = "清除所有缓存") }
        }
    }

    /**
     * 清除错误消息
     */
    fun clearErrorMessage() {
        updateUiState { it.copy(errorMessage = null) }
    }

    /**
     * 更新 UI 状态
     */
    private fun updateUiState(update: (RealApiUiState) -> RealApiUiState) {
        _uiState.value = update(_uiState.value)
    }

    /**
     * 获取统计信息
     */
    fun getStatsInfo(): String {
        val state = _uiState.value
        return "Banner: ${state.banners.size} | 文章: ${state.articles.size} | 置顶: ${state.topArticles.size}"
    }
}