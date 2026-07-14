package com.stockdemo.warehouse.core.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-wide session bus. Emits an event whenever the current session becomes
 * invalid (e.g. the server rejects the access token with a 401) so that the UI
 * layer can clear local credentials and route the user back to the login screen.
 */
@Singleton
class SessionManager @Inject constructor() {

    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired: SharedFlow<Unit> = _sessionExpired.asSharedFlow()

    fun notifySessionExpired() {
        _sessionExpired.tryEmit(Unit)
    }
}
