package nu.brandrisk.kioskmode.ui.bootconfig

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nu.brandrisk.kioskmode.domain.enterprise.AutoStartStatus
import nu.brandrisk.kioskmode.domain.enterprise.EnterpriseBootManager
import javax.inject.Inject

/**
 * ViewModel for Enterprise Boot Configuration
 * Manages advanced auto-start settings
 */
@HiltViewModel
class BootConfigurationViewModel @Inject constructor(
    private val bootManager: EnterpriseBootManager
) : ViewModel() {
    
    private val _bootStatus = MutableStateFlow(
        AutoStartStatus(
            isEnabled = false,
            startupMode = EnterpriseBootManager.Companion.StartupMode.NORMAL,
            bootDelay = 3000L,
            isPersistent = false,
            isDeviceOwner = false,
            bootReceiverEnabled = false
        )
    )
    val bootStatus: StateFlow<AutoStartStatus> = _bootStatus.asStateFlow()
    
    init {
        loadBootStatus()
    }
    
    private fun loadBootStatus() {
        viewModelScope.launch {
            try {
                val status = bootManager.getAutoStartStatus()
                _bootStatus.value = status
            } catch (e: Exception) {
                android.util.Log.e("BootConfigViewModel", "Failed to load boot status", e)
            }
        }
    }
    
    fun enableAutoStart(
        startupMode: EnterpriseBootManager.Companion.StartupMode,
        bootDelayMs: Long,
        persistentMode: Boolean
    ) {
        viewModelScope.launch {
            try {
                val success = bootManager.enableAutoStart(
                    startupMode = startupMode,
                    bootDelayMs = bootDelayMs,
                    persistentMode = persistentMode
                )
                
                if (success) {
                    loadBootStatus() // Refresh status
                }
            } catch (e: Exception) {
                android.util.Log.e("BootConfigViewModel", "Failed to enable auto-start", e)
            }
        }
    }
    
    fun disableAutoStart() {
        viewModelScope.launch {
            try {
                val success = bootManager.disableAutoStart()
                if (success) {
                    loadBootStatus() // Refresh status
                }
            } catch (e: Exception) {
                android.util.Log.e("BootConfigViewModel", "Failed to disable auto-start", e)
            }
        }
    }
    
    fun setAsDefaultLauncher() {
        viewModelScope.launch {
            try {
                bootManager.setAsDefaultLauncher()
                loadBootStatus() // Refresh status
            } catch (e: Exception) {
                android.util.Log.e("BootConfigViewModel", "Failed to set default launcher", e)
            }
        }
    }
    
    fun refreshStatus() {
        loadBootStatus()
    }
}
