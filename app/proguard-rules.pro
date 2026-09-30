# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Aviation Exam Pro - ProGuard Rules

# Keep data models for JSON parsing
-keep class com.suyash.mockcivilaviationexam.domain.model.** { *; }
-keep class com.suyash.mockcivilaviationexam.data.remote.dto.** { *; }

# Keep Gson annotations
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Jetpack Compose
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.lifecycle.** { *; }

# Keep line numbers for crash reports
-keepattributes SourceFile,LineNumberTable

# Coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
# Flight recorder: the in-progress session is persisted as Gson JSON so a
# flight survives process death. Gson reads field names by reflection, so they
# must survive R8 unchanged — otherwise an app update mid-flight would fail to
# restore the session.
-keep class com.suyash.mockcivilaviationexam.domain.logbook.RecorderState { *; }
-keep class com.suyash.mockcivilaviationexam.domain.logbook.RecorderAircraft { *; }
-keep enum com.suyash.mockcivilaviationexam.domain.logbook.FlightPhase { *; }
-keepclassmembers class com.suyash.mockcivilaviationexam.domain.logbook.RecorderState { <init>(...); }
