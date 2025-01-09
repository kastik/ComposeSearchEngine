import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import lucene.Lucene
import models.WikiDocumentResult
import utils.extractZst
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.pathString

/**
 * The values bellow are used for now, we should be getting the vals from terminal/ui once pagination is ready
 * @param inputDir The path were all the html files are located, sub dirs will be included
 * @param indexDir The directory where the index will be stored
 * @author Papastathopoulos Kostas 185255
 * @see search
 * @see createIndex
 */

class SearchEngine(
    private val inputDir: Path = Paths.get(System.getProperty("user.dir")).parent.resolve("data/site/"),
    private val indexDir: Path = Paths.get(System.getProperty("user.dir")).parent.resolve("data/index")
) {
    private val lucene = Lucene(indexDir.toString())

    val isDownloading = mutableStateOf(false)
    val documents = mutableStateListOf<WikiDocumentResult>()
    val totalHits = mutableStateOf(0)

    val totalPages = mutableStateOf(0)

    val queryString = mutableStateOf("")
    val currentPage = mutableStateOf(0)

    val sortByDateState = mutableStateOf(false)
    val searchForTitleOnlyState = mutableStateOf(false)
    val searchWithTfIdfOnlyState = mutableStateOf(false)


    init {
        if (!inputDir.exists()) {
            isDownloading.value = true
            CoroutineScope(Dispatchers.Main).launch {
                val client = HttpClient(CIO)
                try {
                    inputDir.toFile().mkdirs()
                    val response: HttpResponse = client.get("https://archlinux.org/packages/extra/any/arch-wiki-docs/download/")
                    val compressedFile = File(inputDir.pathString, "arch-wiki-docs.tar.zst")
                    response.bodyAsChannel().copyAndClose(compressedFile.writeChannel())
                    extractZst(compressedFile.path, inputDir.toString())
                } finally {
                    isDownloading.value = false
                    client.close()
                }
            }
        }
    }



    /**
     * @param query The value that will be search for in the index
     * @param page The page to goto, can be called like search(page = 2) to 'scroll' without passing queryString again
     * @param resultPerPage The limit of results in a single page
     * @param sortByDate When true the search will sort the results by date modified
     * @return Nothing, the data should be observed by the 'mutableStates' above in composables
     */

    fun search(
        query: String = queryString.value,
        page: Int = 1,
        resultPerPage: Int = 10,
        sortByDate: Boolean = sortByDateState.value,
        searchForTitleOnly: Boolean = searchForTitleOnlyState.value,
        searchWithTfIdfOnly: Boolean = searchWithTfIdfOnlyState.value,
    ) {

        queryString.value = query
        currentPage.value = page
        sortByDateState.value = sortByDate
        searchForTitleOnlyState.value = searchForTitleOnly
        searchWithTfIdfOnlyState.value = searchWithTfIdfOnly

        if (query.isBlank()) {
            queryString.value = ""
            totalHits.value = 0
            currentPage.value = 0
            totalPages.value = 0
            //sortByDateState.value = false
            //searchWithTfIdfOnlyState.value = false
            documents.clear()
            return
        }

        val luceneApiWrapper = lucene.searchIndex(
            query = query,
            page = page,
            resultsPerPage = resultPerPage,
            sortByDate = sortByDate,
            searchForTitleOnly = searchForTitleOnly,
            searchWithTfIdfOnly = searchWithTfIdfOnly,
        )
        documents.clear()
        documents.addAll(luceneApiWrapper.documents)
        totalHits.value = luceneApiWrapper.totalHits.toInt()
        totalPages.value = luceneApiWrapper.totalPages
    }


    fun createIndex(
        updateProgress: (String, Float, Boolean) -> Unit
    ) {
        lucene.createIndex(inputDir.toString(), updateProgress)
    }
}
