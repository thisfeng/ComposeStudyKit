package com.thisfeng.composestudykit.ui.screen.network

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MultilineChart
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thisfeng.composestudykit.navigation.NavRoutes

/**
 * 网络请求案例导航界面
 * 包含所有网络相关的测试案例
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkExamplesScreen(
    onNavigateToRealApi: () -> Unit,
    onNavigateToDataStoreComparison: () -> Unit,
    onNavigateToNoCacheApi: () -> Unit,
    onNavigateToFileOperation: () -> Unit,
    onNavigateToUpdate: () -> Unit,
    onNavigateToSimpleHome: () -> Unit,
    onBackClick: () -> Unit
) {
    // 屏幕列表数据
    val screenItems = listOf(
        ScreenItem("真实API", "调用 WanAndroid 真实 API 接口", Icons.Default.NetworkPing, NavRoutes.REAL_API_SCREEN),
        ScreenItem("DataStore", "DataStore 与 SharedPreferences 对比", Icons.Default.DataObject, NavRoutes.DATASTORE_COMPARISON_SCREEN),
        ScreenItem("并发测试", "网络并发请求性能测试", Icons.Default.Speed, NavRoutes.NO_CACHE_API_SCREEN),
        ScreenItem("文件操作", "文件上传下载功能演示", Icons.Default.FileUpload, NavRoutes.FILE_OPERATION_SCREEN),
        ScreenItem("版本更新", "APK版本检查与下载更新", Icons.Default.Update, NavRoutes.UPDATE_SCREEN),
        ScreenItem("基础框架", "网络框架基础功能演示", Icons.Default.Home, NavRoutes.SIMPLE_HOME_SCREEN)
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部标题栏
        TopAppBar(
            title = { 
                Text(
                    text = "🌐 网络请求案例",
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
        
        // 可滚动的屏幕列表
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(screenItems) { item ->
                ScreenCard(
                    item = item,
                    onClick = {
                        when (item.route) {
                            NavRoutes.REAL_API_SCREEN -> onNavigateToRealApi()
                            NavRoutes.DATASTORE_COMPARISON_SCREEN -> onNavigateToDataStoreComparison()
                            NavRoutes.NO_CACHE_API_SCREEN -> onNavigateToNoCacheApi()
                            NavRoutes.FILE_OPERATION_SCREEN -> onNavigateToFileOperation()
                            NavRoutes.UPDATE_SCREEN -> onNavigateToUpdate()
                            NavRoutes.SIMPLE_HOME_SCREEN -> onNavigateToSimpleHome()
                        }
                    }
                )
            }
        }
    }
}

/**
 * 屏幕列表项数据类
 */
data class ScreenItem(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

/**
 * 屏幕卡片组件
 */
@Composable
fun ScreenCard(
    item: ScreenItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "进入",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// 添加 Preview
@Preview(showBackground = true)
@Composable
private fun NetworkExamplesScreenPreview() {
    NetworkExamplesScreen(
        onNavigateToRealApi = {},
        onNavigateToDataStoreComparison = {},
        onNavigateToNoCacheApi = {},
        onNavigateToFileOperation = {},
        onNavigateToUpdate = {},
        onNavigateToSimpleHome = {},
        onBackClick = {}
    )
}
