package com.example.satostockmanagement.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.satostockmanagement.models.UserSettings
import com.example.satostockmanagement.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: UserSettings,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onSettingsChange: (UserSettings) -> Unit,
    onBack: () -> Unit
) {
    var language by remember { mutableStateOf(settings.language) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cài Đặt") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryColor,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SettingItem(
                icon = Icons.Default.DarkMode,
                title = "Chế độ tối",
                description = "Bật/tắt giao diện tối"
            ) {
                Switch(
                    checked = darkMode,
                    onCheckedChange = {
                        onDarkModeChange(it)
                        onSettingsChange(settings.copy(darkMode = it))
                    }
                )
            }

            HorizontalDivider()

            SettingItem(
                icon = Icons.Default.Language,
                title = "Ngôn ngữ",
                description = language,
                onClick = { showLanguageDialog = true }
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }

            HorizontalDivider()

            SettingItem(
                icon = Icons.Default.Info,
                title = "Thông tin ứng dụng",
                description = "Phiên bản 1.0.0"
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }

        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = { Text("Chọn ngôn ngữ") },
                text = {
                    Column {
                        LanguageOption("Tiếng Việt", language == "Tiếng Việt") {
                            language = "Tiếng Việt"
                            onSettingsChange(settings.copy(language = "Tiếng Việt"))
                            showLanguageDialog = false
                        }
                        LanguageOption("English", language == "English") {
                            language = "English"
                            onSettingsChange(settings.copy(language = "English"))
                            showLanguageDialog = false
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}

@Composable
fun LanguageOption(
    name: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = isSelected, onClick = onSelect)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = name, fontSize = 16.sp)
    }
}

@Composable
fun SettingItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: (() -> Unit)? = null,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = PrimaryColor
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        action()
    }
}