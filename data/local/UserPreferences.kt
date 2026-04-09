package com.example.stockdemo.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferences @Inject constructor(private val context: Context) {

    private val USER_NAME_KEY = stringPreferencesKey("user_name")
    private val USER_ID_KEY = intPreferencesKey("user_id")

    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    val userId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    suspend fun saveUser(name: String, id: Int) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
            preferences[USER_ID_KEY] = id
        }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}