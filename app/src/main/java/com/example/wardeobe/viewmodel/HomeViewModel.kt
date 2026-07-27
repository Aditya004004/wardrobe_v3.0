package com.example.wardeobe.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wardeobe.data.WardrobeRepository
import com.example.wardeobe.model.ClothingItem
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

/**
 * UI state representation for the wardrobe screen.
 */
@Immutable
sealed interface WardrobeUiState {
    object Loading : WardrobeUiState
    data class Success(val items: List<ClothingItem>) : WardrobeUiState
    object Empty : WardrobeUiState
    data class Error(val message: String) : WardrobeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WardrobeRepository
) : ViewModel() {

    private val _wardrobeItems = MutableStateFlow<List<ClothingItem>>(emptyList())
    private val _selectedCategory = MutableStateFlow("All")
    private val _uiState = MutableStateFlow<WardrobeUiState>(WardrobeUiState.Loading)

    val uiState: StateFlow<WardrobeUiState> = _uiState
    val filteredWardrobeItems: StateFlow<List<ClothingItem>> = combine(_wardrobeItems, _selectedCategory) { items, category ->
        if (category == "All") items else items.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    val selectedCategory: StateFlow<String> = _selectedCategory

    fun setFilterCategory(category: String) {
        _selectedCategory.value = category
    }

    fun fetchImages() {
        viewModelScope.launch {
            _uiState.value = WardrobeUiState.Loading
            val uid = Firebase.auth.currentUser?.uid ?: run {
                Log.e("HomeViewModel", "User not authenticated")
                _uiState.value = WardrobeUiState.Error("User not authenticated")
                return@launch
            }
            try {
                val items = repository.fetchItems(uid)
                _wardrobeItems.value = items
                if (items.isEmpty()) {
                    _uiState.value = WardrobeUiState.Empty
                } else {
                    _uiState.value = WardrobeUiState.Success(items)
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching images: ${e.message}")
                _uiState.value = WardrobeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun deleteClothingItem(itemId: String, onDeletionResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val uid = Firebase.auth.currentUser?.uid ?: run {
                onDeletionResult(false)
                return@launch
            }
            try {
                repository.deleteItem(uid, itemId)
                _wardrobeItems.update { current -> current.filter { it.id != itemId } }
                onDeletionResult(true)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error deleting item: ${e.message}")
                onDeletionResult(false)
            }
        }
    }
}
