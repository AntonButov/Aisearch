package com.antonbutov.aisearch.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatResponse(
    val response: String,
    val sources: List<Source>? = null,
    val error: String? = null
)

@Serializable
data class Source(
    val title: String,
    val link: String? = null,
    val chunk: String? = null
)
