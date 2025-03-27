package com.eti.energysaver.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eti.energysaver.repository.AuthRepository
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun updateProfile(displayName: String, avatarUrl: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val user = authRepository.getCurrentUser()
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setPhotoUri(Uri.parse(avatarUrl))
                    .build()
                user?.updateProfile(profileUpdates)?.await()
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun changePassword(newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.getCurrentUser()?.updatePassword(newPassword)?.await()
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
