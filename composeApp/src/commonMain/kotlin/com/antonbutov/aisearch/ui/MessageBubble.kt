package com.antonbutov.aisearch.ui

import androidx.compose.runtime.Composable
import com.antonbutov.aisearch.ui.model.Source

@Composable
fun MessageBubble(
    message: ChatMessage,
    onSourcesClick: (List<Source>) -> Unit = {}
) {
    when (message) {
        is ChatMessage.ChatMessageText -> {
            TextMessageBubble(
                text = message.text,
                isUser = message.isUser
            )
        }
        is ChatMessage.ChatMessageSources -> {
            SourcesMessageBubble(
                sources = message.sources,
                onSourcesClick = onSourcesClick
            )
        }
    }
}
