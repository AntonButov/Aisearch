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
                    _uiState.update { currentState ->
                        currentState.copy(lastMessageState = LastMessageState.Idle)
                    }
                }
                .collect { result ->
                    result.onSuccess { chunk ->
                        val chunkText = chunk.textResponse ?: ""
                        when (chunk.type) {
                            ChunkType.FinalizeResponseStream -> {
                                val currentState = _uiState.value
                                if (currentState.lastMessageState is LastMessageState.Finished) return@collect
                                val lastMessage = currentState.messages.lastOrNull()
                                if (lastMessage is ChatMessage.ChatMessageSources) return@collect
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
                                    val chunkText = normalizeMenuSeparators(chunk.textResponse ?: "")
                                    val previousText = when (currentState.lastMessageState) {
                                        is LastMessageState.Message -> currentState.lastMessageState.text
                                        else -> ""
                                    }
                                    val newText = previousText + chunkText
                                    if (chunk.close == true && !chunk.sources.isNullOrEmpty()) {
                                        val lastMessage = currentState.messages.lastOrNull()
                                        if (lastMessage is ChatMessage.ChatMessageSources) {
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
     * Заменяет символы-стрелки на " > ", чтобы они отображались при любом шрифте.
     */
    private fun extractTextFromSource(sourceText: String): String {
        if (sourceText.isEmpty()) return ""
        
        val metadataEndTag = "</document_metadata>"
        val metadataEndIndex = sourceText.indexOf(metadataEndTag)
        
        val raw = if (metadataEndIndex >= 0) {
            sourceText.substring(metadataEndIndex + metadataEndTag.length)
                .trimStart()
                .replace(Regex("^[\n\r]+"), "")
        } else {
            sourceText.trim()
        }
        return normalizeMenuSeparators(raw)
    }

    /** Заменяет стрелки и подобные разделители путей меню на " > " для корректного отображения без спецсимволов. */
    private fun normalizeMenuSeparators(text: String): String {
        if (text.isEmpty()) return text
        return text
            .replace('\u2192', '>')  // →
            .replace('\u203A', '>')  // ›
            .replace('\u27A1', '>')  // ➡
            .replace('\u279C', '>')  // ➜
            .replace('\u279D', '>')  // ➝
            .replace('\u27F6', '>')  // ⟶
            .replace('\u25B6', '>')  // ▶
            .replace('\u25B8', '>')  // ▸
            .replace('\uFE65', '>')  // ﹥
            .replace(Regex("\\s*>\\s*"), " > ")  // нормализуем пробелы вокруг >
    }
}
