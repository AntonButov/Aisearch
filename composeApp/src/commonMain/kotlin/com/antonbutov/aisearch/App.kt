package com.antonbutov.aisearch

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.antonbutov.aisearch.data.api.ApiClient
import com.antonbutov.aisearch.data.api.ApiClientImpl
import com.antonbutov.aisearch.domain.ChatRepositoryImpl
import com.antonbutov.aisearch.ui.AppTheme
import com.antonbutov.aisearch.ui.ChatScreen
import com.antonbutov.aisearch.ui.ChatViewModel

@Composable
fun App() {
    AppTheme {
        val apiClient: ApiClient = ApiClientImpl()
        val repository = ChatRepositoryImpl(apiClient)
        val viewModel = ChatViewModel(repository)
        
        ChatScreen(viewModel = viewModel)
    }
}
