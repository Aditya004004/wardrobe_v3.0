package com.example.wardeobe.repository

import java.util.concurrent.ConcurrentHashMap

object DeleteTokenRepository {
    // Map of publicId to deleteToken
    val deleteTokenMap = ConcurrentHashMap<String, String>()
}
