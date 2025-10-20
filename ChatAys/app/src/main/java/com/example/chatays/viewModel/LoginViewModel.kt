package com.example.chatays.viewModel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatays.utils.LoginState
import com.example.knowtopia.model.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val auth: FirebaseAuth
) :ViewModel(){
    private val _loginState = MutableLiveData<LoginState>()
    val loginState : LiveData<LoginState> = _loginState


    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading
        Log.d("LoginViewModel", "login() called with $email / $password")

        viewModelScope.launch {
            try {
                val user = authRepository.signIn(email, password).getOrThrow()
                Log.d("LoginViewModel", "signIn successful: ${user.email}")
                _loginState.value = LoginState.Success("Giriş başarılı")
            } catch (e: Exception) {
                Log.e("LoginViewModel", "signIn failed: ${e.message}")
                _loginState.value = LoginState.Error(e.message ?: "Bilinmeyen hata")
            }
        }
    }


























}