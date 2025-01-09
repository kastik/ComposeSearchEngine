package com.kastik.compose_search_engine.ui.screens.searchSceen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.onClick
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.kastik.compose_search_engine.utils.openFileInBrowser

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchItemView(
    title: String,
    highlightString: String,
    score: String,
    path: String,
    lastModified: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .onClick {
                openFileInBrowser(path)
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiary,
        ),
        elevation = CardDefaults.elevatedCardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = parseHighlightString(highlightString),
                    style = MaterialTheme.typography.labelLarge, //body1
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = path,
                    style = MaterialTheme.typography.labelSmall, //caption
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = lastModified,
                    style = MaterialTheme.typography.labelSmall, //caption
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Column {
                //The score is nan when sort by date is active cause no weights are applied
                if (score != "NaN") {
                    Text(
                        text = "Score",
                        style = MaterialTheme.typography.labelMedium, //body2
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = score,
                        style = MaterialTheme.typography.bodyLarge, //h6
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        //fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun parseHighlightString(input: String): AnnotatedString {
    return buildAnnotatedString {
        var index = 0
        val regex = "<B>(.*?)</B>".toRegex()
        regex.findAll(input).forEach { match ->
            val start = match.range.first
            val end = match.range.last
            val boldText = match.groups[1]?.value.orEmpty()

            // Add plain text before bold
            if (index < start) {
                append(input.substring(index, start))
            }
            // Add bold text
            withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                append(boldText)
            }
            index = end + 1
        }
        // Add remaining plain text
        if (index < input.length) {
            append(input.substring(index))
        }
    }
}