package com.example.chatays.model.repository

import android.content.Context
import com.example.chatays.data.api.CloudinaryApi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.net.Uri
import okhttp3.RequestBody
import javax.inject.Inject

class ImageRepository @Inject constructor(){

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.cloudinary.com/")  // Cloudinary base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(CloudinaryApi::class.java)

    suspend fun uploadToCloudinary(context: Context, uri: Uri, cloudName: String, uploadPreset: String): String? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val bytes = inputStream.readBytes()
        inputStream.close()

        val requestBody = bytes.toRequestBody("image/*".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", "profile.jpg", requestBody)
        val presetBody = uploadPreset.toRequestBody("text/plain".toMediaTypeOrNull())

        val response = api.uploadImage(cloudName, filePart, presetBody)

        return if (response.isSuccessful) {
            response.body()?.secure_url
        } else {
            null
        }
    }
}
