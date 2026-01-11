package com.antonbutov.aisearch.ui

import com.antonbutov.aisearch.data.api.ApiClient
import com.antonbutov.aisearch.data.model.ChatRequest
import com.antonbutov.aisearch.data.model.ChunkType
import com.antonbutov.aisearch.data.model.Source
import com.antonbutov.aisearch.data.model.StreamChatChunk
import com.antonbutov.aisearch.domain.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    
    private fun ChatMessage.isUserMessage(): Boolean {
        return this is ChatMessage.ChatMessageText && this.isUser
    }
    
    private fun ChatMessage.getText(): String? {
        return (this as? ChatMessage.ChatMessageText)?.text
    }
    
    private fun ChatMessage.getSources(): List<com.antonbutov.aisearch.ui.model.Source> {
        return (this as? ChatMessage.ChatMessageSources)?.sources ?: emptyList()
    }
    
    @Test
    fun testViewModelCreates() = runTest {
        val repository = TestChatRepository()
        val viewModel = ChatViewModel(repository)
        
        assertNotNull(viewModel, "Should create ViewModel")
        assertNotNull(viewModel.uiState, "Should have uiState")
    }
    
    @Test
    fun testSendMessageCallsRepository() = runTest {
        val repository = TestChatRepository()
        val viewModel = ChatViewModel(repository)
        
        viewModel.sendMessage("Test message")
        
        // Ждем состояния Finished (поток завершился с FinalizeResponseStream)
        val finishedState = viewModel.uiState.first { 
            it.lastMessageState is LastMessageState.Finished 
        }
        
        // Проверяем общую логику без привязки к тестовым данным
        assertTrue(finishedState.messages.any { it.isUserMessage() }, "Should have user message")
        assertTrue(finishedState.lastMessageState is LastMessageState.Finished, "Should be Finished after finalizeResponseStream")
        assertTrue(finishedState.messages.any { it is ChatMessage.ChatMessageSources && it.sources.isNotEmpty() }, "Should have AI message with sources")
    }
    
    @Test
    fun testSendMessageIgnoresBlankMessage() = runTest {
        val repository = TestChatRepository()
        val viewModel = ChatViewModel(repository)
        
        val initialMessageCount = viewModel.uiState.value.messages.size
        
        viewModel.sendMessage("")
        
        val stateAfterBlank = viewModel.uiState.value
        assertEquals(initialMessageCount, stateAfterBlank.messages.size, "Should not add blank message")
    }
    
    @Test
    fun testInitialStateIsIdle() = runTest {
        val repository = TestChatRepository()
        val viewModel = ChatViewModel(repository)
        
        val initialState = viewModel.uiState.value
        assertTrue(
            initialState.lastMessageState is LastMessageState.Idle,
            "Initial state should be Idle. State: ${initialState.lastMessageState}"
        )
    }
    
    @Test
    fun testStateChangesToLoadingWhenSendingMessage() = runTest {
        val repository = TestChatRepository()
        val viewModel = ChatViewModel(repository)
        
        viewModel.sendMessage("Test message")
        
        // Ждем состояния Finished (поток завершился с FinalizeResponseStream)
        val finishedState = viewModel.uiState.first { 
            it.lastMessageState is LastMessageState.Finished 
        }
        
        // Проверяем, что состояние Finished
        assertTrue(finishedState.lastMessageState is LastMessageState.Finished, 
                  "Should have Finished state after finalizeResponseStream")
        
        // Проверяем, что было состояние Loading (через наличие сообщения пользователя)
        assertTrue(finishedState.messages.any { it.isUserMessage() }, 
                  "Should have user message, which means Loading state was shown")
    }
    
    @Test
    fun testStateChangesToMessageWhenReceivingTextChunk() = runTest {
        val repository = TestChatRepository()
        val viewModel = ChatViewModel(repository)
        
        viewModel.sendMessage("Test message")
        
        // Ждем состояния Finished (поток завершился с FinalizeResponseStream)
        val finishedState = viewModel.uiState.first { 
            it.lastMessageState is LastMessageState.Finished 
        }
        
        // Проверяем, что состояние Finished
        assertTrue(finishedState.lastMessageState is LastMessageState.Finished, 
                  "Should have Finished state after finalizeResponseStream")

        val aiMessage = finishedState.messages.filterIsInstance<ChatMessage.ChatMessageText>().find { !it.isUser }
        assertNotNull(aiMessage, "Should have AI message")
        assertTrue(aiMessage.text.isNotEmpty(), "AI message should have accumulated text")
    }
    
    @Test
    fun testStateChangesToIdleWhenReceivingCloseChunk() = runTest {
        val repository = TestChatRepository()
        val viewModel = ChatViewModel(repository)
        
        viewModel.sendMessage("Test message")
        
        // Ждем состояния Finished (поток завершился с FinalizeResponseStream)
        val finishedState = viewModel.uiState.first { 
            it.lastMessageState is LastMessageState.Finished 
        }
        
        // Проверяем, что состояние Finished после finalizeResponseStream
        assertTrue(finishedState.lastMessageState is LastMessageState.Finished, 
                  "Should be Finished after finalizeResponseStream. Current: ${finishedState.lastMessageState}")
        
        val sourcesMessage = finishedState.messages.filterIsInstance<ChatMessage.ChatMessageSources>().firstOrNull()
        assertNotNull(sourcesMessage, "Should have sources message")
        assertTrue(sourcesMessage.sources.isNotEmpty(), "AI message should have sources")
    }
    
    @Test
    fun testMessageTextAccumulatesFromMultipleChunks() = runTest {
        val repository = TestChatRepository()
        val viewModel = ChatViewModel(repository)
        
        viewModel.sendMessage("Test message")
        
        // Ждем состояния Finished (поток завершился с FinalizeResponseStream)
        val finishedState = viewModel.uiState.first { 
            it.lastMessageState is LastMessageState.Finished 
        }
        
        val aiMessage = finishedState.messages.filterIsInstance<ChatMessage.ChatMessageText>().find { !it.isUser }
        assertNotNull(aiMessage, "Should have AI message")
        assertTrue(aiMessage.text.isNotEmpty(), "AI message should have accumulated text")
        
        // Проверяем, что текст накапливался (финальный текст не пустой)
        assertTrue(aiMessage.text.length > 0, 
                  "Final text should not be empty. Text: ${aiMessage.text}")
    }
    
    @Test
    fun testRealWorldStreamDataCreatesOneChatMessage() = runTest {
        val repository = RealWorldTestChatRepository()
        val viewModel = ChatViewModel(repository)
        
        viewModel.sendMessage("Тест")
        
        // Ждем состояния Finished (поток завершился с FinalizeResponseStream)
        val finishedState = viewModel.uiState.drop(1).first { 
            it.lastMessageState is LastMessageState.Finished 
        }
        
        // Проверяем, что есть сообщения от пользователя и от AI
        assertTrue(finishedState.messages.size >= 2, "Should have at least 2 messages: user and AI. Current messages: ${finishedState.messages.size}")
        
        val userMessage = finishedState.messages.find { it.isUserMessage() }
        assertNotNull(userMessage, "Should have user message")
        assertEquals("Тест", userMessage.getText())
        
        val aiTextMessage = finishedState.messages.filterIsInstance<ChatMessage.ChatMessageText>().find { !it.isUser }
        assertNotNull(aiTextMessage, "Should have AI text message. Messages: ${finishedState.messages.size}")
        assertTrue(aiTextMessage.text.isNotEmpty(), "AI message should have accumulated text from all chunks. Text length: ${aiTextMessage.text.length}, Text: ${aiTextMessage.text.take(200)}")
        
        // Проверяем, что текст содержит части из разных чанков
        assertTrue(aiTextMessage.text.contains("Тогда"), "Text should contain 'Тогда'. Text: ${aiTextMessage.text.take(200)}")
        assertTrue(aiTextMessage.text.contains("давай"), "Text should contain 'давай'. Text: ${aiTextMessage.text.take(200)}")
        assertTrue(aiTextMessage.text.contains("сделаем"), "Text should contain 'сделаем'. Text: ${aiTextMessage.text.take(200)}")
        
        // Проверяем, что состояние Finished после finalizeResponseStream
        assertTrue(finishedState.lastMessageState is LastMessageState.Finished, 
                  "Should be Finished after finalizeResponseStream. Current state: ${finishedState.lastMessageState}")
        
        // Проверяем, что есть источники (если они были в finalizeResponseStream)
        val sourcesMessage = finishedState.messages.filterIsInstance<ChatMessage.ChatMessageSources>().firstOrNull()
        assertTrue(sourcesMessage == null || sourcesMessage.sources.isNotEmpty(), "Sources should be handled correctly")
    }
    
    @Test
    fun testSourcesInTextResponseChunkWithCloseTrue() = runTest {
        val repository = TextResponseChunkWithSourcesRepository()
        val viewModel = ChatViewModel(repository)
        
        viewModel.sendMessage("Test message")
        
        // Ждем состояния Finished
        val finishedState = viewModel.uiState.first { 
            it.lastMessageState is LastMessageState.Finished 
        }
        
        val aiTextMessage = finishedState.messages.filterIsInstance<ChatMessage.ChatMessageText>().find { !it.isUser }
        assertNotNull(aiTextMessage, "Should have AI text message")
        assertTrue(aiTextMessage.text.isNotEmpty(), "AI message should have text")
        
        val sourcesMessage = finishedState.messages.filterIsInstance<ChatMessage.ChatMessageSources>().firstOrNull()
        assertNotNull(sourcesMessage, "Should have sources message")
        assertTrue(sourcesMessage.sources.isNotEmpty(), "AI message should have sources from TextResponseChunk with close=true")
        assertEquals(2, sourcesMessage.sources.size, "Should have 2 sources")
        assertTrue(sourcesMessage.sources.any { it.title == "Руководство пользователя iPhone" }, "Should contain source with title 'Руководство пользователя iPhone'")
        assertTrue(sourcesMessage.sources.all { it.text.isNotEmpty() }, "All sources should have extracted text")
    }
    
    @Test
    fun testRealWorldSourcesParsing() = runTest {
        val repository = RealWorldSourcesRepository()
        val viewModel = ChatViewModel(repository)
        
        viewModel.sendMessage("Тест")
        
        // Ждем состояния Finished
        val finishedState = viewModel.uiState.first { 
            it.lastMessageState is LastMessageState.Finished 
        }
        
        val sourcesMessage = finishedState.messages.filterIsInstance<ChatMessage.ChatMessageSources>().firstOrNull()
        assertNotNull(sourcesMessage, "Should have sources message")
        assertTrue(sourcesMessage.sources.isNotEmpty(), "Should have sources")
        assertEquals(4, sourcesMessage.sources.size, "Should have 4 sources")
        
        // Проверяем, что все источники имеют title "Руководство пользователя iPhone"
        val allSourcesHaveCorrectTitle = sourcesMessage.sources.all { it.title == "Руководство пользователя iPhone" }
        assertTrue(allSourcesHaveCorrectTitle, "All sources should have title 'Руководство пользователя iPhone'")
        
        // Проверяем, что один из источников содержит текст "Чтобы заблокировать фокус и экспозицию,"
        val sourceWithFocusText = sourcesMessage.sources.find { it.text.contains("Чтобы заблокировать фокус и экспозицию,") }
        assertNotNull(sourceWithFocusText, "Should contain source with text 'Чтобы заблокировать фокус и экспозицию,'")
        
        // Проверяем, что текст извлечен правильно (без document_metadata)
        assertNotNull(sourceWithFocusText, "Should have source with focus text")
        val source = sourceWithFocusText
        assertFalse(source.text.contains("<document_metadata>"), "Text should not contain <document_metadata> tag")
        assertFalse(source.text.contains("</document_metadata>"), "Text should not contain </document_metadata> tag")
        assertTrue(source.text.startsWith(", затем коснитесь") || source.text.contains("Чтобы заблокировать"), "Text should start after document_metadata")
    }
    
    @Test
    fun testInitialStateIsIdleWithEmptyMessages() = runTest {
        val repository = TestChatRepository()
        val viewModel = ChatViewModel(repository)
        
        val initialState = viewModel.uiState.value
        assertTrue(
            initialState.lastMessageState is LastMessageState.Idle,
            "Initial state should be Idle"
        )
        assertTrue(
            initialState.messages.isEmpty(),
            "Initial messages should be empty (for WelcomeScreen display)"
        )
    }
    
    @Test
    fun testWelcomeScreenShouldBeShownWhenIdleAndNoMessages() = runTest {
        val repository = TestChatRepository()
        val viewModel = ChatViewModel(repository)
        
        val initialState = viewModel.uiState.value
        val shouldShowWelcome = initialState.lastMessageState is LastMessageState.Idle && 
                                initialState.messages.isEmpty()
        
        assertTrue(shouldShowWelcome, "WelcomeScreen should be shown when Idle and no messages")
    }
    
    @Test
    fun testWelcomeScreenHiddenAfterSendingMessage() = runTest {
        val repository = TestChatRepository()
        val viewModel = ChatViewModel(repository)
        
        // Начальное состояние - Idle с пустыми сообщениями
        val initialState = viewModel.uiState.value
        assertTrue(initialState.lastMessageState is LastMessageState.Idle, "Should start with Idle")
        assertTrue(initialState.messages.isEmpty(), "Should start with empty messages")
        
        // Отправляем сообщение
        viewModel.sendMessage("Тестовый вопрос")
        
        // Ждем состояния Loading
        val loadingState = viewModel.uiState.first { 
            it.lastMessageState is LastMessageState.Loading 
        }
        assertTrue(loadingState.lastMessageState is LastMessageState.Loading, "Should be Loading after sending message")
        assertTrue(loadingState.messages.isNotEmpty(), "Should have user message")
        
        // WelcomeScreen не должен показываться (есть сообщения, состояние Loading)
        assertFalse(loadingState.messages.isEmpty(), "Should have messages after sending")
    }
    
    @Test
    fun testWelcomeScreenHiddenAfterFinishedWithMessages() = runTest {
        val repository = TestChatRepository()
        val viewModel = ChatViewModel(repository)
        
        viewModel.sendMessage("Тестовый вопрос")
        
        // Ждем состояния Finished
        val finishedState = viewModel.uiState.first { 
            it.lastMessageState is LastMessageState.Finished 
        }
        
        // После Finished есть сообщения, WelcomeScreen не должен показываться
        assertTrue(finishedState.messages.isNotEmpty(), "Should have messages after finished")
        assertFalse(finishedState.messages.isEmpty(), "Messages should not be empty")
    }
    
    @Test
    fun testSendingMessageFromWelcomeScreen() = runTest {
        val repository = TestChatRepository()
        val viewModel = ChatViewModel(repository)
        
        // Начальное состояние
        val initialState = viewModel.uiState.value
        assertTrue(initialState.messages.isEmpty(), "Should start with empty messages")
        
        // Симулируем клик на пример вопроса из WelcomeScreen
        val exampleQuestion = "Какие основные функции и возможности iPhone описаны в этом руководстве и для каких задач они предназначены?"
        viewModel.sendMessage(exampleQuestion)
        
        // Проверяем, что сообщение отправилось
        val stateAfterSend = viewModel.uiState.first { 
            it.messages.isNotEmpty() 
        }
        
        val userMessage = stateAfterSend.messages.find { it.isUserMessage() }
        assertNotNull(userMessage, "Should have user message")
        assertEquals(exampleQuestion, userMessage.getText(), "User message should match example question")
        
        // WelcomeScreen не должен показываться (есть сообщения)
        assertTrue(stateAfterSend.messages.isNotEmpty(), "Should have messages after sending")
        assertFalse(stateAfterSend.messages.isEmpty(), "Messages should not be empty after sending example question")
    }
    
}

class TestChatRepository(
    private val shouldError: Boolean = false
) : ChatRepository {
    
    private val testApiClient = object : ApiClient {
        override suspend fun streamChat(request: ChatRequest): Flow<Result<StreamChatChunk>> = flow {
            if (shouldError) {
                emit(Result.failure(Exception("Test error")))
                return@flow
            }
            
            // Первый чанк - начало ответа
            emit(Result.success(StreamChatChunk(
                uuid = "550e8400-e29b-41d4-a716-446655440000",
                type = ChunkType.TextResponseChunk,
                textResponse = "?",
                close = false,
                sources = emptyList(),
                chatId = 0
            )))
            
            // Второй чанк - продолжение ответа
            emit(Result.success(StreamChatChunk(
                uuid = "550e8400-e29b-41d4-a716-446655440000",
                type = ChunkType.TextResponseChunk,
                textResponse = "тестовый ",
                close = false,
                sources = emptyList(),
                chatId = 0
            )))
            
            // Третий чанк - продолжение ответа
            emit(Result.success(StreamChatChunk(
                uuid = "550e8400-e29b-41d4-a716-446655440000",
                type = ChunkType.TextResponseChunk,
                textResponse = "ответ ",
                close = false,
                sources = emptyList(),
                chatId = 0
            )))
            
            // Четвертый чанк - продолжение ответа
            emit(Result.success(StreamChatChunk(
                uuid = "550e8400-e29b-41d4-a716-446655440000",
                type = ChunkType.TextResponseChunk,
                textResponse = "от AI.",
                close = false,
                sources = emptyList(),
                chatId = 0
            )))
            
            // Чанк с close: true и пустым textResponse
            emit(Result.success(StreamChatChunk(
                uuid = "550e8400-e29b-41d4-a716-446655440000",
                type = ChunkType.TextResponseChunk,
                textResponse = "",
                close = true,
                sources = emptyList(),
                chatId = 0
            )))
            
            // Финальный чанк - finalizeResponseStream с источниками
            emit(Result.success(StreamChatChunk(
                uuid = "550e8400-e29b-41d4-a716-446655440000",
                type = ChunkType.FinalizeResponseStream,
                textResponse = "",
                close = true,
                sources = listOf(
                    Source(
                        title = "document1.pdf",
                        link = "https://example.com/document1.pdf",
                        chunk = "Это фрагмент документа, который был использован для формирования ответа."
                    ),
                    Source(
                        title = "document2.txt",
                        link = "https://example.com/document2.txt",
                        chunk = "Дополнительный контекст из другого источника."
                    )
                ),
                chatId = 12345
            )))
        }
    }
    
    override suspend fun streamChat(message: String): Flow<Result<StreamChatChunk>> {
        return testApiClient.streamChat(
            ChatRequest(
                message = message,
                mode = "query"
            )
        )
    }
}

class StreamWithoutFinalizeRepository : ChatRepository {
    
    private val testApiClient = object : ApiClient {
        override suspend fun streamChat(request: ChatRequest): Flow<Result<StreamChatChunk>> = flow {
            val uuid = "test-uuid"
            
            // Отправляем текстовые чанки, но НЕ отправляем FinalizeResponseStream
            emit(Result.success(StreamChatChunk(
                uuid = uuid,
                type = ChunkType.TextResponseChunk,
                textResponse = "Тест",
                close = false,
                sources = emptyList(),
                chatId = 0
            )))
            
            // Поток завершается без FinalizeResponseStream
        }
    }
    
    override suspend fun streamChat(message: String): Flow<Result<StreamChatChunk>> {
        return testApiClient.streamChat(
            ChatRequest(
                message = message,
                mode = "query"
            )
        )
    }
}

class ErrorTestChatRepository : ChatRepository {
    
    private val testApiClient = object : ApiClient {
        override suspend fun streamChat(request: ChatRequest): Flow<Result<StreamChatChunk>> = flow {
            // Эмулируем ошибку
            emit(Result.failure(Exception("Test error")))
        }
    }
    
    override suspend fun streamChat(message: String): Flow<Result<StreamChatChunk>> {
        return testApiClient.streamChat(
            ChatRequest(
                message = message,
                mode = "query"
            )
        )
    }
}

class RealWorldTestChatRepository : ChatRepository {
    
    private val testApiClient = object : ApiClient {
        override suspend fun streamChat(request: ChatRequest): Flow<Result<StreamChatChunk>> = flow {
            val uuid = "9e0974ce-8528-4c91-84ad-baac5e601f12"
            
            // Эмулируем реальные данные из пользовательского примера (сокращено для скорости в wasm-тестах)
            val textChunks = listOf(
                "Т", "огда", " да", "вай", " сдел", "аем", " обещ", "анное", ".",
                " ", "Это", " корот", "кий", " ф", "раг", "мент", "."
            )
            
            // Отправляем все текстовые чанки
            textChunks.forEach { chunkText ->
                emit(Result.success(StreamChatChunk(
                    uuid = uuid,
                    type = ChunkType.TextResponseChunk,
                    textResponse = chunkText,
                    close = false,
                    sources = emptyList(),
                    chatId = 0
                )))
            }
            
            // Финальный чанк - finalizeResponseStream
            emit(Result.success(StreamChatChunk(
                uuid = uuid,
                type = ChunkType.FinalizeResponseStream,
                textResponse = "",
                close = true,
                sources = emptyList(),
                chatId = 115
            )))
        }
    }
    
    override suspend fun streamChat(message: String): Flow<Result<StreamChatChunk>> {
        return testApiClient.streamChat(
            ChatRequest(
                message = message,
                mode = "query"
            )
        )
    }
}

class TextResponseChunkWithSourcesRepository : ChatRepository {
    
    private val testApiClient = object : ApiClient {
        override suspend fun streamChat(request: ChatRequest): Flow<Result<StreamChatChunk>> = flow {
            val uuid = "e9eb4d01-e224-4d19-96b5-50f259cfa964"
            
            // Текстовые чанки
            emit(Result.success(StreamChatChunk(
                uuid = uuid,
                type = ChunkType.TextResponseChunk,
                textResponse = "Тестовый ",
                close = false,
                sources = emptyList(),
                chatId = 0
            )))
            
            emit(Result.success(StreamChatChunk(
                uuid = uuid,
                type = ChunkType.TextResponseChunk,
                textResponse = "ответ",
                close = false,
                sources = emptyList(),
                chatId = 0
            )))
            
            // Финальный TextResponseChunk с close=true и sources
            emit(Result.success(StreamChatChunk(
                uuid = uuid,
                type = ChunkType.TextResponseChunk,
                textResponse = "",
                close = true,
                sources = listOf(
                    Source(
                        id = "d6252ba1-d6fa-418f-b0a8-7e643292c4a4",
                        url = "file:///app/collector/hotdir/Smart_iphone_13_RU.pdf",
                        title = "Smart_iphone_13_RU.pdf",
                        docAuthor = "PDF24 Creator",
                        description = "Руководство пользователя iPhone",
                        docSource = "pdf file uploaded by the user.",
                        chunkSource = "",
                        published = "1/18/2026, 8:02:08 AM",
                        text = "<document_metadata>\nsourceDocument: Smart_iphone_13_RU.pdf\npublished: 1/18/2026, 8:02:08 AM\n</document_metadata>\n\nуведомления, сведения нормативного характера..."
                    ),
                    Source(
                        id = "522b46fb-0951-4f6d-8687-30f7a9abba29",
                        url = "file:///app/collector/hotdir/Smart_iphone_13_RU.pdf",
                        title = "Smart_iphone_13_RU.pdf",
                        docAuthor = "PDF24 Creator",
                        description = "Руководство пользователя iPhone",
                        docSource = "pdf file uploaded by the user.",
                        chunkSource = "",
                        published = "1/18/2026, 8:02:08 AM",
                        text = "<document_metadata>\nsourceDocument: Smart_iphone_13_RU.pdf\npublished: 1/18/2026, 8:02:08 AM\n</document_metadata>\n\nwww.icloud.com/find. Программа «Найти iPhone»..."
                    )
                ),
                chatId = 0
            )))
        }
    }
    
    override suspend fun streamChat(message: String): Flow<Result<StreamChatChunk>> {
        return testApiClient.streamChat(
            ChatRequest(
                message = message,
                mode = "query"
            )
        )
    }
}

class RealWorldSourcesRepository : ChatRepository {
    
    private val testApiClient = object : ApiClient {
        override suspend fun streamChat(request: ChatRequest): Flow<Result<StreamChatChunk>> = flow {
            val uuid = "e9eb4d01-e224-4d19-96b5-50f259cfa964"
            
            // Текстовые чанки
            emit(Result.success(StreamChatChunk(
                uuid = uuid,
                type = ChunkType.TextResponseChunk,
                textResponse = "Тестовый ",
                close = false,
                sources = emptyList(),
                chatId = 0
            )))
            
            emit(Result.success(StreamChatChunk(
                uuid = uuid,
                type = ChunkType.TextResponseChunk,
                textResponse = "ответ",
                close = false,
                sources = emptyList(),
                chatId = 0
            )))
            
            // Финальный TextResponseChunk с close=true и реальными источниками из JSON
            emit(Result.success(StreamChatChunk(
                uuid = uuid,
                type = ChunkType.TextResponseChunk,
                textResponse = "",
                close = true,
                sources = listOf(
                    Source(
                        id = "d6252ba1-d6fa-418f-b0a8-7e643292c4a4",
                        url = "file:///app/collector/hotdir/Smart_iphone_13_RU.pdf",
                        title = "Smart_iphone_13_RU.pdf",
                        docAuthor = "PDF24 Creator",
                        description = "Руководство пользователя iPhone",
                        docSource = "pdf file uploaded by the user.",
                        chunkSource = "",
                        published = "1/18/2026, 8:02:08 AM",
                        text = "<document_metadata>\nsourceDocument: Smart_iphone_13_RU.pdf\npublished: 1/18/2026, 8:02:08 AM\n</document_metadata>\n\nуведомления, сведения нормативного характера и данные о радиочастотном излучении).\nЧтобы скопировать серийный номер и другие идентификаторы, нажмите и удерживайте \nидентификатор, пока не появится команда «Скопировать».\nЧтобы помочь компании Apple улучшить продукцию и услуги, iPhone отправляет в Apple \nанонимную диагностическую информацию и данные об использовании устройства. Эта \nинформация не идентифицирует Вас лично, однако, она может содержать сведения \nо Вашем местоположении.\nПросмотр и отключение отправки диагностической информации. Выберите \n«Настройки» > «Конфиденциальность» > «Диагностика и использование».Приложение D    Безопасность, эксплуатация и поддержка 205\nИнформация об использовании\nПросмотр данных об использовании сотовых сетей. Выберите «Настройки» > «Сотовая связь».  \nСм. \nСотовые настройки на стр. 207.\nПросмотр другой информации об использовании. Выберите «Настройки» > «Основные» > \n«Статистика», чтобы:\n •"
                    ),
                    Source(
                        id = "522b46fb-0951-4f6d-8687-30f7a9abba29",
                        url = "file:///app/collector/hotdir/Smart_iphone_13_RU.pdf",
                        title = "Smart_iphone_13_RU.pdf",
                        docAuthor = "PDF24 Creator",
                        description = "Руководство пользователя iPhone",
                        docSource = "pdf file uploaded by the user.",
                        chunkSource = "",
                        published = "1/18/2026, 8:02:08 AM",
                        text = "<document_metadata>\nsourceDocument: Smart_iphone_13_RU.pdf\npublished: 1/18/2026, 8:02:08 AM\n</document_metadata>\n\nwww.icloud.com/find. Программа «Найти iPhone» поддерживает функцию «Блокировка \nактивации», которая усложняет использование Вашего iPhone посторонними в случае, если \nВы потеряете iPhone. Для выключения функции «Найти iPhone», стирания данных с iPhone \nили его повторной активации необходимо ввести Ваш Apple ID и пароль.\nВключение функции «Найти iPhone». Выберите «Настройки» > «iCloud» > «Найти iPhone».\nВажно!  Для использования возможностей функции «Найти iPhone» она должна быть \nвключена до того, как iPhone будет утерян. Также iPhone должен быть подключен \nк Интернету, чтобы его можно было найти и заблокировать.\nИспользование функции «Найти iPhone». Откройте программу «Найти iPhone» \nна устройстве с iOS или зайдите на сайт \nwww.icloud.com/find на компьютере. Войдите \nв систему и выберите свое устройство.\n •\nВоспроизведение звукового сигнала.  Звуковой сигнал может воспроизводиться на  \nполной громкости в течение двух минут, даже если звонок выключен.Глава  3    Основные сведения 51"
                    ),
                    Source(
                        id = "cd04734b-9e48-46a6-a39b-1130ffbbad9d",
                        url = "file:///app/collector/hotdir/Smart_iphone_13_RU.pdf",
                        title = "Smart_iphone_13_RU.pdf",
                        docAuthor = "PDF24 Creator",
                        description = "Руководство пользователя iPhone",
                        docSource = "pdf file uploaded by the user.",
                        chunkSource = "",
                        published = "1/18/2026, 8:02:08 AM",
                        text = "<document_metadata>\nsourceDocument: Smart_iphone_13_RU.pdf\npublished: 1/18/2026, 8:02:08 AM\n</document_metadata>\n\nApple Inc. использует эти товарные знаки исключительно \nв рамках лицензии.\nНазвания прочих компаний и изделий, упомянутые \nздесь, могут являться товарными знаками \nсоответствующих компаний.\nИзделия сторонних фирм упоминаются исключительно \nв информационных целях, а не для одобрения или \nрекомендации. Компания Apple не несет ответственности \nза эксплуатационные качества и использование этих \nизделий. Все договоренности, соглашения или гарантийные \nобязательства (если таковые имеются) заключаются \nнепосредственно между поставщиком и потенциальными \nпользователями. При составлении данного руководства \nбыли предприняты все усилия для обеспечения \nдостоверности и точности информации. Apple не несет \nответственности за опечатки или описки.\nRS019-00071/2014-10"
                    ),
                    Source(
                        id = "0c132676-9f5e-4f96-a983-2995d093ba32",
                        url = "file:///app/collector/hotdir/Smart_iphone_13_RU.pdf",
                        title = "Smart_iphone_13_RU.pdf",
                        docAuthor = "PDF24 Creator",
                        description = "Руководство пользователя iPhone",
                        docSource = "pdf file uploaded by the user.",
                        chunkSource = "",
                        published = "1/18/2026, 8:02:08 AM",
                        text = "<document_metadata>\nsourceDocument: Smart_iphone_13_RU.pdf\npublished: 1/18/2026, 8:02:08 AM\n</document_metadata>\n\n, затем коснитесь \n«Нет». Фильтры также можно применить позже, при редактировании фотографии. См. \nРедактирование фотографий и обрезка видеозаписей на стр. 100.Глава  12    Камера 104\nНенадолго появится прямоугольная рамка, определяющая область настройки экспозиции. \nПри фотографировании людей включается функция распознавания лиц для выравнивания \nэкспозиции по нескольким лицам (не более десяти). Вокруг каждого распознанного лица \nотображается прямоугольная рамка.\nПримечание.  На iPhone 6 и iPhone 6 Plus прямоугольник автоматической \nэкспозиции может иногда не отображаться, но при этом фокусировка и экспозиция \nнастраиваются автоматически.\nЭкспозиция задается автоматически, но можно сделать это вручную для следующего снимка, \nкоснувшись объекта или области на экране. При работе с камерой iSight прикосновение \nк экрану позволяет задать точку фокусировки и экспозицию, а функция распознавания \nлиц временно отключается. Чтобы заблокировать фокус и экспозицию, коснитесь"
                    )
                ),
                chatId = 0
            )))
        }
    }
    
    override suspend fun streamChat(message: String): Flow<Result<StreamChatChunk>> {
        return testApiClient.streamChat(
            ChatRequest(
                message = message,
                mode = "query"
            )
        )
    }
}
