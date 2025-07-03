package nu.brandrisk.kioskmode.ui.changepassword

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
class ChangePasswordViewModel @Inject constructor(
    private val passwordManager: AdminPasswordManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()
    
    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String, callback: () -> Unit) {
        viewModelScope.launch {
            try {
                // Validate current password
                if (!passwordManager.verifyPassword(currentPassword)) {
                    _uiState.value = _uiState.value.copy(
                        error = "Current password is incorrect",
                        isSuccess = false
                    )
                    callback()
                    return@launch
                }
                
                // Validate new password
                if (newPassword.length < 4) {
                    _uiState.value = _uiState.value.copy(
                        error = "New password must be at least 4 characters",
                        isSuccess = false
                    )
                    callback()
                    return@launch
                }
                
                // Validate password confirmation
                if (newPassword != confirmPassword) {
                    _uiState.value = _uiState.value.copy(
                        error = "New password and confirmation do not match",
                        isSuccess = false
                    )
                    callback()
                    return@launch
                }
                
                // Change password
                val success = passwordManager.changePassword(currentPassword, newPassword)
                
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        message = "Password changed successfully!",
                        error = null,
                        isSuccess = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to change password",
                        isSuccess = false
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error: ${e.message}",
                    isSuccess = false
                )
            }
            
            callback()
        }
    }
    
    fun resetToDefault(callback: () -> Unit) {
        viewModelScope.launch {
            try {
                val success = passwordManager.resetToDefault()
                
                if (success) {
                    _uiState.value = _uiState.value.copy(
                        message = "Password reset to default (1234) successfully!",
                        error = null,
                        isSuccess = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to reset password",
                        isSuccess = false
                    )
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error: ${e.message}",
                    isSuccess = false
                )
            }
            
            callback()
        }
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(
            message = null,
            error = null
        )
    }
}

data class ChangePasswordUiState(
    val message: String? = null,
    val error: String? = null,
    val isSuccess: Boolean = false
)
