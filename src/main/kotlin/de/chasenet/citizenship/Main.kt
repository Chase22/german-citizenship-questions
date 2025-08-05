package de.chasenet.citizenship


import de.chasenet.citizenship.serialization.toJson
import de.chasenet.citizenship.serialization.toXml
import java.io.File

fun main(args: Array<String>) {
    if (args.size != 2) {
        println("Usage: <inputDir> <outputDir>")
        return
    }
    val inputDir = File(args[0])
    val outputDir = File(args[1])

    val questions = inputDir.listFiles { it.extension == "html" }!!.flatMap {
        Parser.parseQuestions(it.readText())
    }.sortedBy { it.id }

    outputDir.mkdirs()
    outputDir.resolve("questions.json").writeText(toJson(questions))
    outputDir.resolve("questions.xml").writeText(toXml(questions))
}


