package com.thisfeng.composestudykit.update

import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Streaming
import retrofit2.http.Url

/**
 * 版本更新 API 接口定义
 */
interface UpdateApiService {
    
    /**
     * 检查版本更新 (GET请求)
     * @param url 完整的URL路径
     * @param channel 渠道类型
     * @param company 公司标识
     * @param serial 设备序列号
     * @param outlet 门店标识
     */
    @POST
    suspend fun checkVersion(
        @Url url: String,
        @Query("channel") channel: String,
        @Query("company") company: String,
        @Query("serial") serial: String,
        @Query("outlet") outlet: String
    ): ResponseBody
    
    /**
     * 下载APK文件
     * @param url 下载链接
     */
    @Streaming
    @POST
    suspend fun downloadApk(@Url url: String): ResponseBody
}

/**
 * 版本检查请求参数
 */
data class VersionCheckRequest(
    val channel: String,
    val company: String,
    val serial: String,
    val outlet: String
)

/**
 * 版本检查响应
 */
data class VersionCheckResponse(
    val msg: String,              // 消息
    val result: String,           // 结果描述
    val code: Int,               // 状态码 (1表示成功)
    val singleLogin: Boolean,    // 单点登录
    val apiVerify: String?,      // API验证
    val data: VersionInfo?,      // 版本信息
    val sign: String?            // 签名
)

/**
 * 版本信息
 */
data class VersionInfo(
    val versions: String,        // 版本号字符串，如 "3.6.80"
    val inner: Int,             // 内部版本号，如 3680
    val type: String,           // 平台类型，如 "ANDROID"
    val time: String,           // 发布时间，如 "2025-09-04"
    val explain: String,        // 更新说明，如 "3.6.80. 1、已知問題優化"
    val isMust: Boolean,        // 是否强制更新
    val downloadUrl: String,    // 下载链接
    val downloadCount: Int,     // 下载次数
    val serial: String          // 设备序列号
)