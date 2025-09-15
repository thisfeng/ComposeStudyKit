package com.thisfeng.composestudykit.utils

import android.app.Application
import android.content.Context

/**
 * 全局 Application 获取工具类
 * 
 * 这种方式获取全局的 Application 是一种扩展思路。
 * 对于组件化项目，不可能把项目实际的 Application 下沉到 Base，
 * 而且各个 module 也不需要知道 Application 真实名字。
 * 
 * 这种一次反射就能获取全局 Application 对象的方式相比于在 
 * Application#OnCreate 保存一份的方式显得更加通用了。
 */
object AppGlobals {
    
    @Volatile
    private var application: Application? = null
    
    /**
     * 获取全局 Application 实例
     * 使用反射方式获取，线程安全
     */
    fun getApplication(): Application {
        if (application == null) {
            synchronized(this) {
                if (application == null) {
                    try {
                        application = Class.forName("android.app.ActivityThread")
                            .getMethod("currentApplication")
                            .invoke(null) as Application
                    } catch (e: Exception) {
                        e.printStackTrace()
                        throw RuntimeException("Failed to get Application instance", e)
                    }
                }
            }
        }
        return application!!
    }
    
    /**
     * 获取全局 Application Context
     */
    val applicationContext: Context
        get() = getApplication().applicationContext
    
    /**
     * 清空缓存的 Application 实例（主要用于测试）
     */
    internal fun clearCache() {
        application = null
    }
}