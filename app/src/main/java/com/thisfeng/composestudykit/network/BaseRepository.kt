package com.thisfeng.composestudykit.network

import com.squareup.moshi.JsonAdapter
import com.thisfeng.composestudykit.cache.CacheConfig
import com.thisfeng.composestudykit.cache.CacheResult
import com.thisfeng.composestudykit.cache.CacheStrategy
import com.thisfeng.composestudykit.cache.DataStoreCacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * 基础 Repository 类 - 基于 DataStore
 * 提供通用的网络请求处理方法和 DataStore 缓存策略支持
 */
abstract class BaseRepository {
    
    // 子类需要提供 DataStore 缓存管理器
    protected abstract val dataStoreCacheManager: DataStoreCacheManager?
    
    /**
     * 带 DataStore 缓存策略的安全API调用
     * 
     * @param cacheKey 缓存键
     * @param cacheConfig 缓存配置
     * @param jsonAdapter JSON适配器，用于类型安全的序列化/反序列化
     * @param apiCall 网络请求的挂起函数
     * @return Flow<CacheResult<T>> 包装的结果流
     */
    protected fun <T> safeApiCallWithCache(
        cacheKey: String,
        cacheConfig: CacheConfig = CacheConfig(),
        jsonAdapter: JsonAdapter<T>,
        apiCall: suspend () -> ApiResponse<T>
    ): Flow<CacheResult<T>> = flow {
        val cache = dataStoreCacheManager
        
        when (cacheConfig.strategy) {
            CacheStrategy.NETWORK_ONLY -> {
                emit(CacheResult.Loading)
                val networkResult = performNetworkCall(apiCall)
                if (networkResult.isSuccess && cache != null) {
                    // 缓存成功的网络结果
                    val jsonData = jsonAdapter.toJson(networkResult.getOrNull())
                    cache.cacheData(cacheKey, jsonData)
                }
                emit(networkResult.toCacheResult(false))
            }
            
            CacheStrategy.CACHE_ONLY -> {
                val cachedJson = cache?.getCachedData(cacheKey)
                if (cachedJson != null) {
                    try {
                        val cachedData = jsonAdapter.fromJson(cachedJson)
                        if (cachedData != null) {
                            emit(CacheResult.FromCache(cachedData))
                        } else {
                            emit(CacheResult.Failed)
                        }
                    } catch (e: Exception) {
                        emit(CacheResult.Failed)
                    }
                } else {
                    emit(CacheResult.Failed)
                }
            }
            
            CacheStrategy.CACHE_FIRST -> {
                if (!cacheConfig.forceRefresh) {
                    val cachedJson = cache?.getCachedData(cacheKey)
                    if (cachedJson != null) {
                        try {
                            val cachedData = jsonAdapter.fromJson(cachedJson)
                            if (cachedData != null) {
                                emit(CacheResult.FromCache(cachedData))
                                return@flow
                            }
                        } catch (e: Exception) {
                            // 缓存解析失败，继续网络请求
                        }
                    }
                }
                
                emit(CacheResult.Loading)
                val networkResult = performNetworkCall(apiCall)
                if (networkResult.isSuccess && cache != null) {
                    val jsonData = jsonAdapter.toJson(networkResult.getOrNull())
                    cache.cacheData(cacheKey, jsonData)
                }
                emit(networkResult.toCacheResult(false))
            }
            
            CacheStrategy.NETWORK_FIRST -> {
                emit(CacheResult.Loading)
                val networkResult = performNetworkCall(apiCall)
                
                if (networkResult.isSuccess) {
                    if (cache != null) {
                        val jsonData = jsonAdapter.toJson(networkResult.getOrNull())
                        cache.cacheData(cacheKey, jsonData)
                    }
                    emit(networkResult.toCacheResult(false))
                } else {
                    // 网络失败，尝试使用缓存
                    val cachedJson = cache?.getCachedData(cacheKey)
                    if (cachedJson != null) {
                        try {
                            val cachedData = jsonAdapter.fromJson(cachedJson)
                            if (cachedData != null) {
                                emit(CacheResult.FromCache(cachedData, isExpired = true))
                            } else {
                                emit(CacheResult.Failed)
                            }
                        } catch (e: Exception) {
                            emit(CacheResult.Failed)
                        }
                    } else {
                        emit(CacheResult.Failed)
                    }
                }
            }
            
            CacheStrategy.CACHE_AND_NETWORK -> {
                // 先发送缓存数据
                val cachedJson = cache?.getCachedData(cacheKey)
                if (cachedJson != null) {
                    try {
                        val cachedData = jsonAdapter.fromJson(cachedJson)
                        if (cachedData != null) {
                            emit(CacheResult.FromCache(cachedData))
                        }
                    } catch (e: Exception) {
                        // 忽略缓存解析错误
                    }
                }
                
                // 然后请求网络
                emit(CacheResult.Loading)
                val networkResult = performNetworkCall(apiCall)
                if (networkResult.isSuccess && cache != null) {
                    val jsonData = jsonAdapter.toJson(networkResult.getOrNull())
                    cache.cacheData(cacheKey, jsonData)
                    emit(networkResult.toCacheResult(false))
                }
            }
        }
    }.flowOn(Dispatchers.IO)
    

    
    /**
     * 执行网络请求的私有方法
     */
    private suspend fun <T> performNetworkCall(
        apiCall: suspend () -> ApiResponse<T>
    ): ApiResult<T> {
        return try {
            val response = apiCall()
            response.toApiResult()
        } catch (e: Exception) {
            handleNetworkException(e)
        }
    }
    
    /**
     * 将 ApiResult 转换为 CacheResult
     */
    private fun <T> ApiResult<T>.toCacheResult(isFromCache: Boolean): CacheResult<T> {
        return when (this) {
            is ApiResult.Success -> {
                if (isFromCache) {
                    CacheResult.FromCache(data)
                } else {
                    CacheResult.FromNetwork(data)
                }
            }
            is ApiResult.Error, is ApiResult.Exception -> CacheResult.Failed
            ApiResult.Loading -> CacheResult.Loading
        }
    }
    
    /**
     * 安全地执行网络请求 - 适用于标准格式的 API
     * 
     * @param apiCall 网络请求的挂起函数
     * @return ApiResult 包装的结果
     */
    protected suspend fun <T> safeApiCall(
        apiCall: suspend () -> ApiResponse<T>
    ): ApiResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiCall()
                response.toApiResult()
            } catch (e: Exception) {
                handleNetworkException(e)
            }
        }
    }
    
    /**
     * 安全地执行网络请求 - 适用于第三方 API（原始数据）
     * 
     * @param apiCall 网络请求的挂起函数
     * @return ApiResult 包装的结果
     */
    protected suspend fun <T> safeRawApiCall(
        apiCall: suspend () -> T
    ): ApiResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiCall()
                response.toRawApiResult()
            } catch (e: Exception) {
                handleNetworkException(e)
            }
        }
    }

    /**
     * 通用异常处理方法
     * 统一处理所有网络请求的异常情况
     */
    private inline fun <T> handleNetworkException(exception: Throwable): ApiResult<T> {
        return when (exception) {
            is java.net.UnknownHostException -> {
                // DNS解析失败或无网络连接
                ApiResult.Exception(exception, "无法连接到服务器，请检查网络连接")
            }
            is java.net.SocketTimeoutException -> {
                // 连接超时
                ApiResult.Exception(exception, "网络连接超时，请稍后重试")
            }
            is java.net.ConnectException -> {
                // 连接被拒绝
                ApiResult.Exception(exception, "无法连接到服务器，请检查网络连接")
            }
            is java.io.IOException -> {
                // IO异常，通常是网络问题
                ApiResult.Exception(exception, "网络连接出现问题，请检查网络设置")
            }
            else -> {
                // 其他异常
                ApiResult.Exception(exception, "请求处理失败：${exception.message ?: "未知错误"}")
            }
        }
    }
}