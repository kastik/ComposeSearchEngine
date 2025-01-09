package com.kastik.compose_search_engine.ui.screens.indexScreen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun IndexScreen(
    item: String,
    progress: Float,
) {

    val indexingItem = remember { mutableStateOf("") }
    val currentProgress = remember { mutableStateOf(0f) }
    Box(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Indexing",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.TopCenter).padding(16.dp)
        )
        Column(modifier = Modifier.align(Alignment.Center)) {
            Text(
                text = item,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
            )
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(0.6f),
            )
        }
    }
}