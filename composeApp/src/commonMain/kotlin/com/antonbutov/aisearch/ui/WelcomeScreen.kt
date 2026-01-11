package com.antonbutov.aisearch.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antonbutov.aisearch.ui.model.Source

@Composable
fun WelcomeScreen(
    onExampleQuestionClick: (String) -> Unit
) {
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
            text = "Вы можете задавать вопросы простым языком — даже если не знаете точных формулировок.",
            style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Пример вопроса
        SourceItem(
            source = Source(
                title = "Какие основные функции и возможности iPhone описаны в этом руководстве и для каких задач они предназначены?",
                text = ""
            ),
            onClick = {
                onExampleQuestionClick("Какие основные функции и возможности iPhone описаны в этом руководстве и для каких задач они предназначены?")
            },
            modifier = Modifier,
        )
    }
}
