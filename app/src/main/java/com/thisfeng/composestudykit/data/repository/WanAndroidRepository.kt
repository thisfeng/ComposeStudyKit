package com.thisfeng.composestudykit.data.repository

import android.content.Context
import com.thisfeng.composestudykit.cache.CacheConfig
import com.thisfeng.composestudykit.cache.CacheResult
import com.thisfeng.composestudykit.cache.CacheStrategy
import com.thisfeng.composestudykit.cache.DataStoreCacheManager
import com.thisfeng.composestudykit.data.api.WanAndroidApiService
import com.thisfeng.composestudykit.data.model.Article
import com.thisfeng.composestudykit.data.model.ArticleList
import com.thisfeng.composestudykit.data.model.Banner
import com.thisfeng.composestudykit.network.ApiResult
import com.thisfeng.composestudykit.network.BaseRepository
import com.thisfeng.composestudykit.network.RetrofitClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.flow.Flow

/**
 * WanAndroid 数据仓库 - 基于 DataStore 缓存
 * 调用真实的 WanAndroid 开放接口，并提供 DataStore 缓存策略
 */
class WanAndroidRepository(context: Context) : BaseRepository() {

    // DataStore 缓存管理器
    override val dataStoreCacheManager: DataStoreCacheManager = DataStoreCacheManager(context)

    private val apiService: WanAndroidApiService by lazy {
        RetrofitClient.createStandardRetrofit()
            .create(WanAndroidApiService::class.java)
    }

    // Moshi 实例和适配器
    private val moshi = Moshi.Builder().build()
    private val bannerListAdapter = moshi.adapter<List<Banner>>(
        Types.newParameterizedType(
            List::class.java,
            Banner::class.java
        )
    )
    private val articleListAdapter = moshi.adapter(ArticleList::class.java)
    private val articleAdapter = moshi.adapter<List<Article>>(
        Types.newParameterizedType(
            List::class.java,
            Article::class.java
        )
    )

    /**
     * 获取首页 Banner（带 DataStore 缓存）
     */
    fun getBanners(
        cacheStrategy: CacheStrategy = CacheStrategy.CACHE_FIRST,
        forceRefresh: Boolean = false
    ): Flow<CacheResult<List<Banner>>> {
        val config = CacheConfig(
            strategy = cacheStrategy,
            expireTime = DataStoreCacheManager.CACHE_30_MINUTES,
            forceRefresh = forceRefresh
        )
        return safeApiCallWithCache(
            cacheKey = DataStoreCacheManager.CACHE_KEY_BANNERS,
            cacheConfig = config,
            jsonAdapter = bannerListAdapter
        ) { apiService.getBanners() }
    }

    /**
     * 获取文章列表（带 DataStore 缓存）
     *
     * @param page 页码，从0开始
     */
    fun getArticles(
        page: Int,
        cacheStrategy: CacheStrategy = CacheStrategy.CACHE_FIRST,
        forceRefresh: Boolean = false
    ): Flow<CacheResult<ArticleList>> {
        val config = CacheConfig(
            strategy = cacheStrategy,
            expireTime = DataStoreCacheManager.CACHE_5_MINUTES,
            forceRefresh = forceRefresh
        )
        return safeApiCallWithCache(
            cacheKey = "${DataStoreCacheManager.CACHE_KEY_ARTICLES}$page",
            cacheConfig = config,
            jsonAdapter = articleListAdapter
        ) { apiService.getArticles(page) }
    }

    /**
     * 获取置顶文章（带 DataStore 缓存）
     */
    fun getTopArticles(
        cacheStrategy: CacheStrategy = CacheStrategy.CACHE_FIRST,
        forceRefresh: Boolean = false
    ): Flow<CacheResult<List<Article>>> {
        val config = CacheConfig(
            strategy = cacheStrategy,
            expireTime = DataStoreCacheManager.CACHE_1_HOUR,
            forceRefresh = forceRefresh
        )
        return safeApiCallWithCache(
            cacheKey = DataStoreCacheManager.CACHE_KEY_TOP_ARTICLES,
            cacheConfig = config,
            jsonAdapter = articleAdapter
        ) { apiService.getTopArticles() }
    }

    /**
     * 获取最新项目（带 DataStore 缓存）
     *
     * @param page 页码，从1开始
     */
    fun getProjects(
        page: Int,
        cacheStrategy: CacheStrategy = CacheStrategy.CACHE_FIRST,
        forceRefresh: Boolean = false
    ): Flow<CacheResult<ArticleList>> {
        val config = CacheConfig(
            strategy = cacheStrategy,
            expireTime = DataStoreCacheManager.CACHE_30_MINUTES,
            forceRefresh = forceRefresh
        )
        return safeApiCallWithCache(
            cacheKey = "${DataStoreCacheManager.CACHE_KEY_PROJECTS}$page",
            cacheConfig = config,
            jsonAdapter = articleListAdapter
        ) { apiService.getProjects(page) }
    }

    // ============ DataStore 缓存管理方法 ============

    /**
     * 清空所有 DataStore 缓存
     */
    suspend fun clearAllCache() {
        dataStoreCacheManager.clearAllCache()
    }

    /**
     * 清空指定类型的 DataStore 缓存
     */
    suspend fun clearCacheByType(type: CacheType) {
        when (type) {
            CacheType.BANNERS -> dataStoreCacheManager.removeCache(DataStoreCacheManager.CACHE_KEY_BANNERS)
            CacheType.TOP_ARTICLES -> dataStoreCacheManager.removeCache(DataStoreCacheManager.CACHE_KEY_TOP_ARTICLES)
            CacheType.ARTICLES -> {
                // 清空所有文章页面缓存
                for (i in 0..10) {
                    dataStoreCacheManager.removeCache("${DataStoreCacheManager.CACHE_KEY_ARTICLES}$i")
                }
            }

            CacheType.PROJECTS -> {
                // 清空所有项目页面缓存
                for (i in 1..10) {
                    dataStoreCacheManager.removeCache("${DataStoreCacheManager.CACHE_KEY_PROJECTS}$i")
                }
            }
        }
    }

    /**
     * 检查 DataStore 缓存状态
     */
    suspend fun getCacheStatus(): CacheStatus {
        return CacheStatus(
            hasBanners = dataStoreCacheManager.isCacheExists(DataStoreCacheManager.CACHE_KEY_BANNERS),
            hasTopArticles = dataStoreCacheManager.isCacheExists(DataStoreCacheManager.CACHE_KEY_TOP_ARTICLES),
            hasArticles = dataStoreCacheManager.isCacheExists("${DataStoreCacheManager.CACHE_KEY_ARTICLES}0"),
            hasProjects = dataStoreCacheManager.isCacheExists("${DataStoreCacheManager.CACHE_KEY_PROJECTS}1")
        )
    }

    /**
     * 获取 DataStore 缓存统计信息
     */
    suspend fun getCacheStats() = dataStoreCacheManager.getCacheStats()

    // ============ 便捷方法 ============

    /**
     * 只从缓存获取Banner（用于离线模式）
     */
    fun getBannersFromCacheOnly(): Flow<CacheResult<List<Banner>>> {
        return getBanners(CacheStrategy.CACHE_ONLY)
    }

    /**
     * 先显示缓存，同时更新网络数据
     */
    fun getBannersWithCacheAndNetwork(): Flow<CacheResult<List<Banner>>> {
        return getBanners(CacheStrategy.CACHE_AND_NETWORK)
    }

    /**
     * 强制刷新Banner
     */
    fun refreshBanners(): Flow<CacheResult<List<Banner>>> {
        return getBanners(CacheStrategy.CACHE_FIRST, forceRefresh = true)
    }

    // ============ 不带缓存的直接网络请求方法 ============

    /**
     * 直接获取首页 Banner（不使用缓存）
     * 用于测试纯网络请求性能
     */
    suspend fun getBannersDirectly(): ApiResult<List<Banner>> {
        return safeApiCall { apiService.getBanners() }
    }

    /**
     * 直接获取文章列表（不使用缓存）
     * 用于测试纯网络请求性能
     */
    suspend fun getArticlesDirectly(page: Int): ApiResult<ArticleList> {
        return safeApiCall { apiService.getArticles(page) }
    }

    /**
     * 直接获取置顶文章（不使用缓存）
     * 用于测试纯网络请求性能
     */
    suspend fun getTopArticlesDirectly(): ApiResult<List<Article>> {
        return safeApiCall { apiService.getTopArticles() }
    }
}

/**
 * 缓存类型枚举
 */
enum class CacheType {
    BANNERS,
    ARTICLES,
    TOP_ARTICLES,
    PROJECTS
}

/**
 * 缓存状态数据类
 */
data class CacheStatus(
    val hasBanners: Boolean = false,
    val hasTopArticles: Boolean = false,
    val hasArticles: Boolean = false,
    val hasProjects: Boolean = false
) {
    val hasAnyCache: Boolean
        get() = hasBanners || hasTopArticles || hasArticles || hasProjects
}

/**
 * 首页数据模型
 */
data class HomeData(
    val banners: List<Banner>,
    val topArticles: List<Article>,
    val articles: List<Article>
)