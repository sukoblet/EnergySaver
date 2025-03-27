package com.eti.energysaver.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eti.energysaver.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.login(email, password).let { result ->
                if (result.isSuccess) onSuccess() else _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun register(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.register(email, password).let { result ->
                if (result.isSuccess) onSuccess() else _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            authRepository.sendPasswordResetEmail(email).let { result ->
                if (result.isSuccess) onSuccess() else _errorMessage.value = result.exceptionOrNull()?.message
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
