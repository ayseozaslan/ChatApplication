package com.example.chatays.utils

sealed class LoginState {
     object Idle:LoginState()
     object Loading: LoginState()
     data class Success(val message:String) :LoginState()
    data class Error(val message :String) :LoginState()
}