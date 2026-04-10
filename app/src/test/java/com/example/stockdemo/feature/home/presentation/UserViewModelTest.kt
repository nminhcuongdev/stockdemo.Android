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
    fun `save user update language and logout delegate to preferences`() = runTest {
        every { userPreferences.userName } returns flowOf(null)
        every { userPreferences.userId } returns flowOf(null)
        every { userPreferences.languageCode } returns flowOf("vi")

        val viewModel = UserViewModel(userPreferences)

        viewModel.saveUser("Bob", 7)
        viewModel.updateLanguage("en")
        viewModel.logout()

        advanceUntilIdle()

        coVerify { userPreferences.saveUser("Bob", 7) }
        coVerify { userPreferences.saveLanguageCode("en") }
        coVerify { userPreferences.clear() }
    }
}
