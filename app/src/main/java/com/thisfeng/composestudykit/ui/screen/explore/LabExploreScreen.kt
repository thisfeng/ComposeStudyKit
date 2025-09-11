package com.thisfeng.composestudykit.ui.screen.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 实验室页面
 * 用于展示实验性功能和新特性
 */
@Composable
fun LabExploreScreen(
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
                text = "🧪 实验室",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            Text(
                text = "探索实验性功能和前沿技术",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        
        items(labFeatures) { feature ->
            ExploreButton(
                title = feature.title,
                description = feature.description,
                icon = feature.icon,
                onClick = { onNavigateToFeature(feature.route) }
            )
        }
    }
}

/**
 * 探索按钮组件
 */
@Composable
fun ExploreButton(
    title: String,
    description: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = icon,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(end = 16.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 实验室特性数据类
 */
data class LabFeature(
    val id: String,
    val route: String,
    val icon: String,
    val title: String,
    val description: String
)

/**
 * 预定义的实验室特性列表
 */
private val labFeatures = listOf(
    LabFeature(
        id = "ai_integration",
        route = "ai_integration",
        icon = "🤖",
        title = "AI 集成",
        description = "探索 AI 功能集成和机器学习"
    ),
    LabFeature(
        id = "ar_vr",
        route = "ar_vr",
        icon = "🥽",
        title = "AR/VR 体验",
        description = "增强现实和虚拟现实技术"
    ),
    LabFeature(
        id = "blockchain",
        route = "blockchain",
        icon = "⛓️",
        title = "区块链技术",
        description = "Web3 和去中心化应用开发"
    ),
    LabFeature(
        id = "iot_connect",
        route = "iot_connect",
        icon = "📡",
        title = "IoT 连接",
        description = "物联网设备连接和控制"
    ),
    LabFeature(
        id = "voice_control",
        route = "voice_control",
        icon = "🎤",
        title = "语音控制",
        description = "语音识别和语音交互"
    ),
    LabFeature(
        id = "gesture_recognition",
        route = "gesture_recognition",
        icon = "✋",
        title = "手势识别",
        description = "计算机视觉和手势控制"
    )
)