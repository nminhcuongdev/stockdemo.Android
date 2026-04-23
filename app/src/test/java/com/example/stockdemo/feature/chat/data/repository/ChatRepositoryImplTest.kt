package com.example.stockdemo.feature.chat.data.repository

import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.feature.chat.data.remote.ChatRequestDto
import com.example.stockdemo.feature.chat.data.remote.ChatResponseDto
import com.example.stockdemo.feature.chat.data.remote.HealthResponseDto
import com.example.stockdemo.feature.chat.data.remote.PythonApiService
import com.example.stockdemo.feature.chat.domain.model.ChatMessage
import com.example.stockdemo.feature.chat.domain.model.ChatRequest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ChatRepositoryImplTest {

    private val api: PythonApiService = mockk()

    private fun createRepository(): ChatRepositoryImpl {
        return ChatRepositoryImpl(api)
    }

    private fun sampleChatRequest(): ChatRequest {
        return ChatRequest(
            question = "How many stocks are low?",
            conversationHistory = listOf(
                ChatMessage(content = "Hello", role = "user"),
                ChatMessage(content = "Hi", role = "assistant")
            ),
            collectionName = "warehouse",
            topK = 3,
            useRag = true
        )
    }

    @Test
    fun `checkHealth emits loading then mapped success`() = runTest {
        coEvery { api.checkHealth() } returns HealthResponseDto(
            status = "ok",
            version = "1.0.0"
        )

        val results = createRepository().checkHealth().toList()

        assertEquals(2, results.size)
        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val success = results[1] as Resource.Success
        assertEquals("ok", success.data?.status)
        assertEquals("1.0.0", success.data?.version)
    }

    @Test
    fun `checkHealth http exception emits server error`() = runTest {
        coEvery { api.checkHealth() } throws HttpException(
            Response.error<String>(
                500,
                "{}".toResponseBody("application/json".toMediaType())
            )
        )

        val results = createRepository().checkHealth().toList()

        assertEquals(2, results.size)
        assertTrue(results[1] is Resource.Error)
        val error = results[1] as Resource.Error
        assertEquals("Lá»—i káº¿t ná»‘i server Python", error.message)
    }

    @Test
    fun `chat maps request dto and response domain`() = runTest {
        val requestSlot = slot<ChatRequestDto>()
        coEvery { api.chat(capture(requestSlot)) } returns ChatResponseDto(
            answer = "3 products are low in stock",
            sources = listOf("inventory.csv")
        )

        val results = createRepository().chat(sampleChatRequest()).toList()

        assertEquals(2, results.size)
        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val success = results[1] as Resource.Success
        assertEquals("3 products are low in stock", success.data?.answer)
        assertEquals(listOf("inventory.csv"), success.data?.sources)
        assertEquals("How many stocks are low?", requestSlot.captured.question)
        assertEquals("warehouse", requestSlot.captured.collectionName)
        assertEquals(3, requestSlot.captured.topK)
        assertEquals(2, requestSlot.captured.conversationHistory.size)
        assertEquals("Hello", requestSlot.captured.conversationHistory[0].content)
        assertEquals("assistant", requestSlot.captured.conversationHistory[1].role)
        coVerify(exactly = 1) { api.chat(any()) }
    }

    @Test
    fun `chat generic exception emits original message`() = runTest {
        coEvery { api.chat(any()) } throws IllegalStateException("boom")

        val results = createRepository().chat(sampleChatRequest()).toList()

        assertEquals(2, results.size)
        assertTrue(results[1] is Resource.Error)
        val error = results[1] as Resource.Error
        assertEquals("boom", error.message)
    }
}
