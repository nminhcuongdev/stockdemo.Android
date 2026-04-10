package com.example.stockdemo.app.navigation

sealed class AppDestination(val route: String) {
    data object Login : AppDestination("login")
    data object Menu : AppDestination("menu")
    data object Import : AppDestination("import")
    data object Export : AppDestination("export")
    data object Inventory : AppDestination("inventory")
    data object ChatAI : AppDestination("chat_ai")
    data object Settings : AppDestination("settings")
    data object StockList : AppDestination("stock_list")
    data object ImportHistory : AppDestination("import_history")
    data object ExportHistory : AppDestination("export_history")
}


