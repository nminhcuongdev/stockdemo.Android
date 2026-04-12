package com.example.stockdemo.feature.chat.presentation

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdemo.R
import com.example.stockdemo.core.common.Resource
import com.example.stockdemo.core.ui.UiText
import com.example.stockdemo.core.ui.asUiText
import com.example.stockdemo.feature.chat.domain.model.ChatMessage
import com.example.stockdemo.feature.chat.domain.model.ChatRequest
import com.example.stockdemo.feature.chat.domain.usecase.CheckChatHealthUseCase
import com.example.stockdemo.feature.chat.domain.usecase.SendChatMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val checkChatHealthUseCase: CheckChatHealthUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase
) : ViewModel() {

    private val _isCheckingHealth = MutableStateFlow(true)
    val isCheckingHealth = _isCheckingHealth.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val messages = mutableStateListOf<ChatUiModel>()
    private val conversationHistory = mutableListOf<ChatMessage>()

    init {
        checkHealth()
    }

    private fun checkHealth() {
        checkChatHealthUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _isCheckingHealth.value = false
                    if (result.data?.status == "ok") {
                        if (messages.isEmpty()) {
                            messages.add(
                                ChatUiModel(
                                    text = UiText.StringResource(R.string.chat_welcome),
                                    isUser = false
                                )
                            )
                        }
                    } else {
                        messages.add(
                            ChatUiModel(
                                text = UiText.StringResource(
                                    R.string.chat_server_warning,
                                    listOf(result.data?.status ?: UiText.StringResource(R.string.na))
                                ),
                                isUser = false,
                                isError = true
                            )
                        )
                    }
                }
                is Resource.Error -> {
                    _isCheckingHealth.value = false
                    messages.add(
                        ChatUiModel(
                            text = UiText.StringResource(
                                R.string.chat_connection_error,
                                listOf(result.message.asUiText(R.string.unknown_error))
                            ),
                            isUser = false,
                            isError = true
                        )
                    )
                }
                is Resource.Loading -> {
                    _isCheckingHealth.value = true
                }
            }
        }.launchIn(viewModelScope)
    }

    fun sendMessage(question: String) {
        if (question.isBlank() || _isLoading.value || _isCheckingHealth.value) return

        messages.add(ChatUiModel(UiText.DynamicString(question), true))
        _isLoading.value = true

        val request = ChatRequest(
            question = question,
            conversationHistory = conversationHistory.toList()
        )

        sendChatMessageUseCase(request).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _isLoading.value = false
                    result.data?.let { response ->
                        messages.add(ChatUiModel(UiText.DynamicString(response.answer), false))
                        conversationHistory.add(ChatMessage(content = question, role = "user"))
                        conversationHistory.add(ChatMessage(content = response.answer, role = "assistant"))
                    }
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    messages.add(
                        ChatUiModel(
                            text = UiText.StringResource(
                                R.string.chat_response_error,
                                listOf(result.message.asUiText(R.string.unknown_error))
                            ),
                            isUser = false,
                            isError = true
                        )
                    )
                }
                is Resource.Loading -> {
                    _isLoading.value = true
                }
            }
        }.launchIn(viewModelScope)
    }
}
