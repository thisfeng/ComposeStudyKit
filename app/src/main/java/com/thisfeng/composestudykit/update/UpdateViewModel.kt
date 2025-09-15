package com.thisfeng.composestudykit.update

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thisfeng.composestudykit.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 版本更新ViewModel
 * 管理版本检查、下载、安装的状态和逻辑
 */
class UpdateViewModel(
    private val updateRepository: UpdateRepository
) : ViewModel() {

    companion object {
        private const val TAG = "UpdateViewModel"
    }

    // UI状态
    private val _uiState = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _uiState.asStateFlow()

    /**
     * 检查版本更新
     */
    fun checkUpdate(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingUpdate = true, errorMessage = null)

            when (val result = updateRepository.checkVersion(
                channel = "ANDROID",
                company = "WingFat",
                serial = "W3oqf7L71JG821Q",
                outlet = "6001001"
            )) {
                is ApiResult.Loading -> {
                    // 保持加载状态
                }
                is ApiResult.Success -> {
                    val versionInfo = result.data
                    val (currentVersionCode, currentVersionName) = updateRepository.getCurrentVersion(context)
                    val needUpdate = updateRepository.needUpdate(context, versionInfo.inner)
                    
                    // 检查是否已经完整下载过相同的APK文件
                    val isApkAlreadyDownloaded = updateRepository.isApkFileComplete()
                    
                    _uiState.value = _uiState.value.copy(
                        isCheckingUpdate = false,
                        versionInfo = versionInfo,
                        currentVersionCode = currentVersionCode,
                        currentVersionName = currentVersionName,
                        hasUpdate = needUpdate,
                        showUpdateDialog = needUpdate,
                        downloadCompleted = isApkAlreadyDownloaded, // 如果APK已完整下载，标记为下载完成
                        isDownloading = false,
                        showForceUpdate = versionInfo.isMust
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isCheckingUpdate = false,
                        errorMessage = result.message
                    )
                }
                is ApiResult.Exception -> {
                    _uiState.value = _uiState.value.copy(
                        isCheckingUpdate = false,
                        errorMessage = "检查更新异常: ${result.exception.message}"
                    )
                }
            }
        }
    }

    /**
     * 开始下载APK
     */
    fun startDownload(context: Context) {
        Log.d(TAG, "startDownload called")
        val versionInfo = _uiState.value.versionInfo
        if (versionInfo == null) {
            Log.e(TAG, "versionInfo is null, cannot start download")
            return
        }
        
        Log.d(TAG, "Starting download for URL: ${versionInfo.downloadUrl}")
        
        viewModelScope.launch {
            // 修复：确保对话框保持显示状态
            _uiState.value = _uiState.value.copy(
                isDownloading = true,
                downloadProgress = UpdateManager.DownloadProgress(),
                errorMessage = null
                // 保持 showUpdateDialog = true 以继续显示对话框
            )

            updateRepository.downloadApk(context, versionInfo.downloadUrl)
                .collectLatest { progress ->
                    Log.d(TAG, "Download progress: ${progress.progressPercent}%")
                    _uiState.value = _uiState.value.copy(
                        downloadProgress = progress,
                        isDownloading = !progress.isCompleted && !progress.isError
                    )

                    if (progress.isError) {
                        Log.e(TAG, "Download error: ${progress.errorMessage}")
                        _uiState.value = _uiState.value.copy(
                            errorMessage = progress.errorMessage
                        )
                    } else if (progress.isCompleted) {
                        Log.d(TAG, "Download completed")
                        _uiState.value = _uiState.value.copy(
                            downloadCompleted = true
                        )
                    }
                }
        }
    }

    /**
     * 安装APK
     */
    fun installApk(context: Context) {
        viewModelScope.launch {
            val success = updateRepository.installApk(context)
            if (!success) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "启动安装失败，请检查权限设置"
                )
            }
        }
    }

    /**
     * 显示更新对话框
     */
    fun showUpdateDialog() {
        _uiState.value = _uiState.value.copy(showUpdateDialog = true)
    }

    /**
     * 隐藏更新对话框
     */
    fun hideUpdateDialog() {
        _uiState.value = _uiState.value.copy(showUpdateDialog = false)
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * 重置下载状态
     */
    fun resetDownloadState() {
        _uiState.value = _uiState.value.copy(
            isDownloading = false,
            downloadCompleted = false,
            downloadProgress = UpdateManager.DownloadProgress()
        )
    }

    /**
     * 清理下载的APK文件
     */
    fun clearDownloadedApk(context: Context) {
        viewModelScope.launch {
            updateRepository.clearDownloadedApk(context)
            resetDownloadState()
        }
    }
}