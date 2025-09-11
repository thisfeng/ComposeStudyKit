package com.thisfeng.composestudykit.network

import com.thisfeng.composestudykit.utils.AppGlobals
import com.thisfeng.composestudykit.utils.ToastUtils
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import java.io.IOException

/**
 * 网络错误拦截器 - 用户友好版
 * 主要用于业务 HTTP 状态码的拦截，直接告知用户而不抛出异常
 */
class NetworkErrorInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        try {
            val response = chain.proceed(request)

            // 业务 HTTP 状态码拦截 - 直接告知用户
            when (response.code) {
                in 200..299 -> {
                    // 成功响应，直接返回
                    return response
                }
                401 -> {
                    // 未授权 - 直接提示用户
                    ToastUtils.showLong(AppGlobals.getApplication(), "登录已过期，请重新登录")
                    return response // 让业务层处理
                }
                403 -> {
                    // 禁止访问 - 直接提示用户
                    ToastUtils.showLong(AppGlobals.getApplication(), "访问权限不足，请联系管理员")
                    return response
                }
                404 -> {
                    // 资源不存在 - 直接提示用户
                    ToastUtils.showLong(AppGlobals.getApplication(), "请求的资源不存在")
                    return response
                }
                in 500..599 -> {
                    // 服务器错误 - 直接提示用户
                    ToastUtils.showLong(AppGlobals.getApplication(), "服务器暂时无法响应，请稍后重试")
                    return response
                }
                else -> {
                    // 其他 HTTP 错误 - 直接提示用户
                    ToastUtils.showLong(AppGlobals.getApplication(), "网络请求失败，错误码: ${response.code}")
                    return response
                }
            }
        } catch (e: IOException) {
            // 网络连接异常 - 直接提示用户
            ToastUtils.showLong(AppGlobals.getApplication(), "网络连接失败，请检查网络设置")
            // 返回一个失败的响应而不是抛异常
            return createErrorResponse(request, "网络连接失败")
        } catch (e: Exception) {
            // 其他未知异常 - 直接提示用户
            ToastUtils.showLong(AppGlobals.getApplication(), "网络请求出现未知错误")
            return createErrorResponse(request, "未知网络错误")
        }
    }

    /**
     * 创建错误响应，避免抛出异常
     */
    private fun createErrorResponse(request: Request, message: String): Response {
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(600) // 自定义错误码
            .message(message)
            .body(ResponseBody.create(null, ""))
            .build()
    }
}