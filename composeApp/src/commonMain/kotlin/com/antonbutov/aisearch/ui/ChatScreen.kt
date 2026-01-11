package com.antonbutov.aisearch.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    var uiState by remember { mutableStateOf(viewModel.uiState.value) }
    var messageText by remember { mutableStateOf("") }
    var showSourcesDialog by remember { mutableStateOf(false) }
    var sourcesToShow by remember { mutableStateOf<List<com.antonbutov.aisearch.ui.model.Source>>(emptyList()) }
    val listState = rememberLazyListState()
    
    LaunchedEffect(Unit) {
        viewModel.uiState.collect { newState ->
            uiState = newState
        }
    }
    
    LaunchedEffect(uiState.messages.size, uiState.lastMessageState) {
        val totalItems = uiState.messages.size + if (uiState.lastMessageState is LastMessageState.Message || uiState.lastMessageState is LastMessageState.Loading) 1 else 0
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.messages) { message ->
                MessageBubble(
                    message = message,
                    onSourcesClick = { sources ->
                        sourcesToShow = sources
                        showSourcesDialog = true
                    }
                )
            }
            
            when (val lastMessageState = uiState.lastMessageState) {
                is LastMessageState.Loading -> {
                    item {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(16.dp)
                        )
                    }
                }
                is LastMessageState.Message -> {
                    item {
                        MessageBubble(
                            message = ChatMessage.ChatMessageText(
                                text = lastMessageState.text,
                                isUser = false
                            )
                        )
                    }
                }
                is LastMessageState.Finished -> {
                    // После Finished переводим в Idle
                    // Ничего не показываем, только сообщения
                }
                is LastMessageState.Idle -> {
                    // Показываем приветственный экран только если нет сообщений
                    if (uiState.messages.isEmpty()) {
                        item {
                            WelcomeScreen(
                                onExampleQuestionClick = { question ->
                                    viewModel.sendMessage(question)
                                }
                            )
                        }
                    }
                }
            }
        }

        MessageInput(
            messageText = messageText,
            onMessageTextChange = { messageText = it },
            onSendClick = {
                if (messageText.isNotBlank() && uiState.lastMessageState !is LastMessageState.Loading) {
                    viewModel.sendMessage(messageText)
                    messageText = ""
                }
            },
            isLoading = uiState.lastMessageState is LastMessageState.Loading
        )
        
        // Диалог с источниками
        if (showSourcesDialog) {
            SourcesDialog(
                sources = sourcesToShow,
                onDismiss = { showSourcesDialog = false }
            )
        }
    }
}
