package com.thisfeng.composestudykit.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thisfeng.composestudykit.ui.viewmodel.GlobalDataStoreViewModel

/**
 * 全局 DataStore 工具类演示界面
 * 展示 GlobalDataStore 的各种使用方式
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalDataStoreScreen(
    onBackClick: (() -> Unit)? = null
) {
    val viewModel: GlobalDataStoreViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 顶部标题栏
        if (onBackClick != null) {
            TopAppBar(
                title = { 
                    Text(
                        text = "🗄️ DataStore 工具演示",
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
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
        // 标题
        Text(
            text = "🗄️ 全局 DataStore 工具演示",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "简便易懂的全局 Key-Value 配置管理",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 用户配置区域
        UserConfigCard(
            userName = uiState.userName,
            userId = uiState.userId,
            isLogin = uiState.isLogin,
            onUserNameChange = viewModel::updateUserName,
            onUserIdChange = viewModel::updateUserId,
            onLoginToggle = viewModel::toggleLogin
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 应用设置区域
        AppSettingsCard(
            themeMode = uiState.themeMode,
            language = uiState.language,
            enableNotifications = uiState.enableNotifications,
            onThemeModeChange = viewModel::updateThemeMode,
            onLanguageChange = viewModel::updateLanguage,
            onNotificationsToggle = viewModel::toggleNotifications
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 统计信息
        StatisticsCard(
            totalKeys = uiState.totalKeys,
            cacheSize = uiState.cacheSize,
            onRefreshStats = viewModel::refreshStatistics
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 操作按钮
        OperationButtonsCard(
            onLoadDefaults = viewModel::loadDefaultConfigs,
            onClearAll = viewModel::clearAllData,
            onExportData = viewModel::exportData
        )

        Spacer(modifier = Modifier.height(16.dp))

            // 使用说明
            UsageInstructionsCard()
        }
    }
}

@Composable
private fun UserConfigCard(
    userName: String,
    userId: Int,
    isLogin: Boolean,
    onUserNameChange: (String) -> Unit,
    onUserIdChange: (Int) -> Unit,
    onLoginToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "👤 用户配置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 用户名输入
            var tempUserName by remember(userName) { mutableStateOf(userName) }
            OutlinedTextField(
                value = tempUserName,
                onValueChange = { tempUserName = it },
                label = { Text("用户名") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            Button(
                onClick = { onUserNameChange(tempUserName) },
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text("保存用户名")
            }

            // 用户ID输入
            var tempUserId by remember(userId) { mutableStateOf(userId.toString()) }
            OutlinedTextField(
                value = tempUserId,
                onValueChange = { tempUserId = it },
                label = { Text("用户ID") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
            Button(
                onClick = { 
                    tempUserId.toIntOrNull()?.let { onUserIdChange(it) }
                },
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text("保存用户ID")
            }

            // 登录状态开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("登录状态")
                Switch(
                    checked = isLogin,
                    onCheckedChange = { onLoginToggle() }
                )
            }

            // 当前值显示
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "当前值: 用户名=$userName, ID=$userId, 登录=${if (isLogin) "是" else "否"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun AppSettingsCard(
    themeMode: String,
    language: String,
    enableNotifications: Boolean,
    onThemeModeChange: (String) -> Unit,
    onLanguageChange: (String) -> Unit,
    onNotificationsToggle: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "⚙️ 应用设置",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // 主题模式选择
            Text("主题模式", style = MaterialTheme.typography.bodyMedium)
            Row {
                listOf("light" to "浅色", "dark" to "深色", "auto" to "自动").forEach { (value, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = themeMode == value,
                            onClick = { onThemeModeChange(value) }
                        )
                        Text(label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 语言选择
            Text("语言设置", style = MaterialTheme.typography.bodyMedium)
            Row {
                listOf("zh" to "中文", "en" to "English").forEach { (value, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = language == value,
                            onClick = { onLanguageChange(value) }
                        )
                        Text(label)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 通知开关
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("启用通知")
                Switch(
                    checked = enableNotifications,
                    onCheckedChange = { onNotificationsToggle() }
                )
            }
        }
    }
}

@Composable
private fun StatisticsCard(
    totalKeys: Int,
    cacheSize: Int,
    onRefreshStats: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📊 存储统计",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onRefreshStats) {
                    Text("刷新")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("存储键数量:")
                Text("$totalKeys 个")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("数据大小:")
                Text("${cacheSize}KB")
            }
        }
    }
}

@Composable
private fun OperationButtonsCard(
    onLoadDefaults: () -> Unit,
    onClearAll: () -> Unit,
    onExportData: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🔧 操作按钮",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLoadDefaults,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("加载默认")
                }

                OutlinedButton(
                    onClick = onExportData,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("导出数据")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onClearAll,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("清空所有数据")
            }
        }
    }
}

@Composable
private fun UsageInstructionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "📖 使用说明",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val instructions = listOf(
                "• 支持 String、Int、Boolean、Float、Long 五种基本类型",
                "• 提供同步和异步（Flow）两种读取方式",
                "• 全局单例，无需手动传入 Context",
                "• 类型安全，自动处理序列化/反序列化",
                "• 支持批量操作和默认值设置",
                "• 响应式数据流，数据变化时自动更新 UI"
            )

            instructions.forEach { instruction ->
                Text(
                    text = instruction,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "示例代码：",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "GlobalDataStore.putString(\"key\", \"value\")\nval value = GlobalDataStore.getString(\"key\")",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}