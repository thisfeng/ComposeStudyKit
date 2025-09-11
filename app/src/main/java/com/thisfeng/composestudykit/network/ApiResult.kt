package com.thisfeng.composestudykit.network

/**
 * 统一的 API 结果包装类
 * 用于封装网络请求的各种状态
 */
sealed class ApiResult<out T> {
    /**
     * 成功状态：携带业务数据和提示信息
     * @param data 业务数据（非空，成功时必传）
     * @param msg 提示信息（如"操作成功"，可选）
     */
    data class Success<out T>(val data: T, val msg: String = "") : ApiResult<T>()
    
    /**
     * 业务错误状态：后端返回的业务逻辑错误
     * @param code 错误码
     * @param message 错误信息
     */
    data class Error(val code: Int, val message: String) : ApiResult<Nothing>()
    
    /**
     * 网络异常状态：网络连接、超时等异常
     * @param exception 异常对象
     */
    data class Exception(val exception: Throwable) : ApiResult<Nothing>()
    
    /**
     * 加载中状态
     */
    data object Loading : ApiResult<Nothing>()
}

/**
 * 判断结果是否为成功状态
 */
inline val <T> ApiResult<T>.isSuccess: Boolean
    get() = this is ApiResult.Success

/**
 * 判断结果是否为错误状态
 */
inline val <T> ApiResult<T>.isError: Boolean
    get() = this is ApiResult.Error

/**
 * 判断结果是否为异常状态
 */
inline val <T> ApiResult<T>.isException: Boolean
    get() = this is ApiResult.Exception

/**
 * 判断结果是否为加载中状态
 */
inline val <T> ApiResult<T>.isLoading: Boolean
    get() = this is ApiResult.Loading

/**
 * 获取成功状态的数据，如果不是成功状态则返回 null
 */
inline fun <T> ApiResult<T>.getOrNull(): T? = when (this) {
    is ApiResult.Success -> data
    else -> null
}

/**
 * 获取错误信息，适用于 Error 和 Exception 状态
 */
inline fun <T> ApiResult<T>.getErrorMessage(): String = when (this) {
    is ApiResult.Error -> message
    is ApiResult.Exception -> exception.message ?: "Unknown error"
    else -> ""
}

/**
 * 对成功状态的数据进行转换
 */
inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data), msg)
    is ApiResult.Error -> this
    is ApiResult.Exception -> this
    ApiResult.Loading -> ApiResult.Loading
}

/**
 * 只有成功时才执行指定操作
 */
inline fun <T> ApiResult<T>.onSuccess(action: (T, String) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) {
        action(data, msg)
    }
    return this
}

/**
 * 只有错误时才执行指定操作
 */
inline fun <T> ApiResult<T>.onError(action: (Int, String) -> Unit): ApiResult<T> {
    if (this is ApiResult.Error) {
        action(code, message)
    }
    return this
}

/**
 * 只有异常时才执行指定操作
 */
inline fun <T> ApiResult<T>.onException(action: (Throwable) -> Unit): ApiResult<T> {
    if (this is ApiResult.Exception) {
        action(exception)
    }
    return this
}