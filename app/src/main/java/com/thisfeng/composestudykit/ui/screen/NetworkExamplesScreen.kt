package com.thisfeng.composestudykit.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * 网络请求案例导航界面
 * 包含所有网络相关的测试案例
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkExamplesScreen(
    onBackClick: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    
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
        
        // Tab 选择器
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { 
                    Text(
                        "真实API",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    ) 
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { 
                    Text(
                        "DataStore",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    ) 
                }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { 
                    Text(
                        "并发测试",
                        fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                    ) 
                }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = { 
                    Text(
                        "基础框架",
                        fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                    ) 
                }
            )
        }
        
        // 内容区域
        Box(modifier = Modifier.fillMaxSize()) {
            when (selectedTab) {
                0 -> RealApiScreen()
                1 -> DataStoreComparisonScreen()
                2 -> NoCacheApiScreen()
                3 -> SimpleHomeScreen()
            }
        }
    }
}