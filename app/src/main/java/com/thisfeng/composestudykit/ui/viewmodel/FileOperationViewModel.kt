package com.thisfeng.composestudykit.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.thisfeng.composestudykit.data.api.FileInfo
import com.thisfeng.composestudykit.data.api.FileUploadResult
import com.thisfeng.composestudykit.data.repository.FileRepository
import com.thisfeng.composestudykit.network.ApiResult
import com.thisfeng.composestudykit.utils.AppGlobals
import com.thisfeng.composestudykit.utils.ToastUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * 文件操作 ViewModel
 * 管理文件上传、下载和相关UI状态
 */
class FileOperationViewModel : ViewModel() {

    private val repository = FileRepository(AppGlobals.getApplication())
    private val context: Context = AppGlobals.getApplication()

    // UI 状态
    private val _uiState = MutableStateFlow(FileOperationUiState())
    val uiState: StateFlow<FileOperationUiState> = _uiState.asStateFlow()

    /**
     * 上传单个文件
     */
    fun uploadSingleFile(file: File, description: String? = null) {
        viewModelScope.launch {
            updateUiState { it.copy(isUploading = true, uploadProgress = 0) }

            val result = repository.uploadSingleFile(file, description)

            when (result) {
                is ApiResult.Success -> {
                    updateUiState {
                        it.copy(
                            isUploading = false,
                            uploadProgress = 100,
                            uploadResults = it.uploadResults + result.data,
                            lastOperationMessage = result.msg.ifEmpty { "文件上传成功" }
                        )
                    }
                    ToastUtils.showLong(context, "文件上传成功: ${result.data.fileName}")
                }

                is ApiResult.Error -> {
                    updateUiState {
                        it.copy(
                            isUploading = false,
                            uploadProgress = 0,
                            lastOperationMessage = "上传失败: ${result.message}"
                        )
                    }
                    ToastUtils.showLong(context, "上传失败: ${result.message}")
                }

                is ApiResult.Exception -> {
                    updateUiState {
                        it.copy(
                            isUploading = false,
                            uploadProgress = 0,
                            lastOperationMessage = result.message
                        )
                    }
                    ToastUtils.showLong(context, result.message)
                }

                is ApiResult.Loading -> {
                    // 已经在上面设置了加载状态
                }
            }
        }
    }

    /**
     * 上传多个文件
     */
    fun uploadMultipleFiles(files: List<File>, description: String? = null) {
        viewModelScope.launch {
            updateUiState { it.copy(isUploading = true, uploadProgress = 0) }

            val result = repository.uploadMultipleFiles(files, description)

            when (result) {
                is ApiResult.Success -> {
                    updateUiState {
                        it.copy(
                            isUploading = false,
                            uploadProgress = 100,
                            uploadResults = it.uploadResults + result.data,
                            lastOperationMessage = result.msg.ifEmpty { "多文件上传成功" }
                        )
                    }
                    ToastUtils.showLong(context, "多文件上传成功，共 ${result.data.size} 个文件")
                }

                is ApiResult.Error -> {
                    updateUiState {
                        it.copy(
                            isUploading = false,
                            uploadProgress = 0,
                            lastOperationMessage = "上传失败: ${result.message}"
                        )
                    }
                    ToastUtils.showLong(context, "上传失败: ${result.message}")
                }

                is ApiResult.Exception -> {
                    updateUiState {
                        it.copy(
                            isUploading = false,
                            uploadProgress = 0,
                            lastOperationMessage = result.message
                        )
                    }
                    ToastUtils.showLong(context, result.message)
                }

                is ApiResult.Loading -> {
                    // 已经在上面设置了加载状态
                }
            }
        }
    }

    /**
     * 带进度的文件上传
     */
    fun uploadFileWithProgress(file: File) {
        viewModelScope.launch {
            repository.uploadFileWithProgress(file ) { uploaded, total, percentage ->
                updateUiState { it.copy(uploadProgress = percentage) }
            }.collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        updateUiState { it.copy(isUploading = true) }
                    }

                    is ApiResult.Success -> {
                        updateUiState {
                            it.copy(
                                isUploading = false,
                                uploadProgress = 100,
                                uploadResults = it.uploadResults + result.data,
                                lastOperationMessage = result.msg
                            )
                        }
                        ToastUtils.showLong(context, "文件上传成功: ${result.data.fileName}")
                    }

                    is ApiResult.Error -> {
                        updateUiState {
                            it.copy(
                                isUploading = false,
                                uploadProgress = 0,
                                lastOperationMessage = "上传失败: ${result.message}"
                            )
                        }
                        ToastUtils.showLong(context, "上传失败: ${result.message}")
                    }

                    is ApiResult.Exception -> {
                        updateUiState {
                            it.copy(
                                isUploading = false,
                                uploadProgress = 0,
                                lastOperationMessage = result.message
                            )
                        }
                        ToastUtils.showLong(context, result.message)
                    }
                }
            }
        }
    }

    /**
     * 下载文件
     */
    fun downloadFile(url: String, fileName: String) {
        viewModelScope.launch {
            val downloadsDir = File(context.getExternalFilesDir(null), "downloads")
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }

            val targetFile = File(downloadsDir, fileName)

            repository.downloadFile(url, targetFile) { downloaded, total, percentage ->
                updateUiState { it.copy(downloadProgress = percentage) }
            }.collect { result ->
                when (result) {
                    is ApiResult.Loading -> {
                        updateUiState { it.copy(isDownloading = true) }
                    }

                    is ApiResult.Success -> {
                        updateUiState {
                            it.copy(
                                isDownloading = false,
                                downloadProgress = 100,
                                downloadedFiles = it.downloadedFiles + result.data,
                                lastOperationMessage = result.msg
                            )
                        }
                        ToastUtils.showLong(context, "文件下载成功: ${result.data.name}")
                    }

                    is ApiResult.Error -> {
                        updateUiState {
                            it.copy(
                                isDownloading = false,
                                downloadProgress = 0,
                                lastOperationMessage = "下载失败: ${result.message}"
                            )
                        }
                        ToastUtils.showLong(context, "下载失败: ${result.message}")
                    }

                    is ApiResult.Exception -> {
                        updateUiState {
                            it.copy(
                                isDownloading = false,
                                downloadProgress = 0,
                                lastOperationMessage = result.message
                            )
                        }
                        ToastUtils.showLong(context, result.message)
                    }
                }
            }
        }
    }

    /**
     * 获取文件信息
     */
    fun getFileInfo(fileId: String) {
        viewModelScope.launch {
            updateUiState { it.copy(isLoading = true) }

            val result = repository.getFileInfo(fileId)

            when (result) {
                is ApiResult.Success -> {
                    updateUiState {
                        it.copy(
                            isLoading = false,
                            fileInfos = it.fileInfos + result.data,
                            lastOperationMessage = result.msg.ifEmpty { "获取文件信息成功" }
                        )
                    }
                }

                is ApiResult.Error -> {
                    updateUiState {
                        it.copy(
                            isLoading = false,
                            lastOperationMessage = "获取文件信息失败: ${result.message}"
                        )
                    }
                    ToastUtils.showLong(context, "获取文件信息失败: ${result.message}")
                }

                is ApiResult.Exception -> {
                    updateUiState {
                        it.copy(
                            isLoading = false,
                            lastOperationMessage = result.message
                        )
                    }
                    ToastUtils.showLong(context, result.message)
                }

                is ApiResult.Loading -> {
                    // 已经在上面设置了加载状态
                }
            }
        }
    }

    /**
     * 获取文件列表
     */
    fun getFileList(page: Int = 0, size: Int = 10) {
        viewModelScope.launch {
            updateUiState { it.copy(isLoading = true) }

            val result = repository.getFileList(page, size)

            when (result) {
                is ApiResult.Success -> {
                    updateUiState {
                        it.copy(
                            isLoading = false,
                            fileList = result.data.files,
                            lastOperationMessage = result.msg.ifEmpty { "获取文件列表成功" }
                        )
                    }
                }

                is ApiResult.Error -> {
                    updateUiState {
                        it.copy(
                            isLoading = false,
                            lastOperationMessage = "获取文件列表失败: ${result.message}"
                        )
                    }
                    ToastUtils.showLong(context, "获取文件列表失败: ${result.message}")
                }

                is ApiResult.Exception -> {
                    updateUiState {
                        it.copy(
                            isLoading = false,
                            lastOperationMessage = result.message
                        )
                    }
                    ToastUtils.showLong(context, result.message)
                }

                is ApiResult.Loading -> {
                    // 已经在上面设置了加载状态
                }
            }
        }
    }

    /**
     * 清除上传结果
     */
    fun clearUploadResults() {
        updateUiState { it.copy(uploadResults = emptyList()) }
    }

    /**
     * 清除下载文件
     */
    fun clearDownloadedFiles() {
        updateUiState { it.copy(downloadedFiles = emptyList()) }
    }

    /**
     * 清除所有状态
     */
    fun clearAllStates() {
        updateUiState { FileOperationUiState() }
    }

    private fun updateUiState(update: (FileOperationUiState) -> FileOperationUiState) {
        _uiState.value = update(_uiState.value)
    }
}

/**
 * 文件操作 UI 状态
 */
data class FileOperationUiState(
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val isDownloading: Boolean = false,
    val uploadProgress: Int = 0,
    val downloadProgress: Int = 0,
    val uploadResults: List<FileUploadResult> = emptyList(),
    val downloadedFiles: List<File> = emptyList(),
    val fileInfos: List<FileInfo> = emptyList(),
    val fileList: List<FileInfo> = emptyList(),
    val lastOperationMessage: String = ""
)