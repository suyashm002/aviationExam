package com.suyash.mockcivilaviationexam.domain.growth

import android.content.Context
import android.content.Intent

/**
 * Share entry points. Every shared message carries the Play Store link so that
 * results shared with instructors and study groups convert into installs.
 */
object AppShare {

    const val PLAY_STORE_URL =
        "https://play.google.com/store/apps/details?id=com.suyash.mockcivilaviationexam"

    fun shareExamResult(
        context: Context,
        sectionName: String,
        score: Int,
        correctAnswers: Int,
        totalQuestions: Int
    ) {
        val headline = if (score >= 75) {
            "Passed $sectionName with $score% ✈️"
        } else {
            "Scored $score% on $sectionName ✈️"
        }

        val message = buildString {
            appendLine(headline)
            appendLine("$correctAnswers of $totalQuestions correct.")
            appendLine()
            appendLine("Practising for my KCAA exams on Aviation Exam Pro — 9,000+ ICAO-aligned questions, works offline, free.")
            append(PLAY_STORE_URL)
        }

        share(context, message, "Share your result")
    }

    fun shareApp(context: Context) {
        val message = buildString {
            appendLine("Aviation Exam Pro — free KCAA/ICAO pilot exam prep.")
            appendLine("9,000+ practice questions across 7 subjects, a digital logbook, and full offline support.")
            append(PLAY_STORE_URL)
        }

        share(context, message, "Share Aviation Exam Pro")
    }

    private fun share(context: Context, message: String, chooserTitle: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        val chooser = Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
