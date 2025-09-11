package com.thisfeng.composestudykit.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thisfeng.composestudykit.cache.CacheStats
import com.thisfeng.composestudykit.cache.DataStoreCacheManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * DataStore 对比演示 ViewModel
 * 展示 DataStore 与 SharedPreferences 的性能差异和使用体验
 */
class DataStoreComparisonViewModel(context: Context) : ViewModel() {

    // DataStore 仓库
    private val dataStoreManager = DataStoreCacheManager(context)

    // DataStore 统计信息
    private val _dataStoreStats = MutableStateFlow<CacheStats?>(null)
    val dataStoreStats: StateFlow<CacheStats?> = _dataStoreStats.asStateFlow()

    // 操作状态
    private val _operationStatus = MutableStateFlow("")
    val operationStatus: StateFlow<String> = _operationStatus.asStateFlow()

    // 测试数据
    private val _testData = MutableStateFlow<String?>(null)
    val testData: StateFlow<String?> = _testData.asStateFlow()

    init {
        refreshDataStoreStats()
    }

    /**
     * 测试 DataStore 写入数据
     */
    fun testDataStoreWrite() {
        viewModelScope.launch {
            _operationStatus.value = "正在测试 DataStore 写入..."

            val testJsonData = """{
                "message": "这是一个 DataStore 测试数据",
                "timestamp": ${System.currentTimeMillis()},
                "type": "test"
            }"""

            dataStoreManager.cacheData("test_key", testJsonData)
            refreshDataStoreStats()
            _operationStatus.value = "DataStore 写入成功！"
        }
    }

    /**
     * 测试 DataStore 读取数据
     */
    fun testDataStoreRead() {
        viewModelScope.launch {
            _operationStatus.value = "正在测试 DataStore 读取..."

            val data = dataStoreManager.getCachedData("test_key")
            _testData.value = data
            _operationStatus.value = if (data != null) {
                "DataStore 读取成功！"
            } else {
                "未找到测试数据"
            }
        }
    }

    /**
     * 清空所有缓存
     */
    fun clearAllCaches() {
        viewModelScope.launch {
            _operationStatus.value = "清空所有缓存..."
            dataStoreManager.clearAllCache()
            _testData.value = null
            refreshDataStoreStats()
            _operationStatus.value = "缓存已清空"
        }
    }

    /**
     * 测试批量写入
     */
    fun testBatchWrite() {
        viewModelScope.launch {
            _operationStatus.value = "正在测试批量写入..."

            repeat(10) { index ->
                val data = """{
                    "id": $index,
                    "title": "测试数据 $index",
                    "content": "这是第 $index 条测试数据",
                    "timestamp": ${System.currentTimeMillis()}
                }"""
                dataStoreManager.cacheData("batch_$index", data)
            }

            refreshDataStoreStats()
            _operationStatus.value = "批量写入完成！共写入 10 条数据"
        }
    }

    /**
     * 测试并发性能
     */
    fun testConcurrentPerformance() {
        viewModelScope.launch {
            _operationStatus.value = "测试并发性能..."

            val jobs = (1..120).map { index ->
                launch {
                    val data = "concurrent_data_$index"
                    dataStoreManager.cacheData("concurrent_$index", data)
                }
            }

            // 等待所有操作完成
            jobs.forEach { it.join() }

            refreshDataStoreStats()
            _operationStatus.value = "并发测试完成！共处理 120 个并发操作"
        }
    }

    /**
     * 更新 DataStore 统计信息
     */
    private fun refreshDataStoreStats() {
        viewModelScope.launch {
            _dataStoreStats.value = dataStoreManager.getCacheStats()
        }
    }
}