package lucene

import edu.stanford.nlp.pipeline.*
import edu.stanford.nlp.ling.CoreAnnotations
import java.util.Properties

internal class Lemmatizer {
    private val pipeline: StanfordCoreNLP = StanfordCoreNLP(Properties().apply {
        setProperty("annotators", "tokenize,ssplit,pos,lemma")
        setProperty("tokenize.language", "English")
        setProperty("tokenize.whitespace", "true")
    })
    fun lemmatize(text: String): String {
        val annotation = Annotation(text)
        pipeline.annotate(annotation)
        val sentences = annotation.get(CoreAnnotations.SentencesAnnotation::class.java)
        return sentences
            .flatMap { it.get(CoreAnnotations.TokensAnnotation::class.java) }
            .joinToString(" ") { token ->
                token.get(CoreAnnotations.LemmaAnnotation::class.java)
            }
    }
}