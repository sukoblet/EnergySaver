package com.eti.energysaver.repository

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

open class AuthRepository {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    open suspend fun login(email: String, password: String) = try {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure<Unit>(e)
    }

    suspend fun register(email: String, password: String) = try {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure<Unit>(e)
    }

    suspend fun sendPasswordResetEmail(email: String) = try {
        firebaseAuth.sendPasswordResetEmail(email).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure<Unit>(e)
    }

    fun getCurrentUser() = firebaseAuth.currentUser

    fun signOut() {
        firebaseAuth.signOut()
    }
}
