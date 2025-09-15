package com.thisfeng.composestudykit.update

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.TimeUnit

/**
 * 版本更新管理器
 * 负责APK下载、断点续传、进度监听、安装等功能
 */
class UpdateManager(private val context: Context) {

    companion object {
        private const val DOWNLOAD_FOLDER = "download"
        private const val APK_FILE_NAME = "app_update.apk"
        private const val BUFFER_SIZE = 8192
        private const val CONNECT_TIMEOUT = 30L
        private const val READ_TIMEOUT = 60L
        private const val WRITE_TIMEOUT = 60L
        private const val TAG = "UpdateManager"
    }

    private val downloadDir: File by lazy {
        File(context.getExternalFilesDir(null), DOWNLOAD_FOLDER).apply {
            if (!exists()) mkdirs()
        }
    }

    private val apkFile: File by lazy {
        File(downloadDir, APK_FILE_NAME)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .build()
    }

    /**
     * 下载进度数据类
     */
    data class DownloadProgress(
        val downloadedBytes: Long = 0L,
        val totalBytes: Long = 0L,
        val progress: Float = 0f,
        val isCompleted: Boolean = false,
        val isError: Boolean = false,
        val errorMessage: String? = null
    ) {
        val progressPercent: Int get() = (progress * 100).toInt()
    }

    /**
     * 下载APK文件（支持断点续传）
     * @param downloadUrl 下载链接
     * @return 下载进度Flow
     */
    fun downloadApk(downloadUrl: String): Flow<DownloadProgress> = flow {
        try {
            Log.d(TAG, "Starting download from URL: $downloadUrl")
            
            // 检查现有文件大小
            val existingFileSize = if (apkFile.exists()) {
                Log.d(TAG, "Existing file found, size: ${apkFile.length()}")
                apkFile.length()
            } else {
                Log.d(TAG, "No existing file found")
                0L
            }
            
            // 构建请求，支持断点续传
            val request = Request.Builder()
                .url(downloadUrl)
                .apply {
                    if (existingFileSize > 0) {
                        addHeader("Range", "bytes=$existingFileSize-")
                        Log.d(TAG, "Adding range header: bytes=$existingFileSize-")
                    }
                }
                .build()

            Log.d(TAG, "Executing HTTP request")
            val response = okHttpClient.newCall(request).execute()
            Log.d(TAG, "HTTP response received, code: ${response.code}")
            
            if (!response.isSuccessful) {
                val errorMsg = "下载失败: ${response.code}"
                Log.e(TAG, errorMsg)
                emit(DownloadProgress(isError = true, errorMessage = errorMsg))
                return@flow
            }

            val responseBody = response.body ?: run {
                val errorMsg = "响应体为空"
                Log.e(TAG, errorMsg)
                emit(DownloadProgress(isError = true, errorMessage = errorMsg))
                return@flow
            }

            // 获取文件总大小
            val contentLength = responseBody.contentLength()
            // 修复断点续传的文件大小计算问题
            val totalBytes = if (contentLength != -1L) {
                if (existingFileSize > 0) {
                    // 断点续传时，总大小 = 已下载大小 + 剩余需要下载的大小
                    existingFileSize + contentLength
                } else {
                    // 全新下载
                    contentLength
                }
            } else {
                -1L // 无法确定总大小
            }
            Log.d(TAG, "Content length: $contentLength, Total bytes: $totalBytes, Existing file size: $existingFileSize")

            // 打开文件流（追加模式支持断点续传）
            val fileOutputStream = FileOutputStream(apkFile, existingFileSize > 0)
            val inputStream = responseBody.byteStream()

            var downloadedBytes = existingFileSize
            val buffer = ByteArray(BUFFER_SIZE)
            var bytesRead: Int

            // 发送初始进度
            emit(DownloadProgress(
                downloadedBytes = downloadedBytes,
                totalBytes = totalBytes,
                progress = if (totalBytes > 0) downloadedBytes.toFloat() / totalBytes else 0f
            ))

            // 开始下载
            Log.d(TAG, "Starting download loop")
            var progressUpdateCount = 0
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                fileOutputStream.write(buffer, 0, bytesRead)
                downloadedBytes += bytesRead

                // 计算进度
                val progress = if (totalBytes > 0) {
                    downloadedBytes.toFloat() / totalBytes
                } else {
                    0f
                }

                // 每100次更新发送一次进度，避免过于频繁的更新
                if (progressUpdateCount % 100 == 0) {
                    // 发送进度更新
                    emit(DownloadProgress(
                        downloadedBytes = downloadedBytes,
                        totalBytes = totalBytes,
                        progress = progress
                    ))
                    Log.d(TAG, "Download progress: ${downloadedBytes}/${totalBytes} (${(progress * 100).toInt()}%)")
                }
                progressUpdateCount++
            }

            // 关闭流
            inputStream.close()
            fileOutputStream.close()
            response.close()

            // 验证下载完成
            if (totalBytes > 0 && downloadedBytes < totalBytes) {
                val errorMsg = "下载不完整"
                Log.e(TAG, errorMsg)
                emit(DownloadProgress(isError = true, errorMessage = errorMsg))
            } else {
                Log.d(TAG, "Download completed successfully")
                emit(DownloadProgress(
                    downloadedBytes = downloadedBytes,
                    totalBytes = totalBytes,
                    progress = 1f,
                    isCompleted = true
                ))
            }

        } catch (e: Exception) {
            val errorMsg = "下载异常: ${e.message}"
            Log.e(TAG, errorMsg, e)
            emit(DownloadProgress(
                isError = true,
                errorMessage = errorMsg
            ))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * 安装APK文件
     * @return 是否成功启动安装
     */
    fun installApk(): Boolean {
        return try {
            if (!apkFile.exists()) {
                Toast.makeText(context, "APK文件不存在", Toast.LENGTH_LONG).show()
                return false
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // Android 7.0及以上使用FileProvider
                    val apkUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        apkFile
                    )
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    // Android 7.0以下直接使用文件URI
                    setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive")
                }
            }

            // 检查是否有安装权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    Toast.makeText(context, "请先授予安装未知来源应用的权限", Toast.LENGTH_LONG).show()
                    // 跳转到设置页面
                    val settingIntent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingIntent)
                    return false
                }
            }

            context.startActivity(intent)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "安装失败: ${e.message}", Toast.LENGTH_LONG).show()
            false
        }
    }

    /**
     * 获取当前应用版本号
     */
    fun getCurrentVersionCode(): Long {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            0L
        }
    }

    /**
     * 获取当前应用版本名称
     */
    fun getCurrentVersionName(): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0.0"
        }
    }

    /**
     * 清理下载的APK文件
     */
    fun clearDownloadedApk() {
        try {
            if (apkFile.exists()) {
                apkFile.delete()
            }
        } catch (e: Exception) {
            // 忽略清理错误
        }
    }

    /**
     * 检查APK文件是否已存在
     * @return 是否存在APK文件
     */
    fun isApkFileExists(): Boolean {
        return apkFile.exists()
    }

    /**
     * 检查APK文件是否已完整下载（通过检查文件大小是否合理）
     * @param expectedSize 期望的文件大小（如果已知）
     * @return 是否完整下载
     */
    fun isApkFileComplete(expectedSize: Long = -1L): Boolean {
        if (!apkFile.exists()) {
            return false
        }
        
        val fileSize = apkFile.length()
        // 如果不知道期望大小，只要文件存在且不为空就认为是完整的
        // 在实际应用中，可以存储期望的文件大小并进行比较
        return fileSize > 0
    }

    /**
     * 检查是否需要更新
     * @param serverVersionCode 服务器版本号
     * @return 是否需要更新
     */
    fun needUpdate(serverVersionCode: Int): Boolean {
        return serverVersionCode > getCurrentVersionCode()
    }
}