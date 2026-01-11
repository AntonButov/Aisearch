package com.antonbutov.aisearch.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    val message: String,
    val mode: String = "query"
)
