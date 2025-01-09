package com.kastik.compose_search_engine.ui.screens.searchSceen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kastik.compose_search_engine.utils.getPageDisplayIndices


@Composable
fun ColumnScope.PagesView(
    numberOfPages: Int,
    selectedIndex: Int,
    goToPage: (Int) -> Unit,
) {
    LazyRow(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .fillMaxWidth(0.6f)
    ) {
        items(getPageDisplayIndices(numberOfPages, selectedIndex)) { index ->
            if (index == -1) {
                Text(
                    text = "...",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .padding(8.dp).align(Alignment.CenterHorizontally),
                    textAlign = TextAlign.Center
                )
            } else {
                ElevatedButton(
                    onClick = { goToPage(index) },
                    colors = if (selectedIndex == index) {
                        ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        ButtonDefaults.elevatedButtonColors()
                    },
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .padding(4.dp)
                        .size(40.dp)
                ) {
                    Text(
                        text = index.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        //modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


