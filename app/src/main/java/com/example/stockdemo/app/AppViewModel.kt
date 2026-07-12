package com.example.stockdemo.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdemo.core.session.SessionManager
import com.example.stockdemo.feature.auth.data.local.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Where the app should start once the persisted session has been resolved. */
enum class StartState { Loading, LoggedIn, LoggedOut }

@HiltViewModel
class AppViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    sessionManager: SessionManager
) : ViewModel() {

    /** Emits when the backend rejected the session (401) and the user must re-login. */
    val sessionExpired: SharedFlow<Unit> = sessionManager.sessionExpired

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
}
