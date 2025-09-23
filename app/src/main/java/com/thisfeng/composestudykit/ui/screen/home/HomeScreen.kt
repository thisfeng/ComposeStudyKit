package com.thisfeng.composestudykit.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * ComposeStudyKit 首页
 * 展示各种学习案例的入口
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToNetworkExamples: () -> Unit,
    onNavigateToTestExamples: () -> Unit,
    onNavigateToDataStoreExample: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 标题区域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🎯 ComposeStudyKit",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Jetpack Compose 学习案例集合",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "通过实际案例学习现代 Android 开发最佳实践",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // 案例分类
        Text(
            text = "📚 学习案例",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 网络请求相关案例
        ExampleCategoryCard(
            title = "🌐 网络请求框架",
            description = "学习 Retrofit + OkHttp + Moshi 技术栈\n包含缓存策略、并发处理、错误处理等",
            examples = listOf(
                ExampleItem(
                    icon = Icons.Default.Settings,
                    title = "真实 API 调用",
                    description = "WanAndroid API + DataStore 缓存"
                ),
                ExampleItem(
                    icon = Icons.Default.Info,
                    title = "DataStore 缓存",
                    description = "缓存策略对比演示"
                ),
                ExampleItem(
                    icon = Icons.Default.Refresh,
                    title = "并发性能测试",
                    description = "独立并发 + 重组性能分析"
                ),
                ExampleItem(
                    icon = Icons.Default.Home,
                    title = "基础网络框架",
                    description = "简化版网络请求演示"
                )
            ),
            onExploreClick = onNavigateToNetworkExamples
        )

        // 其它功能案例
        ExampleCategoryCard(
            title = "🌐 其它功能测试",
            description = "功能测试",
            examples = listOf(
                ExampleItem(
                    icon = Icons.Default.Settings,
                    title = "功能调用",
                    description = "测试"
                )
            ),
            onExploreClick = onNavigateToTestExamples
        )

        Spacer(modifier = Modifier.height(16.dp))

        // DataStore 工具类案例
        ExampleCategoryCard(
            title = "🗄️ DataStore 全局工具",
            description = "简便易懂的全局 Key-Value 配置管理\n支持多种数据类型、响应式数据流、类型安全",
            examples = listOf(
                ExampleItem(
                    icon = Icons.Default.Info,
                    title = "用户配置管理",
                    description = "用户名、ID、登录状态等"
                ),
                ExampleItem(
                    icon = Icons.Default.Settings,
                    title = "应用设置管理",
                    description = "主题、语言、通知开关等"
                ),
                ExampleItem(
                    icon = Icons.Default.Refresh,
                    title = "响应式数据流",
                    description = "数据变化时自动更新 UI"
                ),
                ExampleItem(
                    icon = Icons.Default.Home,
                    title = "批量操作支持",
                    description = "默认配置、数据导出等"
                )
            ),
            onExploreClick = onNavigateToDataStoreExample
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 即将推出的案例
        ComingSoonCard()

        Spacer(modifier = Modifier.height(24.dp))

        // 项目信息
        ProjectInfoCard()
    }
}

/**
 * 案例分类卡片
 */
@Composable
private fun ExampleCategoryCard(
    title: String,
    description: String,
    examples: List<ExampleItem>,
    onExploreClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // 案例列表
            examples.forEach { example ->
                ExampleItemRow(example = example)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 探索按钮
            Button(
                onClick = onExploreClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🚀 开始探索")
            }
        }
    }
}

/**
 * 案例项目行
 */
@Composable
private fun ExampleItemRow(example: ExampleItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = example.icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = example.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = example.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * 即将推出卡片
 */
@Composable
private fun ComingSoonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "🔮 即将推出",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "更多精彩案例正在开发中...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            val comingFeatures = listOf(
                "🎨 UI 组件库案例",
                "🗄️ 数据库操作案例",
                "🎬 动画效果案例",
                "📱 自定义 View 案例",
                "🔔 通知管理案例"
            )

            comingFeatures.forEach { feature ->
                Text(
                    text = "• $feature",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
            }
        }
    }
}

/**
 * 项目信息卡片
 */
@Composable
private fun ProjectInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "ℹ️ 项目信息",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            val projectInfo = listOf(
                "📋 技术栈: Kotlin + Jetpack Compose",
                "🏗️ 架构: MVVM + Repository 模式",
                "🌐 网络: Retrofit + OkHttp + Moshi",
                "💾 缓存: DataStore Preferences",
                "🔄 异步: Kotlin Coroutines + Flow",
                "🎯 特色: 无依赖注入框架，简洁易懂"
            )

            projectInfo.forEach { info ->
                Text(
                    text = info,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

/**
 * 案例项目数据类
 */
private data class ExampleItem(
    val icon: ImageVector,
    val title: String,
    val description: String
)