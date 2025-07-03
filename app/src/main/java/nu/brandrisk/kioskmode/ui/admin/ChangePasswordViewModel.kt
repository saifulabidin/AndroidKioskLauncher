package nu.brandrisk.kioskmode.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nu.brandrisk.kioskmode.domain.security.AdminPasswordManager
import nu.brandrisk.kioskmode.utils.Routes
import nu.brandrisk.kioskmode.utils.UiEvent
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val passwordManager: AdminPasswordManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()
    
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()
    
    init {
        viewModelScope.launch {
            passwordManager.initializeAdminPassword()
            updateUiState()
        }
    }
    
    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            try {
                // Clear previous errors
                _uiState.value = _uiState.value.copy(errorMessage = null)
                
                // Validate inputs
                if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                    _uiState.value = _uiState.value.copy(errorMessage = "All fields are required")
                    return@launch
                }
                
                if (newPassword != confirmPassword) {
                    _uiState.value = _uiState.value.copy(errorMessage = "New passwords do not match")
                    return@launch
                }
                
                if (!isPasswordValid(newPassword)) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Password does not meet requirements")
                    return@launch
                }
                
                // Verify current password
                if (!passwordManager.verifyPassword(currentPassword)) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Current password is incorrect")
                    return@launch
                }
                
                // Change password
                val success = passwordManager.changePassword(currentPassword, newPassword)
                
                if (success) {
                    _uiEvent.send(UiEvent.ShowMessage("Password changed successfully!"))
                    updateUiState()
                    // Navigate back after a short delay
                    kotlinx.coroutines.delay(1000)
                    _uiEvent.send(UiEvent.Navigate(Routes.CONFIG))
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = "Failed to change password")
                }
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error: ${e.message}")
            }
        }
    }
    
    fun resetToDefaultPassword() {
        viewModelScope.launch {
            try {
                val success = passwordManager.resetToDefault()
                if (success) {
                    _uiEvent.send(UiEvent.ShowMessage("Password reset to default"))
                    updateUiState()
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = "Failed to reset password")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error: ${e.message}")
            }
        }
    }
    
    private fun updateUiState() {
        _uiState.value = _uiState.value.copy(
            isUsingDefaultPassword = passwordManager.isUsingDefaultPassword()
        )
    }
    
    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() }
    }
}

data class ChangePasswordUiState(
    val isUsingDefaultPassword: Boolean = true,
    val errorMessage: String? = null
)
