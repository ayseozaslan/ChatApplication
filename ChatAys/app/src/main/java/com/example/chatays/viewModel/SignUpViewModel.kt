package com.example.chatays.viewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatays.model.entities.ChatMessage
import com.example.chatays.model.entities.ChatSummary
import com.example.chatays.model.entities.User
import com.example.chatays.model.repository.ChatRepository
import com.example.chatays.model.repository.ImageRepository
import com.example.chatays.utils.ContactsHelper
import com.example.chatays.utils.SignUpState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val auth:FirebaseAuth,
    private val db: FirebaseFirestore,
    private val repo : ImageRepository,
    private val talksRepo: ChatRepository

) :ViewModel() {

     private val CLOUD_NAME = "dtbvibgjg"         // senin Cloudinary cloud name
    private val UPLOAD_PRESET = "chat_app_upload" // senin oluşturduğun unsigned preset


    private val _signUpState = MutableLiveData<SignUpState>(SignUpState.Idle)
    val signUpState: LiveData<SignUpState> = _signUpState

    private val _opengallery = MutableLiveData<Unit>()
    val opengallery: LiveData<Unit> = _opengallery

    private val _profileImageUrl = MutableLiveData<String?>()
    val profileImageUrl: LiveData<String?> = _profileImageUrl

    private val _updateImage = MutableLiveData<Boolean>()
    val updateImage: LiveData<Boolean> = _updateImage

    private val _chats=MutableLiveData<List<ChatSummary>>()
    val chat: LiveData<List<ChatSummary>> = _chats

    private var chatListener : ListenerRegistration? = null
    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> get() = _messages
    private var listenerRegistration: ListenerRegistration? = null

    private val _userList = MutableLiveData<List<User>>()
    val userList: LiveData<List<User>> get() = _userList


    fun listenToChatRoom(chatRoomId: String) {
        listenerRegistration?.remove() // Önceki listener varsa kaldır

        listenerRegistration = db.collection("ChatRooms")
            .document(chatRoomId)
            .collection("messages")
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val chatList = snapshot.toObjects(ChatMessage::class.java)
                _messages.value = chatList
            }
    }
    fun sendMessage(
        chatRoomId: String,
        sender: String,
        receiver: String,
        text: String?,
        imageUrl: String? = null
    ) {
        if ((text.isNullOrBlank()) && (imageUrl.isNullOrBlank())) return

        val chatRoomRef = db.collection("ChatRooms").document(chatRoomId)

        chatRoomRef.get().addOnSuccessListener { doc ->
            if (!doc.exists()) {
                chatRoomRef.set(
                    mapOf(
                        "user1Id" to sender,
                        "user2Id" to receiver
                    )
                )
            }

            val dataMap = hashMapOf<String, Any>(
                "user1Id" to sender,
                "user2Id" to receiver,
                "date" to FieldValue.serverTimestamp()
            )

            text?.let { dataMap["text"] = it }           // text varsa ekle
            imageUrl?.let { dataMap["imageUrl"] = it }   // imageUrl varsa ekle

            chatRoomRef.collection("messages").add(dataMap)
        }
    }



    fun listenChats(currentUser: String) {
        talksRepo.listenUserChatRooms(currentUser,
            onUpdate = { list ->
                _chats.postValue(list)
            },
            onError = { e ->
                Log.e("ViewModel", "Hata: ${e.message}")
            }
        )
    }


    override fun onCleared() {
        super.onCleared()
        chatListener?.remove()
    }



    fun onProfileImageClicked() {
        _opengallery.value = Unit
    }

    private val _contactsUsingApp = MutableLiveData<List<User>>()
    val contactsUsingApp: LiveData<List<User>> = _contactsUsingApp

    fun fetchContactsUsingApp(context: Context) {
        val emails = ContactsHelper.getContactsEmails(context)
        if (emails.isEmpty()) {
            _contactsUsingApp.value = emptyList()
            return
        }

        val matchedUsers = mutableListOf<User>()
        val chunkedEmails = emails.chunked(10)
        var chunksProcessed = 0

        chunkedEmails.forEach { chunk ->
            db.collection("Users")
                .whereIn("email", chunk)
                .get()
                .addOnSuccessListener { snapshot ->
                    val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
                    matchedUsers.addAll(users)

                    Log.d("ContactsHelper", "Eşleşen kullanıcı sayısı: ${matchedUsers.size}")
                    matchedUsers.forEach {
                        Log.d("ContactsHelper", "Kullanıcı: ${it.username}, Email: ${it.email}")
                    }

                    chunksProcessed++
                    if (chunksProcessed == chunkedEmails.size) _contactsUsingApp.value =
                        matchedUsers
                }
                .addOnFailureListener {
                    chunksProcessed++
                    if (chunksProcessed == chunkedEmails.size) {
                        _contactsUsingApp.value = matchedUsers
                    }
                }
        }
    }

    fun signUp(context: Context, email: String, password: String, username: String, imageUri: Uri?) {
        Log.d("SignUp", "👉 signUp() çağrıldı. email=$email username=$username")

        _signUpState.value = SignUpState.Loading

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                Log.d(
                    "SignUp",
                    "👉 createUserWithEmailAndPassword() bitti. success=${task.isSuccessful}"
                )

                if (task.isSuccessful) {
                    val user = task.result?.user
                    val userId = user?.uid
                    val userEmail = auth.currentUser?.email ?: email
                    Log.d("SignUp", "👉 Yeni kullanıcı oluşturuldu. uid=$userId email=$userEmail")

                    if (userId != null) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build()

                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { updateTask ->
                                Log.d(
                                    "SignUp",
                                    "👉 updateProfile() bitti. success=${updateTask.isSuccessful}"
                                )

                                if (updateTask.isSuccessful) {
                                    Log.d("SignUp", "✅ Kullanıcı profili güncellendi")

                                    viewModelScope.launch {
                                        try {
                                            Log.d(
                                                "SignUp",
                                                "👉 Coroutine başlatıldı, Cloudinary kontrol ediliyor"
                                            )

                                            val uploadedUrl = if (imageUri != null) {
                                                Log.d(
                                                    "SignUp",
                                                    "👉 Resim seçildi: $imageUri, Cloudinary’e yükleniyor..."
                                                )
                                                repo.uploadToCloudinary(
                                                    context,
                                                    imageUri,
                                                    CLOUD_NAME,
                                                    UPLOAD_PRESET
                                                )
                                                    .also {
                                                        Log.d(
                                                            "SignUp",
                                                            "✅ Cloudinary’den URL geldi: $it"
                                                        )
                                                    }
                                            } else {
                                                Log.d(
                                                    "SignUp",
                                                    "👉 imageUri null, Cloudinary’e yüklenmeyecek"
                                                )
                                                null
                                            }

                                            Log.d("SignUp", "👉 Firestore’a kayıt yapılıyor...")
                                            saveUserToFirestore(
                                                userId,
                                                userEmail,
                                                username,
                                                uploadedUrl
                                            )

                                            _signUpState.postValue(SignUpState.Success("Kayıt başarılı"))
                                        } catch (e: Exception) {
                                            Log.e(
                                                "SignUp",
                                                "❌ Cloudinary/Firestore hatası: ${e.message}"
                                            )
                                            _signUpState.postValue(SignUpState.Error("Kayıt sırasında hata oluştu"))
                                        }
                                    }
                                } else {
                                    Log.e(
                                        "SignUp",
                                        "❌ Profil güncellenemedi: ${updateTask.exception?.message}"
                                    )
                                    _signUpState.value =
                                        SignUpState.Error("Kullanıcı adı güncellenemedi")
                                }
                            }
                    } else {
                        Log.e("SignUp", "❌ User ID null geldi")
                        _signUpState.value = SignUpState.Error("User ID alınamadı")
                    }
                } else {
                    val exception = task.exception
                    val message = when (exception) {
                        is FirebaseAuthWeakPasswordException -> "Şifre çok zayıf, en az 6 karakter olmalı"
                        is FirebaseAuthInvalidCredentialsException -> "Email formatı geçersiz"
                        is FirebaseAuthUserCollisionException -> "Bu email zaten kullanımda"
                        else -> exception?.message ?: "Bilinmeyen hata oluştu"
                    }
                    Log.e("SignUp", "❌ Auth hatası: $message")
                    _signUpState.value = SignUpState.Error(message)
                }
            }
    }


    private fun saveUserToFirestore(
        userId: String,
        email: String,
        username: String,
        imageUrl: String?
    ) {
        val user = User(username = username, email = email, imageUrl = imageUrl)
        Log.d("Firestore", "👉 Firestore kayıt başlıyor: $user")

        db.collection("Users").document(userId).set(user)
            .addOnSuccessListener {
                Log.d("Firestore", "✅ Kullanıcı başarıyla kaydedildi")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "❌ Kullanıcı kaydı başarısız: ${e.message}")
            }
    }

    fun resetSignUpState() {
        _signUpState.value = SignUpState.Idle
    }

    fun  saveGoogleUser(firebaseUser: FirebaseUser){
        val userId = firebaseUser.uid
        val email = firebaseUser.email ?: ""
        val username= firebaseUser.displayName ?: ""
        val imageUrl = firebaseUser.photoUrl?. toString()

        saveUserToFirestore(userId,email,username,imageUrl)
    }

}

