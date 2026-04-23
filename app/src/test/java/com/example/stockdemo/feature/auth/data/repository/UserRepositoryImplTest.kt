package com.example.stockdemo.feature.auth.data.repository

import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.feature.auth.data.local.UserPreferences
import com.example.stockdemo.feature.auth.domain.model.LoginRequest
import com.example.stockdemo.feature.stock.data.remote.ApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
import com.example.stockdemo.core.network.model.BaseResponse

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplTest {

    private val api: ApiService = mockk()
    private val userPreferences: UserPreferences = mockk(relaxed = true)

    private fun createRepository(): UserRepositoryImpl {
        return UserRepositoryImpl(api, userPreferences)
    }

    private fun sampleLoginRequest(): LoginRequest {
        return LoginRequest(username = "alice", password = "secret")
    }

    private fun sampleLoginResponse(): LoginResponseDto {
        return LoginResponseDto(
            accessToken = "token-123",
            user = UserDto(
                userId = 7,
                username = "alice",
                fullName = "Alice Nguyen",
                role = "admin",
                isActive = true,
                createdDate = "2026-04-23T00:00:00",
                lastLoginDate = null
            )
        )
    }

    @Test
    fun `login success emits loading then success and saves user data`() = runTest {
        coEvery { api.login(sampleLoginRequest()) } returns BaseResponse(
            success = true,
            message = "OK",
            data = sampleLoginResponse()
        )

        val results = createRepository().login(sampleLoginRequest()).toList()

        assertEquals(2, results.size)
        assertTrue(results[0] is Resource.Loading)
        assertTrue(results[1] is Resource.Success)
        val success = results[1] as Resource.Success
        assertEquals(7, success.data?.userId)
        assertEquals("Alice Nguyen", success.data?.fullName)
        coVerify(exactly = 1) { userPreferences.saveAccessToken("token-123") }
        coVerify(exactly = 1) { userPreferences.saveUser("Alice Nguyen", 7) }
    }

    @Test
    fun `login missing token emits error and does not save preferences`() = runTest {
        coEvery { api.login(sampleLoginRequest()) } returns BaseResponse(
            success = true,
            message = "OK",
            data = sampleLoginResponse().copy(accessToken = "")
        )

        val results = createRepository().login(sampleLoginRequest()).toList()

        assertEquals(2, results.size)
        assertTrue(results[1] is Resource.Error)
        val error = results[1] as Resource.Error
        assertEquals("Pháº£n há»“i Ä‘Äƒng nháº­p khÃ´ng cÃ³ access token", error.message)
        coVerify(exactly = 0) { userPreferences.saveAccessToken(any()) }
        coVerify(exactly = 0) { userPreferences.saveUser(any(), any()) }
    }

    @Test
    fun `login http 400 parses validation error message`() = runTest {
        val errorJson = """
            {
              "errors": {
                "Username": ["Username is required"],
                "Password": ["Password is too short"]
              }
            }
        """.trimIndent()
        coEvery { api.login(sampleLoginRequest()) } throws HttpException(
            Response.error<String>(
                400,
                errorJson.toResponseBody("application/json".toMediaType())
            )
        )

        val results = createRepository().login(sampleLoginRequest()).toList()

        assertEquals(2, results.size)
        assertTrue(results[1] is Resource.Error)
        val error = results[1] as Resource.Error
        assertEquals("Username is required\nPassword is too short", error.message)
    }

    @Test
    fun `login http 401 uses base response message`() = runTest {
        val errorJson = """
            {
              "success": false,
              "message": "Invalid credentials"
            }
        """.trimIndent()
        coEvery { api.login(sampleLoginRequest()) } throws HttpException(
            Response.error<String>(
                401,
                errorJson.toResponseBody("application/json".toMediaType())
            )
        )

        val results = createRepository().login(sampleLoginRequest()).toList()

        assertEquals(2, results.size)
        assertTrue(results[1] is Resource.Error)
        val error = results[1] as Resource.Error
        assertEquals("Invalid credentials", error.message)
    }
}
