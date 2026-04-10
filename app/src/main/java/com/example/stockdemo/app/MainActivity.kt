package com.example.stockdemo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.stockdemo.app.navigation.AppNavGraph
import com.example.stockdemo.core.ui.theme.StockDemoTheme
import com.example.stockdemo.core.ui.util.LanguageManager
import com.example.stockdemo.feature.home.presentation.UserViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StockDemoTheme {
                val userViewModel: UserViewModel = hiltViewModel()
                val languageCode = userViewModel.languageCode.collectAsStateWithLifecycle(initialValue = "vi")

                LaunchedEffect(languageCode.value) {
                    LanguageManager.applyLanguage(languageCode.value)
                }

                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
