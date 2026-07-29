package com.example.wardeobe

import com.example.wardeobe.data.WardrobeRepository
import com.example.wardeobe.model.ClothingItem
import com.example.wardeobe.viewmodel.HomeViewModel
import com.example.wardeobe.viewmodel.WardrobeUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: WardrobeRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var mockUser: FirebaseUser
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        auth = mockk(relaxed = true)
        mockUser = mockk(relaxed = true)

        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns "test_user_id"
    }

    @Test
    fun `fetchImages returns empty list emits WardrobeUiState Empty`() = runTest {
        coEvery { repository.fetchItems("test_user_id") } returns emptyList()

        viewModel = HomeViewModel(repository, auth)
        viewModel.fetchImages()
        advanceUntilIdle()

        assertEquals(WardrobeUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `fetchImages returns items emits WardrobeUiState Success`() = runTest {
        val items = listOf(
            ClothingItem(id = "1", imageUrl = "url1", category = "Top"),
            ClothingItem(id = "2", imageUrl = "url2", category = "Bottom")
        )
        coEvery { repository.fetchItems("test_user_id") } returns items

        viewModel = HomeViewModel(repository, auth)
        viewModel.fetchImages()
        advanceUntilIdle()

        val state = viewModel.uiState.value as WardrobeUiState.Success
        assertEquals(2, state.items.size)
    }
}
