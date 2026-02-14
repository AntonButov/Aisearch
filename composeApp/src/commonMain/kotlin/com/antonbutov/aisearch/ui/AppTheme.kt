package com.antonbutov.aisearch.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val baseColorScheme = if (darkTheme) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }
    
    val colorScheme = baseColorScheme.copy(
        surface = Color(0xFFF5F5F5), // Серый цвет
        surfaceVariant = Color(0xFFE0E0E0), // Светло-серый для вариантов
        primary = if (darkTheme) Color(0xFF42A5F5) else Color(0xFF1976D2),
        onPrimary = Color.White
    )
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
