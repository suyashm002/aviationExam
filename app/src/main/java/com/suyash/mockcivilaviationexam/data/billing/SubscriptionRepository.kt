package com.suyash.mockcivilaviationexam.data.billing

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth

class SubscriptionRepository(context: Context) {

    companion object {
        private const val PREFS_NAME = "exam_usage_tracking"
        private const val KEY_PREFIX_EXAM_COUNT = "exam_count_"
        const val FREE_EXAMS_PER_SECTION = 1
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val userEmail: String?
        get() = FirebaseAuth.getInstance().currentUser?.email

    private fun examCountKey(sectionId: String): String {
        val email = userEmail ?: "anonymous"
        return "${KEY_PREFIX_EXAM_COUNT}${email}_$sectionId"
    }

    fun getExamsTaken(sectionId: String): Int {
        return prefs.getInt(examCountKey(sectionId), 0)
    }

    fun recordExamTaken(sectionId: String) {
        val key = examCountKey(sectionId)
        val current = prefs.getInt(key, 0)
        prefs.edit().putInt(key, current + 1).apply()
    }

    fun canTakeFreeExam(sectionId: String): Boolean {
        return getExamsTaken(sectionId) < FREE_EXAMS_PER_SECTION
    }

    fun getRemainingFreeExams(sectionId: String): Int {
        val taken = getExamsTaken(sectionId)
        return (FREE_EXAMS_PER_SECTION - taken).coerceAtLeast(0)
    }
}
