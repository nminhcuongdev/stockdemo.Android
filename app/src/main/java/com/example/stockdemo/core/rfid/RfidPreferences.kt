package com.example.stockdemo.core.rfid

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Separate DataStore from user_prefs so RFID reader tuning is isolated from the session/user data.
private val Context.rfidDataStore: DataStore<Preferences> by preferencesDataStore(name = "rfid_prefs")

/**
 * Default RFID reader settings the Inventory screen applies before each scan.
 * These were validated on the RS36 (E310 module): Session S1 + Dynamic Q + MultiTag lets the
 * reader keep finding new tags instead of stopping after the strongest couple. The user can
 * override them from the Settings screen (and power directly from the Inventory screen).
 */
@Singleton
class RfidPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val TX_POWER_KEY = intPreferencesKey("rfid_tx_power")
    private val SESSION_KEY = stringPreferencesKey("rfid_session")
    private val Q_DYNAMIC_KEY = booleanPreferencesKey("rfid_q_dynamic")
    private val WORK_MODE_KEY = stringPreferencesKey("rfid_work_mode")
    private val FILTER_DUPLICATE_KEY = booleanPreferencesKey("rfid_filter_duplicate")

    val txPower: Flow<Int> = context.rfidDataStore.data.map { it[TX_POWER_KEY] ?: DEFAULT_TX_POWER }
    val session: Flow<String> = context.rfidDataStore.data.map { it[SESSION_KEY] ?: DEFAULT_SESSION }
    val qDynamic: Flow<Boolean> = context.rfidDataStore.data.map { it[Q_DYNAMIC_KEY] ?: DEFAULT_Q_DYNAMIC }
    val workMode: Flow<String> = context.rfidDataStore.data.map { it[WORK_MODE_KEY] ?: DEFAULT_WORK_MODE }
    val filterDuplicate: Flow<Boolean> =
        context.rfidDataStore.data.map { it[FILTER_DUPLICATE_KEY] ?: DEFAULT_FILTER_DUPLICATE }

    suspend fun setTxPower(value: Int) {
        context.rfidDataStore.edit { it[TX_POWER_KEY] = value }
    }

    suspend fun setSession(value: String) {
        context.rfidDataStore.edit { it[SESSION_KEY] = value }
    }

    suspend fun setQDynamic(value: Boolean) {
        context.rfidDataStore.edit { it[Q_DYNAMIC_KEY] = value }
    }

    suspend fun setWorkMode(value: String) {
        context.rfidDataStore.edit { it[WORK_MODE_KEY] = value }
    }

    suspend fun setFilterDuplicate(value: Boolean) {
        context.rfidDataStore.edit { it[FILTER_DUPLICATE_KEY] = value }
    }

    companion object {
        const val DEFAULT_TX_POWER = 26
        const val DEFAULT_SESSION = "S1"
        const val DEFAULT_Q_DYNAMIC = true
        const val DEFAULT_WORK_MODE = "MultiTagMode"
        // Default ON: the reader filters duplicate reads so each tag is reported once.
        const val DEFAULT_FILTER_DUPLICATE = true

        // Selectable options exposed in the Settings screen.
        val SESSION_OPTIONS = listOf("S0", "S1", "S2", "S3")
        val WORK_MODE_OPTIONS = listOf("MultiTagMode", "ComprehensiveMode", "SingleTagMode")

        // Safe UHF power window (dBm) for the slider bounds when the module range is unknown.
        const val MIN_TX_POWER = 5
        const val MAX_TX_POWER = 30
    }
}
