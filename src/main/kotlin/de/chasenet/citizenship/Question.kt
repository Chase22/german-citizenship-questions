package de.chasenet.citizenship

import kotlinx.serialization.Serializable

@Serializable
data class Answer(
    val text: String,
    val isCorrect: Boolean
)

@Serializable
data class Question(
    val id: Int,
    val text: String,
    val image: String? = null,
    val answers: List<Answer>
)