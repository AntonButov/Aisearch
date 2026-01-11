package com.antonbutov.aisearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antonbutov.aisearch.domain.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentResponse: String = ""
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val sources: List<String> = emptyList()
)

class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    fun sendMessage(message: String) {
        if (message.isBlank() || _uiState.value.isLoading) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + ChatMessage(
                    text = message,
                    isUser = true
                ),
                isLoading = true,
                error = null,
                currentResponse = ""
            )
            
            var fullResponse = ""
            var sources = emptyList<String>()
            
            repository.streamChat(message).collect { result ->
                result.onSuccess { chunk ->
                    val text = chunk.textResponse
                    if (text != null) {
                        fullResponse += text
                        _uiState.value = _uiState.value.copy(currentResponse = fullResponse)
                    }
                    
                    val chunkSources = chunk.sources
                    if (chunkSources != null) {
                        sources = chunkSources.map { source -> source.title }
                    }
                    
                    if (chunk.close) {
                        _uiState.value = _uiState.value.copy(
                            messages = _uiState.value.messages + ChatMessage(
                                text = fullResponse,
                                isUser = false,
                                sources = sources
                            ),
                            isLoading = false,
                            currentResponse = ""
                        )
                    }
                    
                    if (chunk.error) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = chunk.textResponse ?: "Unknown error occurred",
                            currentResponse = ""
                        )
                    }
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Unknown error occurred",
                        currentResponse = ""
                    )
                }
            }
        }
    }
}
