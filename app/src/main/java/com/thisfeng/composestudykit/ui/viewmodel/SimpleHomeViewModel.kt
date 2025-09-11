package com.thisfeng.composestudykit.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thisfeng.composestudykit.network.ApiResult
import com.thisfeng.composestudykit.network.RetrofitClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 简化版 ViewModel（不使用 Hilt）
 * 演示网络请求框架的基本使用
 */
class SimpleHomeViewModel : ViewModel() {
    
    private val _bannerState = MutableStateFlow<ApiResult<String>>(ApiResult.Loading)
    val bannerState: StateFlow<ApiResult<String>> = _bannerState.asStateFlow()
    
    private val _articleState = MutableStateFlow<ApiResult<String>>(ApiResult.Loading)
    val articleState: StateFlow<ApiResult<String>> = _articleState.asStateFlow()
    
    init {
        // 初始状态显示说明
        _bannerState.value = ApiResult.Success("点击上方按钮开始测试", "")
        _articleState.value = ApiResult.Success("点击上方按钮开始测试", "")
    }
    
    /**
     * 模拟加载 Banner 数据
     */
    fun loadBanners() {
        viewModelScope.launch {
            _bannerState.value = ApiResult.Loading
            
            try {
                // 模拟网络请求延迟
                delay(1500)
                
                // 模拟随机成功/失败
                when ((0..10).random()) {
                    in 0..7 -> {
                        // 80% 成功率
                        _bannerState.value = ApiResult.Success(
                            data = "成功获取到 5 个 Banner 数据",
                            msg = "数据加载完成"
                        )
                    }
                    in 8..9 -> {
                        // 业务错误
                        _bannerState.value = ApiResult.Error(
                            code = 1001,
                            message = "Banner 数据暂时不可用"
                        )
                    }
                    else -> {
                        // 网络异常
                        _bannerState.value = ApiResult.Exception(
                            exception = RuntimeException("网络连接超时")
                        )
                    }
                }
            } catch (e: Exception) {
                _bannerState.value = ApiResult.Exception(e)
            }
        }
    }
    
    /**
     * 模拟加载文章数据
     */
    fun loadArticles() {
        viewModelScope.launch {
            _articleState.value = ApiResult.Loading
            
            try {
                // 模拟网络请求延迟
                delay(2000)
                
                // 模拟随机成功/失败
                when ((0..10).random()) {
                    in 0..6 -> {
                        // 70% 成功率
                        _articleState.value = ApiResult.Success(
                            data = "成功获取到 20 篇文章",
                            msg = "包含最新的技术文章"
                        )
                    }
                    in 7..8 -> {
                        // 业务错误
                        _articleState.value = ApiResult.Error(
                            code = 1002,
                            message = "文章列表加载失败，请重试"
                        )
                    }
                    else -> {
                        // 网络异常
                        _articleState.value = ApiResult.Exception(
                            exception = RuntimeException("服务器响应异常")
                        )
                    }
                }
            } catch (e: Exception) {
                _articleState.value = ApiResult.Exception(e)
            }
        }
    }
    
    /**
     * 演示真实的 WanAndroid API 调用（需要网络）
     */
    fun loadRealData() {
        viewModelScope.launch {
            _bannerState.value = ApiResult.Loading
            
            try {
                // 创建 Moshi 实例
                val moshi = Moshi.Builder()
                    .add(KotlinJsonAdapterFactory())
                    .build()
                
                // 创建 Retrofit 实例
                val retrofit = RetrofitClient.createCustomRetrofit(
                    baseUrl = "https://www.wanandroid.com/",
                    moshi = moshi
                )
                
                // 这里可以创建 API 服务并调用真实接口
                // 由于简化演示，我们就不实际调用了
                
                _bannerState.value = ApiResult.Success(
                    data = "真实 API 调用示例（需要完整配置）",
                    msg = "请查看代码了解如何集成真实 API"
                )
            } catch (e: Exception) {
                _bannerState.value = ApiResult.Exception(e)
            }
        }
    }
}