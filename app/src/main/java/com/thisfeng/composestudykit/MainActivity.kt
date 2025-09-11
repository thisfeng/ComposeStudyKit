package com.thisfeng.composestudykit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.thisfeng.composestudykit.ui.screen.GlobalDataStoreScreen
import com.thisfeng.composestudykit.ui.screen.HomeScreen
import com.thisfeng.composestudykit.ui.screen.NetworkExamplesScreen
import com.thisfeng.composestudykit.ui.theme.ComposeStudyKitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComposeStudyKitTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    var currentScreen by remember { mutableStateOf("home") }

    when (currentScreen) {
        "home" -> {
            HomeScreen(
                onNavigateToNetworkExamples = {
                    currentScreen = "network_examples"
                },
                onNavigateToDataStoreExample = {
                    currentScreen = "datastore_example"
                }
            )
        }

        "network_examples" -> {
            NetworkExamplesScreen(
                onBackClick = {
                    currentScreen = "home"
                }
            )
        }

        "datastore_example" -> {
            GlobalDataStoreScreen(
                onBackClick = {
                    currentScreen = "home"
                }
            )
        }
    }
}