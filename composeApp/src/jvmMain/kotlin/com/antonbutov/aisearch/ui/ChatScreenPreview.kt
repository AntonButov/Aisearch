package com.antonbutov.aisearch.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.desktop.ui.tooling.preview.Preview
import com.antonbutov.aisearch.ui.model.Source

// MessageBubble Previews
@Preview
@Composable
fun MessageBubbleUserPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MessageBubble(
                message = ChatMessage.ChatMessageText(
                    text = "Привет! Как дела?",
                    isUser = true
                )
            )
        }
    }
}

@Preview
@Composable
fun MessageBubbleAiPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MessageBubble(
                message = ChatMessage.ChatMessageText(
                    text = "Привет! У меня всё отлично, спасибо за вопрос. Чем могу помочь?",
                    isUser = false
                )
            )
        }
    }
}

@Preview
@Composable
fun MessageBubbleWithSourcesPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MessageBubble(
                    message = ChatMessage.ChatMessageText(
                        text = "Это ответ с источниками информации, которые были использованы для формирования ответа.",
                        isUser = false
                    )
                )
                MessageBubble(
                    message = ChatMessage.ChatMessageSources(
                        sources = listOf(
                            com.antonbutov.aisearch.ui.model.Source(
                                "document1.pdf",
                                "Фрагмент из первого документа..."
                            ),
                            com.antonbutov.aisearch.ui.model.Source(
                                "document2.txt",
                                "Фрагмент из второго документа..."
                            ),
                            com.antonbutov.aisearch.ui.model.Source("article.md", "Фрагмент из статьи...")
                        )
                    )
                )
            }
        }
    }
}

// MessageInput Previews
@Preview
@Composable
fun MessageInputEmptyPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MessageInput(
                messageText = "",
                onMessageTextChange = { },
                onSendClick = { },
                isLoading = false
            )
        }
    }
}

@Preview
@Composable
fun MessageInputWithTextPreview() {
    AppTheme {
        var messageText by remember { mutableStateOf("Как работает RAG?") }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MessageInput(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendClick = { },
                isLoading = false
            )
        }
    }
}

@Preview
@Composable
fun MessageInputLoadingPreview() {
    AppTheme {
        var messageText by remember { mutableStateOf("Как работает RAG?") }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MessageInput(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendClick = { },
                isLoading = true
            )
        }
    }
}

// ErrorCard Previews
@Preview
@Composable
fun ErrorCardShortPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ErrorCard(error = "Не удалось подключиться к серверу")
        }
    }
}

@Preview
@Composable
fun ErrorCardLongPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ErrorCard(
                error = "Произошла ошибка при обработке запроса. Пожалуйста, попробуйте еще раз или обратитесь в службу поддержки, если проблема сохраняется."
            )
        }
    }
}

@Preview
@Composable
fun ErrorCardNetworkPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ErrorCard(error = "Network error: Connection timeout")
        }
    }
}

// SourceItem Previews
@Preview
@Composable
fun SourceItemPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SourceItem(
                source = Source(
                    title = "Руководство пользователя iPhone",
                    text = "уведомления, сведения нормативного характера и данные о радиочастотном излучении). Чтобы скопировать серийный номер и другие идентификаторы, нажмите и удерживайте идентификатор, пока не появится команда «Копировать»."
                ),
                onClick = {}
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun SourcesFlowRowWide1Preview() {
    AppTheme {
        Box(modifier = Modifier.width(1000.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Источники:",
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "уведомления, сведения нормативного характера и данные о радиочастотном излучении). Чтобы скопировать серийный номер и другие идентификаторы, нажмите и удерживайте идентификатор, пока не появится команда «Копировать»."
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun SourcesFlowRowNarrow1Preview() {
    AppTheme {
        Box(modifier = Modifier.width(360.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Источники:",
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "уведомления, сведения нормативного характера и данные о радиочастотном излучении). Чтобы скопировать серийный номер и другие идентификаторы, нажмите и удерживайте идентификатор, пока не появится команда «Копировать»."
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun SourcesFlowRowWide2Preview() {
    AppTheme {
        Box(modifier = Modifier.width(1000.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Источники:",
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "уведомления, сведения нормативного характера и данные о радиочастотном излучении). Чтобы скопировать серийный номер и другие идентификаторы, нажмите и удерживайте идентификатор, пока не появится команда «Копировать»."
                        )
                    )
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "www.icloud.com/find. Программа «Найти iPhone» поддерживает функцию «Блокировка активации», которая усложняет использование Вашего iPhone посторонними в случае, если Вы потеряете iPhone."
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun SourcesFlowRowNarrow2Preview() {
    AppTheme {
        Box(modifier = Modifier.width(360.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Источники:",
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "уведомления, сведения нормативного характера и данные о радиочастотном излучении). Чтобы скопировать серийный номер и другие идентификаторы, нажмите и удерживайте идентификатор, пока не появится команда «Копировать»."
                        )
                    )
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "www.icloud.com/find. Программа «Найти iPhone» поддерживает функцию «Блокировка активации», которая усложняет использование Вашего iPhone посторонними в случае, если Вы потеряете iPhone."
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun SourcesFlowRowWide4Preview() {
    AppTheme {
        Box(modifier = Modifier.width(1000.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Источники:",
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "уведомления, сведения нормативного характера и данные о радиочастотном излучении). Чтобы скопировать серийный номер и другие идентификаторы, нажмите и удерживайте идентификатор, пока не появится команда «Копировать»."
                        )
                    )
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "www.icloud.com/find. Программа «Найти iPhone» поддерживает функцию «Блокировка активации», которая усложняет использование Вашего iPhone посторонними в случае, если Вы потеряете iPhone."
                        )
                    )
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "Apple Inc. использует эти товарные знаки исключительно в рамках лицензии. Названия прочих компаний и изделий, упомянутые здесь, могут являться товарными знаками соответствующих компаний."
                        )
                    )
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = ", затем коснитесь «Нет». Фильтры также можно применить позже, при редактировании фотографии. См. Редактирование фотографий и обрезка видеозаписей на стр. 100. Глава 12 Камера 104"
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun SourcesFlowRowNarrow4Preview() {
    AppTheme {
        Box(modifier = Modifier.width(360.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Источники:",
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "уведомления, сведения нормативного характера и данные о радиочастотном излучении). Чтобы скопировать серийный номер и другие идентификаторы, нажмите и удерживайте идентификатор, пока не появится команда «Копировать»."
                        )
                    )
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "www.icloud.com/find. Программа «Найти iPhone» поддерживает функцию «Блокировка активации», которая усложняет использование Вашего iPhone посторонними в случае, если Вы потеряете iPhone."
                        )
                    )
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "Apple Inc. использует эти товарные знаки исключительно в рамках лицензии. Названия прочих компаний и изделий, упомянутые здесь, могут являться товарными знаками соответствующих компаний."
                        )
                    )
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = ", затем коснитесь «Нет». Фильтры также можно применить позже, при редактировании фотографии. См. Редактирование фотографий и обрезка видеозаписей на стр. 100. Глава 12 Камера 104"
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun SourcesFlowRowWide6Preview() {
    AppTheme {
        Box(modifier = Modifier.width(1000.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Источники:",
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "уведомления, сведения нормативного характера и данные о радиочастотном излучении). Чтобы скопировать серийный номер и другие идентификаторы, нажмите и удерживайте идентификатор, пока не появится команда «Копировать»."
                        )
                    )
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "www.icloud.com/find. Программа «Найти iPhone» поддерживает функцию «Блокировка активации», которая усложняет использование Вашего iPhone посторонними в случае, если Вы потеряете iPhone."
                        )
                    )
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "Apple Inc. использует эти товарные знаки исключительно в рамках лицензии. Названия прочих компаний и изделий, упомянутые здесь, могут являться товарными знаками соответствующих компаний."
                        )
                    )
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = ", затем коснитесь «Нет». Фильтры также можно применить позже, при редактировании фотографии. См. Редактирование фотографий и обрезка видеозаписей на стр. 100. Глава 12 Камера 104"
                        )
                    )
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "Чтобы заблокировать фокус и экспозицию, коснитесь и удерживайте область фокусировки, пока не появится индикатор блокировки AE/AF. Чтобы разблокировать, коснитесь в другом месте экрана."
                        )
                    )
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "Используйте режим «Портрет» для создания фотографий с эффектом глубины резкости. Этот режим автоматически размывает фон, делая объект съемки более выразительным."
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun SourcesFlowRowNarrow6Preview() {
    AppTheme {
        Box(modifier = Modifier.width(360.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Источники:",
                    style = TextStyle(
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "уведомления, сведения нормативного характера и данные о радиочастотном излучении). Чтобы скопировать серийный номер и другие идентификаторы, нажмите и удерживайте идентификатор, пока не появится команда «Копировать»."
                        )
                    )
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "www.icloud.com/find. Программа «Найти iPhone» поддерживает функцию «Блокировка активации», которая усложняет использование Вашего iPhone посторонними в случае, если Вы потеряете iPhone."
                        )
                    )
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "Apple Inc. использует эти товарные знаки исключительно в рамках лицензии. Названия прочих компаний и изделий, упомянутые здесь, могут являться товарными знаками соответствующих компаний."
                        )
                    )
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = ", затем коснитесь «Нет». Фильтры также можно применить позже, при редактировании фотографии. См. Редактирование фотографий и обрезка видеозаписей на стр. 100. Глава 12 Камера 104"
                        )
                    )
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "Чтобы заблокировать фокус и экспозицию, коснитесь и удерживайте область фокусировки, пока не появится индикатор блокировки AE/AF. Чтобы разблокировать, коснитесь в другом месте экрана."
                        )
                    )
                    SourceItem(
                        source = Source(
                            title = "Руководство пользователя iPhone",
                            text = "Используйте режим «Портрет» для создания фотографий с эффектом глубины резкости. Этот режим автоматически размывает фон, делая объект съемки более выразительным."
                        )
                    )
                }
            }
        }
    }
}

// SourcesMessageBubble Previews
@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun SourcesMessageBubblePreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SourcesMessageBubble(
                sources = listOf(
                    Source(
                        title = "Руководство пользователя iPhone",
                        text = "уведомления, сведения нормативного характера и данные о радиочастотном излучении). Чтобы скопировать серийный номер и другие идентификаторы, нажмите и удерживайте идентификатор, пока не появится команда «Копировать»."
                    ),
                    Source(
                        title = "Руководство пользователя iPhone",
                        text = "www.icloud.com/find. Программа «Найти iPhone» поддерживает функцию «Блокировка активации», которая усложняет использование Вашего iPhone посторонними в случае, если Вы потеряете iPhone."
                    )
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun SourcesMessageBubbleMultiplePreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SourcesMessageBubble(
                sources = listOf(
                    Source(
                        title = "Руководство пользователя iPhone",
                        text = "уведомления, сведения нормативного характера и данные о радиочастотном излучении). Чтобы скопировать серийный номер и другие идентификаторы, нажмите и удерживайте идентификатор, пока не появится команда «Копировать»."
                    ),
                    Source(
                        title = "Руководство пользователя iPhone",
                        text = "www.icloud.com/find. Программа «Найти iPhone» поддерживает функцию «Блокировка активации», которая усложняет использование Вашего iPhone посторонними в случае, если Вы потеряете iPhone."
                    ),
                    Source(
                        title = "Руководство пользователя iPhone",
                        text = "Apple Inc. использует эти товарные знаки исключительно в рамках лицензии. Названия прочих компаний и изделий, упомянутые здесь, могут являться товарными знаками соответствующих компаний."
                    ),
                    Source(
                        title = "Руководство пользователя iPhone",
                        text = ", затем коснитесь «Нет». Фильтры также можно применить позже, при редактировании фотографии. См. Редактирование фотографий и обрезка видеозаписей на стр. 100. Глава 12 Камера 104"
                    )
                )
            )
        }
    }
}

// TextMessageBubble Previews
@Preview
@Composable
fun TextMessageBubbleUserPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextMessageBubble(
                text = "Привет! Как дела?",
                isUser = true
            )
        }
    }
}

@Preview
@Composable
fun TextMessageBubbleAiPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextMessageBubble(
                text = "Привет! У меня всё отлично, спасибо за вопрос. Чем могу помочь?",
                isUser = false
            )
        }
    }
}

@Preview
@Composable
fun TextMessageBubbleLongTextPreview() {
    AppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextMessageBubble(
                text = "Это очень длинный текст сообщения, который должен показать, как компонент TextMessageBubble обрабатывает многострочный текст. Текст должен корректно переноситься и отображаться в карточке сообщения.",
                isUser = false
            )
        }
    }
}

// WelcomeScreen Preview
@Preview
@Composable
fun WelcomeScreenPreview() {
    AppTheme {
        WelcomeScreen(
            onExampleQuestionClick = { }
        )
    }
}
