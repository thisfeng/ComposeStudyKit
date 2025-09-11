package com.thisfeng.composestudykit.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thisfeng.composestudykit.cache.CacheResult
import com.thisfeng.composestudykit.data.model.Article
import com.thisfeng.composestudykit.data.model.Banner
import com.thisfeng.composestudykit.ui.viewmodel.RealApiViewModel
import com.thisfeng.composestudykit.ui.viewmodel.DataState
import com.thisfeng.composestudykit.ui.viewmodel.getSourceDescription

/**
 * 真实 WanAndroid API 调用演示界面
 * 展示如何调用真实的网络接口
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealApiScreen() {
    val context = LocalContext.current
    val viewModel: RealApiViewModel = remember { RealApiViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 标题
        Text(
            text = "真实 WanAndroid API 调用",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "调用 https://www.wanandroid.com 开放API",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 页面级加载状态指示器
        if (uiState.isPageLoading) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "正在加载页面数据...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // 操作按钮区域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "API 测试按钮",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                // 第一行按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.refreshAllData() },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isPageLoading
                    ) {
                        Text("全量刷新")
                    }
                    
                    Button(
                        onClick = { viewModel.loadArticles() },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isPageLoading
                    ) {
                        Text("文章")
                    }
                }
                
                // 第二行按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.loadTopArticles() },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isPageLoading
                    ) {
                        Text("置顶")
                    }
                    
                    Button(
                        onClick = { viewModel.clearAllCache() },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isPageLoading
                    ) {
                        Text("清缓存")
                    }
                }
                
                // 第三行按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.loadFromCacheOnly() },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isPageLoading
                    ) {
                        Text("仅缓存")
                    }
                    
                    OutlinedButton(
                        onClick = { viewModel.loadFromNetworkOnly() },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isPageLoading
                    ) {
                        Text("仅网络")
                    }
                }
            }
        }

        // 统计信息
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
                    text = "数据统计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = viewModel.getStatsInfo(),
                    style = MaterialTheme.typography.bodyMedium
                )
                if (uiState.lastApiCall.isNotEmpty()) {
                    Text(
                        text = "最近调用: ${uiState.lastApiCall}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // Banner 展示
        DataStateCard(
            title = "Banner 数据",
            state = uiState.bannerState,
            data = uiState.banners,
            successContent = { bannerList ->
                Text("获取到 ${bannerList.size} 个 Banner")
                bannerList.take(3).forEach { banner ->
                    Text(
                        text = "• ${banner.title}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        )

        // 文章展示
        DataStateCard(
            title = "文章列表",
            state = uiState.articleState,
            data = uiState.articles,
            successContent = { articleList ->
                Text("获取到 ${articleList.size} 篇文章")
                articleList.take(3).forEach { article ->
                    Column(
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    ) {
                        Text(
                            text = "• ${article.title}",
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "  作者: ${article.author.ifEmpty { article.shareUser }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        )

        // 置顶文章展示
        DataStateCard(
            title = "置顶文章",
            state = uiState.topArticleState,
            data = uiState.topArticles,
            successContent = { topList ->
                Text("获取到 ${topList.size} 篇置顶文章")
                topList.take(2).forEach { article ->
                    Text(
                        text = "• ${article.title}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        )



        // 错误提示
        uiState.errorMessage?.let { message ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { viewModel.clearErrorMessage() }
                    ) {
                        Text("关闭")
                    }
                }
            }
        }
    }
}

@Composable
private fun <T> DataStateCard(
    title: String,
    state: DataState,
    data: List<T>,
    successContent: @Composable (List<T>) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
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
                is DataState.Loading -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("加载中...")
                    }
                }
                is DataState.Success -> {
                    Row(modifier = Modifier.padding(bottom = 4.dp)) {
                        val emoji = when (state.source) {
                            "缓存" -> "🟢"
                            "网络" -> "🔵" 
                            "过期缓存" -> "🟡"
                            else -> "✅"
                        }
                        Text(
                            text = "$emoji ${state.source}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (data.isNotEmpty()) {
                        successContent(data)
                    } else {
                        Text("暂无数据")
                    }
                }
                is DataState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                is DataState.Initial -> {
                    Text(
                        text = "暂未加载",
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}