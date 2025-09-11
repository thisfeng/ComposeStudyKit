package com.thisfeng.composestudykit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thisfeng.composestudykit.utils.ConfigKeys
import com.thisfeng.composestudykit.utils.DataStoreHelper
import com.thisfeng.composestudykit.utils.GlobalDataStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * 全局 DataStore 演示 ViewModel
 * 展示 GlobalDataStore 工具类的使用方式
 */
class GlobalDataStoreViewModel : ViewModel() {

    // UI 状态
    private val _uiState = MutableStateFlow(GlobalDataStoreUiState())
    val uiState: StateFlow<GlobalDataStoreUiState> = _uiState.asStateFlow()

    init {
        // 初始化时加载数据
        loadAllData()
        // 监听数据变化
        observeDataChanges()
    }

    /**
     * 加载所有数据
     */
    private fun loadAllData() {
        viewModelScope.launch {
            // 初始化默认配置
            DataStoreHelper.initDefaultConfigs()
            
            // 加载用户数据
            val userName = GlobalDataStore.getString(ConfigKeys.USER_NAME, "未设置")
            val userId = GlobalDataStore.getInt(ConfigKeys.USER_ID, 0)
            val isLogin = GlobalDataStore.getBoolean(ConfigKeys.IS_LOGIN, false)
            
            // 加载应用设置
            val themeMode = GlobalDataStore.getString(ConfigKeys.THEME_MODE, "auto")
            val language = GlobalDataStore.getString(ConfigKeys.LANGUAGE, "zh")
            val enableNotifications = GlobalDataStore.getBoolean(ConfigKeys.ENABLE_NOTIFICATIONS, true)
            
            // 加载统计信息
            refreshStatisticsInternal()
            
            _uiState.value = _uiState.value.copy(
                userName = userName,
                userId = userId,
                isLogin = isLogin,
                themeMode = themeMode,
                language = language,
                enableNotifications = enableNotifications
            )
        }
    }

    /**
     * 监听数据变化（响应式）
     */
    private fun observeDataChanges() {
        viewModelScope.launch {
            // 监听用户名变化
            GlobalDataStore.getStringFlow(ConfigKeys.USER_NAME, "未设置").collect { userName ->
                _uiState.value = _uiState.value.copy(userName = userName)
            }
        }
        
        viewModelScope.launch {
            // 监听登录状态变化
            GlobalDataStore.getBooleanFlow(ConfigKeys.IS_LOGIN, false).collect { isLogin ->
                _uiState.value = _uiState.value.copy(isLogin = isLogin)
            }
        }
        
        viewModelScope.launch {
            // 监听通知设置变化
            GlobalDataStore.getBooleanFlow(ConfigKeys.ENABLE_NOTIFICATIONS, true).collect { enableNotifications ->
                _uiState.value = _uiState.value.copy(enableNotifications = enableNotifications)
            }
        }
    }

    /**
     * 更新用户名
     */
    fun updateUserName(userName: String) {
        viewModelScope.launch {
            GlobalDataStore.putString(ConfigKeys.USER_NAME, userName)
            // 由于有响应式监听，这里不需要手动更新 UI 状态
            println("💾 保存用户名: $userName")
        }
    }

    /**
     * 更新用户ID
     */
    fun updateUserId(userId: Int) {
        viewModelScope.launch {
            GlobalDataStore.putInt(ConfigKeys.USER_ID, userId)
            _uiState.value = _uiState.value.copy(userId = userId)
            println("💾 保存用户ID: $userId")
        }
    }

    /**
     * 切换登录状态
     */
    fun toggleLogin() {
        viewModelScope.launch {
            val newLoginState = !_uiState.value.isLogin
            GlobalDataStore.putBoolean(ConfigKeys.IS_LOGIN, newLoginState)
            if (newLoginState) {
                // 登录时记录时间
                GlobalDataStore.putLong(ConfigKeys.LOGIN_TIME, System.currentTimeMillis())
            }
            // 响应式监听会自动更新 UI
            println("💾 切换登录状态: $newLoginState")
        }
    }

    /**
     * 更新主题模式
     */
    fun updateThemeMode(themeMode: String) {
        viewModelScope.launch {
            GlobalDataStore.putString(ConfigKeys.THEME_MODE, themeMode)
            _uiState.value = _uiState.value.copy(themeMode = themeMode)
            println("💾 保存主题模式: $themeMode")
        }
    }

    /**
     * 更新语言设置
     */
    fun updateLanguage(language: String) {
        viewModelScope.launch {
            GlobalDataStore.putString(ConfigKeys.LANGUAGE, language)
            _uiState.value = _uiState.value.copy(language = language)
            println("💾 保存语言设置: $language")
        }
    }

    /**
     * 切换通知开关
     */
    fun toggleNotifications() {
        viewModelScope.launch {
            val newNotificationState = !_uiState.value.enableNotifications
            GlobalDataStore.putBoolean(ConfigKeys.ENABLE_NOTIFICATIONS, newNotificationState)
            // 响应式监听会自动更新 UI
            println("💾 切换通知状态: $newNotificationState")
        }
    }

    /**
     * 刷新统计信息
     */
    fun refreshStatistics() {
        viewModelScope.launch {
            refreshStatisticsInternal()
        }
    }

    /**
     * 内部刷新统计信息
     */
    private suspend fun refreshStatisticsInternal() {
        val totalKeys = GlobalDataStore.getSize()
        val allKeys = GlobalDataStore.getAllKeys()
        
        // 模拟计算缓存大小（实际项目中可以更精确计算）
        val cacheSize = totalKeys * 10 // 简单估算，每个键约10KB
        
        _uiState.value = _uiState.value.copy(
            totalKeys = totalKeys,
            cacheSize = cacheSize
        )
        
        println("📊 统计信息 - 键数量: $totalKeys, 大小: ${cacheSize}KB")
        println("📋 所有键名: ${allKeys.joinToString(", ")}")
    }

    /**
     * 加载默认配置
     */
    fun loadDefaultConfigs() {
        viewModelScope.launch {
            DataStoreHelper.initDefaultConfigs()
            loadAllData() // 重新加载数据
            println("🔄 已加载默认配置")
        }
    }

    /**
     * 清空所有数据
     */
    fun clearAllData() {
        viewModelScope.launch {
            GlobalDataStore.clear()
            // 重置 UI 状态到默认值
            _uiState.value = GlobalDataStoreUiState()
            println("🗑️ 已清空所有数据")
        }
    }

    /**
     * 导出数据（演示用）
     */
    fun exportData() {
        viewModelScope.launch {
            val allKeys = GlobalDataStore.getAllKeys()
            val exportData = mutableMapOf<String, Any?>()
            
            // 遍历所有键，获取值（这里简化处理，实际项目中需要更完善的类型处理）
            allKeys.forEach { key ->
                // 尝试不同类型的读取
                try {
                    val stringValue = GlobalDataStore.getString(key, "")
                    if (stringValue.isNotEmpty()) {
                        exportData[key] = stringValue
                    } else {
                        val intValue = GlobalDataStore.getInt(key, Int.MIN_VALUE)
                        if (intValue != Int.MIN_VALUE) {
                            exportData[key] = intValue
                        } else {
                            val boolValue = GlobalDataStore.getBoolean(key, false)
                            exportData[key] = boolValue
                        }
                    }
                } catch (e: Exception) {
                    println("❌ 读取键 $key 时出错: ${e.message}")
                }
            }
            
            println("📤 导出数据:")
            exportData.forEach { (key, value) ->
                println("  $key = $value (${value?.javaClass?.simpleName})")
            }
        }
    }
}

/**
 * 全局 DataStore UI 状态
 */
data class GlobalDataStoreUiState(
    // 用户数据
    val userName: String = "未设置",
    val userId: Int = 0,
    val isLogin: Boolean = false,
    
    // 应用设置
    val themeMode: String = "auto",
    val language: String = "zh",
    val enableNotifications: Boolean = true,
    
    // 统计信息
    val totalKeys: Int = 0,
    val cacheSize: Int = 0 // KB
)