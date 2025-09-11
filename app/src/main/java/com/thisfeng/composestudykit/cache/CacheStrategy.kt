package com.thisfeng.composestudykit.cache

/**
 * 缓存策略枚举
 */
enum class CacheStrategy {
    /**
     * 仅从网络获取，不使用缓存
     */
    NETWORK_ONLY,
    
    /**
     * 仅从缓存获取，不请求网络
     */
    CACHE_ONLY,
    
    /**
     * 先从缓存获取，如果缓存不存在或过期则请求网络
     */
    CACHE_FIRST,
    
    /**
     * 先请求网络，如果网络失败则使用缓存
     */
    NETWORK_FIRST,
    
    /**
     * 先返回缓存数据（如果有），同时请求网络更新缓存
     */
    CACHE_AND_NETWORK
}

/**
 * 缓存策略配置
 */
data class CacheConfig(
    val strategy: CacheStrategy = CacheStrategy.CACHE_FIRST,
    val expireTime: Long = DataStoreCacheManager.DEFAULT_EXPIRE_TIME,
    val forceRefresh: Boolean = false
)

/**
 * 缓存结果包装类
 */
sealed class CacheResult<out T> {
    /**
     * 从缓存获取的数据
     */
    data class FromCache<out T>(val data: T, val isExpired: Boolean = false) : CacheResult<T>()
    
    /**
     * 从网络获取的数据
     */
    data class FromNetwork<out T>(val data: T) : CacheResult<T>()
    
    /**
     * 缓存和网络都失败
     */
    data object Failed : CacheResult<Nothing>()
    
    /**
     * 正在加载中
     */
    data object Loading : CacheResult<Nothing>()
}

/**
 * 获取数据，无论来源
 */
fun <T> CacheResult<T>.getData(): T? = when (this) {
    is CacheResult.FromCache -> data
    is CacheResult.FromNetwork -> data
    else -> null
}

/**
 * 检查是否来自缓存
 */
fun <T> CacheResult<T>.isFromCache(): Boolean = this is CacheResult.FromCache

/**
 * 检查是否来自网络
 */
fun <T> CacheResult<T>.isFromNetwork(): Boolean = this is CacheResult.FromNetwork