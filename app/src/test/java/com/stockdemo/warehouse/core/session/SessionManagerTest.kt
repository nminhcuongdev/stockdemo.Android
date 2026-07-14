package com.stockdemo.warehouse.core.session

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionManagerTest {

    @Test
    fun `notifySessionExpired emits on the shared flow`() = runTest {
        val manager = SessionManager()
        val collected = mutableListOf<Unit>()

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            manager.sessionExpired.collect { collected.add(it) }
        }

        manager.notifySessionExpired()

        assertEquals(1, collected.size)
        job.cancel()
    }

    @Test
    fun `collects two consecutive expiry events`() = runTest {
        val manager = SessionManager()

        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            val events = manager.sessionExpired.take(2).toList()
            assertEquals(2, events.size)
        }

        manager.notifySessionExpired()
        manager.notifySessionExpired()

        job.join()
    }
}
