package com.thisfeng.composestudykit.update

/**
 * 版本更新UI状态
 */
data class UpdateUiState(
    val isCheckingUpdate: Boolean = false,      // 检查更新状态
    val hasUpdate: Boolean = false,             // 是否有更新
    val showUpdateDialog: Boolean = false,      // 显示更新对话框
    val versionInfo: VersionInfo? = null,       // 版本信息
    val currentVersionCode: Long = 0L,          // 当前版本号
    val currentVersionName: String = "",        // 当前版本名
    val isDownloading: Boolean = false,         // 下载状态
    val downloadProgress: UpdateManager.DownloadProgress = UpdateManager.DownloadProgress(),
    val downloadCompleted: Boolean = false,     // 下载完成状态
    val showForceUpdate: Boolean = false,       // 是否强制更新
    val errorMessage: String? = null            // 错误信息
)