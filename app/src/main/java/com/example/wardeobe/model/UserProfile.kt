package com.example.wardeobe.model

import androidx.compose.runtime.Immutable

data class UserProfile(
    val gender: String = "", // Male or Female
    val bodyType: String = "", // H, X, Y, O, A
    val skinTone: String = "", // Cool or Warm
    val ageGroup: String = "",
    val heightGroup: String = ""
)