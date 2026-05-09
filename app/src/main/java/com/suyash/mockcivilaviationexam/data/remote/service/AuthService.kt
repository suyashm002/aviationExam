package com.suyash.mockcivilaviationexam.data.remote.service

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthService {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    companion object {
        private const val TAG = "AuthService"
    }
    
    val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    val isUserLoggedIn: Boolean
        get() = currentUser != null
    
    val userEmail: String?
        get() = currentUser?.email
    
    // Flow to observe authentication state changes
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
    
    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            Log.d(TAG, "📤 REQUEST: Signing in user with email: $email")
            
            val result = auth.signInWithEmailAndPassword(email, password).await()
            
            result.user?.let { user ->
                Log.d(TAG, "✅ SUCCESS: User signed in - UID: ${user.uid}, Email: ${user.email}")
                Result.success(user)
            } ?: run {
                Log.e(TAG, "❌ ERROR: Sign in failed - user is null")
                Result.failure(Exception("Sign in failed: user is null"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR: Sign in failed - ${e.message}", e)
            Result.failure(e)
        }
    }
    
    suspend fun createUserWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            Log.d(TAG, "📤 REQUEST: Creating user with email: $email")
            
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            
            result.user?.let { user ->
                Log.d(TAG, "✅ SUCCESS: User created - UID: ${user.uid}, Email: ${user.email}")
                Result.success(user)
            } ?: run {
                Log.e(TAG, "❌ ERROR: Registration failed - user is null")
                Result.failure(Exception("Registration failed: user is null"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR: Registration failed - ${e.message}", e)
            Result.failure(e)
        }
    }
    
    fun signOut() {
        Log.d(TAG, "📤 REQUEST: Signing out user: ${auth.currentUser?.email}")
        auth.signOut()
        Log.d(TAG, "✅ SUCCESS: User signed out")
    }
    
    suspend fun signInWithGoogleCredential(idToken: String): Result<FirebaseUser> {
        return try {
            Log.d(TAG, "📤 REQUEST: Signing in with Google credential")

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()

            result.user?.let { user ->
                Log.d(TAG, "✅ SUCCESS: Google sign-in - UID: ${user.uid}, Email: ${user.email}")
                Result.success(user)
            } ?: run {
                Log.e(TAG, "❌ ERROR: Google sign-in failed - user is null")
                Result.failure(Exception("Google sign-in failed: user is null"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR: Google sign-in failed - ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            Log.d(TAG, "📤 REQUEST: Sending password reset email to: $email")
            
            auth.sendPasswordResetEmail(email).await()
            
            Log.d(TAG, "✅ SUCCESS: Password reset email sent to: $email")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "❌ ERROR: Failed to send password reset email - ${e.message}", e)
            Result.failure(e)
        }
    }
}