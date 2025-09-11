package com.thisfeng.composestudykit.cache

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// DataStore 扩展属性
private val Context.apiCacheDataStore: DataStore<Preferences> by preferencesDataStore(name = "api_cache")

/**
 * 基于 DataStore 的缓存管理器
 * 相比 SharedPreferences，DataStore 具有以下优势：
 * 1. 异步操作，不会阻塞 UI 线程
 * 2. 类型安全
 * 3. 支持协程和 Flow
 * 4. 事务性操作，保证数据一致性
 * 5. 更好的错误处理
 */
class DataStoreCacheManager(private val context: Context) {
    
    private val dataStore = context.apiCacheDataStore
    private val moshi = Moshi.Builder().build()
    
    /**
     * 异步缓存数据
     */
    suspend fun cacheData(key: String, jsonData: String) {
        try {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey(key)] = jsonData
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 异步获取缓存数据
     */
    suspend fun getCachedData(key: String): String? {
        return try {
            dataStore.data.map { preferences ->
                preferences[stringPreferencesKey(key)]
            }.first()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * 获取缓存数据的 Flow
     */
    fun getCachedDataFlow(key: String): Flow<String?> {
        return dataStore.data.map { preferences ->
            try {
                preferences[stringPreferencesKey(key)]
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    
    /**
     * 删除指定缓存
     */
    suspend fun removeCache(key: String) {
        try {
            dataStore.edit { preferences ->
                preferences.remove(stringPreferencesKey(key))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 清空所有缓存
     */
    suspend fun clearAllCache() {
        try {
            dataStore.edit { preferences ->
                preferences.clear()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * 检查缓存是否存在
     */
    suspend fun isCacheExists(key: String): Boolean {
        return try {
            val data = dataStore.data.map { preferences ->
                preferences[stringPreferencesKey(key)]
            }.first()
            data != null
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    suspend fun getCacheStats(): CacheStats {
        return try {
            val allKeys = dataStore.data.map { preferences ->
                preferences.asMap().keys.map { it.name }
            }.first()
            
            var totalSize = 0
            for (key in allKeys) {
                val data = getCachedData(key)
                if (data != null) {
                    totalSize += data.length
                }
            }
            
            CacheStats(
                totalKeys = allKeys.size,
                validCount = allKeys.size,
                expiredCount = 0,
                totalSize = totalSize
            )
        } catch (e: Exception) {
            CacheStats(0, 0, 0, 0)
        }
    }
    
    companion object {
        // 默认缓存时间：5分钟
        const val DEFAULT_EXPIRE_TIME = 5 * 60 * 1000L
        
        // 缓存时间常量
        const val CACHE_5_MINUTES = 5 * 60 * 1000L
        const val CACHE_30_MINUTES = 30 * 60 * 1000L
        const val CACHE_1_HOUR = 60 * 60 * 1000L
        const val CACHE_1_DAY = 24 * 60 * 60 * 1000L
        
        // 缓存键常量
        const val CACHE_KEY_BANNERS = "cache_banners"
        const val CACHE_KEY_ARTICLES = "cache_articles_"
        const val CACHE_KEY_TOP_ARTICLES = "cache_top_articles"
        const val CACHE_KEY_PROJECTS = "cache_projects_"
    }
}

/**
 * 缓存统计信息
 */
data class CacheStats(
    val totalKeys: Int,
    val validCount: Int,
    val expiredCount: Int,
    val totalSize: Int // 字节大小
)