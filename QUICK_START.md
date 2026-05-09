# Quick Start Guide

## Firebase Setup Complete! ✅

You've set up Firebase and have the `google-services.json` file. Here's what to do next:

### Step 1: Place google-services.json

Place your `google-services.json` file in the `app/` directory:
```
app/google-services.json
```

The file should be at the same level as `app/build.gradle.kts`.

### Step 2: Sync and Build

1. **Sync Gradle**: In Android Studio, click "Sync Now" when prompted
2. **Build Project**: Build > Make Project (or press Ctrl+F9 / Cmd+F9)
3. **Run**: Click the Run button or press Shift+F10

### Step 3: Test Firebase Authentication

1. **Run the app** on an emulator or device
2. **Navigate to Profile** from the home screen
3. **Click "Login"** if not logged in
4. **Register a new account**:
   - Enter an email address
   - Enter a password (at least 6 characters)
   - Confirm password
   - Click "Register"
5. **Login** with your new account
6. **Verify in Firebase Console**:
   - Go to Firebase Console > Authentication
   - You should see your new user account

### Step 4: Enable Firestore (Optional - for Cloud Sync)

If you want to sync exam progress across devices:

1. Go to Firebase Console > Firestore Database
2. Click "Create database"
3. Start in **test mode** (for development)
4. Select a location (choose closest to your users)
5. Click "Enable"

The app will automatically start syncing exam attempts to Firestore once enabled.

### Current Features Working

✅ **Local Storage**: All questions and study materials stored locally  
✅ **Authentication**: Login/Register with Firebase  
✅ **Mock Exams**: Take 16-question exams for each section  
✅ **Results**: View detailed results with explanations  
✅ **Study Materials**: Access study content (basic structure)  

### Next Steps

- Add your PDF questions using the PDF parsing utility (to be created)
- Enable Firestore for cloud sync
- Expand study materials content
- Add performance analytics

### Troubleshooting

**Build Error about google-services.json:**
- Make sure the file is named exactly `google-services.json` (case-sensitive)
- Verify it's in the `app/` directory, not `app/src/`
- Check that the package name in the JSON matches `com.suyash.mockcivilaviationexam`

**Authentication Not Working:**
- Verify Email/Password is enabled in Firebase Console > Authentication
- Check that you have internet connection
- Look at Logcat for error messages

**Questions:**
- Check `FIREBASE_VERIFICATION.md` for detailed troubleshooting
- Check `README.md` for general project information

