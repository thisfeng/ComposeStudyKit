package com.thisfeng.composestudykit.ui.screen.network

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thisfeng.composestudykit.ui.viewmodel.NoCacheApiViewModel
import com.thisfeng.composestudykit.ui.viewmodel.RequestState

/**
 * 不带缓存的 API 测试界面
 * 
 * 🎯 演示重点：
 * 1. 并发请求的独立处理
 * 2. 渐进式 UI 更新效果
 * 3. 重组性能分析
 * 4. 请求完成顺序追踪
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoCacheApiScreen() {
    val viewModel: NoCacheApiViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val performanceStats by viewModel.performanceStats.collectAsState()
    
    // 重组计数器 - 用于观察重组性能
    var recompositionCount by remember { mutableIntStateOf(0) }




    LaunchedEffect(uiState) {
        recompositionCount++
        println("🔄 UI 重组第 $recompositionCount 次")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 标题和说明
        Text(
            text = "🚀 不带缓存的并发 API 测试",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "测试独立并发请求 • 渐进式更新 • 重组性能分析",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 测试按钮
        Button(
            onClick = { viewModel.loadDataIndependently() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text("🚀 开始独立并发测试")
        }

        // 重组性能统计卡片
        PerformanceCard(
            recompositionCount = recompositionCount,
            performanceStats = performanceStats,
            completionOrder = viewModel.getCompletionOrder()
        )

        // 各个数据源的状态卡片
        RequestStateCard(
            title = "🖼️ Banner 数据",
            state = uiState.bannerState,
            data = uiState.banners,
            dataDescription = { "获取到 ${uiState.banners.size} 个 Banner" }
        )

        RequestStateCard(
            title = "📰 文章列表",
            state = uiState.articleState,
            data = uiState.articles,
            dataDescription = { "获取到 ${uiState.articles.size} 篇文章" }
        )

        RequestStateCard(
            title = "📌 置顶文章",
            state = uiState.topArticleState,
            data = uiState.topArticles,
            dataDescription = { "获取到 ${uiState.topArticles.size} 篇置顶文章" }
        )

        // 详细数据展示
        if (uiState.banners.isNotEmpty()) {
            DataDetailCard(
                title = "Banner 详情",
                items = uiState.banners.take(3).map { it.title }
            )
        }

        if (uiState.articles.isNotEmpty()) {
            DataDetailCard(
                title = "文章详情",
                items = uiState.articles.take(3).map { "${it.title} - ${it.author.ifEmpty { it.shareUser }}" }
            )
        }

        if (uiState.topArticles.isNotEmpty()) {
            DataDetailCard(
                title = "置顶文章详情",
                items = uiState.topArticles.take(2).map { it.title }
            )
        }

// 错误提示已移除，改为直接显示Toast提示

    }
}

@Composable
private fun PerformanceCard(
    recompositionCount: Int,
    performanceStats: com.thisfeng.composestudykit.ui.viewmodel.PerformanceStats,
    completionOrder: List<String>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📊 性能分析",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // 重组统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "UI 重组次数:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$recompositionCount 次",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (recompositionCount > 5) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            
            if (performanceStats.totalRequests > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // 成功率
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("成功率:")
                    Text(
                        text = "${(performanceStats.successRate * 100).toInt()}%",
                        color = if (performanceStats.successRate > 0.8f) Color.Green else Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // 平均耗时
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("平均耗时:")
                    Text("${performanceStats.avgDuration}ms")
                }
                
                // 最长耗时
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("最长耗时:")
                    Text("${performanceStats.maxDuration}ms")
                }
                
                // 完成顺序
                if (completionOrder.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "完成顺序: ${completionOrder.joinToString(" → ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> RequestStateCard(
    title: String,
    state: RequestState,
    data: List<T>,
    dataDescription: () -> String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            when (state) {
                is RequestState.Initial -> {
                    Text(
                        text = "⏳ 等待开始...",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                
                is RequestState.Loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                is RequestState.Success -> {
                    Column {
                        Text(
                            text = "✅ ${state.message}",
                            color = Color.Green,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "耗时: ${state.duration}ms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        if (data.isNotEmpty()) {
                            Text(
                                text = dataDescription(),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                
                is RequestState.Error -> {
                    val context = LocalContext.current

                    LaunchedEffect(state) {
                        Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    }
                    Column {
                        Text(
                            text = "❌ ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "耗时: ${state.duration}ms",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DataDetailCard(
    title: String,
    items: List<String>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            items.forEach { item ->
                Text(
                    text = "• $item",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}