package lucene

import org.apache.lucene.document.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

internal class HtmlParser(
    private val lemmatize: (string: String) -> String
) {
    fun parseHtmlToDocument(file: File): Document {
        val jsoupDoc = Jsoup.parse(file, "UTF-8")
        val title = jsoupDoc.title().replace(" - ArchWiki","")
        val lastModTime = jsoupDoc.getElementById("footer-info-lastmod")?.text()?.let {
            convertToDate(it)
        }

        val content: Element? = jsoupDoc.selectFirst("#mw-content-text")
        content?.select("nav, .vector-toc, script, footer, .vector-column-start, .mw-page-container-inner, .catlinks")?.remove()
        val finalContent = content?.text() ?: ""
        val bodyContent = lemmatize(finalContent)

        return Document().apply {
            add(StringField("title", title, Field.Store.YES))
            add(TextField("content", bodyContent, Field.Store.YES))
            add(StringField("path",file.absolutePath, Field.Store.YES))
            add(StoredField("lastModStr",lastModTime.toString())  )
            add(NumericDocValuesField("lastModSort", lastModTime?.time ?: 0)) //IF DATE DIDN'T RESOLVE DEFAULT TO 0
        }
    }
}
private fun convertToDate(dateString: String): Date? {
    // Regex to extract the date and time part: "31 October 2024, at 18:48" Made by AI btw :'(
    val regex = "on (\\d{1,2} \\w+ \\d{4}), at (\\d{2}:\\d{2})".toRegex()
    val matchResult = regex.find(dateString)

    return matchResult?.let {
        val datePart = it.groupValues[1]  // "31 October 2024”
        val timePart = it.groupValues[2]  // "18:48"

        val format = SimpleDateFormat("d MMMM yyyy HH:mm", Locale.ENGLISH)
        val date = format.parse("$datePart $timePart")

        date
    }
}
