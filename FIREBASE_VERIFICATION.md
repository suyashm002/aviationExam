# Firebase Setup Verification

## File Location

Your `google-services.json` file should be placed at:
```
app/google-services.json
```

This is the same directory level as `app/build.gradle.kts`.

## Verification Steps

1. **Check File Location**: Ensure `google-services.json` is in the `app/` directory
2. **Sync Gradle**: In Android Studio, click "Sync Now" when prompted, or go to File > Sync Project with Gradle Files
3. **Build Project**: Build the project (Build > Make Project) to verify there are no errors
4. **Check Firebase Console**: 
   - Go to Firebase Console > Authentication
   - Enable "Email/Password" sign-in method if not already enabled
   - Go to Firestore Database and create a database if you want cloud sync

## Testing Firebase

Once the file is in place:

1. Run the app
2. Try to register a new account from the Register screen
3. Try to login with the registered account
4. Check Firebase Console > Authentication to see if the user was created

## Common Issues

- **Build Error**: If you get an error about google-services.json, make sure:
  - The file is named exactly `google-services.json` (case-sensitive)
  - The file is in the `app/` directory, not `app/src/`
  - The package name in the JSON matches `com.suyash.mockcivilaviationexam`

- **Authentication Not Working**: 
  - Check that Email/Password is enabled in Firebase Console
  - Verify the SHA-1 certificate fingerprint is added to Firebase (for release builds)

