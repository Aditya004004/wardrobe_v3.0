package com.example.wardeobe.utils

import okhttp3.OkHttpClient

object OkHttpClientProvider {
    val client: OkHttpClient by lazy {
        OkHttpClient()
    }
}
