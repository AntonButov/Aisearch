package com.antonbutov.aisearch.data.api

import com.antonbutov.aisearch.data.model.StreamChatChunk
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json

private const val STREAM_PREFIX = "data:"

/**
 * Парсит SSE события из ответа и эмитит их в FlowCollector.
 */
internal suspend inline fun FlowCollector<Result<StreamChatChunk>>.streamEventsFrom(
    response: HttpResponse,
    json: Json
) {
    val channel: ByteReadChannel = response.body()
    try {
        while (currentCoroutineContext().isActive && !channel.isClosedForRead) {
            val line = channel.readUTF8Line() ?: break
            if (line.startsWith(STREAM_PREFIX)) {
                val jsonData = line.removePrefix(STREAM_PREFIX).trimStart()
                if (jsonData.isNotEmpty()) {
                    emit(runCatching {
                        json.decodeFromString<StreamChatChunk>(jsonData)
                    })
                }
            }
        }
    } finally {
        channel.cancel()
    }
}
