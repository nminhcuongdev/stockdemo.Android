package com.example.stockdemo.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.stockdemo.feature.auth.presentation.login.LoginScreen
import com.example.stockdemo.feature.auth.presentation.login.LoginViewModel
import com.example.stockdemo.feature.home.presentation.MainScaffold
import com.example.stockdemo.feature.home.presentation.UserViewModel
import com.example.stockdemo.feature.stock.presentation.ExportHistoryScreen
import com.example.stockdemo.feature.stock.presentation.ExportHistoryViewModel
import com.example.stockdemo.feature.stock.presentation.ExportScreen
import com.example.stockdemo.feature.stock.presentation.ImportHistoryScreen
import com.example.stockdemo.feature.stock.presentation.ImportHistoryViewModel
import com.example.stockdemo.feature.stock.presentation.ImportScreen
import com.example.stockdemo.feature.stock.presentation.StockViewModel
import com.example.stockdemo.feature.stock.presentation.transfer.TransferScreen
import com.example.stockdemo.feature.stock.presentation.transfer.TransferViewModel
import com.example.stockdemo.feature.stock.presentation.stocktake.StocktakeScreen
import com.example.stockdemo.feature.stock.presentation.stocktake.StocktakeViewModel
import com.example.stockdemo.feature.stock.presentation.report.ReportScreen
import com.example.stockdemo.feature.stock.presentation.report.ReportViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(AppDestination.Login.route) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            val userViewModel: UserViewModel = hiltViewModel()
            LoginScreen(
                viewModel = loginViewModel,
                userViewModel = userViewModel,
                onLoginSuccess = {
                    navController.navigate(AppDestination.Home.route) {
                        popUpTo(AppDestination.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestination.Home.route) {
            MainScaffold(
                onNavigateToImport = { navController.navigate(AppDestination.Import.route) },
                onNavigateToExport = { navController.navigate(AppDestination.Export.route) },
                onNavigateToTransfer = { navController.navigate(AppDestination.Transfer.route) },
                onNavigateToStocktake = { navController.navigate(AppDestination.Stocktake.route) },
                onNavigateToReport = { navController.navigate(AppDestination.Report.route) },
                onNavigateToImportHistory = { navController.navigate(AppDestination.ImportHistory.route) },
                onNavigateToExportHistory = { navController.navigate(AppDestination.ExportHistory.route) },
                onLogout = onLogout
            )
        }

        composable(AppDestination.Transfer.route) {
            val transferViewModel: TransferViewModel = hiltViewModel()
            TransferScreen(
                viewModel = transferViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppDestination.Stocktake.route) {
            val stocktakeViewModel: StocktakeViewModel = hiltViewModel()
            StocktakeScreen(
                viewModel = stocktakeViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppDestination.Report.route) {
            val reportViewModel: ReportViewModel = hiltViewModel()
            ReportScreen(
                viewModel = reportViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppDestination.Import.route) {
            val stockViewModel: StockViewModel = hiltViewModel()
            val userViewModel: UserViewModel = hiltViewModel()
            ImportScreen(
                viewModel = stockViewModel,
                userViewModel = userViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        @Suppress("DEPRECATION")
        composable(AppDestination.Export.route) {
            val stockViewModel: StockViewModel = hiltViewModel()
            val userViewModel: UserViewModel = hiltViewModel()
            ExportScreen(
                viewModel = stockViewModel,
                userViewModel = userViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppDestination.ImportHistory.route) {
            val viewModel: ImportHistoryViewModel = hiltViewModel()
            ImportHistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(AppDestination.ExportHistory.route) {
            val viewModel: ExportHistoryViewModel = hiltViewModel()
            ExportHistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
