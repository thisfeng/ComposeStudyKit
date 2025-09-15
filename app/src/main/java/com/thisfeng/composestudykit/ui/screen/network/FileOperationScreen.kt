package com.thisfeng.composestudykit.ui.screen.network

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thisfeng.composestudykit.data.api.FileInfo
import com.thisfeng.composestudykit.data.api.FileUploadResult
import com.thisfeng.composestudykit.ui.viewmodel.FileOperationViewModel
import java.io.File
import java.io.FileOutputStream

/**
 * 文件上传下载演示界面
 * 展示文件操作的各种功能
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileOperationScreen(
    viewModel: FileOperationViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { 
            // 将 URI 复制到临时文件
            val tempFile = copyUriToTempFile(context, it)
            if (tempFile != null) {
                viewModel.uploadFileWithProgress(tempFile)
            }
        }
    }
    
    // 多文件选择器
    val multipleFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val tempFiles = uris.mapNotNull { uri ->
                copyUriToTempFile(context, uri)
            }
            if (tempFiles.isNotEmpty()) {
                viewModel.uploadMultipleFiles(tempFiles, "批量上传测试")
            }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部标题栏
        TopAppBar(
            title = { 
                Text(
                    text = "文件操作",
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
        
        // 内容区域
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 标题
            item {
                Text(
                    text = "📁 文件上传下载演示",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
            
            // 操作按钮区域
            item {
                FileOperationButtons(
                    isUploading = uiState.isUploading,
                    isDownloading = uiState.isDownloading,
                    onSingleFileUpload = { filePickerLauncher.launch("*/*") },
                    onMultipleFileUpload = { multipleFilePickerLauncher.launch("*/*") },
                    onDownloadSample = { 
                        viewModel.downloadFile("https://picsum.photos/200/300", "sample_image_${System.currentTimeMillis()}.jpg")
                    },
                    onGetFileList = { viewModel.getFileList() },
                    onClearResults = { viewModel.clearAllStates() }
                )
            }
            
            // 进度显示
            if (uiState.isUploading || uiState.isDownloading) {
                item {
                    ProgressSection(
                        isUploading = uiState.isUploading,
                        isDownloading = uiState.isDownloading,
                        uploadProgress = uiState.uploadProgress,
                        downloadProgress = uiState.downloadProgress
                    )
                }
            }
            
            // 最后操作信息
            if (uiState.lastOperationMessage.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "📋 最后操作",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.lastOperationMessage,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
            
            // 上传结果
            if (uiState.uploadResults.isNotEmpty()) {
                item {
                    Text(
                        text = "📤 上传结果 (${uiState.uploadResults.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                items(uiState.uploadResults) { result ->
                    UploadResultCard(result = result)
                }
            }
            
            // 下载文件
            if (uiState.downloadedFiles.isNotEmpty()) {
                item {
                    Text(
                        text = "📥 下载文件 (${uiState.downloadedFiles.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                items(uiState.downloadedFiles) { file ->
                    DownloadedFileCard(file = file)
                }
            }
            
            // 文件列表（模拟）
            if (uiState.fileList.isNotEmpty()) {
                item {
                    Text(
                        text = "📋 文件列表 (${uiState.fileList.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                items(uiState.fileList) { fileInfo ->
                    FileInfoCard(fileInfo = fileInfo)
                }
            }
            
            // 使用说明
            item {
                UsageInstructionsCard()
            }
        }
    }
}

@Composable
private fun FileOperationButtons(
    isUploading: Boolean,
    isDownloading: Boolean,
    onSingleFileUpload: () -> Unit,
    onMultipleFileUpload: () -> Unit,
    onDownloadSample: () -> Unit,
    onGetFileList: () -> Unit,
    onClearResults: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "🔧 文件操作",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            // 上传按钮行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSingleFileUpload,
                    enabled = !isUploading && !isDownloading,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("单文件上传")
                }
                
                Button(
                    onClick = onMultipleFileUpload,
                    enabled = !isUploading && !isDownloading,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.UploadFile, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("多文件上传")
                }
            }
            
            // 下载和列表按钮行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDownloadSample,
                    enabled = !isUploading && !isDownloading,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("下载示例")
                }
                
                OutlinedButton(
                    onClick = onGetFileList,
                    enabled = !isUploading && !isDownloading,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.List, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("文件列表")
                }
            }
            
            // 清除按钮
            OutlinedButton(
                onClick = onClearResults,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(Icons.Default.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("清除结果")
            }
        }
    }
}

@Composable
private fun ProgressSection(
    isUploading: Boolean,
    isDownloading: Boolean,
    uploadProgress: Int,
    downloadProgress: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isUploading) {
                Text(
                    text = "📤 正在上传...",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                LinearProgressIndicator(
                    progress = uploadProgress / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "$uploadProgress%",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            if (isDownloading) {
                Text(
                    text = "📥 正在下载...",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                LinearProgressIndicator(
                    progress = downloadProgress / 100f,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "$downloadProgress%",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun UploadResultCard(result: FileUploadResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = result.fileName,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "✅ 成功",
                    color = Color.Green,
                    fontSize = 12.sp
                )
            }
            
            Text(
                text = "文件ID: ${result.fileId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "大小: ${formatFileSize(result.fileSize)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "类型: ${result.contentType}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "时间: ${result.uploadTime}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DownloadedFileCard(file: File) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = file.name,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "📥 已下载",
                    color = Color.Blue,
                    fontSize = 12.sp
                )
            }
            
            Text(
                text = "路径: ${file.absolutePath}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "大小: ${formatFileSize(file.length())}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FileInfoCard(fileInfo: FileInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = fileInfo.fileName,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "📄 模拟",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp
                )
            }
            
            Text(
                text = "ID: ${fileInfo.fileId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "大小: ${formatFileSize(fileInfo.fileSize)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "类型: ${fileInfo.contentType}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Text(
                text = "时间: ${fileInfo.uploadTime}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun UsageInstructionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📖 使用说明",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "• 单文件上传：选择一个文件进行上传，支持进度显示",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "• 多文件上传：一次选择多个文件进行批量上传",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "• 下载示例：下载一个示例图片文件到本地",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "• 文件列表：获取模拟的服务器文件列表",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "• 由于没有真实服务器，上传使用模拟数据，下载使用真实图片",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 将 URI 复制到临时文件
 */
private fun copyUriToTempFile(context: android.content.Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val fileName = getFileName(context, uri) ?: "temp_file_${System.currentTimeMillis()}"
        val tempFile = File(context.cacheDir, fileName)
        
        inputStream?.use { input ->
            FileOutputStream(tempFile).use { output ->
                input.copyTo(output)
            }
        }
        
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * 获取文件名
 */
private fun getFileName(context: android.content.Context, uri: Uri): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) {
            cursor.getString(nameIndex)
        } else {
            null
        }
    }
}

/**
 * 格式化文件大小
 */
private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}

// 添加 Preview
@Preview(showBackground = true)
@Composable
private fun FileOperationScreenPreview() {
    FileOperationScreen(
        onBackClick = {}
    )
}
