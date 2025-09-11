package com.thisfeng.composestudykit.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * 网络错误拦截器
 * 用于统一处理通用的网络错误和业务错误代码映射
 */
class NetworkErrorInterceptor : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        try {
            val response = chain.proceed(request)
            
            // 检查 HTTP 状态码
            when (response.code) {
                in 200..299 -> {
                    // 成功响应，直接返回
                    return response
                }
                401 -> {
                    // 未授权 - 可以在这里处理 token 刷新逻辑
                    throw NetworkException.UnauthorizedException("用户未授权，请重新登录")
                }
                403 -> {
                    // 禁止访问
                    throw NetworkException.ForbiddenException("访问被拒绝")
                }
                404 -> {
                    // 资源不存在
                    throw NetworkException.NotFoundException("请求的资源不存在")
                }
                in 500..599 -> {
                    // 服务器错误
                    throw NetworkException.ServerException("服务器错误，请稍后重试")
                }
                else -> {
                    // 其他 HTTP 错误
                    throw NetworkException.HttpException(response.code, "HTTP错误: ${response.code}")
                }
            }
        } catch (e: IOException) {
            // 网络连接异常
            throw NetworkException.NetworkConnectionException("网络连接失败，请检查网络设置")
        } catch (e: NetworkException) {
            // 重新抛出我们自定义的异常
            throw e
        } catch (e: Exception) {
            // 其他未知异常
            throw NetworkException.UnknownException("未知错误: ${e.message}")
        }
    }
}

/**
 * 自定义网络异常类
 */
sealed class NetworkException(message: String) : Exception(message) {
    class NetworkConnectionException(message: String) : NetworkException(message)
    class UnauthorizedException(message: String) : NetworkException(message)
    class ForbiddenException(message: String) : NetworkException(message)
    class NotFoundException(message: String) : NetworkException(message)
    class ServerException(message: String) : NetworkException(message)
    class HttpException(val code: Int, message: String) : NetworkException(message)
    class UnknownException(message: String) : NetworkException(message)
}