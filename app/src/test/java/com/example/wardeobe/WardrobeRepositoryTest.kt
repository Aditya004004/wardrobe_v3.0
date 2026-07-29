package com.example.wardeobe

import com.example.wardeobe.data.WardrobeRepository
import com.example.wardeobe.model.ClothingItem
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.HttpsCallableReference
import com.google.firebase.functions.HttpsCallableResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WardrobeRepositoryTest {

    private lateinit var functions: FirebaseFunctions
    private lateinit var firestore: FirebaseFirestore
    private lateinit var repository: WardrobeRepository

    private lateinit var mockCallable: HttpsCallableReference
    private lateinit var mockResult: HttpsCallableResult

    @Before
    fun setup() {
        functions = mockk(relaxed = true)
        firestore = mockk(relaxed = true)
        mockCallable = mockk(relaxed = true)
        mockResult = mockk(relaxed = true)

        repository = WardrobeRepository(functions, firestore)
    }

    @Test
    fun `fetchItems returns list of ClothingItem`() = runTest {
        val rawData = listOf(
            mapOf(
                "id" to "item_1",
                "imageUrl" to "https://example.com/1.jpg",
                "category" to "Top",
                "createdAt" to 123456789L,
                "publicId" to "public_1"
            )
        )

        every { functions.getHttpsCallable("fetchWardrobeItems") } returns mockCallable
        every { mockCallable.call(any()) } returns Tasks.forResult(mockResult)
        every { mockResult.data } returns rawData

        val items = repository.fetchItems("test_user_id")

        assertEquals(1, items.size)
        assertEquals("item_1", items[0].id)
        assertEquals("Top", items[0].category)
    }
}
