package com.stockdemo.warehouse.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stockdemo.warehouse.R
import com.stockdemo.warehouse.core.rfid.RfidPreferences
import com.stockdemo.warehouse.core.ui.theme.OrangeColor
import com.stockdemo.warehouse.feature.stock.presentation.RfidSettingsViewModel

@Composable
fun SettingsScreen(
    userViewModel: UserViewModel,
    onBack: () -> Unit,
    showBackButton: Boolean = true,
    rfidViewModel: RfidSettingsViewModel = hiltViewModel()
) {
    val languageCode by userViewModel.languageCode.collectAsStateWithLifecycle(initialValue = "vi")
    val txPower by rfidViewModel.txPower.collectAsStateWithLifecycle()
    val session by rfidViewModel.session.collectAsStateWithLifecycle()
    val qDynamic by rfidViewModel.qDynamic.collectAsStateWithLifecycle()
    val workMode by rfidViewModel.workMode.collectAsStateWithLifecycle()
    val filterDuplicate by rfidViewModel.filterDuplicate.collectAsStateWithLifecycle()

    SettingsContent(
        selectedLanguageCode = languageCode,
        onLanguageSelected = userViewModel::updateLanguage,
        txPower = txPower,
        onTxPowerChange = rfidViewModel::setTxPower,
        session = session,
        onSessionChange = rfidViewModel::setSession,
        qDynamic = qDynamic,
        onQDynamicChange = rfidViewModel::setQDynamic,
        workMode = workMode,
        onWorkModeChange = rfidViewModel::setWorkMode,
        filterDuplicate = filterDuplicate,
        onFilterDuplicateChange = rfidViewModel::setFilterDuplicate,
        onBack = onBack,
        showBackButton = showBackButton
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    selectedLanguageCode: String,
    onLanguageSelected: (String) -> Unit,
    txPower: Int,
    onTxPowerChange: (Int) -> Unit,
    session: String,
    onSessionChange: (String) -> Unit,
    qDynamic: Boolean,
    onQDynamicChange: (Boolean) -> Unit,
    workMode: String,
    onWorkModeChange: (String) -> Unit,
    filterDuplicate: Boolean,
    onFilterDuplicateChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    showBackButton: Boolean = true
) {
    val languageOptions = listOf(
        LanguageOption(code = "vi", label = stringResource(R.string.language_vietnamese)),
        LanguageOption(code = "en", label = stringResource(R.string.language_english))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OrangeColor,
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.settings_language_section_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.settings_language_section_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            languageOptions.forEach { option ->
                val hintRes = if (option.code == "vi") {
                    R.string.settings_language_vietnamese_hint
                } else {
                    R.string.settings_language_english_hint
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .selectable(
                            selected = selectedLanguageCode == option.code,
                            onClick = { onLanguageSelected(option.code) },
                            role = Role.RadioButton
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = option.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(hintRes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        RadioButton(
                            selected = selectedLanguageCode == option.code,
                            onClick = null
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            RfidSettingsSection(
                txPower = txPower,
                onTxPowerChange = onTxPowerChange,
                session = session,
                onSessionChange = onSessionChange,
                qDynamic = qDynamic,
                onQDynamicChange = onQDynamicChange,
                workMode = workMode,
                onWorkModeChange = onWorkModeChange,
                filterDuplicate = filterDuplicate,
                onFilterDuplicateChange = onFilterDuplicateChange
            )
        }
    }
}

@Composable
private fun RfidSettingsSection(
    txPower: Int,
    onTxPowerChange: (Int) -> Unit,
    session: String,
    onSessionChange: (String) -> Unit,
    qDynamic: Boolean,
    onQDynamicChange: (Boolean) -> Unit,
    workMode: String,
    onWorkModeChange: (String) -> Unit,
    filterDuplicate: Boolean,
    onFilterDuplicateChange: (Boolean) -> Unit
) {
    Text(
        text = stringResource(R.string.rfid_settings_section_title),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.rfid_settings_section_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(16.dp))

    // Power
    var powerSlider by remember(txPower) { mutableFloatStateOf(txPower.toFloat()) }
    Text(
        text = stringResource(R.string.rfid_settings_power, powerSlider.toInt()),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Medium
    )
    Slider(
        value = powerSlider,
        onValueChange = { powerSlider = it },
        onValueChangeFinished = { onTxPowerChange(powerSlider.toInt()) },
        valueRange = RfidPreferences.MIN_TX_POWER.toFloat()..RfidPreferences.MAX_TX_POWER.toFloat(),
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Session
    Text(
        text = stringResource(R.string.rfid_settings_session),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Medium
    )
    Text(
        text = stringResource(R.string.rfid_settings_session_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    OptionRadioRow(
        options = RfidPreferences.SESSION_OPTIONS,
        selected = session,
        onSelected = onSessionChange
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Work mode
    Text(
        text = stringResource(R.string.rfid_settings_workmode),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Medium
    )
    Text(
        text = stringResource(R.string.rfid_settings_workmode_hint),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    Column {
        RfidPreferences.WORK_MODE_OPTIONS.forEach { mode ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = workMode == mode,
                        onClick = { onWorkModeChange(mode) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = workMode == mode, onClick = null)
                Spacer(modifier = Modifier.height(0.dp))
                Text(text = mode, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Dynamic Q
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.rfid_settings_q_dynamic),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(checked = qDynamic, onCheckedChange = onQDynamicChange)
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Duplicate filter
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.rfid_settings_filter_duplicate),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = stringResource(R.string.rfid_settings_filter_duplicate_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = filterDuplicate, onCheckedChange = onFilterDuplicateChange)
    }
}

@Composable
private fun OptionRadioRow(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .selectable(
                        selected = selected == option,
                        onClick = { onSelected(option) },
                        role = Role.RadioButton
                    )
                    .padding(end = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selected == option, onClick = null)
                Text(text = option, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

private data class LanguageOption(
    val code: String,
    val label: String
)
