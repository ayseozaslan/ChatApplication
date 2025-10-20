package com.example.chatays.model.entities

import android.os.Message
import com.google.firebase.auth.FirebaseUser


data class User(
    val username: String = "",
    val password: String = "",
    val email: String = "",
    val imageUrl: String? = ""

)
data class ChatMessage(
    val user1Id: String = "",
    val user2Id: String = "",
    val text: String = "",
    val imageUrl: String? = "",
    val date: com.google.firebase.Timestamp? = null
)


data class ChatSummary(
    val userId: String = "",
    val username: String = "",
    val imageUrl: String? = "",
    val lastMessage: String = ""
)
