package com.thisfeng.composestudykit.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thisfeng.composestudykit.ui.screen.main.MainScreen
import com.thisfeng.composestudykit.ui.screen.network.NetworkExamplesScreen
import com.thisfeng.composestudykit.ui.screen.datastore.GlobalDataStoreScreen

/**
 * 应用全局导航图
 * 管理主页面和全屏子页面之间的导航
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoutes.MAIN_ROUTE,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // 主页面容器（包含底部导航栏）
        composable(NavRoutes.MAIN_ROUTE) {
            MainScreen(
                onNavigateToNetworkExamples = {
                    navController.navigate(NavRoutes.NETWORK_EXAMPLES)
                },
                onNavigateToDataStoreExample = {
                    navController.navigate(NavRoutes.DATASTORE_EXAMPLE)
                }
            )
        }
        
        // 网络请求示例页面（全屏）
        composable(NavRoutes.NETWORK_EXAMPLES) {
            NetworkExamplesScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // DataStore 示例页面（全屏）
        composable(NavRoutes.DATASTORE_EXAMPLE) {
            GlobalDataStoreScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // 带参数的路由示例（预留）
        composable(NavRoutes.NetworkDetail.ROUTE_WITH_ARGS) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: ""
            // TODO: 创建详情页面
            // NetworkDetailScreen(type = type, onBackClick = { navController.popBackStack() })
        }
    }
}