# Release Setup Guide

## 1. Create Release Keystore

Run this command in your project root directory to generate a keystore:

```bash
keytool -genkey -v -keystore aviation-exam-pro-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias aviation-exam-pro
```

**Important:** Save the keystore password and alias password securely! You'll need them for future app updates.

## 2. Configure Signing in build.gradle.kts

Add this to your `app/build.gradle.kts` file in the `android` block:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("../aviation-exam-pro-release.jks")
        storePassword = "YOUR_KEYSTORE_PASSWORD"
        keyAlias = "aviation-exam-pro"
        keyPassword = "YOUR_KEY_PASSWORD"
    }
}

buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        signingConfig = signingConfigs.getByName("release")
        isDebuggable = false
    }
}
```

## 3. Create ProGuard Rules

Create/update `app/proguard-rules.pro`:

```
-keep class com.suyash.mockcivilaviationexam.domain.model.** { *; }
-keep class com.suyash.mockcivilaviationexam.data.remote.dto.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
```

## 4. Build Release APK

```bash
./gradlew assembleRelease
```

The APK will be generated in: `app/build/outputs/apk/release/app-release.apk`

## 5. Test Release Build

Install and test the release APK on multiple devices before uploading to Play Store.