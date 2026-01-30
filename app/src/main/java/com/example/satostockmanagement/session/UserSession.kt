package com.example.satostockmanagement.session

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Khởi tạo thuộc tính mở rộng cho Context để dùng DataStore
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

class UserSession(private val context: Context) {

    companion object {
        // Định nghĩa các Key để lưu trữ
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val IS_LOGGED_IN_KEY = booleanPreferencesKey("is_logged_in")
    }

    // Lấy dữ liệu dưới dạng Flow
    // Flow sẽ tự động phát ra giá trị mới mỗi khi dữ liệu trong file thay đổi
    val userName: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_NAME_KEY] ?: "Khách"
        }

    val userId: Flow<Int?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID_KEY] ?: 1
        }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_LOGGED_IN_KEY] ?: false
        }

    // Hàm lưu dữ liệu (phải là suspend function vì DataStore ghi file bất đồng bộ)
    suspend fun saveSession(name: String, id: Int) {
        context.dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
            preferences[USER_ID_KEY] = id
            preferences[IS_LOGGED_IN_KEY] = true
        }
    }

    // Hàm xóa dữ liệu (Đăng xuất)
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}