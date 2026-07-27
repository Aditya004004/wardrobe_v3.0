package com.example.wardeobe.repository

data class UploadResult(
    val publicId: String,
    val secureUrl: String,
    val deleteToken: String
)
