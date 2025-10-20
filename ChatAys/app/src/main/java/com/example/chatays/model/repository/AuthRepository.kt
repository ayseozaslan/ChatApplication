package com.example.knowtopia.model.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import android.util.Patterns
import com.example.chatays.model.entities.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,

) {

    private var cachedUserData: User? = null

    val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("603826188305-5iijocsjvjikd5hs9393gh6qrj8nsosk.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    suspend fun firebaseAuthWithGoogle(idToken: String): FirebaseUser? {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return auth.signInWithCredential(credential).await().user
    }

    fun getSignedInAccountFromIntent(data: Intent?): Task<GoogleSignInAccount> {
        return GoogleSignIn.getSignedInAccountFromIntent(data)
    }



    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        if (email.isBlank() || password.isBlank())
            return Result.failure(IllegalArgumentException("Email veya şifre boş olamaz"))

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            return Result.failure(IllegalArgumentException("Geçersiz e-posta formatı"))

        if (password.length < 6)
            return Result.failure(IllegalArgumentException("Şifre en az 6 karakter olmalı"))

        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Log.d("AuthRepository", "signIn başarılı: ${result.user?.email}")
            Result.success(result.user!!)
        } catch (e: Exception) {
            Log.e("AuthRepository", "signIn exception: ${e.message}")
            Result.failure(e)
        }
    }



    suspend fun loadUserData(userID: String): Result<User> {
        return try {
            val document = db.collection("Users").document(userID).get().await()

            if (document.exists()) {
                val userData = document.toObject(User::class.java)

                userData?.let {it ->
                    cachedUserData = it
                    Result.success(it)
                } ?: Result.failure(Exception("User data is null"))
            } else {
                Result.failure(Exception("User data not found"))
            }
        } catch (e: FirebaseFirestoreException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    fun getCachedUserData(): User? {
        return cachedUserData
    }

    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()

    }

    fun checkUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserID(): String? {
        val currentUser = auth.currentUser
        return currentUser?.uid
    }
    fun getCurrentUser(): FirebaseUser? = auth.currentUser


    suspend fun loadCurrentUserData(): Result<User> {
        val userId = getCurrentUserID()
        return if (userId != null) {
            loadUserData(userId)
        } else {
            Result.failure(Exception("No user logged in"))
        }
    }


}
