package com.thisfeng.composestudykit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.thisfeng.composestudykit.ui.screen.MainContainerScreen
import com.thisfeng.composestudykit.ui.theme.ComposeStudyKitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeStudyKitTheme {
                MainContainerScreen()
            }
        }
    }
}