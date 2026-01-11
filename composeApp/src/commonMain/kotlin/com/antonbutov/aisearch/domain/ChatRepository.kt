package com.antonbutov.aisearch.domain

import com.antonbutov.aisearch.data.api.ApiClient
import com.antonbutov.aisearch.data.model.ChatRequest
import com.antonbutov.aisearch.data.model.StreamChatChunk
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    suspend fun streamChat(message: String): Flow<Result<StreamChatChunk>>
}

class ChatRepositoryImpl(private val apiClient: ApiClient) : ChatRepository {
    override suspend fun streamChat(message: String): Flow<Result<StreamChatChunk>> {
        return apiClient.streamChat(
            ChatRequest(
                message = message,
                mode = "query"
            )
        )
    }
}
