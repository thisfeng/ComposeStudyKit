package com.thisfeng.composestudykit.network

import com.thisfeng.composestudykit.cache.CacheConfig
import com.thisfeng.composestudykit.cache.CacheResult
import com.thisfeng.composestudykit.cache.CacheStrategy
import com.thisfeng.composestudykit.cache.DataStoreCacheManager
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
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
    
    private val moshi = Moshi.Builder().build()
    
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
        } catch (e: NetworkException) {
            when (e) {
                is NetworkException.UnauthorizedException -> ApiResult.Error(401, e.message ?: "未授权")
                is NetworkException.ForbiddenException -> ApiResult.Error(403, e.message ?: "访问被拒绝")
                is NetworkException.NotFoundException -> ApiResult.Error(404, e.message ?: "资源不存在")
                is NetworkException.ServerException -> ApiResult.Error(500, e.message ?: "服务器错误")
                is NetworkException.HttpException -> ApiResult.Error(e.code, e.message ?: "HTTP错误")
                is NetworkException.NetworkConnectionException -> ApiResult.Exception(e)
                else -> ApiResult.Exception(e)
            }
        } catch (e: Exception) {
            ApiResult.Exception(e)
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
            } catch (e: NetworkException) {
                // 网络相关的自定义异常
                when (e) {
                    is NetworkException.UnauthorizedException -> ApiResult.Error(401, e.message ?: "未授权")
                    is NetworkException.ForbiddenException -> ApiResult.Error(403, e.message ?: "访问被拒绝")
                    is NetworkException.NotFoundException -> ApiResult.Error(404, e.message ?: "资源不存在")
                    is NetworkException.ServerException -> ApiResult.Error(500, e.message ?: "服务器错误")
                    is NetworkException.HttpException -> ApiResult.Error(e.code, e.message ?: "HTTP错误")
                    is NetworkException.NetworkConnectionException -> ApiResult.Exception(e)
                    else -> ApiResult.Exception(e)
                }
            } catch (e: Exception) {
                // 其他异常
                ApiResult.Exception(e)
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
            } catch (e: NetworkException) {
                // 网络相关的自定义异常
                when (e) {
                    is NetworkException.UnauthorizedException -> ApiResult.Error(401, e.message ?: "未授权")
                    is NetworkException.ForbiddenException -> ApiResult.Error(403, e.message ?: "访问被拒绝")
                    is NetworkException.NotFoundException -> ApiResult.Error(404, e.message ?: "资源不存在")
                    is NetworkException.ServerException -> ApiResult.Error(500, e.message ?: "服务器错误")
                    is NetworkException.HttpException -> ApiResult.Error(e.code, e.message ?: "HTTP错误")
                    is NetworkException.NetworkConnectionException -> ApiResult.Exception(e)
                    else -> ApiResult.Exception(e)
                }
            } catch (e: Exception) {
                // 其他异常
                ApiResult.Exception(e)
            }
        }
    }
}