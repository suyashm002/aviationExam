# Firebase Setup Guide

## Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project"
3. Enter project name: "Civil Aviation Mock Exam" (or your preferred name)
4. Follow the setup wizard

## Step 2: Add Android App

1. In Firebase Console, click "Add app" and select Android
2. Enter package name: `com.suyash.mockcivilaviationexam`
3. Enter app nickname (optional)
4. Click "Register app"

## Step 3: Download google-services.json

1. Download the `google-services.json` file
2. Place it in the `app/` directory (same level as `build.gradle.kts`)

## Step 4: Enable Authentication

1. In Firebase Console, go to "Authentication"
2. Click "Get started"
3. Enable "Email/Password" sign-in method
4. Click "Save"

## Step 5: Enable Firestore (Optional - for cloud sync)

1. In Firebase Console, go to "Firestore Database"
2. Click "Create database"
3. Start in test mode (for development)
4. Select a location for your database
5. Click "Enable"

## Step 6: Build and Run

1. Sync Gradle files in Android Studio
2. Build the project
3. Run the app

**Note**: The app will work without Firebase for local-only usage, but authentication and cloud sync features require Firebase setup.

