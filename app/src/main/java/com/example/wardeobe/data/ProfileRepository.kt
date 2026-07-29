package com.example.wardeobe.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val _profilePictureUrl = MutableStateFlow("")
    val profilePictureUrl: StateFlow<String> = _profilePictureUrl.asStateFlow()

    suspend fun fetchProfilePictureUrl(userId: String): String {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            val url = document.getString("profilePictureUrl") ?: ""
            _profilePictureUrl.value = url
            url
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            ""
        }
    }

    fun updateProfilePictureUrl(url: String) {
        _profilePictureUrl.value = url
    }

    fun clear() {
        _profilePictureUrl.value = ""
    }
}
