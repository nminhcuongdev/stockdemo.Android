package com.example.stockdemo.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdemo.core.notification.NotificationTokenManager
import com.example.stockdemo.core.session.SessionManager
import com.example.stockdemo.feature.auth.data.local.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Where the app should start once the persisted session has been resolved. */
enum class StartState { Loading, LoggedIn, LoggedOut }

@HiltViewModel
class AppViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val notificationTokenManager: NotificationTokenManager,
    sessionManager: SessionManager
) : ViewModel() {

    /** Emits when the backend rejected the session (401) and the user must re-login. */
    val sessionExpired: SharedFlow<Unit> = sessionManager.sessionExpired

    /** True while a user is logged in; used to (re)register the FCM token on login. */
    val isLoggedIn: StateFlow<Boolean> = userPreferences.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private val _startState = MutableStateFlow(StartState.Loading)
    val startState: StateFlow<StartState> = _startState.asStateFlow()

    init {
        viewModelScope.launch {
            _startState.value = if (userPreferences.isLoggedIn.first()) {
                StartState.LoggedIn
            } else {
                StartState.LoggedOut
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferences.clear()
        }
    }

    /** Registers this device's FCM token with the backend (call once logged in). */
    fun registerPushToken() {
        viewModelScope.launch {
            notificationTokenManager.registerCurrentToken()
        }
    }
}
