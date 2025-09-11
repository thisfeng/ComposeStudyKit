package com.thisfeng.composestudykit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thisfeng.composestudykit.cache.CacheResult
import com.thisfeng.composestudykit.cache.CacheStrategy
import com.thisfeng.composestudykit.data.model.Article
import com.thisfeng.composestudykit.data.model.Banner
import com.thisfeng.composestudykit.data.repository.WanAndroidRepository
import com.thisfeng.composestudykit.utils.AppGlobals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async

/**
 * 真实 API ViewModel - 优化版本
 * 
 * 🔧 架构优化：
 * 1. 不再依赖外部传入的 Context，使用全局 Application
 * 2. 更好的生命周期管理，避免内存泄漏
 * 3. 符合单一职责原则，ViewModel 专注于业务逻辑
 */
class RealApiViewModelV2 : ViewModel() {

    // 直接使用全局 Application，不依赖外部 Context 传递
    // 这样更符合 ViewModel 的设计原则，避免了 Context 传递链
    private val repository = WanAndroidRepository(AppGlobals.getApplication())

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

                is CacheResult.Loading -> {
                    // 接口级Loading状态已在上面设置，这里不需要额外处理
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
            val articlesDeferred = async { loadArticlesInternal(0, CacheStrategy.CACHE_AND_NETWORK) }
            val topArticlesDeferred = async { loadTopArticlesInternal(CacheStrategy.CACHE_AND_NETWORK) }

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