package com.example.stockdemo.feature.home.presentation

import com.example.stockdemo.feature.auth.data.local.UserPreferences
import com.example.stockdemo.testutil.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val userPreferences: UserPreferences = mockk(relaxed = true)

    private fun createViewModel(): UserViewModel {
        every { userPreferences.userName } returns flowOf(null)
        every { userPreferences.userId } returns flowOf(null)
        every { userPreferences.languageCode } returns flowOf("vi")
        return UserViewModel(userPreferences)
    }

    @Test
    fun `exposes user flows from preferences`() = runTest {
        every { userPreferences.userName } returns flowOf("Alice")
        every { userPreferences.userId } returns flowOf(42)
        every { userPreferences.languageCode } returns flowOf("en")

        val viewModel = UserViewModel(userPreferences)

        assertEquals("Alice", viewModel.userName.first())
        assertEquals(42, viewModel.userId.first())
        assertEquals("en", viewModel.languageCode.first())
    }

    @Test
    fun `saveUser delegates to preferences`() = runTest {
        val viewModel = createViewModel()

        viewModel.saveUser("Bob", 7)

        advanceUntilIdle()

        coVerify(exactly = 1) { userPreferences.saveUser("Bob", 7) }
    }

    @Test
    fun `updateLanguage delegates to preferences`() = runTest {
        val viewModel = createViewModel()

        viewModel.updateLanguage("en")

        advanceUntilIdle()

        coVerify(exactly = 1) { userPreferences.saveLanguageCode("en") }
    }

    @Test
    fun `logout delegates to preferences clear`() = runTest {
        val viewModel = createViewModel()

        viewModel.logout()

        advanceUntilIdle()

        coVerify(exactly = 1) { userPreferences.clear() }
    }
}
