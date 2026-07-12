package com.example.stockdemo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.stockdemo.app.navigation.AppDestination
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
                val appViewModel: AppViewModel = hiltViewModel()

                val languageCode = userViewModel.languageCode.collectAsStateWithLifecycle(initialValue = "vi")
                LaunchedEffect(languageCode.value) {
                    LanguageManager.applyLanguage(languageCode.value)
                }

                val startState by appViewModel.startState.collectAsStateWithLifecycle()

                when (startState) {
                    StartState.Loading -> SplashScreen()
                    else -> {
                        val navController = rememberNavController()
                        val startDestination = if (startState == StartState.LoggedIn) {
                            AppDestination.Home.route
                        } else {
                            AppDestination.Login.route
                        }

                        LaunchedEffect(Unit) {
                            appViewModel.sessionExpired.collect {
                                appViewModel.logout()
                                navController.navigate(AppDestination.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }

                        AppNavGraph(
                            navController = navController,
                            startDestination = startDestination,
                            onLogout = {
                                appViewModel.logout()
                                navController.navigate(AppDestination.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}
