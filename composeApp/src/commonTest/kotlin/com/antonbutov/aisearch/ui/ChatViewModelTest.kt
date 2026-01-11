package com.antonbutov.aisearch.ui

import com.antonbutov.aisearch.data.model.Source
import com.antonbutov.aisearch.data.model.StreamChatChunk
import com.antonbutov.aisearch.domain.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ChatViewModelTest {
    
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
        
        viewModel.sendMessage("Тест")
        
        val state = viewModel.uiState.value
        assertTrue(state.messages.isNotEmpty() || state.isLoading, "Should add message or start loading")
    }
    
    @Test
    fun testSendMessageIgnoresBlankMessage() = runTest {
        val repository = TestChatRepository()
        val viewModel = ChatViewModel(repository)
        
        val initialState = viewModel.uiState.value
        val initialMessageCount = initialState.messages.size
        
        viewModel.sendMessage("")
        
        val stateAfterBlank = viewModel.uiState.value
        assertEquals(initialMessageCount, stateAfterBlank.messages.size, "Should not add blank message")
    }
}

class TestChatRepository(
    private val shouldError: Boolean = false
) : ChatRepository {
    
    private val testApiClient = object : com.antonbutov.aisearch.data.api.ApiClient {
        override suspend fun streamChat(request: com.antonbutov.aisearch.data.model.ChatRequest): Flow<Result<StreamChatChunk>> = flow {
            if (shouldError) {
                emit(Result.failure(Exception("Test error")))
                return@flow
            }
            
            emit(Result.success(StreamChatChunk(
                uuid = "test-uuid",
                type = "textResponseChunk",
                textResponse = "Тест",
                close = false,
                error = false,
                sources = listOf(Source(title = "Источник 1"))
            )))
            
            emit(Result.success(StreamChatChunk(
                uuid = "test-uuid",
                type = "finalizeResponseStream",
                textResponse = "",
                close = true,
                error = false,
                sources = null
            )))
        }
    }
    
    override suspend fun streamChat(message: String): Flow<Result<StreamChatChunk>> {
        return testApiClient.streamChat(
            com.antonbutov.aisearch.data.model.ChatRequest(
                message = message,
                mode = "query"
            )
        )
    }
}
