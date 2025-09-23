package com.thisfeng.composestudykit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.thisfeng.composestudykit.ui.screen.MainContainerScreen
import com.thisfeng.composestudykit.ui.theme.ComposeStudyKitTheme
import com.thisfeng.composestudykit.utils.GlobalDataStore
import com.thisfeng.composestudykit.utils.ConfigKeys
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MainApp()
        }
    }
}

@Composable
fun MainApp() {
    val systemIsDark = isSystemInDarkTheme()
    var themeMode by remember { mutableStateOf("auto") }
    var isDarkTheme by remember { mutableStateOf(false) }
    
    // 监听主题模式变化和系统主题变化
    LaunchedEffect(systemIsDark) {
        // 初始化主题模式
        themeMode = GlobalDataStore.getString(ConfigKeys.THEME_MODE, "auto")
        isDarkTheme = when (themeMode) {
            "dark" -> true
            "light" -> false
            else -> systemIsDark // auto mode
        }
        
        // 监听主题模式变化
        GlobalDataStore.getStringFlow(ConfigKeys.THEME_MODE, "auto").collect { newThemeMode ->
            themeMode = newThemeMode
            isDarkTheme = when (newThemeMode) {
                "dark" -> true
                "light" -> false
                else -> systemIsDark // auto mode
            }
        }
    }
    
    // 当系统主题变化时，如果使用自动模式则更新主题
    LaunchedEffect(systemIsDark, themeMode) {
        if (themeMode == "auto") {
            isDarkTheme = systemIsDark
        }
    }
    
    ComposeStudyKitTheme(
        darkTheme = isDarkTheme
    ) {
        MainContainerScreen()
    }
}