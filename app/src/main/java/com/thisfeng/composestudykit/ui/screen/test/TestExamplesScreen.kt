package com.thisfeng.composestudykit.ui.screen.test

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

 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestExamplesScreen(
    onNavigateToPermission: () -> Unit,

    onBackClick: () -> Unit
) {
    // 屏幕列表数据
    val screenItems = listOf(
        ScreenItem(
            "XXPermission 权限请求使用",
            "",
            Icons.Default.NetworkPing,
            NavRoutes.TEST_PERMISSION
        ),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // 顶部标题栏
        TopAppBar(
            title = {
                Text(
                    text = "🌐 其它测试",
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
                            NavRoutes.TEST_PERMISSION -> onNavigateToPermission()

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


        }
    }
}

// 添加 Preview
@Preview(showBackground = true)
@Composable
private fun TestExamplesScreenPreview() {
    TestExamplesScreen(
        onNavigateToPermission = {},

        onBackClick = {}
    )
}