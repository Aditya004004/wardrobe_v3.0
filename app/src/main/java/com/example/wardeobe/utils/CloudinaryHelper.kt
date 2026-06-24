package com.example.wardeobe.utils

import android.content.Context
import com.cloudinary.Cloudinary
import com.cloudinary.android.MediaManager

object CloudinaryHelper {

    fun init(context: Context, cloudName: String, apiKey: String, apiSecret: String) {
        val config = hashMapOf(
            "cloud_name" to cloudName,
            "api_key" to apiKey,
            "api_secret" to apiSecret,
            "secure" to true
        )
        MediaManager.init(context, config)
    }
}
