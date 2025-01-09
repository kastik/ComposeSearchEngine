package models

data class LuceneWrapper(
    val documents: List<WikiDocumentResult> = emptyList(),
    val totalHits: Long = 0,
    val totalPages: Int = 0,
)