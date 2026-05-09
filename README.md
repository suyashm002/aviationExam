# Civil Aviation Mock Exam App

An Android native application for civil aviation mock exams with 8 sections covering all major topics for pilot certification exams.

## Features

- **8 Exam Sections**: Air Law, Meteorology, Principles of Flight, Aircraft General, Human Performance and Limitations, Operational Procedures, Navigation, and Communication
- **Mock Exams**: 16 multiple-choice questions per exam with 4 options each
- **Results & Analytics**: Detailed results showing correct/incorrect answers with explanations
- **Study Materials**: Embedded study content and external references for each section
- **User Accounts**: Firebase authentication with progress tracking and cloud sync
- **Offline Support**: All question banks and study materials stored locally

## Architecture

- **Platform**: Android Native (Jetpack Compose)
- **Database**: Room Database for local storage
- **Authentication**: Firebase Auth
- **Cloud Sync**: Firebase Firestore (for user progress)
- **UI**: Material Design 3 with Jetpack Compose

## Setup Instructions

### 1. Firebase Configuration

1. Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
2. Add an Android app to your Firebase project
3. Download `google-services.json` from Firebase Console
4. Place `google-services.json` in the `app/` directory

**Note**: The app will compile without `google-services.json`, but authentication features will not work until it's added.

### 2. Build the Project

1. Open the project in Android Studio
2. Sync Gradle files
3. Build and run the app

### 3. Adding Questions from PDFs

The app includes sample questions for testing. To add questions from your PDF files:

1. Parse your PDF files using a PDF parsing tool
2. Extract questions and convert them to the JSON format
3. Update `DatabaseInitializer.kt` to load questions from JSON files or directly insert them

**Question JSON Format:**
```json
{
  "id": "section_001",
  "section": "Air Law",
  "questionText": "Question text here?",
  "options": ["Option A", "Option B", "Option C", "Option D"],
  "correctAnswer": 0,
  "explanation": "Explanation text",
  "difficulty": "medium",
  "references": ["Reference 1", "Reference 2"]
}
```

## Project Structure

```
app/src/main/
├── java/com/suyash/mockcivilaviationexam/
│   ├── data/
│   │   ├── local/          # Room database, entities, DAOs, repositories
│   │   └── remote/         # Firebase integration (to be implemented)
│   ├── domain/
│   │   └── model/          # Domain models
│   ├── ui/
│   │   ├── navigation/     # Navigation setup
│   │   ├── screens/        # All UI screens
│   │   └── viewmodel/      # ViewModels
│   └── util/               # Utilities and helpers
└── assets/
    └── questions/          # JSON question files (to be added)
```

## Current Implementation Status

✅ **Completed:**
- Room database setup with all entities
- Navigation structure
- Home screen with 8 sections
- Exam taking screen
- Results screen with detailed feedback
- Study materials screen (basic)
- Login/Register screens
- Profile screen
- Sample question data (20 questions per section)
- Database initialization

🔄 **In Progress:**
- Firebase integration for cloud sync
- Performance analytics engine
- Enhanced study materials content

📋 **To Do:**
- PDF parsing utility for bulk question import
- Advanced analytics and insights
- Progress tracking visualization
- Study material content expansion

## Dependencies

- Jetpack Compose
- Room Database
- Navigation Compose
- Firebase (Auth, Firestore, Analytics)
- Coroutines
- Material Design 3

## License

This project is for educational purposes.

