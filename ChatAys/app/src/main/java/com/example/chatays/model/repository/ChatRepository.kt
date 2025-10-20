package com.example.chatays.model.repository

import android.util.Log
import com.example.chatays.model.entities.ChatSummary
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject



class ChatRepository @Inject constructor(private val db: FirebaseFirestore) {



    fun setTypingState(chatRoomId: String,userId: String, isTyping: Boolean){

        val typingData = mapOf(
            "typing" to isTyping,
            "timestamp" to FieldValue.serverTimestamp()
        )

        db.collection("ChatRooms")
            .document(chatRoomId)
            .collection("typing")
            .document(userId)
            .set(typingData, SetOptions.merge())
    }

    fun listenToTyping(chatRoomId: String, otherUserId: String, onChange: (Boolean) -> Unit): ListenerRegistration {
        return db.collection("ChatRooms")
            .document(chatRoomId)
            .collection("typing")
            .document(otherUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val isTyping = snapshot?.getBoolean("typing") ?: false
                onChange(isTyping)
            }
    }
    fun listenUserChatRooms(
        currentUser: String,
        onUpdate: (List<ChatSummary>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        Log.d("Repo", "listenUserChatRooms çağrıldı, currentUser=$currentUser")

        return db.collection("ChatRooms")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Repo", "Hata: ${error.message}")
                    onError(error)
                    return@addSnapshotListener
                }
                if (snapshot == null) {
                    Log.d("Repo", "Snapshot null geldi")
                    return@addSnapshotListener
                }

                Log.d("Repo", "Toplam chatRoom belgesi: ${snapshot.documents.size}")

                val chatMap = mutableMapOf<String, ChatSummary>()
                val tasks = mutableListOf<Task<*>>()

                for (doc in snapshot.documents) {
                    val user1 = doc.getString("user1Id") ?: continue
                    val user2 = doc.getString("user2Id") ?: continue

                    if (user1 != currentUser && user2 != currentUser) continue

                    val otherUser = if (user1 == currentUser) user2 else user1
                    Log.d("Repo", "ChatRoom: ${doc.id}, otherUser=$otherUser")

                    // 🔹 Son mesaj ve kullanıcı bilgisini tek task zincirinde al
                    val task = doc.reference.collection("messages")
                        .orderBy("date", Query.Direction.DESCENDING)
                        .limit(1)
                        .get()
                        .continueWithTask { msgSnapTask ->
                            val lastMessage = msgSnapTask.result?.documents?.firstOrNull()?.getString("text") ?: ""
                            Log.d("Repo", "ChatRoom: ${doc.id}, lastMessage='$lastMessage'")

                            // Users koleksiyonundan otherUser bilgisi al
                            db.collection("Users")
                                .whereEqualTo("username", otherUser)
                                .limit(1)
                                .get()
                                .addOnSuccessListener { userSnap ->
                                    val userDoc = userSnap.documents.firstOrNull()
                                    val imageUrl = userDoc?.getString("imageUrl") ?: ""

                                    chatMap[otherUser] = ChatSummary(
                                        userId = otherUser,
                                        username = otherUser,
                                        imageUrl = imageUrl,
                                        lastMessage = lastMessage
                                    )
                                    Log.d("Repo", "chatMap güncellendi: ${chatMap.keys}")
                                }
                        }
                    tasks.add(task)
                }
                Tasks.whenAllComplete(tasks).addOnSuccessListener {
                    Log.d("Repo", "Tüm async işlemler tamamlandı. chatMapSize=${chatMap.size}")
                    onUpdate(chatMap.values.sortedByDescending { it.lastMessage }.toList())
                }
            }
    }




}
