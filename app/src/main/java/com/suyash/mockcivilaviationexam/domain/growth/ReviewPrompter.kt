package com.suyash.mockcivilaviationexam.domain.growth

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.review.ReviewManagerFactory

/**
 * Asks for a Play Store rating via the In-App Review API.
 *
 * Ratings are a direct ranking input on Play, so the prompt is deliberately
 * cheap for the user and shown at a high point: after a passed exam, once the
 * user has completed enough exams to have an opinion. Play itself rate-limits
 * how often the dialog actually appears, so we only ever ask once per install.
 */
class ReviewPrompter(private val context: Context) {

    companion object {
        private const val TAG = "ReviewPrompter"
        private const val PREFS_NAME = "growth_tracking"
        private const val KEY_EXAMS_COMPLETED = "exams_completed"
        private const val KEY_REVIEW_REQUESTED = "review_requested"

        /** Exams a user must finish before we ask for a rating. */
        private const val EXAMS_BEFORE_PROMPT = 3
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val examsCompleted: Int
        get() = prefs.getInt(KEY_EXAMS_COMPLETED, 0)

    fun recordExamCompleted() {
        prefs.edit().putInt(KEY_EXAMS_COMPLETED, examsCompleted + 1).apply()
    }

    fun shouldPrompt(passed: Boolean): Boolean {
        if (!passed) return false
        if (prefs.getBoolean(KEY_REVIEW_REQUESTED, false)) return false
        return examsCompleted >= EXAMS_BEFORE_PROMPT
    }

    fun requestReview(activity: Activity) {
        val manager = ReviewManagerFactory.create(context)
        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (!request.isSuccessful) {
                Log.w(TAG, "Review flow unavailable", request.exception)
                return@addOnCompleteListener
            }
            // Mark as asked regardless of whether Play chooses to show the dialog:
            // Play suppresses repeats anyway, and retrying would only nag.
            prefs.edit().putBoolean(KEY_REVIEW_REQUESTED, true).apply()
            manager.launchReviewFlow(activity, request.result)
        }
    }
}
