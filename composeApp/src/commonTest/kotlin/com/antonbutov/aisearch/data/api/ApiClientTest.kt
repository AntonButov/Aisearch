package com.antonbutov.aisearch.data.api

import com.antonbutov.aisearch.data.model.ChatRequest
import com.antonbutov.aisearch.data.model.StreamChatChunk
import com.antonbutov.aisearch.data.model.ChunkType
import app.cash.turbine.test
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApiClientTest {
    
    private fun createMockClient(sseResponse: String): HttpClient {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
        }
        
        return HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    when (request.url.encodedPath) {
                        "/api/workspace/ios/stream-chat" -> {
                            respond(
                                content = ByteReadChannel(sseResponse.toByteArray()),
                                status = HttpStatusCode.OK,
                                headers = headersOf(HttpHeaders.ContentType, ContentType.Text.EventStream.toString())
                            )
                        }
                        else -> error("Unhandled ${request.url.encodedPath}")
                    }
                }
            }
            install(ContentNegotiation) {
                json(json)
            }
        }
    }
    
    @Test
    fun testStreamChatReturnsFlowOfResult() = runTest {
        val sseResponse = """
            data: {"type":"textResponseChunk","textResponse":"Hello"}
            
            data: {"type":"finalizeResponseStream","close":true}
            
        """.trimIndent()
        
        val mockClient = createMockClient(sseResponse)
        val apiClient: ApiClient = ApiClientImpl(mockClient)
        val request = ChatRequest(
            message = "Тест",
            mode = "query"
        )
        
        val flow = apiClient.streamChat(request)
        assertNotNull(flow, "Should return Flow")
    }
    
    @Test
    fun testStreamChatEmitsChunksIncrementally() = runTest {
        val sseResponse = """
            data: {"type":"textResponseChunk","textResponse":"Hello"}
            
            data: {"type":"textResponseChunk","textResponse":" World"}
            
            data: {"type":"finalizeResponseStream","close":true}
            
        """.trimIndent()
        
        val mockClient = createMockClient(sseResponse)
        val apiClient: ApiClient = ApiClientImpl(mockClient)
        val request = ChatRequest(
            message = "привет",
            mode = "query"
        )
        
        apiClient.streamChat(request).test(timeout = kotlin.time.Duration.parse("3s")) {
            // Проверяем, что Flow эмитит данные постепенно
            var chunkCount = 0
            
            // Получаем первый чанк
            val firstItem = awaitItem()
            chunkCount++
            
            assertTrue(firstItem.isSuccess, "First chunk should be successful")
            val firstChunk = firstItem.getOrNull()
            assertNotNull(firstChunk, "First chunk should not be null")
            assertEquals(ChunkType.TextResponseChunk, firstChunk.type, "First chunk should be TextResponseChunk")
            assertEquals("Hello", firstChunk.textResponse, "First chunk text should be 'Hello'")
            println("✓ Received first chunk: type=${firstChunk.type}, textResponse='${firstChunk.textResponse}'")
            
            // Получаем второй чанк
            val secondItem = awaitItem()
            chunkCount++
            
            assertTrue(secondItem.isSuccess, "Second chunk should be successful")
            val secondChunk = secondItem.getOrNull()
            assertNotNull(secondChunk, "Second chunk should not be null")
            assertEquals(ChunkType.TextResponseChunk, secondChunk.type, "Second chunk should be TextResponseChunk")
            assertEquals(" World", secondChunk.textResponse, "Second chunk text should be ' World'")
            println("✓ Received second chunk - streaming works!")
            
            // Получаем финальный чанк
            val finalItem = awaitItem()
            chunkCount++
            
            assertTrue(finalItem.isSuccess, "Final chunk should be successful")
            val finalChunk = finalItem.getOrNull()
            assertNotNull(finalChunk, "Final chunk should not be null")
            assertEquals(ChunkType.FinalizeResponseStream, finalChunk.type, "Final chunk should be FinalizeResponseStream")
            println("✓ Received final chunk: type=${finalChunk.type}")
            
            assertTrue(chunkCount >= 3, "Should receive at least 3 chunks")
            
            // Ожидаем завершения Flow
            awaitComplete()
        }
    }
    
    @Test
    fun testStreamChatHandlesSSEFormat() = runTest {
        val sseResponse = """
            data: {"type":"textResponseChunk","textResponse":"Test"}
            
            data: {"type":"finalizeResponseStream","close":true}
            
        """.trimIndent()
        
        val mockClient = createMockClient(sseResponse)
        val apiClient: ApiClient = ApiClientImpl(mockClient)
        val request = ChatRequest(
            message = "test",
            mode = "query"
        )
        
        apiClient.streamChat(request).test(timeout = kotlin.time.Duration.parse("3s")) {
            // Проверяем, что Flow правильно обрабатывает SSE формат
            var receivedChunks = 0
            
            val firstItem = awaitItem()
            receivedChunks++
            
            assertTrue(firstItem.isSuccess, "First chunk should be successful")
            firstItem.onSuccess { chunk ->
                assertNotNull(chunk, "Chunk should not be null")
                assertEquals(ChunkType.TextResponseChunk, chunk.type, "First chunk should be TextResponseChunk")
                println("✓ Chunk $receivedChunks: type=${chunk.type}, textResponse='${chunk.textResponse}'")
            }
            
            val secondItem = awaitItem()
            receivedChunks++
            
            assertTrue(secondItem.isSuccess, "Second chunk should be successful")
            secondItem.onSuccess { chunk ->
                assertNotNull(chunk, "Chunk should not be null")
                assertEquals(ChunkType.FinalizeResponseStream, chunk.type, "Second chunk should be FinalizeResponseStream")
                println("✓ Chunk $receivedChunks: type=${chunk.type}")
            }
            
            assertTrue(receivedChunks >= 2, "Should receive at least 2 chunks")
            
            // Ожидаем завершения Flow
            awaitComplete()
        }
    }
}
