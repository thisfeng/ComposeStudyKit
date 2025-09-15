package com.thisfeng.composestudykit.update

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * 版本更新主屏幕
 * 只保留检查更新和清空已下载文件两个按钮
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { UpdateViewModel(UpdateRepository()) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // 显示错误消息
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }
    
    // 页面进入时检查APK文件状态
    LaunchedEffect(Unit) {
        // 检查是否有未完成的下载任务
        if (viewModel.uiState.value.isDownloading) {
            // 如果正在下载，重置状态以避免UI显示异常
            viewModel.resetDownloadState()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "🔄 版本更新",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 当前版本信息卡片
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "当前版本信息",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "版本号:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = uiState.currentVersionName.ifEmpty { "未知" },
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "版本代码:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = uiState.currentVersionCode.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // 检查更新按钮
            Button(
                onClick = { viewModel.checkUpdate(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !uiState.isCheckingUpdate && !uiState.isDownloading
            ) {
                if (uiState.isCheckingUpdate) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("检查中...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Update,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("检查更新")
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 清空已下载文件按钮
            OutlinedButton(
                onClick = { viewModel.clearDownloadedApk(context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("清空已下载文件")
            }
            
            // 显示更新对话框（如果需要）
            val versionInfo = uiState.versionInfo
            if (uiState.showUpdateDialog && versionInfo != null) {
                UpdateDialog(
                    uiState = uiState,
                    onUpdateClick = {
                        // 修复：不再隐藏对话框，保持显示以显示下载进度
                        viewModel.startDownload(context)
                    },
                    onDismiss = {
                        viewModel.hideUpdateDialog()
                        if (versionInfo.isMust == false) {
                            // 非强制更新才允许关闭
                            viewModel.hideUpdateDialog()
                        }
                    },
                    onInstallClick = { viewModel.installApk(context) }
                )
            }
        }
    }
}

/**
 * 版本更新对话框
 */
@Composable
private fun UpdateDialog(
    uiState: UpdateUiState,
    onUpdateClick: () -> Unit,
    onDismiss: () -> Unit,
    onInstallClick: () -> Unit
) {
    val versionInfo = uiState.versionInfo
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,  // 允许通过返回键关闭
            dismissOnClickOutside = !(versionInfo?.isMust ?: false)  // 强制更新时不允许点击外部关闭
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Box {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // 标题
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "发现新版本",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // 右上角关闭图标（半透明）
                        if (versionInfo?.isMust == false) {
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "关闭",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 版本信息
                    versionInfo?.let { info ->
                        Text(
                            text = "版本 ${info.versions}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "发布时间: ${info.time}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (info.isMust) {
                            Text(
                                text = "⚠️ 这是一个强制更新",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // 更新说明
                        Text(
                            text = "更新说明:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = info.explain,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 120.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // 下载进度
                    if (uiState.isDownloading || uiState.downloadCompleted) {
                        LinearProgressIndicator(
                            progress = { uiState.downloadProgress.progress },
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = if (uiState.downloadCompleted) {
                                "下载完成"
                            } else {
                                "下载进度: ${uiState.downloadProgress.progressPercent}%"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 按钮区域
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (versionInfo?.isMust == false) {
                            Arrangement.spacedBy(12.dp)
                        } else {
                            Arrangement.Center
                        }
                    ) {
                        if (versionInfo?.isMust == false) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                // 修复：在下载过程中禁用关闭按钮
                                enabled = !uiState.isDownloading
                            ) {
                                Text("稍后更新")
                            }
                        }
                        
                        Button(
                            onClick = {
                                if (uiState.downloadCompleted) {
                                    onInstallClick()
                                } else {
                                    onUpdateClick()
                                }
                            },
                            // 修复：在下载完成时启用按钮
                            enabled = !uiState.isDownloading || uiState.downloadCompleted,
                            modifier = if (versionInfo?.isMust == false) {
                                Modifier.weight(1f)
                            } else {
                                Modifier.fillMaxWidth()
                            }
                        ) {
                            if (uiState.isDownloading && !uiState.downloadCompleted) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("下载中...")
                            } else if (uiState.downloadCompleted) {
                                Icon(
                                    imageVector = Icons.Default.InstallMobile,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("安装")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("立即更新")
                            }
                        }
                    }
                }
            }
        }
    }
}