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
import com.thisfeng.composestudykit.ui.screen.network.RealApiScreen
import com.thisfeng.composestudykit.ui.screen.datastore.DataStoreComparisonScreen
import com.thisfeng.composestudykit.ui.screen.network.NoCacheApiScreen
import com.thisfeng.composestudykit.ui.screen.network.FileOperationScreen
import com.thisfeng.composestudykit.update.UpdateScreen
import com.thisfeng.composestudykit.ui.screen.home.SimpleHomeScreen
import com.thisfeng.composestudykit.ui.screen.test.CameraPermissionScreen
import com.thisfeng.composestudykit.ui.screen.test.TestExamplesScreen

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
                onNavigateToTestExamples = {
                    navController.navigate(NavRoutes.TEST_EXAMPLE)
                },
                onNavigateToDataStoreExample = {
                    navController.navigate(NavRoutes.DATASTORE_EXAMPLE)
                }
            )
        }

        // 网络请求示例页面（全屏）
        composable(NavRoutes.NETWORK_EXAMPLES) {
            NetworkExamplesScreen(
                onNavigateToRealApi = {
                    navController.navigate(NavRoutes.REAL_API_SCREEN)
                },
                onNavigateToDataStoreComparison = {
                    navController.navigate(NavRoutes.DATASTORE_COMPARISON_SCREEN)
                },
                onNavigateToNoCacheApi = {
                    navController.navigate(NavRoutes.NO_CACHE_API_SCREEN)
                },
                onNavigateToFileOperation = {
                    navController.navigate(NavRoutes.FILE_OPERATION_SCREEN)
                },
                onNavigateToUpdate = {
                    navController.navigate(NavRoutes.UPDATE_SCREEN)
                },
                onNavigateToSimpleHome = {
                    navController.navigate(NavRoutes.SIMPLE_HOME_SCREEN)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // 测试示例页面（预留）
        composable(NavRoutes.TEST_EXAMPLE) {
            TestExamplesScreen(
                onNavigateToPermission = {
                    navController.navigate(NavRoutes.TEST_PERMISSION)
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )

        }

        // 测试权限页面（预留）
        composable(NavRoutes.TEST_PERMISSION) {
            // TODO: 创建权限页面
            CameraPermissionScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }


        // 网络示例子页面
        composable(NavRoutes.REAL_API_SCREEN) {
            RealApiScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.DATASTORE_COMPARISON_SCREEN) {
            DataStoreComparisonScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.NO_CACHE_API_SCREEN) {
            NoCacheApiScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.FILE_OPERATION_SCREEN) {
            FileOperationScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.UPDATE_SCREEN) {
            UpdateScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(NavRoutes.SIMPLE_HOME_SCREEN) {
            SimpleHomeScreen(
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