// src/test/java/com/eti/energysaver/viewmodel/AuthViewModelTest.kt
package com.eti.energysaver.viewmodel

import com.eti.energysaver.repository.AuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

// Фейковий репозиторій для аутентифікації
class FakeAuthRepository : AuthRepository() {
    override suspend fun login(email: String, password: String): Result<Unit> {
        return if (email == "test@example.com" && password == "password123") {
            Result.success(Unit)
        } else {
            Result.failure(Exception("Invalid credentials"))
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private lateinit var authViewModel: TestAuthViewModel

    @Before
    fun setup() {
        authViewModel = TestAuthViewModel(FakeAuthRepository())
    }

    @Test
    fun testLoginWithValidCredentials() = runTest {
        val result = authViewModel.login("test@example.com", "password123")
        assertTrue(result)
    }

    private fun assertTrue(result: Unit) {
        TODO("Not yet implemented")
    }
}

class TestAuthViewModel(private val fakeAuthRepository: FakeAuthRepository) : AuthViewModel() {
    fun login(s: String, s1: String) {
    }

    val authRepository: AuthRepository
        get() = fakeAuthRepository
}
