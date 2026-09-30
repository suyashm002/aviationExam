package com.suyash.mockcivilaviationexam.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuestionResultTest {

    private val question = Question(
        id = "q1", questionText = "Standard sea-level pressure is:",
        optionA = "1003.25 hPa", optionB = "1013.25 hPa", optionC = "1023.25 hPa", optionD = null,
        correctAnswer = "B"
    )

    @Test
    fun `answers show the letter and the option text`() {
        val r = QuestionResult(questionId = "q1", selectedAnswer = "a", correctAnswer = "B", isCorrect = false)
            .withOptionTexts(question)
        assertEquals("A — 1003.25 hPa", r.selectedAnswerDisplay)
        assertEquals("B — 1013.25 hPa", r.correctAnswerDisplay)
    }

    @Test
    fun `a bare letter is shown when the question is unavailable`() {
        val r = QuestionResult(selectedAnswer = "c", correctAnswer = "B").withOptionTexts(null)
        assertEquals("C", r.selectedAnswerDisplay)
        assertEquals("B", r.correctAnswerDisplay)
    }

    @Test
    fun `a missing option D or an unknown letter yields no text`() {
        assertNull(question.optionText("D"))
        assertNull(question.optionText("E"))
        assertEquals("1013.25 hPa", question.optionText(" b "))
    }
}
