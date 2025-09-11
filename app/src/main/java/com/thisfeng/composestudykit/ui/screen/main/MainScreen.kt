package com.thisfeng.composestudykit.ui.screen.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.thisfeng.composestudykit.navigation.BottomNavItem
import com.thisfeng.composestudykit.navigation.NavRoutes
import com.thisfeng.composestudykit.ui.components.BottomNavigationBar
import com.thisfeng.composestudykit.ui.screen.home.HomeScreen
import com.thisfeng.composestudykit.ui.screen.explore.TechExploreScreen
import com.thisfeng.composestudykit.ui.screen.explore.LabExploreScreen
import com.thisfeng.composestudykit.ui.screen.settings.SettingsScreen

/**
 * 主页面容器
 * 包含底部导航栏和主要页面的 NavHost
 */
@Composable
fun MainScreen(
    onNavigateToNetworkExamples: () -> Unit,
    onNavigateToDataStoreExample: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            MainBottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        MainNavHost(
            navController = navController,
            onNavigateToNetworkExamples = onNavigateToNetworkExamples,
            onNavigateToDataStoreExample = onNavigateToDataStoreExample,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

/**
 * 主页面导航图
 * 只包含底部导航栏中的页面
 */
@Composable
private fun MainNavHost(
    navController: NavHostController,
    onNavigateToNetworkExamples: () -> Unit,
    onNavigateToDataStoreExample: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME,
        modifier = modifier
    ) {
        // 首页
        composable(NavRoutes.HOME) {
            HomeScreen(
                onNavigateToNetworkExamples = onNavigateToNetworkExamples,
                onNavigateToDataStoreExample = onNavigateToDataStoreExample
            )
        }
        
        // 新技术探索页面
        composable(NavRoutes.TECH_EXPLORE) {
            TechExploreScreen(
                onNavigateToFeature = { route ->
                    // TODO: 实现具体特性页面导航
                }
            )
        }
        
        // 实验室页面
        composable(NavRoutes.LAB_EXPLORE) {
            LabExploreScreen(
                onNavigateToFeature = { route ->
                    // TODO: 实现具体特性页面导航
                }
            )
        }
        
        // 设定页面
        composable(NavRoutes.SETTINGS) {
            SettingsScreen()
        }
    }
}

/**
 * 主页面底部导航栏
 * 只在主页面之间切换
 */
@Composable
private fun MainBottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    NavigationBar(modifier = modifier) {
        BottomNavItem.values().forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { 
                it.route == item.route 
            } == true
            
            NavigationBarItem(
                icon = {
                    Text(
                        text = item.icon,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        // 避免重复的目标在后退堆栈中堆积
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // 避免点击同一选项卡时创建多个副本
                        launchSingleTop = true
                        // 重新选择之前选择的项目时恢复状态
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}