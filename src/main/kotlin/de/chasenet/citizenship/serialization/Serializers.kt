package de.chasenet.citizenship.serialization

import de.chasenet.citizenship.Question
import kotlinx.serialization.json.Json
import org.w3c.dom.Element
import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun toJson(questions: List<Question>): String {
    val json = Json {
        prettyPrint = true
    }

    return json.encodeToString(questions)
}

fun toXml(questions: List<Question>): String {
    val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()
    val rootElement = document.createElement("questions").apply {
        setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance")
        setAttribute("xsi:noNamespaceSchemaLocation", "https://raw.githubusercontent.com/Chase22/german-citizenship-questions/refs/heads/main/questions.xsd")
    }
    document.appendChild(rootElement)

    questions.forEach { question ->
        val questionElement = document.createElement("question")
        questionElement.setAttribute("id", question.id.toString())

        questionElement.appendElement("text", question.text)

        question.image?.let { image ->
            questionElement.appendElement("image", image)
        }

        val answersElement = questionElement.appendElement("answers")

        question.answers.forEach { answer ->
            answersElement.appendElement("answer", answer.text).apply {
                setAttribute("correct", answer.isCorrect.toString())
            }
        }
        rootElement.appendChild(questionElement)
    }

    val stringWriter = StringWriter()
    val transformer = TransformerFactory.newInstance().newTransformer()
    transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes")
    transformer.transform(
        DOMSource(document),
        StreamResult(stringWriter)
    )

    return stringWriter.toString()
}

private fun Element.appendElement(tagName: String, content: String? = null): Element {
    val child = ownerDocument.createElement(tagName)
    if (content != null) {
        child.textContent = content
    }
    appendChild(child)
    return child
}