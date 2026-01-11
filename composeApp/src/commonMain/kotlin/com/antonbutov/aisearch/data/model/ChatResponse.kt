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
    val id: String? = null,
    val url: String? = null,
    val title: String,
    val docAuthor: String? = null,
    val description: String? = null,
    val docSource: String? = null,
    val chunkSource: String? = null,
    val published: String? = null,
    val link: String? = null,
    val chunk: String? = null,
    val text: String? = null
)
