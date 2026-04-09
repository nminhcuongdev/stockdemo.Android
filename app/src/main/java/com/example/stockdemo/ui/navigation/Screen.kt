package com.example.stockdemo.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Menu : Screen("menu")
    object Import : Screen("import")
    object Export : Screen("export")
    object Inventory : Screen("inventory")
    object ChatAI : Screen("chat_ai")
    object Settings : Screen("settings")
    object StockList : Screen("stock_list")
    object ImportHistory : Screen("import_history")
    object ExportHistory : Screen("export_history")
}