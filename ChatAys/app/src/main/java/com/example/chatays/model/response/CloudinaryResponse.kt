package com.example.chatays.model.response

data class CloudinaryResponse(
    val secure_url: String?,    // Cloudinary döndürdüğü halka açık güvenli URL
    val url: String?,
    val public_id: String?
)