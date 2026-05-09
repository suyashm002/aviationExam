# Testing Guide - Firebase Setup Complete ✅

Your `google-services.json` file is in place! Here's how to test everything:

## Step 1: Sync and Build

1. **Sync Gradle**: 
   - In Android Studio, click "Sync Now" if prompted
   - Or go to: File > Sync Project with Gradle Files
   - Wait for sync to complete

2. **Build Project**:
   - Build > Make Project (or press `Ctrl+F9` / `Cmd+F9`)
   - Check for any build errors in the Build output

3. **Run the App**:
   - Click the Run button (green play icon) or press `Shift+F10`
   - Select an emulator or connected device

## Step 2: Test Firebase Authentication

### Test Registration

1. **Launch the app** - You should see the Home screen with 8 sections
2. **Navigate to Profile** - Click "Profile" button in the top right
3. **Click "Login"** - If you're not logged in
4. **Click "Don't have an account? Register"**
5. **Enter test credentials**:
   - Email: `test@example.com` (or any valid email)
   - Password: `test1234` (must be at least 6 characters)
   - Confirm Password: `test1234`
6. **Click "Register"**
7. **Expected Result**: 
   - You should be redirected to the Home screen
   - No error messages should appear

### Verify in Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Navigate to **Authentication** > **Users**
4. You should see your test user account listed

### Test Login

1. **Logout** (if logged in): Go to Profile > Logout
2. **Navigate to Login** screen
3. **Enter your credentials**:
   - Email: `test@example.com`
   - Password: `test1234`
4. **Click "Login"**
5. **Expected Result**: 
   - You should be redirected to Home screen
   - Profile should show your email address

## Step 3: Test Exam Functionality

1. **From Home screen**, click on any section (e.g., "Air Law")
2. **Click "Exam"** button
3. **Take the exam**:
   - Answer 16 questions
   - Use Previous/Next buttons to navigate
   - Click "Submit" after the last question
4. **View Results**:
   - You should see your score
   - Correct/incorrect answers highlighted
   - Explanations for each question

## Step 4: Test Local Storage

1. **Take an exam** (as above)
2. **Close the app completely**
3. **Reopen the app**
4. **Go to Profile**
5. **Expected**: Your exam attempts should be saved locally (will be visible once we add exam history UI)

## Common Issues & Solutions

### Issue: Build fails with "google-services.json not found"
**Solution**: 
- Make sure the file is exactly at `app/google-services.json`
- File name is case-sensitive: `google-services.json` (not `Google-Services.json`)
- Sync Gradle again

### Issue: "Email/Password sign-in method is not enabled"
**Solution**:
1. Go to Firebase Console > Authentication
2. Click "Get started" if first time
3. Go to "Sign-in method" tab
4. Click on "Email/Password"
5. Enable it and click "Save"

### Issue: "Registration failed" or "Login failed"
**Solution**:
- Check internet connection
- Verify Email/Password is enabled in Firebase Console
- Check Logcat for detailed error messages
- Ensure password is at least 6 characters

### Issue: App crashes on launch
**Solution**:
- Check Logcat for error messages
- Verify all dependencies are synced
- Clean and rebuild: Build > Clean Project, then Build > Rebuild Project

## Next Steps After Testing

Once authentication is working:

1. **Enable Firestore** (for cloud sync):
   - Firebase Console > Firestore Database
   - Create database in test mode
   - This will enable syncing exam progress across devices

2. **Add Real Questions**:
   - Parse your PDF files
   - Update `DatabaseInitializer.kt` with real questions
   - Or create JSON files in `app/src/main/assets/questions/`

3. **Test Cloud Sync** (after enabling Firestore):
   - Take an exam on one device
   - Login on another device
   - Exam history should sync (once we implement sync)

## Verification Checklist

- [ ] Gradle sync completed without errors
- [ ] App builds successfully
- [ ] App launches without crashing
- [ ] Can register a new account
- [ ] User appears in Firebase Console
- [ ] Can login with registered account
- [ ] Can take an exam
- [ ] Results screen displays correctly
- [ ] Profile shows logged-in user

## Getting Help

If you encounter issues:
1. Check Logcat for error messages
2. Verify Firebase Console settings
3. Review `FIREBASE_VERIFICATION.md` for troubleshooting
4. Check that all steps in `FIREBASE_SETUP.md` were completed

