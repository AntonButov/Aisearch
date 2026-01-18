package com.antonbutov.aisearch.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// Векторная иконка стрелки навигации из SVG (упрощенная версия)
val SendIcon: ImageVector = ImageVector.Builder(
    name = "Send",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 91f,
    viewportHeight = 91f
).apply {
    // Упрощенная версия стрелки навигации
    // Основной контур стрелки (аппроксимация)
    path(
        fill = SolidColor(Color(0xFF3B6A75))
    ) {
        // Основная форма стрелки
        moveTo(85.4f, 3.4f)
        lineTo(4.9f, 29.3f)
        lineTo(5.3f, 37.6f)
        lineTo(42.6f, 51.6f)
        lineTo(50f, 86.9f)
        lineTo(56.6f, 87f)
        lineTo(89.7f, 7.8f)
        close()
    }
    // Внутренние детали
    path(
        fill = SolidColor(Color(0xFF2A4F58))
    ) {
        moveTo(28.8f, 34.3f)
        lineTo(18.1f, 31.6f)
        lineTo(66.3f, 18.5f)
        lineTo(61.3f, 22.6f)
        lineTo(42.2f, 39.6f)
        close()
    }
}.build()
