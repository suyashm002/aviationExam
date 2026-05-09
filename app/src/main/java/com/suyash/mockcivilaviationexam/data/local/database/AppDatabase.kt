package com.suyash.mockcivilaviationexam.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.suyash.mockcivilaviationexam.data.local.dao.*
import com.suyash.mockcivilaviationexam.data.local.entities.*

@Database(
    entities = [
        FlightEntryEntity::class,
        AircraftEntity::class,
        InstructorEntity::class,
        UserProfileEntity::class,
        SectionEntity::class,
        StudyMaterialEntity::class,
        ExamQuestionEntity::class,
        ExamSectionEntity::class,
        CacheMetadata::class,
        ExamSessionEntity::class,
        ExamQuestionResultEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun flightEntryDao(): FlightEntryDao
    abstract fun aircraftDao(): AircraftDao
    abstract fun instructorDao(): InstructorDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun sectionDao(): SectionDao
    abstract fun studyMaterialDao(): StudyMaterialDao
    abstract fun examQuestionDao(): ExamQuestionDao
    abstract fun examSectionDao(): ExamSectionDao
    abstract fun cacheMetadataDao(): CacheMetadataDao
    abstract fun examSessionDao(): ExamSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `exam_sessions_local` (
                        `id` TEXT NOT NULL,
                        `sectionId` TEXT NOT NULL,
                        `userEmail` TEXT NOT NULL,
                        `totalQuestions` INTEGER NOT NULL,
                        `score` REAL NOT NULL DEFAULT 0.0,
                        `correctAnswers` INTEGER NOT NULL DEFAULT 0,
                        `completed` INTEGER NOT NULL DEFAULT 0,
                        `timeTaken` INTEGER,
                        `startedAt` INTEGER NOT NULL,
                        `completedAt` INTEGER,
                        PRIMARY KEY(`id`)
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `exam_question_results` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sessionId` TEXT NOT NULL,
                        `questionId` TEXT NOT NULL,
                        `questionText` TEXT NOT NULL,
                        `selectedAnswer` TEXT NOT NULL,
                        `correctAnswer` TEXT NOT NULL,
                        `isCorrect` INTEGER NOT NULL DEFAULT 0,
                        `explanation` TEXT
                    )"""
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `exam_questions` (
                        `id` TEXT NOT NULL,
                        `sectionId` TEXT NOT NULL,
                        `questionText` TEXT NOT NULL,
                        `optionA` TEXT NOT NULL,
                        `optionB` TEXT NOT NULL,
                        `optionC` TEXT NOT NULL,
                        `optionD` TEXT,
                        `correctAnswer` TEXT NOT NULL,
                        `explanation` TEXT,
                        `difficulty` TEXT NOT NULL,
                        `source` TEXT NOT NULL,
                        `cachedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `exam_sections` (
                        `id` TEXT NOT NULL,
                        `sectionId` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `description` TEXT,
                        `icon` TEXT,
                        `cachedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )"""
                )
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `cache_metadata` (
                        `cacheKey` TEXT NOT NULL,
                        `cachedAt` INTEGER NOT NULL,
                        PRIMARY KEY(`cacheKey`)
                    )"""
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kcaa_pilot_logbook_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
