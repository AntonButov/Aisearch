package com.antonbutov.aisearch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MessageInput(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    // Флаг для отслеживания, был ли нажат Ctrl/Cmd при последнем Enter
    var allowNewline by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = messageText,
            onValueChange = { newText ->
                // Если текст заканчивается на \n (Enter был нажат)
                if (newText.endsWith("\n") && !messageText.endsWith("\n")) {
                    if (allowNewline) {
                        // Ctrl+Enter был нажат - разрешаем новую строку
                        allowNewline = false
                        onMessageTextChange(newText)
                    } else {
                        // Обычный Enter - удаляем \n и отправляем сообщение
                        val textWithoutNewline = newText.dropLast(1)
                        onMessageTextChange(textWithoutNewline)
                        if (textWithoutNewline.isNotBlank() && !isLoading) {
                            onSendClick()
                        }
                    }
                } else {
                    // Обычное изменение текста
                    onMessageTextChange(newText)
                }
            },
            modifier = Modifier
                .weight(1f)
                .onPreviewKeyEvent { keyEvent ->
                    // Отслеживаем нажатие Ctrl/Cmd перед Enter
                    if (keyEvent.key == Key.Enter) {
                        if (keyEvent.type == KeyEventType.KeyDown) {
                            // При нажатии Enter устанавливаем флаг, если Ctrl/Cmd нажат
                            allowNewline = keyEvent.isCtrlPressed || keyEvent.isMetaPressed
                        }
                        // Не блокируем событие, чтобы TextField мог обработать его
                        false
                    } else {
                        false
                    }
                },
            placeholder = { 
                Text(
                    text = "Введите запрос...",
                    style = TextStyle(
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                )
            },
            textStyle = TextStyle(
                fontSize = 18.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Normal
            ),
            enabled = !isLoading,
            singleLine = false,
            maxLines = 4,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        IconButton(
            onClick = onSendClick,
            enabled = messageText.isNotBlank() && !isLoading,
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (messageText.isNotBlank() && !isLoading) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = SendIcon,
                    contentDescription = "Отправить",
                    tint = if (messageText.isNotBlank() && !isLoading) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                    },
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
