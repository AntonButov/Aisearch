package com.antonbutov.aisearch.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
private const val PDF_URL = "Smart_iphone_13_RU.pdf"

@Composable
fun WelcomeScreen(
    onExampleQuestionClick: (String) -> Unit
) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Этот чат помогает быстро находить информацию в документе Smart iPhone 13.",
            style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Text(
            text = "Скачать руководство Smart iPhone 13 (PDF)",
            style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Normal,
                textDecoration = TextDecoration.Underline,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .padding(bottom = 12.dp)
                .clickable { uriHandler.openUri(PDF_URL) }
        )
        
        Text(
            text = "Вы можете задавать вопросы простым языком — даже если не знаете точных формулировок.",
            style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Пример вопроса
        Button(
            onClick = {
                onExampleQuestionClick("Какие основные функции и возможности iPhone описаны в этом руководстве и для каких задач они предназначены?")
            },
            modifier = Modifier,
        ) {
            Text(
                "Какие основные функции и возможности iPhone описаны в этом руководстве и для каких задач они предназначены?",
            )
        }
    }
}
