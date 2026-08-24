package com.suyash.mockcivilaviationexam.data.sync

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.suyash.mockcivilaviationexam.CivilAviationApp
import kotlinx.coroutines.tasks.await

class FeedbackSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "FeedbackSyncWorker"
        const val WORK_NAME = "feedback_sync_weekly"
    }

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as CivilAviationApp
            val dao = app.database.questionFeedbackDao()
            val firestore = FirebaseFirestore.getInstance()

            val unsyncedFeedback = dao.getUnsyncedFeedback()
            if (unsyncedFeedback.isEmpty()) {
                Log.d(TAG, "No feedback to sync")
                return Result.success()
            }

            Log.d(TAG, "Syncing ${unsyncedFeedback.size} feedback entries")

            val batch = firestore.batch()
            for (feedback in unsyncedFeedback) {
                val docRef = firestore.collection("question_feedback").document()
                batch.set(docRef, hashMapOf(
                    "questionId" to feedback.questionId,
                    "sectionId" to feedback.sectionId,
                    "questionText" to feedback.questionText,
                    "feedbackType" to feedback.feedbackType,
                    "comment" to feedback.comment,
                    "userEmail" to feedback.userEmail,
                    "createdAt" to feedback.createdAt
                ))
            }

            batch.commit().await()
            dao.markAsSynced(unsyncedFeedback.map { it.id })

            // Clean up feedback older than 30 days that's already synced
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            dao.deleteOldSynced(thirtyDaysAgo)

            Log.d(TAG, "Synced ${unsyncedFeedback.size} feedback entries successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Feedback sync failed: ${e.message}", e)
            Result.retry()
        }
    }
}
