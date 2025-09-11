package com.thisfeng.composestudykit.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.thisfeng.composestudykit.utils.AppGlobals
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * 全局 DataStore 工具类
 * 
 * 🎯 特性：
 * - 简便易懂的 API 设计
 * - 支持常用数据类型：String、Int、Boolean、Float、Long
 * - 自动类型安全处理
 * - 协程支持，响应式数据流
 * - 全局单例，无需手动传入 Context
 * 
 * 🔧 使用方式：
 * ```kotlin
 * // 保存数据
 * GlobalDataStore.putString("user_name", "张三")
 * GlobalDataStore.putBoolean("is_login", true)
 * 
 * // 读取数据
 * val userName = GlobalDataStore.getString("user_name", "默认用户")
 * val isLogin = GlobalDataStore.getBoolean("is_login", false)
 * 
 * // 响应式监听
 * GlobalDataStore.getStringFlow("user_name", "默认用户").collect { name ->
 *     // 数据变化时会自动回调
 * }
 * ```
 */
object GlobalDataStore {
    
    // DataStore 实例扩展属性
    private val Context.globalDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "global_settings"
    )
    
    // 获取 DataStore 实例
    private val dataStore: DataStore<Preferences>
        get() = AppGlobals.getApplication().globalDataStore
    
    // ============ String 类型操作 ============
    
    /**
     * 保存 String 值
     * 
     * @param key 键名
     * @param value 要保存的值
     */
    suspend fun putString(key: String, value: String) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(key)] = value
        }
    }
    
    /**
     * 获取 String 值（一次性）
     * 
     * @param key 键名
     * @param defaultValue 默认值
     * @return 保存的值或默认值
     */
    suspend fun getString(key: String, defaultValue: String = ""): String {
        return dataStore.data.first()[stringPreferencesKey(key)] ?: defaultValue
    }
    
    /**
     * 获取 String 值的 Flow（响应式）
     * 
     * @param key 键名
     * @param defaultValue 默认值
     * @return Flow<String> 响应式数据流
     */
    fun getStringFlow(key: String, defaultValue: String = ""): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(key)] ?: defaultValue
        }
    }
    
    // ============ Boolean 类型操作 ============
    
    /**
     * 保存 Boolean 值
     */
    suspend fun putBoolean(key: String, value: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(key)] = value
        }
    }
    
    /**
     * 获取 Boolean 值（一次性）
     */
    suspend fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return dataStore.data.first()[booleanPreferencesKey(key)] ?: defaultValue
    }
    
    /**
     * 获取 Boolean 值的 Flow（响应式）
     */
    fun getBooleanFlow(key: String, defaultValue: Boolean = false): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey(key)] ?: defaultValue
        }
    }
    
    // ============ Int 类型操作 ============
    
    /**
     * 保存 Int 值
     */
    suspend fun putInt(key: String, value: Int) {
        dataStore.edit { preferences ->
            preferences[intPreferencesKey(key)] = value
        }
    }
    
    /**
     * 获取 Int 值（一次性）
     */
    suspend fun getInt(key: String, defaultValue: Int = 0): Int {
        return dataStore.data.first()[intPreferencesKey(key)] ?: defaultValue
    }
    
    /**
     * 获取 Int 值的 Flow（响应式）
     */
    fun getIntFlow(key: String, defaultValue: Int = 0): Flow<Int> {
        return dataStore.data.map { preferences ->
            preferences[intPreferencesKey(key)] ?: defaultValue
        }
    }
    
    // ============ Float 类型操作 ============
    
    /**
     * 保存 Float 值
     */
    suspend fun putFloat(key: String, value: Float) {
        dataStore.edit { preferences ->
            preferences[floatPreferencesKey(key)] = value
        }
    }
    
    /**
     * 获取 Float 值（一次性）
     */
    suspend fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return dataStore.data.first()[floatPreferencesKey(key)] ?: defaultValue
    }
    
    /**
     * 获取 Float 值的 Flow（响应式）
     */
    fun getFloatFlow(key: String, defaultValue: Float = 0f): Flow<Float> {
        return dataStore.data.map { preferences ->
            preferences[floatPreferencesKey(key)] ?: defaultValue
        }
    }
    
    // ============ Long 类型操作 ============
    
    /**
     * 保存 Long 值
     */
    suspend fun putLong(key: String, value: Long) {
        dataStore.edit { preferences ->
            preferences[longPreferencesKey(key)] = value
        }
    }
    
    /**
     * 获取 Long 值（一次性）
     */
    suspend fun getLong(key: String, defaultValue: Long = 0L): Long {
        return dataStore.data.first()[longPreferencesKey(key)] ?: defaultValue
    }
    
    /**
     * 获取 Long 值的 Flow（响应式）
     */
    fun getLongFlow(key: String, defaultValue: Long = 0L): Flow<Long> {
        return dataStore.data.map { preferences ->
            preferences[longPreferencesKey(key)] ?: defaultValue
        }
    }
    
    // ============ 通用操作 ============
    
    /**
     * 删除指定键的值
     * 
     * @param key 要删除的键名
     */
    suspend fun remove(key: String) {
        dataStore.edit { preferences ->
            // 尝试删除所有可能的类型
            preferences.remove(stringPreferencesKey(key))
            preferences.remove(booleanPreferencesKey(key))
            preferences.remove(intPreferencesKey(key))
            preferences.remove(floatPreferencesKey(key))
            preferences.remove(longPreferencesKey(key))
        }
    }
    
    /**
     * 检查指定键是否存在
     * 
     * @param key 要检查的键名
     * @return true 如果键存在，false 否则
     */
    suspend fun contains(key: String): Boolean {
        val preferences = dataStore.data.first()
        return preferences.contains(stringPreferencesKey(key)) ||
                preferences.contains(booleanPreferencesKey(key)) ||
                preferences.contains(intPreferencesKey(key)) ||
                preferences.contains(floatPreferencesKey(key)) ||
                preferences.contains(longPreferencesKey(key))
    }
    
    /**
     * 清空所有数据
     */
    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * 获取所有键名
     * 
     * @return Set<String> 所有键名的集合
     */
    suspend fun getAllKeys(): Set<String> {
        return dataStore.data.first().asMap().keys.map { it.name }.toSet()
    }
    
    /**
     * 获取存储的数据总数
     * 
     * @return Int 数据总数
     */
    suspend fun getSize(): Int {
        return dataStore.data.first().asMap().size
    }
}

/**
 * 常用配置键名定义
 * 建议在这里定义常用的配置键，避免硬编码和拼写错误
 */
object ConfigKeys {
    // 用户相关
    const val USER_NAME = "user_name"
    const val USER_ID = "user_id"
    const val IS_LOGIN = "is_login"
    const val LOGIN_TIME = "login_time"
    
    // 应用设置
    const val IS_FIRST_LAUNCH = "is_first_launch"
    const val APP_VERSION = "app_version"
    const val THEME_MODE = "theme_mode"  // "light", "dark", "auto"
    const val LANGUAGE = "language"      // "zh", "en"
    
    // 功能开关
    const val ENABLE_NOTIFICATIONS = "enable_notifications"
    const val ENABLE_AUTO_UPDATE = "enable_auto_update"
    const val ENABLE_CRASH_REPORT = "enable_crash_report"
    
    // 缓存设置
    const val CACHE_SIZE_LIMIT = "cache_size_limit"  // MB
    const val CACHE_EXPIRE_TIME = "cache_expire_time"  // 秒
    
    // 网络设置
    const val API_BASE_URL = "api_base_url"
    const val REQUEST_TIMEOUT = "request_timeout"  // 秒
    const val RETRY_COUNT = "retry_count"
}

/**
 * 快捷操作扩展函数
 * 提供更便捷的操作方式
 */
object DataStoreHelper {
    
    /**
     * 批量保存配置
     * 
     * @param configs 配置映射 Map<String, Any>
     */
    suspend fun saveConfigs(configs: Map<String, Any>) {
        configs.forEach { (key, value) ->
            when (value) {
                is String -> GlobalDataStore.putString(key, value)
                is Boolean -> GlobalDataStore.putBoolean(key, value)
                is Int -> GlobalDataStore.putInt(key, value)
                is Float -> GlobalDataStore.putFloat(key, value)
                is Long -> GlobalDataStore.putLong(key, value)
                else -> throw IllegalArgumentException("不支持的数据类型: ${value::class.java}")
            }
        }
    }
    
    /**
     * 初始化默认配置
     * 在 Application 启动时调用，设置默认值
     */
    suspend fun initDefaultConfigs() {
        // 只在第一次启动时设置默认值
        if (!GlobalDataStore.contains(ConfigKeys.IS_FIRST_LAUNCH)) {
            saveConfigs(mapOf(
                ConfigKeys.IS_FIRST_LAUNCH to false,
                ConfigKeys.THEME_MODE to "auto",
                ConfigKeys.LANGUAGE to "zh",
                ConfigKeys.ENABLE_NOTIFICATIONS to true,
                ConfigKeys.ENABLE_AUTO_UPDATE to true,
                ConfigKeys.ENABLE_CRASH_REPORT to true,
                ConfigKeys.CACHE_SIZE_LIMIT to 100, // 100MB
                ConfigKeys.CACHE_EXPIRE_TIME to 3600L, // 1小时
                ConfigKeys.REQUEST_TIMEOUT to 30, // 30秒
                ConfigKeys.RETRY_COUNT to 3
            ))
        }
    }
}