package com.thisfeng.composestudykit.data.api

import com.thisfeng.composestudykit.network.ApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * 文件上传下载 API 接口定义
 * 提供文件上传、下载和管理的相关功能
 */
interface FileApiService {
    
    /**
     * 单文件上传
     * @param file 要上传的文件
     * @param description 文件描述（可选）
     */
    @Multipart
    @POST("upload/single")
    suspend fun uploadSingleFile(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody? = null
    ): ApiResponse<FileUploadResult>
    
    /**
     * 多文件上传
     * @param files 要上传的文件列表
     * @param description 文件描述（可选）
     */
    @Multipart
    @POST("upload/multiple")
    suspend fun uploadMultipleFiles(
        @Part files: List<MultipartBody.Part>,
        @Part("description") description: RequestBody? = null
    ): ApiResponse<List<FileUploadResult>>
    
    /**
     * 带进度的文件上传（模拟接口）
     * @param file 要上传的文件
     * @param filename 文件名
     */
    @Multipart
    @POST("upload/with-progress")
    suspend fun uploadFileWithProgress(
        @Part file: MultipartBody.Part,
        @Part("filename") filename: RequestBody
    ): ApiResponse<FileUploadResult>
    
    /**
     * 文件下载
     * @param fileId 文件ID
     */
    @Streaming
    @GET("download/{fileId}")
    suspend fun downloadFile(@Path("fileId") fileId: String): Response<ResponseBody>
    
    /**
     * 下载网络图片（用于测试）
     * @param url 图片URL
     */
    @Streaming
    @GET
    suspend fun downloadImage(@Url url: String): Response<ResponseBody>
    
    /**
     * 获取文件信息
     * @param fileId 文件ID
     */
    @GET("file/{fileId}/info")
    suspend fun getFileInfo(@Path("fileId") fileId: String): ApiResponse<FileInfo>
    
    /**
     * 删除文件
     * @param fileId 文件ID
     */
    @DELETE("file/{fileId}")
    suspend fun deleteFile(@Path("fileId") fileId: String): ApiResponse<String>
    
    /**
     * 获取文件列表
     * @param page 页码
     * @param size 每页大小
     */
    @GET("files")
    suspend fun getFileList(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): ApiResponse<FileListResult>
}

/**
 * 文件上传结果
 */
data class FileUploadResult(
    val fileId: String,
    val fileName: String,
    val fileSize: Long,
    val contentType: String,
    val uploadTime: String,
    val downloadUrl: String
)

/**
 * 文件信息
 */
data class FileInfo(
    val fileId: String,
    val fileName: String,
    val fileSize: Long,
    val contentType: String,
    val uploadTime: String,
    val downloadUrl: String,
    val metadata: Map<String, String>? = null
)

/**
 * 文件列表结果
 */
data class FileListResult(
    val files: List<FileInfo>,
    val totalCount: Int,
    val currentPage: Int,
    val totalPages: Int
)