package com.antonbutov.aisearch.data.api

import com.antonbutov.aisearch.data.model.ChatRequest
import com.antonbutov.aisearch.data.model.StreamChatChunk
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

interface ApiClient {
    suspend fun streamChat(request: ChatRequest): Flow<Result<StreamChatChunk>>
}

class ApiClientImpl : ApiClient {
    
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = false
            })
        }
    }
    
    override suspend fun streamChat(request: ChatRequest): Flow<Result<StreamChatChunk>> = flow {
        val endpoint = "https://chat.pravochat.ru/api/workspace/ios/stream-chat"
        
        val responseResult = runCatching {
            client.post(endpoint) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }
        }
        
        responseResult.onFailure {
            emit(Result.failure(it))
            return@flow
        }
        
        val bodyResult = runCatching {
            responseResult.getOrThrow().body<String>()
        }
        
        bodyResult.onFailure {
            emit(Result.failure(it))
            return@flow
        }
        
        val body = bodyResult.getOrThrow()
        
        body.split("\n").forEach { line ->
            if (line.startsWith("data: ")) {
                val jsonData = line.removePrefix("data: ").trim()
                if (jsonData.isNotEmpty() && jsonData != "[DONE]") {
                    val chunkResult = runCatching {
                        Json.decodeFromString<StreamChatChunk>(jsonData)
                    }
                    emit(chunkResult)
                }
            } else if (line.trim().isNotEmpty() && !line.startsWith(":")) {
                val chunkResult = runCatching {
                    Json.decodeFromString<StreamChatChunk>(line.trim())
                }
                emit(chunkResult)
            }
        }
    }
}
