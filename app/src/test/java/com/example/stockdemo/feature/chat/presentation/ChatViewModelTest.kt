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
import io.mockk.verify
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
            R.string.chat_welcome to "Welcome to Sato Stock",
            R.string.chat_server_warning to "Warning: AI server issue (Status: %1\$s).",
            R.string.chat_connection_error to "Connection error (%1\$s)",
            R.string.chat_response_error to "Response error (%1\$s)",
            R.string.unknown_error to "Unknown error",
            R.string.na to "N/A"
        )
    )

    private fun healthyResponse() = flowOf(
        Resource.Success(HealthResponse(status = "ok", version = "1.0"))
    )

    @Test
    fun `init with healthy server adds welcome message`() = runTest {
        every { checkChatHealthUseCase() } returns healthyResponse()

        val viewModel = ChatViewModel(checkChatHealthUseCase, sendChatMessageUseCase)

        advanceUntilIdle()

        assertFalse(viewModel.isCheckingHealth.value)
        assertEquals(1, viewModel.messages.size)
        assertEquals("Welcome to Sato Stock", viewModel.messages.first().text.asString(context))
        assertFalse(viewModel.messages.first().isError)
    }

    @Test
    fun `sendMessage appends user and assistant messages`() = runTest {
        every { checkChatHealthUseCase() } returns healthyResponse()
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

    @Test
    fun `init with unhealthy server adds warning message`() = runTest {
        every { checkChatHealthUseCase() } returns flowOf(
            Resource.Success(HealthResponse(status = "degraded", version = "1.0"))
        )

        val viewModel = ChatViewModel(checkChatHealthUseCase, sendChatMessageUseCase)

        advanceUntilIdle()

        assertFalse(viewModel.isCheckingHealth.value)
        assertEquals(1, viewModel.messages.size)
        assertTrue(viewModel.messages.first().isError)
        assertEquals(
            "Warning: AI server issue (Status: degraded).",
            viewModel.messages.first().text.asString(context)
        )
    }

    @Test
    fun `init health error adds fallback connection error`() = runTest {
        every { checkChatHealthUseCase() } returns flowOf(Resource.Error(""))

        val viewModel = ChatViewModel(checkChatHealthUseCase, sendChatMessageUseCase)

        advanceUntilIdle()

        assertFalse(viewModel.isCheckingHealth.value)
        assertEquals(1, viewModel.messages.size)
        assertTrue(viewModel.messages.first().isError)
        assertEquals(
            "Connection error (Unknown error)",
            viewModel.messages.first().text.asString(context)
        )
    }

    @Test
    fun `sendMessage ignores blank question`() = runTest {
        every { checkChatHealthUseCase() } returns healthyResponse()

        val viewModel = ChatViewModel(checkChatHealthUseCase, sendChatMessageUseCase)
        advanceUntilIdle()

        viewModel.sendMessage("   ")
        advanceUntilIdle()

        assertEquals(1, viewModel.messages.size)
        verify(exactly = 0) { sendChatMessageUseCase(any()) }
    }

    @Test
    fun `sendMessage while health check in progress is ignored`() = runTest {
        every { checkChatHealthUseCase() } returns flowOf(Resource.Loading(true))

        val viewModel = ChatViewModel(checkChatHealthUseCase, sendChatMessageUseCase)

        viewModel.sendMessage("Should not send")
        advanceUntilIdle()

        assertTrue(viewModel.isCheckingHealth.value)
        assertTrue(viewModel.messages.isEmpty())
        verify(exactly = 0) { sendChatMessageUseCase(any()) }
    }

    @Test
    fun `sendMessage error appends fallback error message`() = runTest {
        every { checkChatHealthUseCase() } returns healthyResponse()
        every { sendChatMessageUseCase(any()) } returns flowOf(Resource.Error(""))

        val viewModel = ChatViewModel(checkChatHealthUseCase, sendChatMessageUseCase)
        advanceUntilIdle()

        viewModel.sendMessage("Need help")
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
        assertEquals(3, viewModel.messages.size)
        assertTrue(viewModel.messages.last().isError)
        assertEquals("Response error (Unknown error)", viewModel.messages.last().text.asString(context))
    }

    @Test
    fun `sendMessage forwards previous conversation history`() = runTest {
        every { checkChatHealthUseCase() } returns healthyResponse()

        val requests = mutableListOf<ChatRequest>()
        every { sendChatMessageUseCase(capture(requests)) } returnsMany listOf(
            flowOf(Resource.Success(ChatResponse(answer = "Answer 1"))),
            flowOf(Resource.Success(ChatResponse(answer = "Answer 2")))
        )

        val viewModel = ChatViewModel(checkChatHealthUseCase, sendChatMessageUseCase)
        advanceUntilIdle()

        viewModel.sendMessage("Question 1")
        advanceUntilIdle()
        viewModel.sendMessage("Question 2")
        advanceUntilIdle()

        assertEquals(2, requests.size)
        assertEquals("Question 1", requests[0].question)
        assertTrue(requests[0].conversationHistory.isEmpty())
        assertEquals("Question 2", requests[1].question)
        assertEquals(
            listOf(
                ChatMessage(content = "Question 1", role = "user"),
                ChatMessage(content = "Answer 1", role = "assistant")
            ),
            requests[1].conversationHistory
        )
    }
}
