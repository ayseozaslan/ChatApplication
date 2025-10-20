package com.example.chatays.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatays.data.api.CloudinaryApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class ProfilViewModel  @Inject constructor(
    private val cloudinaryApi: CloudinaryApi,
    private val db: FirebaseFirestore,
) :ViewModel(){

    private val CLOUD_NAME = "dtbvibgjg"         // senin Cloudinary cloud name
    private val UPLOAD_PRESET = "chat_app_upload" // senin oluşturduğun unsigned preset
    private val CLOUDINARY_API_KEY = "672958973647513"


    private val _uploadImage= MutableLiveData<String>()
    var uploadImage :LiveData<String> = _uploadImage


    fun uploadProfileImage(
        imageBytes: ByteArray,
        signature: String,
        timestamp: String,
       onSuccess: () -> Unit,
        onError: (String) -> Unit
    ){
            viewModelScope.launch {
                try {
                    val filePart = MultipartBody.Part.createFormData(
                        "file", "image.jpg",
                        RequestBody.create("image/*".toMediaTypeOrNull(), imageBytes)
                    )

                    val apiKeyBody = RequestBody.create("text/plain".toMediaTypeOrNull(), CLOUDINARY_API_KEY)
                    val timestampBody = RequestBody.create("text/plain".toMediaTypeOrNull(), timestamp)
                    val signatureBody = RequestBody.create("text/plain".toMediaTypeOrNull(), signature)
                    val presetBody = RequestBody.create("text/plain".toMediaTypeOrNull(), "chat_app_upload")

                    val response = cloudinaryApi.uploadImageSetting(
                        cloudName = CLOUD_NAME,
                        file = filePart,
                        apiKey = apiKeyBody,
                        timestamp = timestampBody,
                        signature = signatureBody,
                        preset = presetBody
                    )
                    if (response.isSuccessful) {
                        val imageUrl = response.body()?.secure_url ?: ""
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                       db.collection("Users").document(uid)
                            .update("imageUrl", imageUrl)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener { onError(it.message ?: "Firestore güncelleme hatası") }
                    } else {
                        onError("Cloudinary yükleme başarısız: ${response.code()}")
                    }

                } catch (e: Exception) {
                    onError("Hata: ${e.message}")
                }
            }
        }

}