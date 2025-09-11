package com.thisfeng.composestudykit

import android.app.Application

/**
 * Application 类
 * 普通的 Application 类，不使用依赖注入
 */
class ComposeStudyKitApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // 应用初始化代码
    }
}