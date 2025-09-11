package com.thisfeng.composestudykit.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit 客户端工厂类
 * 提供创建不同配置的 Retrofit 实例的方法
 */
object RetrofitClient {
    
    /**
     * 创建自定义配置的 Retrofit 实例
     */
    fun createCustomRetrofit(
        baseUrl: String,
        moshi: Moshi,
        okHttpClient: OkHttpClient? = null
    ): Retrofit {
        val client = okHttpClient ?: createDefaultOkHttpClient()
        
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
    
    /**
     * 创建标准 API 的 Retrofit 实例
     */
    fun createStandardRetrofit(): Retrofit {
        val moshi = createMoshi()
        return createCustomRetrofit(
            baseUrl = "https://www.wanandroid.com/",
            moshi = moshi
        )
    }
    
    /**
     * 创建第三方 API 的 Retrofit 实例
     */
    fun createThirdPartyRetrofit(baseUrl: String): Retrofit {
        val moshi = createMoshi()
        return createCustomRetrofit(
            baseUrl = baseUrl,
            moshi = moshi
        )
    }
    
    /**
     * 创建 Moshi 实例
     */
    fun createMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
    }
    
    /**
     * 创建默认的 OkHttpClient
     */
    fun createDefaultOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(NetworkErrorInterceptor())
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}