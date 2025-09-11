package com.thisfeng.composestudykit.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.annotation.StringRes

/**
 * Toast 工具类
 * 提供简单的 Toast 显示功能，支持在任何线程中安全调用
 */
object ToastUtils {
    
    private var toast: Toast? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    
    /**
     * 显示短时间 Toast
     */
    fun showShort(context: Context, message: String) {
        show(context, message, Toast.LENGTH_SHORT)
    }
    
    /**
     * 显示长时间 Toast
     */
    fun showLong(context: Context, message: String) {
        show(context, message, Toast.LENGTH_LONG)
    }
    
    /**
     * 显示短时间 Toast（使用字符串资源）
     */
    fun showShort(context: Context, @StringRes messageRes: Int) {
        show(context, context.getString(messageRes), Toast.LENGTH_SHORT)
    }
    
    /**
     * 显示长时间 Toast（使用字符串资源）
     */
    fun showLong(context: Context, @StringRes messageRes: Int) {
        show(context, context.getString(messageRes), Toast.LENGTH_LONG)
    }
    
    /**
     * 显示 Toast
     * 自动切换到主线程，支持在任何线程中安全调用
     */
    private fun show(context: Context, message: String, duration: Int) {
        // 检查是否在主线程
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // 在主线程，直接显示
            showToastOnMainThread(context, message, duration)
        } else {
            // 在子线程，切换到主线程显示
            mainHandler.post {
                showToastOnMainThread(context, message, duration)
            }
        }
    }
    
    /**
     * 在主线程显示 Toast
     */
    private fun showToastOnMainThread(context: Context, message: String, duration: Int) {
        // 取消之前的 Toast，避免重复显示
        toast?.cancel()
        
        toast = Toast.makeText(context.applicationContext, message, duration)
        toast?.show()
    }
    
    /**
     * 取消当前显示的 Toast
     */
    fun cancel() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            // 在主线程，直接取消
            toast?.cancel()
            toast = null
        } else {
            // 在子线程，切换到主线程取消
            mainHandler.post {
                toast?.cancel()
                toast = null
            }
        }
    }
}