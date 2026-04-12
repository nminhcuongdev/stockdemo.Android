package com.example.stockdemo.feature.chat.presentation

import android.content.Context
import com.example.stockdemo.R
import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.feature.chat.domain.model.ChatMessage
import com.example.stockdemo.feature.chat.domain.model.ChatRequest
import com.example.stockdemo.feature.chat.domain.model.ChatResponse
import com.example.stockdemo.feature.chat.domain.model.HealthResponse
import com.example.stockdemo.feature.chat.domain.usecase.CheckChatHealthUseCase
import com.example.stockdemo.feature.chat.domain.usecase.SendChatMessageUseCase
import com.example.stockdemo.testutil.MainDispatcherRule
import com.example.stockdemo.testutil.mockContext
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val checkChatHealthUseCase: CheckChatHealthUseCase = mockk()
    private val sendChatMessageUseCase: SendChatMessageUseCase = mockk()
    private val context: Context = mockContext(
        mapOf(
            R.string.chat_welcome to "Welcome to Sato Stock"
        )
    )

    @Test
    fun `init with healthy server adds welcome message`() = runTest {
        every { checkChatHealthUseCase() } returns flowOf(
            Resource.Success(HealthResponse(status = "ok", version = "1.0"))
        )

        val viewModel = ChatViewModel(checkChatHealthUseCase, sendChatMessageUseCase)

        advanceUntilIdle()

        assertFalse(viewModel.isCheckingHealth.value)
        assertEquals(1, viewModel.messages.size)
        assertEquals("Welcome to Sato Stock", viewModel.messages.first().text.asString(context))
        assertFalse(viewModel.messages.first().isError)
    }

    @Test
    fun `sendMessage appends user and assistant messages`() = runTest {
        every { checkChatHealthUseCase() } returns flowOf(
            Resource.Success(HealthResponse(status = "ok", version = "1.0"))
        )
        every { sendChatMessageUseCase(any()) } returns flowOf(
            Resource.Success(ChatResponse(answer = "Use offline cache first"))
        )

        val viewModel = ChatViewModel(checkChatHealthUseCase, sendChatMessageUseCase)
        advanceUntilIdle()

        viewModel.sendMessage("How does offline sync work?")
        advanceUntilIdle()

        assertEquals(3, viewModel.messages.size)
        assertEquals("Welcome to Sato Stock", viewModel.messages[0].text.asString(context))
        assertEquals("How does offline sync work?", viewModel.messages[1].text.asString(context))
        assertTrue(viewModel.messages[1].isUser)
        assertEquals("Use offline cache first", viewModel.messages[2].text.asString(context))
        assertFalse(viewModel.messages[2].isUser)
        assertFalse(viewModel.messages[2].isError)
    }
}
