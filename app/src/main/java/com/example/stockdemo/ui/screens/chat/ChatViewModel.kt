package com.example.stockdemo.ui.screens.chat

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stockdemo.domain.model.chat.ChatMessage
import com.example.stockdemo.domain.model.chat.ChatRequest
import com.example.stockdemo.domain.use_case.chat.CheckChatHealthUseCase
import com.example.stockdemo.domain.use_case.chat.SendChatMessageUseCase
import com.example.stockdemo.domain.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val checkChatHealthUseCase: CheckChatHealthUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase
) : ViewModel() {

    private val _isCheckingHealth = MutableStateFlow(true)
    val isCheckingHealth = _isCheckingHealth.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val messages = mutableStateListOf<ChatUIModel>()
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
                            messages.add(ChatUIModel("Xin chào! Tôi là trợ lý AI của Sato Stock. Tôi có thể giúp gì cho bạn về quản lý kho hàng?", false))
                        }
                    } else {
                        messages.add(ChatUIModel("Cảnh báo: Máy chủ AI đang gặp sự cố (Status: ${result.data?.status}).", false, isError = true))
                    }
                }
                is Resource.Error -> {
                    _isCheckingHealth.value = false
                    messages.add(ChatUIModel("Lỗi kết nối: Không thể kết nối tới máy chủ AI. Vui lòng kiểm tra mạng hoặc server. (${result.message})", false, isError = true))
                }
                is Resource.Loading -> {
                    _isCheckingHealth.value = true
                }
            }
        }.launchIn(viewModelScope)
    }

    fun sendMessage(question: String) {
        if (question.isBlank() || _isLoading.value || _isCheckingHealth.value) return

        messages.add(ChatUIModel(question, true))
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
                        messages.add(ChatUIModel(response.answer, false))
                        conversationHistory.add(ChatMessage(content = question, role = "user"))
                        conversationHistory.add(ChatMessage(content = response.answer, role = "assistant"))
                    }
                }
                is Resource.Error -> {
                    _isLoading.value = false
                    messages.add(ChatUIModel("Lỗi: Không thể nhận phản hồi từ AI. (${result.message})", false, isError = true))
                }
                is Resource.Loading -> {
                    _isLoading.value = true
                }
            }
        }.launchIn(viewModelScope)
    }
}
