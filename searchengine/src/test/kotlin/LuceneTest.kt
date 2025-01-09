import io.kotest.core.spec.style.StringSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import lucene.HtmlParser
import lucene.Lemmatizer
import lucene.Lucene
import java.io.File
import io.mockk.*
import org.apache.lucene.document.*
import kotlinx.coroutines.runBlocking
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.FSDirectory
import utils.endsWithPlusOrMinus
import utils.escapeDashOutsideQuotes

import kotlin.io.path.createTempDirectory
import kotlin.io.path.pathString

class LuceneTests : StringSpec({

    "EscapeTests" {
        escapeDashOutsideQuotes("abcd") shouldBe "abcd"
        escapeDashOutsideQuotes("'abc-'") shouldBe "'abc\\-'"
        escapeDashOutsideQuotes("-") shouldBe "\\-"
    }

    "MinusPlusTests" {
        endsWithPlusOrMinus("abc") shouldBe true
        endsWithPlusOrMinus("abc-") shouldBe false
        endsWithPlusOrMinus("abc -") shouldBe false
        endsWithPlusOrMinus("+abc") shouldBe true
        endsWithPlusOrMinus("ab -cde") shouldBe true
        endsWithPlusOrMinus("a-c") shouldBe true
    }

    "ParserTests" {
        val lemmatizer = Lemmatizer()
        val htmlParser = HtmlParser(lemmatize = { string -> lemmatizer.lemmatize(string) })

        val file = File("/home/kastik/Projects/ComposeSearchEngine/data/site/en/Systemd-resolved.html")
        val document = htmlParser.parseHtmlToDocument(file)
        document.get("title") shouldBe "systemd-resolved"
        document.get("content") shouldContain "that provide network name resolution to local application via a"
        document.get("path") shouldBe "/home/kastik/Projects/ComposeSearchEngine/data/site/en/Systemd-resolved.html"
        document.get("lastModStr") shouldBe "Sat Aug 17 07:39:00 EEST 2024"
        document.get("lastModSort") shouldBe "1723869540000"
    }

    "LeminizeTest" {
        //TODO ADD MORE
        val lemmatizer = Lemmatizer()
        lemmatizer.lemmatize("abc") shouldBe "abc"
        lemmatizer.lemmatize("playing") shouldBe "play"
        lemmatizer.lemmatize("system") shouldBe "system"
        lemmatizer.lemmatize("systemd") shouldBe "systemd"
        lemmatizer.lemmatize("systemd-resolved") shouldBe "systemd-resolve"
        lemmatizer.lemmatize("systemd-networkd") shouldBe "systemd-networkd"
        lemmatizer.lemmatize("The quick brown fox jumps over the lazy dog") shouldBe "the quick brown fox jump over the lazy dog"
    }

    //TODO RE-WRITE THE ONES BELOW
/*
    "should return results in the correct ranking order" {
        // Set up an in-memory Lucene index
        val directory = FSDirectory.open(tempdir("temp").toPath())
        val analyzer = StandardAnalyzer()
        val config = IndexWriterConfig(analyzer)
        val writer = IndexWriter(directory, config)

        // Add documents to the index
        val documents = listOf(
            Document().apply {
                add(StringField("title", "Lucene Search", Field.Store.YES))
                add(TextField("content", "Lucene is a search library for Java.", Field.Store.YES))
            },
            Document().apply {
                add(StringField("title", "Search Engine", Field.Store.YES))
                add(TextField("content", "Search engines like Lucene are powerful.", Field.Store.YES))
            },
            Document().apply {
                add(StringField("title", "Introduction to Lucene", Field.Store.YES))
                add(TextField("content", "An introduction to the basics of Lucene.", Field.Store.YES))
            }
        )

        documents.forEach { writer.addDocument(it) }
        writer.close()

        // Perform the search
        val lucene = Lucene(directory.directory.pathString)
        val query = "Lucene search engine"
        val result = lucene.searchIndex(query)

        // Verify the ranking of the results
        val titles = result.documents.map { it.title }
        titles.shouldContainInOrder(
            listOf(
                "Search Engine",
                "Lucene Search",
                "Introduction to Lucene"
            )
        )
        // Clean up
        lucene.close()
        directory.close()
    }



    "should index files and call updateProgress correctly" {
        // Create temporary input directory
        val tempDir = createTempDirectory("test-input").toFile()
        val testFile = File(tempDir, "test.html")
        testFile.writeText("<html><body>Sample Content</body></html>")

        // Mock dependencies
        val mockHtmlParser = mockk<HtmlParser>()
        val mockUpdateProgress = mockk<(String, Float, Boolean) -> Unit>(relaxed = true)

        every { mockHtmlParser.parseHtmlToDocument(testFile) } returns mockk(relaxed = true)

        val lucene = spyk(Lucene(tempDir.absolutePath), recordPrivateCalls = true) {

        }

        runBlocking {
            lucene.createIndex(tempDir.absolutePath, mockUpdateProgress)
        }

        // Verify mocks
        verify { mockUpdateProgress("test.html", 1.0f, true) }
        verify { mockUpdateProgress("Done", 1.0f, false) }

        // Clean up
        tempDir.deleteRecursively()
    }


 */
    "should skip already indexed files" {
        val tempDir = createTempDirectory("test-input").toFile()
        val testFile = File(tempDir, "test.html")
        testFile.writeText("<html><body>Sample Content</body></html>")

        // Mock dependencies
        val mockHtmlParser = mockk<HtmlParser>()
        val mockUpdateProgress = mockk<(String, Float, Boolean) -> Unit>(relaxed = true)

        every { mockHtmlParser.parseHtmlToDocument(testFile) } returns mockk(relaxed = true)

        val lucene = spyk(Lucene(tempDir.absolutePath), recordPrivateCalls = true) {

        }

        // Index once
        runBlocking {
            lucene.createIndex(tempDir.absolutePath, mockUpdateProgress)
        }

        // Index again
        runBlocking {
            lucene.createIndex(tempDir.absolutePath, mockUpdateProgress)
        }

        // Ensure indexing was skipped
        verify(exactly = 0) { mockHtmlParser.parseHtmlToDocument(testFile) }

        // Clean up
        tempDir.deleteRecursively()
    }
})
