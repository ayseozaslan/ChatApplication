package com.example.chatays.data.api

import com.example.chatays.model.response.CloudinaryResponse
import com.google.android.gms.common.api.Response
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface CloudinaryApi {
    // POST https://api.cloudinary.com/v1_1/{cloud_name}/image/upload
    @Multipart
    @POST("v1_1/{cloud_name}/image/upload")
    suspend fun uploadImage(
        @Path("cloud_name") cloudName: String,           // cloud name
        @Part file: MultipartBody.Part,                   // görsel dosyası
        @Part("upload_preset") preset: RequestBody       // unsigned preset
    ): retrofit2.Response<CloudinaryResponse>       // Retrofit Response wrapper


    @Multipart
    @POST("v1_1/{cloud_name}/image/upload")
    suspend fun uploadImageSetting(
        @Path("cloud_name") cloudName: String,           // cloud name
        @Part file: MultipartBody.Part,
        @Part("api_key") apiKey: RequestBody,
        @Part("timestamp") timestamp: RequestBody,
        @Part("signature") signature: RequestBody,// görsel dosyası
        @Part("upload_preset") preset: RequestBody       // unsigned preset
    ): retrofit2.Response<CloudinaryResponse>
}




/*
interface CloudinaryApi {
    // POST https://api.cloudinary.com/v1_1/{cloud_name}/image/upload
    @Multipart
    @POST("v1_1/{cloud_name}/image/upload")
    suspend fun uploadImage(
        @Path("cloud_name") cloudName: String,           // cloud name
        @Part file: MultipartBody.Part,
        @Part("api_key") apiKey: RequestBody,
        @Part("timestamp") timestamp: RequestBody,
        @Part("signature") signature: RequestBody,// görsel dosyası
        @Part("upload_preset") preset: RequestBody       // unsigned preset
    ): retrofit2.Response<CloudinaryResponse>                      // Retrofit Response wrapper
}

 */






