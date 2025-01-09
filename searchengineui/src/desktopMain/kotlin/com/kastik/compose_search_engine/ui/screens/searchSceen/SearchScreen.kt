package com.kastik.compose_search_engine.ui.screens.searchSceen


import SearchEngine
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import models.WikiDocumentResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically

@Composable
fun SearchScreen(
    searchEngine: SearchEngine,
    updateIndexProgress: (String, Float, Boolean) -> Unit,
    changeColor: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val optionsVisibility = remember { mutableStateOf(false) }
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MySearchBar(
                    modifier = Modifier
                        .weight(1f),
                    search = searchEngine.queryString.value,
                    onValueChange = { newQuery ->
                        scope.launch {
                            searchEngine.search(newQuery)
                            lazyListState.animateScrollToItem(0)
                        }
                    },
                    onSortChange = { sort ->
                        scope.launch {
                            searchEngine.search(sortByDate = sort)
                            lazyListState.animateScrollToItem(0)
                        }

                    },
                )

                IconButton(
                    onClick = { changeColor() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                ) {
                    Icon(imageVector = Icons.Default.DarkMode, contentDescription = "Dark Mode Toggle")
                }


                IconButton(
                    onClick = { optionsVisibility.value = !optionsVisibility.value },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null
                    )
                }


                Button(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clip(CircleShape),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                    onClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                searchEngine.createIndex { fileName, progress, done ->
                                    updateIndexProgress(fileName, progress, done)
                                }
                            }
                        }
                    }
                ) {
                    Text("Create Index")
                }
            }

            AnimatedVisibility(
                visible = optionsVisibility.value,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            style = MaterialTheme.typography.bodyLarge,
                            text = if (searchEngine.searchForTitleOnlyState.value) {
                                "Searching for titles"
                            } else {
                                "Searching for everything"
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = searchEngine.searchForTitleOnlyState.value,
                            onCheckedChange = {
                                searchEngine.search(searchForTitleOnly = !searchEngine.searchForTitleOnlyState.value)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            style = MaterialTheme.typography.bodyLarge,
                            text = if (searchEngine.searchWithTfIdfOnlyState.value) {
                                "Searching with tf-idf"
                            } else {
                                "Custom search"
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = searchEngine.searchWithTfIdfOnlyState.value,
                            onCheckedChange = {
                                searchEngine.search(searchWithTfIdfOnly = !searchEngine.searchWithTfIdfOnlyState.value)
                            }
                        )
                    }
                }
            }

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(searchEngine.documents) { doc: WikiDocumentResult ->
                    SearchItemView(
                        doc.title,
                        doc.hightlightString,
                        doc.score,
                        doc.path,
                        doc.lastMod
                    )
                }
                item {
                    PagesView(
                        numberOfPages = searchEngine.totalPages.value,
                        selectedIndex = searchEngine.currentPage.value,
                        goToPage = { page ->
                            scope.launch {
                                lazyListState.animateScrollToItem(0)
                            }
                            searchEngine.search(page = page)
                        }
                    )
                }
            }
    }
}


