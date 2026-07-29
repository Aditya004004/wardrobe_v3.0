package com.example.wardeobe.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CancellationException
import androidx.lifecycle.viewModelScope
import com.example.wardeobe.MyApplication
import com.example.wardeobe.model.ClothingItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val profileRepository: com.example.wardeobe.data.ProfileRepository,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _authResult = MutableStateFlow<AuthResult>(AuthResult.Idle)
    val authResult = _authResult.asStateFlow()

    sealed class AuthResult {
        data object Idle : AuthResult()
        data object Success : AuthResult()
        data object LoggedOut : AuthResult() // 🌟 NEW: State for successful logout
        data class Error(val message: String) : AuthResult()
    }

    fun resetState() {
        _authResult.value = AuthResult.Idle
    }

    // --- Authentication Functions (Register/Login remain the same) ---

    // ... registerUser and loginUser remain the same ...

    fun registerUser(email: String, password: String) {
        _isLoading.value = true
        try {
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userData = hashMapOf(
                        "userId" to userId,
                        "email" to email,
                        "profilePictureUrl" to null
                    )
                    db.collection("users").document(userId).set(userData)
                        .addOnSuccessListener {
                            _authResult.value = AuthResult.Success
                        }
                        .addOnFailureListener { e ->
                            auth.currentUser?.delete()?.addOnCompleteListener {
                                _authResult.value = AuthResult.Error("Registration failed: Could not setup profile. Please try again.")
                                Log.e("AuthViewModel", "Firestore save failed, user rolled back: $e")
                            }
                        }
                } else {
                    Log.e("AuthViewModel", "Registration failed", task.exception)
                    _authResult.value = AuthResult.Error("Registration failed. Please check your details and try again.")
                }
                _isLoading.value = false
            }
        } catch (e: CancellationException) { throw e } catch (e: Exception) {
            Log.e("AuthViewModel", "Registration failed with exception", e)
            _authResult.value = AuthResult.Error("Registration failed due to a network or system error.")
            _isLoading.value = false
        }
    }

    fun loginUser(email: String, password: String) {
        _isLoading.value = true
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _authResult.value = AuthResult.Success
            } else {
                Log.e("AuthViewModel", "Login failed", task.exception)
                _authResult.value = AuthResult.Error("Login failed. Please check your email and password.")
            }
            _isLoading.value = false
        }
    }

    // 🌟 NEW: Logout Functionality
    fun logout() {
        auth.signOut()
        profileRepository.clear()
        _authResult.value = AuthResult.LoggedOut
        // We typically clear the global current user here too, if using a global singleton:
        // MyApplication.currentUser = null
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}