package com.example.satostockmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.example.satostockmanagement.navigation.NavigationGraph
import com.example.satostockmanagement.ui.theme.BackgroundColor

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WarehouseApp()
        }
    }
}

@Composable
fun WarehouseApp() {
    var darkMode by remember { mutableStateOf(false) }

    MaterialTheme(
        colorScheme = if (darkMode) darkColorScheme() else lightColorScheme()
    ) {
        Surface(
            color = if (darkMode) Color(0xFF121212) else BackgroundColor
        ) {
            NavigationGraph(
                darkMode = darkMode,
                onDarkModeChange = { darkMode = it }
            )
        }
    }
}
