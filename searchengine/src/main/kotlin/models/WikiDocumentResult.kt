package models

data class WikiDocumentResult(
    val title: String,
    val content: String,
    val path: String,
    val hightlightString: String,
    val score: String,
    val lastMod: String
)