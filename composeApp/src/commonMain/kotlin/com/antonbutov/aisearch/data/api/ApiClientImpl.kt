package com.antonbutov.aisearch.data.api

import com.antonbutov.aisearch.data.model.ChatRequest
import com.antonbutov.aisearch.data.model.StreamChatChunk
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

private const val ENDPOINT = "https://chat.pravochat.ru/api/workspace/ios/stream-chat"

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = false
}

class ApiClientImpl(
    private val client: HttpClient = HttpClient {
        // Без ContentNegotiation для минимизации буферизации
    }
) : ApiClient {
    
    override suspend fun streamChat(request: ChatRequest): Flow<Result<StreamChatChunk>> = flow {
        try {
            val requestJson = json.encodeToString(ChatRequest.serializer(), request)
            
            HttpStatement(
                builder = HttpRequestBuilder().apply {
                    method = HttpMethod.Post
                    url(ENDPOINT)
                    setBody(requestJson)
                    contentType(ContentType.Application.Json)
                    accept(ContentType.Text.EventStream)
                    headers {
                        append(HttpHeaders.CacheControl, "no-cache")
                        append(HttpHeaders.Connection, "keep-alive")
                        remove(HttpHeaders.AcceptEncoding)
                        append(HttpHeaders.AcceptEncoding, "identity")
                    }
                },
                client = client
            ).execute { response ->
                streamEventsFrom(response, json)
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
