package com.example.stockdemo.feature.auth.presentation.login

import android.content.Context
import com.example.stockdemo.R
import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.feature.auth.domain.model.User
import com.example.stockdemo.feature.auth.domain.usecase.LoginUseCase
import com.example.stockdemo.testutil.MainDispatcherRule
import com.example.stockdemo.testutil.mockContext
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val loginUseCase: LoginUseCase = mockk()
    private val context: Context = mockContext(
        mapOf(
            R.string.login_user_data_null to "User data is null",
            R.string.login_failed to "Login failed"
        )
    )

    @Test
    fun `login success updates ui state`() = runTest {
        val user = User(
            userId = 1,
            username = "cuongboyhc",
            fullName = "Nguyen Minh Cuong",
            role = "admin",
            isActive = true
        )
        every { loginUseCase(any()) } returns flowOf(Resource.Success(user))

        val viewModel = LoginViewModel(loginUseCase, context)
        viewModel.login("cuongboyhc", "123456")

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Success)
        assertEquals(user, (state as LoginUiState.Success).user)
    }

    @Test
    fun `login success with null user emits error`() = runTest {
        every { loginUseCase(any()) } returns flowOf(Resource.Success(null))

        val viewModel = LoginViewModel(loginUseCase, context)
        viewModel.login("john", "secret")

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is LoginUiState.Error)
        assertEquals("User data is null", (state as LoginUiState.Error).message)
    }

    @Test
    fun `login error uses fallback message and resetState returns idle`() = runTest {
        every { loginUseCase(any()) } returns flowOf(Resource.Error("Network failed"))

        val viewModel = LoginViewModel(loginUseCase, context)
        viewModel.login("john", "secret")

        advanceUntilIdle()

        val errorState = viewModel.uiState.value
        assertTrue(errorState is LoginUiState.Error)
        assertEquals("Network failed", (errorState as LoginUiState.Error).message)

        viewModel.resetState()
        assertTrue(viewModel.uiState.value is LoginUiState.Idle)
    }
}
