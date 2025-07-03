package nu.brandrisk.kioskmode.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nu.brandrisk.kioskmode.domain.security.AdminPasswordManager
import javax.inject.Inject

@HiltViewModel
class AdminPasswordViewModel @Inject constructor(
    private val passwordManager: AdminPasswordManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AdminPasswordUiState())
    val uiState: StateFlow<AdminPasswordUiState> = _uiState.asStateFlow()
    
    init {
        viewModelScope.launch {
            // Initialize password system
            passwordManager.initializeAdminPassword()
            updateUiState()
        }
    }
    
    fun verifyPassword(password: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            val isValid = passwordManager.verifyPassword(password)
            
            if (isValid) {
                _uiState.value = _uiState.value.copy(
                    isAuthenticated = true,
                    errorMessage = null
                )
                callback(true)
            } else {
                updateUiState()
                _uiState.value = _uiState.value.copy(
                    errorMessage = if (passwordManager.isLockedOut()) {
                        "Account locked due to too many failed attempts"
                    } else {
                        "Incorrect password. ${passwordManager.getMaxAttempts() - passwordManager.getFailedAttemptCount()} attempts remaining."
                    }
                )
                callback(false)
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    private fun updateUiState() {
        _uiState.value = _uiState.value.copy(
            isUsingDefaultPassword = passwordManager.isUsingDefaultPassword(),
            isLockedOut = passwordManager.isLockedOut(),
            failedAttempts = passwordManager.getFailedAttemptCount(),
            maxAttempts = passwordManager.getMaxAttempts(),
            lockoutTimeRemaining = passwordManager.getRemainingLockoutTime()
        )
    }
}

data class AdminPasswordUiState(
    val isAuthenticated: Boolean = false,
    val isUsingDefaultPassword: Boolean = true,
    val isLockedOut: Boolean = false,
    val failedAttempts: Int = 0,
    val maxAttempts: Int = 5,
    val lockoutTimeRemaining: Long = 0,
    val errorMessage: String? = null
)
