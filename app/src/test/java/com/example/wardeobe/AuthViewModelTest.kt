package com.example.wardeobe

import com.example.wardeobe.data.ProfileRepository
import com.example.wardeobe.viewmodel.AuthViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var profileRepository: ProfileRepository
    private lateinit var viewModel: AuthViewModel

    private lateinit var mockUser: FirebaseUser
    private lateinit var mockCollection: CollectionReference
    private lateinit var mockDocument: DocumentReference

    @Before
    fun setup() {
        auth = mockk(relaxed = true)
        db = mockk(relaxed = true)
        profileRepository = mockk(relaxed = true)
        mockUser = mockk(relaxed = true)
        mockCollection = mockk(relaxed = true)
        mockDocument = mockk(relaxed = true)

        every { db.collection("users") } returns mockCollection
        every { mockCollection.document(any()) } returns mockDocument
        every { auth.currentUser } returns mockUser
        every { mockUser.uid } returns "test_user_id"

        viewModel = AuthViewModel(auth, profileRepository, db)
    }

    @Test
    fun `loginUser success emits AuthResult Success`() = runTest {
        val mockTask = mockk<Task<AuthResult>>(relaxed = true)
        every { mockTask.isSuccessful } returns true
        every { mockTask.addOnCompleteListener(any()) } answers { 
            arg<com.google.android.gms.tasks.OnCompleteListener<AuthResult>>(0).onComplete(mockTask)
            mockTask 
        }
        every { auth.signInWithEmailAndPassword("test@test.com", "password") } returns mockTask

        viewModel.loginUser("test@test.com", "password")
        advanceUntilIdle()

        assertEquals(AuthViewModel.AuthResult.Success, viewModel.authResult.value)
    }

    @Test
    fun `loginUser failure emits AuthResult Error`() = runTest {
        val mockTask = mockk<Task<AuthResult>>(relaxed = true)
        every { mockTask.isSuccessful } returns false
        every { mockTask.exception } returns Exception("Invalid credentials")
        every { mockTask.addOnCompleteListener(any()) } answers { 
            arg<com.google.android.gms.tasks.OnCompleteListener<AuthResult>>(0).onComplete(mockTask)
            mockTask 
        }
        every { auth.signInWithEmailAndPassword("test@test.com", "wrong") } returns mockTask

        viewModel.loginUser("test@test.com", "wrong")
        advanceUntilIdle()

        val error = viewModel.authResult.value as AuthViewModel.AuthResult.Error
        assertEquals("Login failed: Invalid credentials", error.message)
    }

    @Test
    fun `registerUser success with successful profile write emits Success`() = runTest {
        val mockAuthTask = mockk<Task<AuthResult>>(relaxed = true)
        every { mockAuthTask.isSuccessful } returns true
        every { mockAuthTask.addOnCompleteListener(any()) } answers { 
            arg<com.google.android.gms.tasks.OnCompleteListener<AuthResult>>(0).onComplete(mockAuthTask)
            mockAuthTask 
        }

        val mockDbTask = mockk<Task<Void>>(relaxed = true)
        every { mockDbTask.isSuccessful } returns true
        every { mockDbTask.addOnSuccessListener(any()) } answers { 
            arg<com.google.android.gms.tasks.OnSuccessListener<Void>>(0).onSuccess(null)
            mockDbTask 
        }

        every { auth.createUserWithEmailAndPassword("test@test.com", "password") } returns mockAuthTask
        every { mockDocument.set(any()) } returns mockDbTask

        viewModel.registerUser("test@test.com", "password")
        advanceUntilIdle()

        assertEquals(AuthViewModel.AuthResult.Success, viewModel.authResult.value)
    }

    @Test
    fun `registerUser partial failure triggers rollback and emits Error`() = runTest {
        val mockAuthTask = mockk<Task<AuthResult>>(relaxed = true)
        every { mockAuthTask.isSuccessful } returns true
        every { mockAuthTask.addOnCompleteListener(any()) } answers { 
            arg<com.google.android.gms.tasks.OnCompleteListener<AuthResult>>(0).onComplete(mockAuthTask)
            mockAuthTask 
        }

        val mockDbTask = mockk<Task<Void>>(relaxed = true)
        every { mockDbTask.isSuccessful } returns false
        every { mockDbTask.addOnSuccessListener(any()) } answers { mockDbTask }
        every { mockDbTask.addOnFailureListener(any()) } answers { 
            arg<com.google.android.gms.tasks.OnFailureListener>(0).onFailure(Exception("Network error"))
            mockDbTask 
        }

        val mockDeleteTask = mockk<Task<Void>>(relaxed = true)
        every { mockDeleteTask.isSuccessful } returns true
        every { mockDeleteTask.addOnCompleteListener(any()) } answers { 
            arg<com.google.android.gms.tasks.OnCompleteListener<Void>>(0).onComplete(mockDeleteTask)
            mockDeleteTask 
        }

        every { auth.createUserWithEmailAndPassword("test@test.com", "password") } returns mockAuthTask
        every { mockDocument.set(any()) } returns mockDbTask
        every { mockUser.delete() } returns mockDeleteTask

        viewModel.registerUser("test@test.com", "password")
        advanceUntilIdle()

        val error = viewModel.authResult.value as AuthViewModel.AuthResult.Error
        assertEquals("Registration failed: Could not setup profile. Please try again.", error.message)
        
        // Verify rollback was called
        verify { mockUser.delete() }
    }
}
