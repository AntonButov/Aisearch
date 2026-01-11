package com.antonbutov.aisearch.data.api

import com.antonbutov.aisearch.data.model.StreamChatChunk
import kotlinx.coroutines.flow.Flow

interface ApiClient {
    suspend fun streamChat(request: com.antonbutov.aisearch.data.model.ChatRequest): Flow<Result<StreamChatChunk>>
}
