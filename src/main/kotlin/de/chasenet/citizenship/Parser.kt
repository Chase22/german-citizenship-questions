package de.chasenet.citizenship

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.select.Elements

object Parser {
    fun parseQuestions(html: String): List<Question> {
        val doc = Ksoup.parse(html)

        val containerChildren = doc.select("div.page-content").flatMap { it.childElementsList() }.let(::Elements)
        val questionElements = containerChildren.select("div[id^=frage-]")
        return questionElements.map(::parseQuestion)
    }

    private fun parseQuestion(element: Element): Question {
        val questionId = element.id().removePrefix("frage-").toInt()
        val questionText = element.select("h3").first()!!.text().substringAfter(": ")

        val image = element.select("img").firstOrNull()?.attr("src")?.let {
            "https://www.lebenindeutschland.eu/$it"
        }

        val answers = element.select(":root > div > div > div:last-child").map {
            Answer(
                text = it.text(),
                isCorrect = it.parent()!!.classNames().contains("bg-green-100")
            )
        }.sortedByDescending { it.isCorrect }

        return Question(
            id = questionId,
            text = questionText,
            image = image,
            answers = answers
        )
    }

}