package com.antonbutov.aisearch.data.model

import kotlinx.serialization.Serializable

@Serializable
data class StreamChatChunk(
    val uuid: String? = null,
    val type: String? = null,
    val textResponse: String? = null,
    val close: Boolean = false,
    val error: Boolean = false,
    val sources: List<Source>? = null,
    val chatId: Int? = null,
    val metrics: Metrics? = null
)

@Serializable
data class Metrics(
    val completion_tokens: Int? = null,
    val prompt_tokens: Int? = null,
    val total_tokens: Int? = null,
    val outputTps: Double? = null,
    val duration: Double? = null
)