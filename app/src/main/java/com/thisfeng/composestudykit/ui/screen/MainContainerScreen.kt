package com.thisfeng.composestudykit.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.thisfeng.composestudykit.navigation.AppNavGraph

/**
 * 主容器页面
 * 简化为只包含全局导航图
 */
@Composable
fun MainContainerScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        AppNavGraph(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}