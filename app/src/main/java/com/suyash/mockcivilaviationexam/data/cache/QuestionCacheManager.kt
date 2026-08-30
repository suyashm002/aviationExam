package com.suyash.mockcivilaviationexam.data.cache

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.suyash.mockcivilaviationexam.data.local.dao.CacheMetadataDao
import com.suyash.mockcivilaviationexam.data.local.dao.ExamQuestionDao
import com.suyash.mockcivilaviationexam.data.local.entities.CacheMetadata
import com.suyash.mockcivilaviationexam.data.local.entities.ExamQuestionEntity
import com.suyash.mockcivilaviationexam.domain.model.Question
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages MCQ questions locally. All 7,948 questions are bundled with the app
 * as a TSV asset file, so no Firebase calls are needed for question data.
 *
 * Flow:
 * 1. Check memory cache (ConcurrentHashMap)
 * 2. Check Room DB
 * 3. If Room is empty, parse bundled TSV asset and populate Room DB
 */
class QuestionCacheManager(
    private val context: Context,
    private val examQuestionDao: ExamQuestionDao? = null,
    private val cacheMetadataDao: CacheMetadataDao? = null,
    private val featureFlags: FeatureFlags? = null
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val memoryCache = ConcurrentHashMap<String, List<Question>>()

    companion object {
        private const val TAG = "QuestionCacheManager"
        private const val PREFS_NAME = "question_cache"
        private const val PREF_TSV_VERSION = "tsv_data_version"
        private const val TSV_ASSET_FILE = "all_questions.tsv"

        /**
         * Bump this whenever the bundled TSV changes (new explanations,
         * answer corrections, added questions). Existing users will
         * re-parse the TSV on next launch when the stored version
         * doesn't match.
         */
        const val TSV_ASSET_VERSION = 5

        // Available sections matching the bundled TSV data
        private val AVAILABLE_SECTIONS = setOf(
            "air_law",
            "aircraft_general",
            "human_performance",
            "meteorology",
            "navigation",
            "operational_procedures",
            "principles_of_flight"
        )
    }

    fun isDataAvailable(sectionId: String): Boolean {
        return AVAILABLE_SECTIONS.contains(sectionId)
    }

    fun getAvailableSections(): Set<String> {
        return AVAILABLE_SECTIONS
    }

    /**
     * Ensures all questions from the bundled TSV are loaded into Room DB.
     * This should be called once at app startup on a background thread.
     * It's idempotent — skips if data is already loaded.
     */
    suspend fun ensureQuestionsLoaded() {
        if (examQuestionDao == null || cacheMetadataDao == null) {
            Log.w(TAG, "Room DAOs not available, cannot load questions")
            return
        }

        // Check if bundled TSV version matches what was previously loaded
        if (prefs.getInt(PREF_TSV_VERSION, 0) >= TSV_ASSET_VERSION) {
            Log.d(TAG, "TSV asset version $TSV_ASSET_VERSION already loaded, skipping")
            return
        }

        Log.d(TAG, "TSV asset version changed (stored=${prefs.getInt(PREF_TSV_VERSION, 0)}, current=$TSV_ASSET_VERSION) — reloading")

        Log.d(TAG, "Loading bundled questions from TSV asset...")

        try {
            val questions = parseTsvAsset()
            if (questions.isEmpty()) {
                Log.e(TAG, "No questions parsed from TSV asset!")
                return
            }

            // Insert into Room DB in batches per section
            val bySection = questions.groupBy { it.sectionId }
            for ((sectionId, sectionQuestions) in bySection) {
                val entities = sectionQuestions.map { ExamQuestionEntity.fromDomain(it) }
                examQuestionDao.deleteQuestionsBySection(sectionId)
                examQuestionDao.insertQuestions(entities)
                cacheMetadataDao.upsert(CacheMetadata(cacheKey = "questions_$sectionId"))
                Log.d(TAG, "Loaded ${entities.size} questions for section '$sectionId'")
            }

            // Store the version so we don't re-parse until the next asset update
            prefs.edit().putInt(PREF_TSV_VERSION, TSV_ASSET_VERSION).apply()

            Log.d(TAG, "Successfully loaded ${questions.size} questions across ${bySection.size} sections")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load questions from TSV: ${e.message}", e)
        }
    }

    /**
     * Parse the bundled TSV asset file into Question objects.
     * TSV format: id, questionText, optionA, optionB, optionC, optionD, correctAnswer, sectionId, updatedAt, explanation
     */
    private fun parseTsvAsset(): List<Question> {
        val questions = mutableListOf<Question>()

        context.assets.open(TSV_ASSET_FILE).use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                // Skip header line
                reader.readLine()

                var line = reader.readLine()
                while (line != null) {
                    try {
                        val parts = line.split("\t")
                        if (parts.size >= 8) {
                            questions.add(
                                Question(
                                    id = parts[0],
                                    questionText = parts[1],
                                    optionA = parts[2],
                                    optionB = parts[3],
                                    optionC = parts[4],
                                    optionD = if (parts.size > 5 && parts[5].isNotBlank()) parts[5] else null,
                                    correctAnswer = parts[6],
                                    sectionId = parts[7],
                                    // Column 9 (index 9) holds the answer
                                    // explanation. Absent in older assets, so
                                    // treat a missing or blank value as none.
                                    explanation = parts.getOrNull(9)?.takeIf { it.isNotBlank() },
                                    difficulty = "medium",
                                    source = "bundled"
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Skipping malformed TSV line: ${e.message}")
                    }
                    line = reader.readLine()
                }
            }
        }

        return questions
    }

    suspend fun getQuestionsFromCache(sectionId: String): List<Question>? {
        return try {
            if (!isDataAvailable(sectionId)) {
                Log.d(TAG, "Section '$sectionId' has no data available")
                return null
            }

            // Check memory cache first
            memoryCache[sectionId]?.let { questions ->
                Log.d(TAG, "MEMORY HIT: ${questions.size} questions for '$sectionId'")
                return questions
            }

            // Check Room DB
            if (examQuestionDao != null) {
                val entities = examQuestionDao.getQuestionsBySection(sectionId)
                if (entities.isNotEmpty()) {
                    val questions = entities.map { it.toDomain() }
                    memoryCache[sectionId] = questions
                    Log.d(TAG, "ROOM HIT: ${questions.size} questions for '$sectionId'")
                    return questions
                }
            }

            // Room was empty — load from TSV asset (first launch or data was cleared)
            Log.d(TAG, "CACHE MISS for '$sectionId' - loading from bundled TSV")
            ensureQuestionsLoaded()

            // Retry from Room after loading
            if (examQuestionDao != null) {
                val entities = examQuestionDao.getQuestionsBySection(sectionId)
                if (entities.isNotEmpty()) {
                    val questions = entities.map { it.toDomain() }
                    memoryCache[sectionId] = questions
                    Log.d(TAG, "ROOM HIT (after TSV load): ${questions.size} questions for '$sectionId'")
                    return questions
                }
            }

            Log.w(TAG, "No questions found for '$sectionId' even after TSV load")
            null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get questions for '$sectionId': ${e.message}", e)
            null
        }
    }

    /**
     * Kept for compatibility — now simply delegates to getQuestionsFromCache
     * since all data is local. No Firebase calls.
     */
    suspend fun fetchAndCacheQuestions(sectionId: String): List<Question> {
        return getQuestionsFromCache(sectionId) ?: emptyList()
    }

    fun clearAllCache() {
        try {
            memoryCache.clear()
            // Reset the version so TSV will be re-parsed on next access
            prefs.edit()
                .putInt(PREF_TSV_VERSION, 0)
                .apply()
            Log.d(TAG, "All cache cleared — TSV will reload on next access")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cache: ${e.message}", e)
        }
    }

    /**
     * Clears Room DB questions as well (for full reset).
     */
    suspend fun clearAllData() {
        clearAllCache()
        try {
            examQuestionDao?.deleteAll()
            cacheMetadataDao?.deleteAll()
            Log.d(TAG, "All Room data cleared")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear Room data: ${e.message}", e)
        }
    }

    fun destroy() {
        memoryCache.clear()
        Log.d(TAG, "Cache manager destroyed")
    }
}
