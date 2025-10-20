package com.example.chatays.viewModel


import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.knowtopia.model.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow<FirebaseUser?>(null)
    val user: StateFlow<FirebaseUser?> = _user

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun getSignInIntent() = repository.getSignInIntent()

    fun handleSignInResult(intent: Intent?) {
        if (intent == null) return
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.getResult(ApiException::class.java)
            viewModelScope.launch {
                try {
                    val user = repository.firebaseAuthWithGoogle(account.idToken!!)
                    _user.value = user
                } catch (e: Exception) {
                    _error.value = e.message
                }
            }
        } catch (e: ApiException) {
            _error.value = "Google oturumu alınamadı: ${e.message}"
        }
    }

    fun signOut() {
        repository.signOut()
        _user.value = null
    }

    fun getCurrentUser() {
        _user.value = repository.getCurrentUser()
    }
}
