package com.antonbutov.aisearch.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.antonbutov.aisearch.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "ИИ поиск"
    ) {
        App()
    }
}
