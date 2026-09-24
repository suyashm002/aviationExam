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
        ExamQuestionResultEntity::class,
        QuestionFeedbackEntity::class,
        FlightTrackPointEntity::class
    ],
    version = 7,
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
    abstract fun questionFeedbackDao(): QuestionFeedbackDao
    abstract fun flightTrackPointDao(): FlightTrackPointDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Route, performance and recorder data for the PPL flight tracker, plus
         * the GPS track table. Every new column is nullable or defaulted so
         * existing entries stay valid.
         */
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN aircraftId INTEGER")
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN routeVia TEXT")
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN cruiseAltitudeFt INTEGER")
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN cruiseSpeedKt INTEGER")
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN maxAltitudeFt INTEGER")
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN maxGroundSpeedKt INTEGER")
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN distanceNm REAL")
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN hobbsStart REAL")
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN hobbsEnd REAL")
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN hasTrack INTEGER NOT NULL DEFAULT 0")

                db.execSQL("ALTER TABLE aircraft ADD COLUMN cruiseSpeedKt INTEGER")
                db.execSQL("ALTER TABLE aircraft ADD COLUMN cruiseAltitudeFt INTEGER")

                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `flight_track_points` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `sessionId` TEXT NOT NULL,
                        `flightId` INTEGER,
                        `timestamp` INTEGER NOT NULL,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `altitudeFt` REAL NOT NULL,
                        `groundSpeedKt` REAL NOT NULL,
                        `bearingDeg` REAL
                    )"""
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_flight_track_points_flightId` ON `flight_track_points` (`flightId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_flight_track_points_sessionId` ON `flight_track_points` (`sessionId`)")
            }
        }

        /**
         * Adds block/air clock times, landings and approach counts to the
         * logbook. Every column is added with a default so existing entries
         * stay valid — they simply have no clock times recorded.
         */
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN offBlockTime TEXT")
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN takeoffTime TEXT")
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN landingTime TEXT")
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN onBlockTime TEXT")
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN blockTime REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN airTime REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN dayLandings INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN nightLandings INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE flight_entries ADD COLUMN instrumentApproaches INTEGER NOT NULL DEFAULT 0")

                // Existing rows have a logged total but no block figure. Flight
                // time as logged IS block time, so seed it rather than leaving
                // historic hours out of block-time totals.
                db.execSQL("UPDATE flight_entries SET blockTime = totalFlightTime WHERE blockTime = 0.0")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `question_feedback` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `questionId` TEXT NOT NULL,
                        `sectionId` TEXT NOT NULL,
                        `questionText` TEXT NOT NULL,
                        `feedbackType` TEXT NOT NULL,
                        `comment` TEXT NOT NULL DEFAULT '',
                        `userEmail` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `synced` INTEGER NOT NULL DEFAULT 0
                    )"""
                )
            }
        }

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
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
