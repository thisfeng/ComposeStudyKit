package com.thisfeng.composestudykit.ui.screen.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 新技术探索页面
 * 用于展示和测试新的技术特性
 */
@Composable
fun TechExploreScreen(
    onNavigateToFeature: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "🚀 新技术探索",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            Text(
                text = "探索最新的 Android 开发技术和框架",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        items(techFeatures) { feature ->
            TechFeatureCard(
                feature = feature,
                onClick = { onNavigateToFeature(feature.route) }
            )
        }
    }
}

/**
 * 技术特性卡片组件
 */
@Composable
private fun TechFeatureCard(
    feature: TechFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = feature.icon,
                    style = MaterialTheme.typography.headlineLarge
                )
                
                if (feature.isNew) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "NEW",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
            )
            
            if (feature.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    feature.tags.take(3).forEach { tag ->
                        Surface(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 技术特性数据类
 */
data class TechFeature(
    val id: String,
    val route: String,
    val icon: String,
    val title: String,
    val description: String,
    val tags: List<String> = emptyList(),
    val isNew: Boolean = false
)

/**
 * 预定义的技术特性列表
 */
private val techFeatures = listOf(
    TechFeature(
        id = "animation",
        route = "animation_explore",
        icon = "🎭",
        title = "动画系统",
        description = "探索 Compose 动画 API，包括基础动画、过渡动画和手势动画",
        tags = listOf("Animation", "Transition", "Gesture"),
        isNew = true
    ),
    TechFeature(
        id = "performance",
        route = "performance_explore", 
        icon = "⚡",
        title = "性能优化",
        description = "学习 Compose 性能优化技巧，重组优化和渲染优化",
        tags = listOf("Performance", "Recomposition", "Optimization")
    ),
    TechFeature(
        id = "canvas",
        route = "canvas_explore",
        icon = "🎨", 
        title = "自定义绘制",
        description = "使用 Canvas API 进行自定义绘制，创建复杂的图形界面",
        tags = listOf("Canvas", "CustomDraw", "Graphics")
    ),
    TechFeature(
        id = "accessibility",
        route = "accessibility_explore",
        icon = "♿",
        title = "无障碍支持", 
        description = "实现无障碍功能，让应用更加友好和包容",
        tags = listOf("Accessibility", "A11y", "UX")
    ),
    TechFeature(
        id = "testing",
        route = "testing_explore",
        icon = "🧪",
        title = "UI 测试",
        description = "Compose UI 测试框架，编写可靠的界面测试",
        tags = listOf("Testing", "UI Test", "Quality"),
        isNew = true
    ),
    TechFeature(
        id = "multiplatform",
        route = "multiplatform_explore", 
        icon = "🌐",
        title = "多平台开发",
        description = "Compose Multiplatform 跨平台开发实践",
        tags = listOf("KMP", "Multiplatform", "Cross-platform"),
        isNew = true
    ),
    TechFeature(
        id = "material_you",
        route = "material_you_explore",
        icon = "🎨",
        title = "Material You",
        description = "Material You 设计系统和动态颜色主题",
        tags = listOf("Material You", "Design", "Theme")
    ),
    TechFeature(
        id = "paging",
        route = "paging_explore",
        icon = "📄",
        title = "分页加载",
        description = "Paging 3 库与 Compose 集成，实现高效的分页加载",
        tags = listOf("Paging", "LazyList", "Performance")
    )
)