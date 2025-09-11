package com.thisfeng.composestudykit.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 设定页面
 * 包含用户头像、基本信息和常用设置选项
 */
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isDarkMode by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var autoSync by remember { mutableStateOf(true) }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 用户信息区域
        item {
            UserProfileSection()
        }
        
        // 系统设置区域
        item {
            SettingsSection(
                title = "系统设置",
                items = listOf(
                    SettingsItem.Switch(
                        icon = Icons.Outlined.DarkMode,
                        title = "深色模式",
                        subtitle = "切换应用主题",
                        checked = isDarkMode,
                        onCheckedChange = { isDarkMode = it }
                    ),
                    SettingsItem.Switch(
                        icon = Icons.Default.Notifications,
                        title = "通知推送",
                        subtitle = "接收应用通知",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    ),
                    SettingsItem.Switch(
                        icon = Icons.Outlined.Sync,
                        title = "自动同步",
                        subtitle = "后台自动同步数据",
                        checked = autoSync,
                        onCheckedChange = { autoSync = it }
                    )
                )
            )
        }
        
        // 数据设置区域
        item {
            SettingsSection(
                title = "数据设置",
                items = listOf(
                    SettingsItem.Action(
                        icon = Icons.Outlined.Storage,
                        title = "缓存管理",
                        subtitle = "清理应用缓存数据",
                        onClick = { /* TODO: 实现缓存清理 */ }
                    ),
                    SettingsItem.Action(
                        icon = Icons.Outlined.FileUpload,
                        title = "数据导出",
                        subtitle = "导出本地数据",
                        onClick = { /* TODO: 实现数据导出 */ }
                    ),
                    SettingsItem.Action(
                        icon = Icons.Outlined.Backup,
                        title = "数据备份",
                        subtitle = "备份重要数据",
                        onClick = { /* TODO: 实现数据备份 */ }
                    )
                )
            )
        }
        
        // 关于设置区域
        item {
            SettingsSection(
                title = "关于",
                items = listOf(
                    SettingsItem.Action(
                        icon = Icons.Default.Info,
                        title = "应用版本",
                        subtitle = "1.0.0",
                        onClick = { /* TODO: 显示版本信息 */ }
                    ),
                    SettingsItem.Action(
                        icon = Icons.Outlined.HelpOutline,
                        title = "帮助反馈",
                        subtitle = "使用帮助和问题反馈",
                        onClick = { /* TODO: 打开帮助页面 */ }
                    ),
                    SettingsItem.Action(
                        icon = Icons.Outlined.Security,
                        title = "隐私政策",
                        subtitle = "查看隐私保护政策",
                        onClick = { /* TODO: 打开隐私政策 */ }
                    )
                )
            )
        }
    }
}

/**
 * 用户信息展示组件
 */
@Composable
private fun UserProfileSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 头像
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👤",
                    fontSize = 40.sp,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 用户名
            Text(
                text = "Compose 学习者",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // 用户描述
            Text(
                text = "探索 Jetpack Compose 的精彩世界",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * 设置分组组件
 */
@Composable
private fun SettingsSection(
    title: String,
    items: List<SettingsItem>
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            items.forEachIndexed { index, item ->
                when (item) {
                    is SettingsItem.Switch -> {
                        SettingsItemRow(
                            icon = item.icon,
                            title = item.title,
                            subtitle = item.subtitle,
                            trailingContent = {
                                Switch(
                                    checked = item.checked,
                                    onCheckedChange = item.onCheckedChange
                                )
                            }
                        )
                    }
                    is SettingsItem.Action -> {
                        SettingsItemRow(
                            icon = item.icon,
                            title = item.title,
                            subtitle = item.subtitle,
                            onClick = item.onClick,
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        )
                    }
                }
                
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

/**
 * 设置项行组件
 */
@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        trailingContent()
    }
}

/**
 * 设置项数据类
 */
sealed class SettingsItem {
    data class Switch(
        val icon: ImageVector,
        val title: String,
        val subtitle: String,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit
    ) : SettingsItem()
    
    data class Action(
        val icon: ImageVector,
        val title: String,
        val subtitle: String,
        val onClick: () -> Unit
    ) : SettingsItem()
}