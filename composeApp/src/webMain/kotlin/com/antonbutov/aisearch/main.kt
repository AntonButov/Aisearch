package com.antonbutov.aisearch

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady
import org.w3c.dom.HTMLElement

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    onWasmReady {
        val container = document.getElementById("ComposeTarget") as? HTMLElement
        if (container != null) {
            ComposeViewport(viewportContainer = container) {
                App()
            }
        }
    }
}
