package com.thisfeng.composestudykit.network

import com.squareup.moshi.JsonClass

/**
 * 统一接口响应结构（我方固定格式）
 * 
 * @param errorCode 业务状态码（0表示成功，非0表示业务错误）
 * @param errorMsg 接口提示语
 * @param data 业务数据（可能为空）
 */
@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val errorCode: Int,
    val errorMsg: String,
    val data: T? = null
)

/**
 * 扩展函数：将 ApiResponse 转换为 ApiResult
 */
fun <T> ApiResponse<T>.toApiResult(): ApiResult<T> {
    return when (errorCode) {
        0 -> {
            // 成功状态
            val responseData = data
            if (responseData != null) {
                ApiResult.Success(responseData, errorMsg)
            } else {
                // 数据为空但操作成功的情况（如删除操作）
                @Suppress("UNCHECKED_CAST")
                ApiResult.Success(Unit as T, errorMsg)
            }
        }
        else -> {
            // 业务错误
            ApiResult.Error(errorCode, errorMsg)
        }
    }
}

/**
 * 第三方接口响应的原始数据包装
 * 用于那些不遵循我方标准格式的第三方接口
 */
@JsonClass(generateAdapter = true)
data class RawResponse<T>(
    val rawData: T
)

/**
 * 扩展函数：将原始响应转换为成功的 ApiResult
 */
fun <T> T.toRawApiResult(): ApiResult<T> {
    return ApiResult.Success(this, "")
}