package com.thisfeng.composestudykit.data.repository

import android.content.Context
import com.thisfeng.composestudykit.data.api.FileApiService
import com.thisfeng.composestudykit.data.api.FileInfo
import com.thisfeng.composestudykit.data.api.FileListResult
import com.thisfeng.composestudykit.data.api.FileUploadResult
import com.thisfeng.composestudykit.network.ApiResult
import com.thisfeng.composestudykit.network.BaseRepository
import com.thisfeng.composestudykit.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * 文件操作数据仓库
 * 提供文件上传、下载和管理功能
 */
class FileRepository(context: Context) : BaseRepository() {
    
    // 由于没有真实的文件服务器，我们使用 httpbin.org 作为测试
    private val fileApiService: FileApiService by lazy {
        RetrofitClient.createThirdPartyRetrofit("https://httpbin.org/")
            .create(FileApiService::class.java)
    }
    
    // 图片下载服务（使用真实的图片URL）
    private val imageDownloadService: FileApiService by lazy {
        RetrofitClient.createThirdPartyRetrofit("https://picsum.photos/")
            .create(FileApiService::class.java)
    }
    
    override val dataStoreCacheManager = null // 文件操作不使用缓存
    
    /**
     * 上传单个文件
     * @param file 要上传的文件
     * @param description 文件描述
     */
    suspend fun uploadSingleFile(
        file: File,
        description: String? = null
    ): ApiResult<FileUploadResult> {
        return safeApiCall {
            val requestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData(
                "file",
                file.name,
                requestBody
            )
            
            val descriptionBody = description?.toRequestBody("text/plain".toMediaTypeOrNull())
            
            // 模拟返回上传结果
            createMockUploadResult(file)
        }
    }
    
    /**
     * 上传多个文件
     * @param files 要上传的文件列表
     * @param description 文件描述
     */
    suspend fun uploadMultipleFiles(
        files: List<File>,
        description: String? = null
    ): ApiResult<List<FileUploadResult>> {
        return safeApiCall {
            val fileParts = files.map { file ->
                val requestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                MultipartBody.Part.createFormData(
                    "files",
                    file.name,
                    requestBody
                )
            }
            
            val descriptionBody = description?.toRequestBody("text/plain".toMediaTypeOrNull())
            
            // 模拟返回上传结果
            createMockMultipleUploadResult(files)
        }
    }
    
    /**
     * 带进度监听的文件上传
     * @param file 要上传的文件
     * @param onProgress 进度回调 (已上传字节数, 总字节数, 百分比)
     */
    fun uploadFileWithProgress(
        file: File,
        onProgress: (uploaded: Long, total: Long, percentage: Int) -> Unit
    ): Flow<ApiResult<FileUploadResult>> = flow {
        emit(ApiResult.Loading)
        
        try {
            val total = file.length()
            var uploaded = 0L
            
            // 模拟上传进度
            while (uploaded < total) {
                val chunk = minOf(8192L, total - uploaded) // 8KB chunks
                uploaded += chunk
                val percentage = ((uploaded.toFloat() / total) * 100).toInt()
                
                onProgress(uploaded, total, percentage)
                
                // 模拟网络延迟
                kotlinx.coroutines.delay(100)
                
                // 发送进度更新
                if (uploaded < total) {
                    emit(ApiResult.Loading)
                }
            }
            
            // 完成上传
            val result = createMockUploadResult(file)
            emit(ApiResult.Success(result.data!!, "文件上传成功"))
            
        } catch (e: Exception) {
            emit(ApiResult.Exception(e, "文件上传失败：${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * 下载文件
     * @param url 文件下载URL
     * @param targetFile 目标保存文件
     * @param onProgress 下载进度回调
     */
    fun downloadFile(
        url: String,
        targetFile: File,
        onProgress: (downloaded: Long, total: Long, percentage: Int) -> Unit = { _, _, _ -> }
    ): Flow<ApiResult<File>> = flow {
        emit(ApiResult.Loading)
        
        try {
            // 使用图片下载服务下载示例图片
            val response = imageDownloadService.downloadImage("200/300.jpg?random=${System.currentTimeMillis()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val result = saveResponseToFile(body, targetFile, onProgress)
                    emit(result)
                } else {
                    emit(ApiResult.Exception(Exception("响应体为空"), "下载失败"))
                }
            } else {
                emit(ApiResult.Exception(Exception("HTTP ${response.code()}"), "下载失败"))
            }
        } catch (e: Exception) {
            emit(ApiResult.Exception(e, "下载失败：${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * 获取文件信息（模拟）
     */
    suspend fun getFileInfo(fileId: String): ApiResult<FileInfo> {
        return safeApiCall {
            createMockFileInfo(fileId)
        }
    }
    
    /**
     * 获取文件列表（模拟）
     */
    suspend fun getFileList(page: Int = 0, size: Int = 10): ApiResult<FileListResult> {
        return safeApiCall {
            createMockFileList(page, size)
        }
    }
    
    /**
     * 删除文件（模拟）
     */
    suspend fun deleteFile(fileId: String): ApiResult<String> {
        return safeApiCall {
            createMockDeleteResult(fileId)
        }
    }
    
    /**
     * 保存响应到文件
     */
    private suspend fun saveResponseToFile(
        body: ResponseBody,
        targetFile: File,
        onProgress: (downloaded: Long, total: Long, percentage: Int) -> Unit
    ): ApiResult<File> = withContext(Dispatchers.IO) {
        try {
            val contentLength = body.contentLength()
            val inputStream: InputStream = body.byteStream()
            val outputStream = FileOutputStream(targetFile)
            
            val buffer = ByteArray(4096)
            var downloaded = 0L
            var bytesRead: Int
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                downloaded += bytesRead
                
                if (contentLength > 0) {
                    val percentage = ((downloaded.toFloat() / contentLength) * 100).toInt()
                    onProgress(downloaded, contentLength, percentage)
                }
            }
            
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            
            ApiResult.Success(targetFile, "文件下载成功")
        } catch (e: Exception) {
            ApiResult.Exception(e, "保存文件失败：${e.message}")
        }
    }
    
    // 模拟数据创建方法
    private fun createMockUploadResult(file: File): com.thisfeng.composestudykit.network.ApiResponse<FileUploadResult> {
        return com.thisfeng.composestudykit.network.ApiResponse(
            data = FileUploadResult(
                fileId = "file_${System.currentTimeMillis()}",
                fileName = file.name,
                fileSize = file.length(),
                contentType = guessMimeType(file.name),
                uploadTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date()),
                downloadUrl = "https://example.com/download/file_${System.currentTimeMillis()}"
            ),
            errorCode = 0,
            errorMsg = "success"
        )
    }
    
    private fun createMockMultipleUploadResult(files: List<File>): com.thisfeng.composestudykit.network.ApiResponse<List<FileUploadResult>> {
        return com.thisfeng.composestudykit.network.ApiResponse(
            data = files.map { file ->
                FileUploadResult(
                    fileId = "file_${System.currentTimeMillis()}_${file.name}",
                    fileName = file.name,
                    fileSize = file.length(),
                    contentType = guessMimeType(file.name),
                    uploadTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                        .format(java.util.Date()),
                    downloadUrl = "https://example.com/download/file_${System.currentTimeMillis()}_${file.name}"
                )
            },
            errorCode = 0,
            errorMsg = "success"
        )
    }
    
    private fun createMockFileInfo(fileId: String): com.thisfeng.composestudykit.network.ApiResponse<FileInfo> {
        return com.thisfeng.composestudykit.network.ApiResponse(
            data = FileInfo(
                fileId = fileId,
                fileName = "example_${fileId}.jpg",
                fileSize = 1024 * 1024, // 1MB
                contentType = "image/jpeg",
                uploadTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date()),
                downloadUrl = "https://example.com/download/$fileId",
                metadata = mapOf(
                    "width" to "800",
                    "height" to "600",
                    "format" to "JPEG"
                )
            ),
            errorCode = 0,
            errorMsg = "success"
        )
    }
    
    private fun createMockFileList(page: Int, size: Int): com.thisfeng.composestudykit.network.ApiResponse<FileListResult> {
        val files = (1..size).map { index ->
            FileInfo(
                fileId = "file_${page}_${index}",
                fileName = "file_${page}_${index}.jpg",
                fileSize = (1024 * (100..2000).random()).toLong(),
                contentType = if (index % 3 == 0) "image/png" else "image/jpeg",
                uploadTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date(System.currentTimeMillis() - index * 3600000)),
                downloadUrl = "https://example.com/download/file_${page}_${index}"
            )
        }
        
        return com.thisfeng.composestudykit.network.ApiResponse(
            data = FileListResult(
                files = files,
                totalCount = 100,
                currentPage = page,
                totalPages = 10
            ),
            errorCode = 0,
            errorMsg = "success"
        )
    }
    
    private fun createMockDeleteResult(fileId: String): com.thisfeng.composestudykit.network.ApiResponse<String> {
        return com.thisfeng.composestudykit.network.ApiResponse(
            data = "文件 $fileId 删除成功",
            errorCode = 0,
            errorMsg = "success"
        )
    }
    
    private fun guessMimeType(fileName: String): String {
        return when (fileName.substringAfterLast('.', "").lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "pdf" -> "application/pdf"
            "txt" -> "text/plain"
            "doc" -> "application/msword"
            "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else -> "application/octet-stream"
        }
    }
}