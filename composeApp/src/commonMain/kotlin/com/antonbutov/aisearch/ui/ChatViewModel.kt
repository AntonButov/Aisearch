package com.antonbutov.aisearch.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.antonbutov.aisearch.data.model.ChunkType
import com.antonbutov.aisearch.data.model.Source as ApiSource
import com.antonbutov.aisearch.domain.ChatRepository
import com.antonbutov.aisearch.ui.model.Source
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class LastMessageState {
    data object Idle : LastMessageState()
    data object Loading : LastMessageState()
    data class Message(val text: String) : LastMessageState()
    data object Finished : LastMessageState()
}

data class ChatUiState(
    val messages: List<ChatMessage>,
    val lastMessageState: LastMessageState
)

sealed class ChatMessage {
    data class ChatMessageText(
        val text: String,
        val isUser: Boolean
    ) : ChatMessage()
    
    data class ChatMessageSources(
        val sources: List<Source>
    ) : ChatMessage()
}

class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ChatUiState(
            messages = emptyList(),
            lastMessageState = LastMessageState.Idle
        )
    )
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(message: String) {
        if (message.isBlank() || _uiState.value.lastMessageState is LastMessageState.Loading) return
        
        // Если предыдущее состояние было Finished, переводим в Idle перед новым запросом
        if (_uiState.value.lastMessageState is LastMessageState.Finished) {
            _uiState.update { it.copy(lastMessageState = LastMessageState.Idle) }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + ChatMessage.ChatMessageText(
                    text = message,
                    isUser = true
                ),
                lastMessageState = LastMessageState.Loading
            )

            repository.streamChat(message)
                .catch { cause ->
                    // Обработка ошибок на уровне потока - переводим в Idle
                    _uiState.update { currentState ->
                        currentState.copy(lastMessageState = LastMessageState.Idle)
                    }
                }
                .collect { result ->
                    result.onSuccess { chunk ->
                        val chunkText = chunk.textResponse ?: ""
                        when (chunk.type) {
                            ChunkType.FinalizeResponseStream -> {
                                // FinalizeResponseStream - завершаем сообщение с источниками
                                // Проверяем, не были ли источники уже добавлены
                                val currentState = _uiState.value
                                if (currentState.lastMessageState is LastMessageState.Finished) {
                                    // Источники уже были добавлены, пропускаем
                                    return@collect
                                }
                                
                                // Проверяем, не является ли последнее сообщение уже ChatMessageSources
                                val lastMessage = currentState.messages.lastOrNull()
                                if (lastMessage is ChatMessage.ChatMessageSources) {
                                    // Источники уже были добавлены, пропускаем
                                    return@collect
                                }
                                
                                val sources = (chunk.sources ?: emptyList()).map { apiSource ->
                                    Source(
                                        title = apiSource.description ?: apiSource.title,
                                        text = extractTextFromSource(apiSource.text ?: "")
                                    )
                                }
                                _uiState.update { state ->
                                    val accumulatedText = (state.lastMessageState as? LastMessageState.Message)?.text ?: ""
                                    val newMessages = if (accumulatedText.isNotEmpty()) {
                                        state.messages + ChatMessage.ChatMessageText(
                                            text = accumulatedText,
                                            isUser = false
                                        ) + ChatMessage.ChatMessageSources(sources)
                                    } else {
                                        state.messages + ChatMessage.ChatMessageSources(sources)
                                    }
                                    state.copy(
                                        messages = newMessages,
                                        lastMessageState = LastMessageState.Finished
                                    )
                                }
                            }

                            ChunkType.TextResponseChunk -> {
                                _uiState.update { currentState ->
                                    val chunkText = chunk.textResponse ?: ""
                                    // Если это первый TextResponseChunk (состояние Loading), сразу переходим в Message
                                    // даже если текст пустой - это скроет лоадер немедленно
                                    val previousText = when (currentState.lastMessageState) {
                                        is LastMessageState.Message -> currentState.lastMessageState.text
                                        else -> "" // Loading или Idle - начинаем с пустой строки
                                    }
                                    val newText = previousText + chunkText
                                    
                                    // Если close: true и есть sources, завершаем сообщение с источниками
                                    if (chunk.close == true && !chunk.sources.isNullOrEmpty()) {
                                        // Проверяем, не были ли источники уже добавлены
                                        val lastMessage = currentState.messages.lastOrNull()
                                        if (lastMessage is ChatMessage.ChatMessageSources) {
                                            // Источники уже были добавлены, только обновляем состояние
                                            currentState.copy(
                                                lastMessageState = LastMessageState.Finished
                                            )
                                        } else {
                                            val sources = chunk.sources.map { apiSource ->
                                                Source(
                                                    title = apiSource.description ?: apiSource.title,
                                                    text = extractTextFromSource(apiSource.text ?: "")
                                                )
                                            }
                                            val newMessages = if (newText.isNotEmpty()) {
                                                currentState.messages + ChatMessage.ChatMessageText(
                                                    text = newText,
                                                    isUser = false
                                                ) + ChatMessage.ChatMessageSources(sources)
                                            } else {
                                                currentState.messages + ChatMessage.ChatMessageSources(sources)
                                            }
                                            currentState.copy(
                                                messages = newMessages,
                                                lastMessageState = LastMessageState.Finished
                                            )
                                        }
                                    } else {
                                        currentState.copy(
                                            lastMessageState = LastMessageState.Message(newText)
                                        )
                                    }
                                }
                            }
                        }
                    }.onFailure {
                        // Обработка ошибок - переводим в Idle
                        _uiState.update { currentState ->
                            currentState.copy(lastMessageState = LastMessageState.Idle)
                        }
                    }
                }
        }
    }

    /**
     * Извлекает текст из поля text источника, удаляя метаданные документа.
     * Ищет закрывающий тег </document_metadata> и возвращает текст после него.
     */
    private fun extractTextFromSource(sourceText: String): String {
        if (sourceText.isEmpty()) return ""
        
        val metadataEndTag = "</document_metadata>"
        val metadataEndIndex = sourceText.indexOf(metadataEndTag)
        
        return if (metadataEndIndex >= 0) {
            // Берем текст после закрывающего тега и убираем начальные пробелы/переносы строк
            sourceText.substring(metadataEndIndex + metadataEndTag.length)
                .trimStart()
                .replace(Regex("^[\n\r]+"), "") // Убираем начальные переносы строк
        } else {
            // Если тега нет, возвращаем исходный текст
            sourceText.trim()
        }
    }
}
