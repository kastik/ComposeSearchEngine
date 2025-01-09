package com.kastik.compose_search_engine

import SearchEngine
import androidx.annotation.Keep
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.kastik.compose_search_engine.ui.screens.downloadScreen.DownloadScreen
import com.kastik.compose_search_engine.ui.screens.indexScreen.IndexScreen
import com.kastik.compose_search_engine.ui.screens.searchSceen.SearchScreen
import com.kastik.compose_search_engine.ui.theme.AppTheme

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose SearchEngine",
    ) {
        val searchEngine = remember { SearchEngine() }
        val isDarkTheme = remember { mutableStateOf(false) }

        AppTheme(
            darkTheme = isDarkTheme.value
        ) {
            Surface(tonalElevation = 5.dp) {
                val isIndexing = remember { mutableStateOf(false) }
                val indexingItem = remember { mutableStateOf("") }
                val currentProgress = remember { mutableStateOf(0f) }

                Scaffold(
                    modifier = Modifier.background(MaterialTheme.colorScheme.background),
                ) {

                    AnimatedVisibility(
                        visible = searchEngine.isDownloading.value && !isIndexing.value,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        DownloadScreen()
                    }
                    AnimatedVisibility(
                        visible = isIndexing.value && !searchEngine.isDownloading.value,
                        enter = slideInVertically(),
                        exit = slideOutVertically()
                        ) {
                        IndexScreen(
                            indexingItem.value,
                            currentProgress.value
                        )
                    }
                    AnimatedVisibility(
                        visible = !isIndexing.value && !searchEngine.isDownloading.value,
                        enter = slideInVertically(),
                        exit = slideOutVertically()) {
                        SearchScreen(
                            searchEngine = searchEngine,
                            changeColor = {
                                isDarkTheme.value = !isDarkTheme.value
                            },
                            updateIndexProgress = { item: String, progress: Float, indexing: Boolean ->
                                indexingItem.value = item
                                currentProgress.value = progress
                                isIndexing.value = indexing
                            })
                    }
                }
            }
        }
    }

}

