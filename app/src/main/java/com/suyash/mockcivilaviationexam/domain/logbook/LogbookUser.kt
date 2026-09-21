package com.suyash.mockcivilaviationexam.domain.logbook

import com.google.firebase.auth.FirebaseAuth

/**
 * The identity logbook rows are stored under.
 *
 * Every read and write of a flight entry must use this, so the logbook, the
 * summary and the PDF export all agree on whose flights they are looking at.
 * Earlier builds wrote entries under the literal [LEGACY_USER_ID] while the
 * exporter queried the Firebase uid, which meant exports silently produced an
 * empty logbook.
 */
object LogbookUser {

    /** Identity used by builds before per-account scoping existed. */
    const val LEGACY_USER_ID = "current_user"

    /**
     * Signed-in uid, falling back to the legacy id when signed out so that a
     * pilot who has not logged in still sees the entries they just made.
     */
    fun id(): String = FirebaseAuth.getInstance().currentUser?.uid ?: LEGACY_USER_ID
}
