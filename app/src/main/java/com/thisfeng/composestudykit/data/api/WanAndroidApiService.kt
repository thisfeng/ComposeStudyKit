package com.thisfeng.composestudykit.data.api

import com.thisfeng.composestudykit.data.model.Article
import com.thisfeng.composestudykit.data.model.ArticleList
import com.thisfeng.composestudykit.data.model.Banner
import com.thisfeng.composestudykit.network.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * WanAndroid API 接口定义
 * 基于 https://www.wanandroid.com/blog/show/2 的开放 API
 */
interface WanAndroidApiService {
    
    /**
     * 获取首页 Banner
     * GET /banner/json
     */
    @GET("banner/json")
    suspend fun getBanners(): ApiResponse<List<Banner>>
    
    /**
     * 获取首页文章列表
     * GET /article/list/{page}/json
     * 
     * @param page 页码，从0开始
     */
    @GET("article/list/{page}/json")
    suspend fun getArticles(@Path("page") page: Int): ApiResponse<ArticleList>
    
    /**
     * 获取置顶文章
     * GET /article/top/json
     */
    @GET("article/top/json")
    suspend fun getTopArticles(): ApiResponse<List<Article>>
    
    /**
     * 获取最新项目
     * GET /article/listproject/{page}/json
     * 
     * @param page 页码，从1开始
     */
    @GET("article/listproject/{page}/json")
    suspend fun getProjects(@Path("page") page: Int): ApiResponse<ArticleList>
}