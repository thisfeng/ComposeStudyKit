package com.thisfeng.composestudykit.navigation

/**
 * 导航路由定义
 * 集中管理所有页面的路由名称和参数
 */
object NavRoutes {
    
    // 主容器路由（包含底部导航栏）
    const val MAIN_ROUTE = "main"
    
    // 主要页面路由（在底部导航栏内切换）
    const val HOME = "home"
    const val TECH_EXPLORE = "tech_explore"
    const val LAB_EXPLORE = "lab_explore"
    const val SETTINGS = "settings"
    
    // 全屏子页面路由（独立页面，无底部导航栏）
    const val NETWORK_EXAMPLES = "network_examples"
    const val DATASTORE_EXAMPLE = "datastore_example"
    
    // 网络示例子页面路由
    const val REAL_API_SCREEN = "real_api_screen"
    const val DATASTORE_COMPARISON_SCREEN = "datastore_comparison_screen"
    const val NO_CACHE_API_SCREEN = "no_cache_api_screen"
    const val FILE_OPERATION_SCREEN = "file_operation_screen"
    const val UPDATE_SCREEN = "update_screen"
    const val SIMPLE_HOME_SCREEN = "simple_home_screen"
    
    // 带参数的路由示例
    object NetworkDetail {
        const val ROUTE = "network_detail"
        const val ROUTE_WITH_ARGS = "network_detail/{type}"
        
        fun createRoute(type: String) = "network_detail/$type"
    }
}

/**
 * 底部导航栏选项定义
 */
enum class BottomNavItem(
    val route: String,
    val title: String,
    val icon: String // 这里使用 emoji，也可以改为使用 Icon
) {
    HOME(NavRoutes.HOME, "首页", "🏠"),
    TECH_EXPLORE(NavRoutes.TECH_EXPLORE, "技术", "🚀"),
    LAB_EXPLORE(NavRoutes.LAB_EXPLORE, "实验室", "🧪"),
    SETTINGS(NavRoutes.SETTINGS, "设定", "⚙️")
}