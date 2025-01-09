package com.kastik.compose_search_engine.ui.screens.searchSceen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp


@Composable
fun MySearchBar(
    search: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
    onSortChange: (Boolean) -> Unit,
) {
    val sortByTime = remember { mutableStateOf(false) }
    Box(modifier =
        modifier
            .padding(start = 24.dp, end = 68.dp)
            .clip(CircleShape)) {
        TextField(
            modifier = Modifier
                .fillMaxWidth(),
            value = search,
            onValueChange = onValueChange,
            colors = TextFieldDefaults.colors(),
            trailingIcon = {
                Row {
                    IconButton(
                        onClick = {
                            sortByTime.value = !sortByTime.value
                            onSortChange(sortByTime.value)
                        },
                        enabled = true,
                    ) {
                        Icon(
                            imageVector = if (sortByTime.value) Icons.Default.CalendarMonth else Icons.AutoMirrored.Default.Sort,
                            contentDescription = null

                        )
                    }
                }
            },
            placeholder = {
                Text(
                    text = "Search",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        )
    }
}