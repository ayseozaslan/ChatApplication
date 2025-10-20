package com.example.chatays.viewModel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatays.model.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository:ChatRepository
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val client = OkHttpClient()
    private val _uploadState = MutableLiveData<Boolean>()
    val uploadState: LiveData<Boolean> = _uploadState

    private val chatRoomId = "chatRoom1"

    private val _otherUserTyping = MutableLiveData<Boolean>()
    val otherUserTyping: LiveData<Boolean> = _otherUserTyping

    private var typingListener: ListenerRegistration? = null

    fun setTypingState(chatRoomId: String, userId: String, isTyping: Boolean) {
        repository.setTypingState(chatRoomId, userId, isTyping)
    }

    fun listenToTyping(chatRoomId: String, otherUserId: String) {
        typingListener?.remove()
        typingListener = repository.listenToTyping(chatRoomId, otherUserId) { isTyping ->
            _otherUserTyping.value = isTyping
        }
    }
    fun uploadImageToCloudinary(context: Context, imageUri: Uri, onComplete: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cloudName =  "dtbvibgjg"
                val uploadPreset = "chat_app_upload"

                val inputStream = context.contentResolver.openInputStream(imageUri)
                val bytes = inputStream?.readBytes() ?: return@launch onComplete(null)
                val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())

                val request = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file", "image.jpg", requestBody)
                    .addFormDataPart("upload_preset", uploadPreset)
                    .build()

                val requestCloud = Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/$cloudName/image/upload")
                    .post(request)
                    .build()

                val response = client.newCall(requestCloud).execute()
                val responseBody = response.body?.string()
                Log.d("Cloudinary", "responseBody: $responseBody")
                val json = JSONObject(responseBody ?: "")
                val imageUrl = json.optString("secure_url", null)
                Log.d("Cloudinary", "imageUrl: $imageUrl")


                onComplete(imageUrl)
                _uploadState.postValue(true)

            } catch (e: Exception) {
                e.printStackTrace()
                _uploadState.postValue(false)
                onComplete(null)
            }
        }
    }
}
