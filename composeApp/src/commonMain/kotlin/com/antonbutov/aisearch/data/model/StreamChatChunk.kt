package com.antonbutov.aisearch.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = ChunkTypeSerializer::class)
enum class ChunkType {
    TextResponseChunk,
    FinalizeResponseStream;
    
    companion object {
        fun fromString(value: String): ChunkType {
            return when (value) {
                "textResponseChunk" -> TextResponseChunk
                "finalizeResponseStream" -> FinalizeResponseStream
                else -> throw IllegalArgumentException("Unknown chunk type: $value")
            }
        }
    }
}

object ChunkTypeSerializer : KSerializer<ChunkType> {
    override val descriptor: SerialDescriptor = serialDescriptor<String>()

    override fun serialize(encoder: Encoder, value: ChunkType) {
        val stringValue = when (value) {
            ChunkType.TextResponseChunk -> "textResponseChunk"
            ChunkType.FinalizeResponseStream -> "finalizeResponseStream"
        }
        encoder.encodeString(stringValue)
    }

    override fun deserialize(decoder: Decoder): ChunkType {
        val stringValue = decoder.decodeString()
        return ChunkType.fromString(stringValue)
    }
}

@Serializable
data class StreamChatChunk(
    val uuid: String? = null,
    val type: ChunkType,
    val textResponse: String? = null,
    val close: Boolean? = null,
    val sources: List<Source>? = null,
    val chatId: Int? = null
)
