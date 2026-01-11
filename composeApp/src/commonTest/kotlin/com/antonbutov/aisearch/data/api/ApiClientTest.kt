package com.antonbutov.aisearch.data.api

import com.antonbutov.aisearch.data.model.ChatRequest
import com.antonbutov.aisearch.data.model.StreamChatChunk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ApiClientTest {
    
    @Test
    fun testStreamChatReturnsFlowOfResult() = runTest {
        val apiClient: ApiClient = ApiClientImpl()
        val request = ChatRequest(
            message = "Тест",
            mode = "query"
        )
        
        val flow = apiClient.streamChat(request)
        assertNotNull(flow, "Should return Flow")
    }
}
