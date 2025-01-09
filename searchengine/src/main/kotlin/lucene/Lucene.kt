package lucene

import utils.endsWithPlusOrMinus
import kotlinx.coroutines.*
import models.LuceneWrapper
import models.WikiDocumentResult
import org.apache.lucene.analysis.en.EnglishAnalyzer
import org.apache.lucene.codecs.simpletext.SimpleTextCodec
import org.apache.lucene.document.Document
import org.apache.lucene.index.*
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.*
import org.apache.lucene.search.BooleanClause.Occur
import org.apache.lucene.search.highlight.Fragmenter
import org.apache.lucene.search.highlight.Highlighter
import org.apache.lucene.search.highlight.QueryScorer
import org.apache.lucene.search.highlight.SimpleSpanFragmenter
import org.apache.lucene.search.similarities.BM25Similarity
import org.apache.lucene.search.similarities.ClassicSimilarity
import org.apache.lucene.store.FSDirectory
import java.io.File
import java.util.*


internal class Lucene(
    indexDir: String,
) : AutoCloseable {

    private val directory: FSDirectory = FSDirectory.open(File(indexDir).toPath())
    private val analyzer = EnglishAnalyzer() //EnglishAnalyzer() has the same stop words but does steaming too
    private val lemmatizer = Lemmatizer()
    private val htmlParser = HtmlParser(lemmatize = { string -> lemmatizer.lemmatize(string) })
    private val similarity = ClassicSimilarity()  //tf-idf, default was bm25

    private var isIndexing = false

    fun createIndex(
        inputDir: String,
        updateProgress: (currentFileProcess: String, progress: Float, isIndexing: Boolean) -> Unit,
    ) {
        if (isIndexing) {
            return
        }
        isIndexing = true

        val writerConfig = IndexWriterConfig(analyzer)
        writerConfig.similarity = similarity
        //writerConfig.codec = SimpleTextCodec() //TODO Use for testing only
        val writer = IndexWriter(directory, writerConfig)

        val searcher: IndexSearcher? = if (DirectoryReader.indexExists(directory)) {
            val reader = DirectoryReader.open(directory)
            IndexSearcher(reader)
        } else {
            null // No index exists yet
        }

        runBlocking {
            val htmlFiles = File(inputDir).walkTopDown()
                .filter { it.isFile && it.extension == "html" }
                .toList()

            val totalFiles = htmlFiles.size
            var processed = 0

            try {
                val indexingJobs = htmlFiles.map { file ->
                    async(Dispatchers.IO) {
                        try {
                            val isDocumentIndexed = searcher?.let {
                                val query = TermQuery(Term("path", file.absolutePath))
                                val hits = it.search(query, 1)
                                hits.totalHits.value > 0
                            } ?: false
                            if (isDocumentIndexed) {
                                println("Skipping already indexed document: ${file.name}")
                            } else {
                                val document = htmlParser.parseHtmlToDocument(file)
                                writer.addDocument(document)
                            }

                            withContext(Dispatchers.Main) {
                                processed++
                                updateProgress(file.nameWithoutExtension, processed.toFloat() / totalFiles, true)
                            }
                        } catch (e: Exception) {
                            println("Error indexing ${file.name}: ${e.message}")
                        }
                    }
                }

                indexingJobs.awaitAll()
            } finally {
                writer.close()
                updateProgress("Done", totalFiles.toFloat() / totalFiles, false)
                isIndexing = false
            }
        }
    }

    fun searchIndex(
        query: String,
        page: Int = 1,
        resultsPerPage: Int = 10,
        sortByDate: Boolean = false,
        searchForTitleOnly: Boolean = false,
        searchWithTfIdfOnly: Boolean = true
    ): LuceneWrapper {

        var reader: DirectoryReader? = null
        val searcher: IndexSearcher = if (DirectoryReader.indexExists(directory)) {
            reader = DirectoryReader.open(directory)
            IndexSearcher(reader)
        } else {
            return LuceneWrapper()
        }

        val queryStr = lemmatizer.lemmatize(query).lowercase()

        if (isIndexing) return LuceneWrapper()
        if (queryStr.isEmpty()) return LuceneWrapper()

        val sort = if (sortByDate) {
            Sort(SortField("lastModSort", SortField.Type.LONG, true))
        } else {
            null
        }

        var combinedQuery = BooleanQuery.Builder()

        //If there is a single quote return empty
        if (queryStr.filter {
                it == '\"'
            }.length == 1) {
            return LuceneWrapper()
        }

        if (!endsWithPlusOrMinus(queryStr)) {
            return LuceneWrapper()
        }


        if (searchWithTfIdfOnly) {
            searcher.similarity = similarity
        } else {
            searcher.similarity = BM25Similarity()
        }

        val finalQuery = combinedQuery.apply {
            if (!searchWithTfIdfOnly) {
                if (!searchForTitleOnly) {
                    add(BoostQuery(QueryParser("content", analyzer).parse(queryStr), 2f), Occur.SHOULD)
                    add(BoostQuery(PhraseQuery.Builder().add(Term("content", queryStr)).build(), 1f), Occur.SHOULD)
                    add(BoostQuery(TermQuery(Term("content", queryStr)), .4f), Occur.SHOULD)
                    add(BoostQuery(PrefixQuery(Term("content", queryStr)), .3f), Occur.SHOULD)
                    add(BoostQuery(FuzzyQuery((Term("content", queryStr))), .2f), Occur.SHOULD)
                }
                add(BoostQuery(PhraseQuery.Builder().add(Term("title", queryStr)).build(), 1f), Occur.SHOULD)
                add(BoostQuery(TermQuery(Term("title", queryStr)), .6f), Occur.SHOULD)
                add(BoostQuery(PrefixQuery(Term("title", queryStr)), .3f), Occur.SHOULD)
                add(BoostQuery(FuzzyQuery((Term("title", queryStr))), .2f), Occur.SHOULD)
                add(BoostQuery(QueryParser("title", analyzer).parse(queryStr), .1f), Occur.SHOULD)
            } else {
                if (!searchForTitleOnly){
                    add(QueryParser("content", analyzer).parse(queryStr), Occur.SHOULD)
                }else {
                    add(QueryParser("title", analyzer).parse(queryStr), Occur.SHOULD)
                }
            }
        }.build()

        val queryScorer = QueryScorer(finalQuery)
        val highlighter = Highlighter(queryScorer)
        val fragmenter: Fragmenter = SimpleSpanFragmenter(queryScorer, 100)
        highlighter.textFragmenter = fragmenter


        val results = sort?.let {
            searcher.search(finalQuery, page * resultsPerPage, sort)

        } ?: searcher.search(finalQuery, page * resultsPerPage)

        val storedFields: StoredFields = searcher.storedFields()

        val customResults = results.scoreDocs.takeLast(resultsPerPage).map {
            val doc: Document = storedFields.document(it.doc)
            val title = doc.get("title") ?: "No title available"
            val content = doc.get("content") ?: "No content available"
            val path = doc.get("path") ?: "Unknown"
            val lastModified = doc.get("lastModStr") ?: "Unknown"
            val highlightedContent = try {
                highlighter.getBestFragment(analyzer, "content", content) ?: "No matching snippet"
            } catch (e: Exception) {
                "Error highlighting content: ${e.message}"
            }

            WikiDocumentResult(
                title,
                content,
                path,
                highlightedContent,
                it.score.toString(),
                lastModified
            )
        }
        reader.close()
        return LuceneWrapper(
            documents = customResults,
            totalHits = results.totalHits.value,
            //TODO BUGGED
            totalPages = kotlin.math.ceil((results.totalHits.value / resultsPerPage).toDouble()).toInt()
        )
    }

    override fun close() {
        directory.close()
    }
}





