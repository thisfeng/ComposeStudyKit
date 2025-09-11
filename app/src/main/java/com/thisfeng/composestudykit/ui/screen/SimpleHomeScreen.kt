package com.thisfeng.composestudykit.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thisfeng.composestudykit.network.ApiResult
import com.thisfeng.composestudykit.ui.viewmodel.SimpleHomeViewModel

/**
 * 简化版首页演示界面
 * 展示如何使用网络请求框架（不使用 Hilt）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleHomeScreen(
    viewModel: SimpleHomeViewModel = viewModel()
) {
    val bannerState by viewModel.bannerState.collectAsState()
    val articleState by viewModel.articleState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 标题
        Text(
            text = "网络请求框架演示",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 操作按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.loadBanners() },
                modifier = Modifier.weight(1f)
            ) {
                Text("加载Banner")
            }
            
            Button(
                onClick = { viewModel.loadArticles() },
                modifier = Modifier.weight(1f)
            ) {
                Text("加载文章")
            }
        }

        // Banner 状态显示
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Banner 数据",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                when (bannerState) {
                    is ApiResult.Loading -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("加载中...")
                        }
                    }
                    is ApiResult.Success -> {
                        val successBanner = bannerState as ApiResult.Success<String>
                        Text("成功！数据: ${successBanner.data}")
                        if (successBanner.msg.isNotEmpty()) {
                            Text(
                                text = "消息: ${successBanner.msg}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        val errorBanner = bannerState as ApiResult.Error
                        Text(
                            text = "错误 ${errorBanner.code}: ${errorBanner.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    is ApiResult.Exception -> {
                        val exceptionBanner = bannerState as ApiResult.Exception
                        Text(
                            text = "异常: ${exceptionBanner.exception.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // 文章状态显示
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "文章数据",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                when (articleState) {
                    is ApiResult.Loading -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("加载中...")
                        }
                    }
                    is ApiResult.Success -> {
                        val successArticle = articleState as ApiResult.Success<String>
                        Text("成功！数据: ${successArticle.data}")
                        if (successArticle.msg.isNotEmpty()) {
                            Text(
                                text = "消息: ${successArticle.msg}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                    is ApiResult.Error -> {
                        val errorArticle = articleState as ApiResult.Error
                        Text(
                            text = "错误 ${errorArticle.code}: ${errorArticle.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    is ApiResult.Exception -> {
                        val exceptionArticle = articleState as ApiResult.Exception
                        Text(
                            text = "异常: ${exceptionArticle.exception.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 说明文字
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
                    text = "框架特性演示",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "• 统一的 ApiResult 状态管理\n" +
                            "• 自动错误处理和异常捕获\n" +
                            "• 协程支持的异步网络请求\n" +
                            "• Retrofit + OkHttp + Moshi 技术栈\n" +
                            "• 支持标准和第三方 API 格式\n" +
                            "• 完整的 Loading/Success/Error 状态",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}