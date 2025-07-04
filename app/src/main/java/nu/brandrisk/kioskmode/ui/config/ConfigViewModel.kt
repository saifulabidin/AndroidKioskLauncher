package nu.brandrisk.kioskmode.ui.config

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nu.brandrisk.kioskmode.data.model.App
import nu.brandrisk.kioskmode.domain.AppRepository
import nu.brandrisk.kioskmode.domain.ToggleKioskMode
import nu.brandrisk.kioskmode.utils.Routes
import nu.brandrisk.kioskmode.utils.UiEvent
import javax.inject.Inject


@HiltViewModel
open class ConfigViewModel @Inject constructor(
    internal val toggleKioskMode: ToggleKioskMode,
    private val repository: AppRepository,
    @ApplicationContext private val context: Context,
    val imageLoader: ImageLoader
) : ViewModel() {

    val apps = repository.getApps()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun enableKioskMode(context: Context) {
        val result = toggleKioskMode.enableKioskMode(context)
        if (result == ToggleKioskMode.Result.NotDeviceOwner) {
            showADBSetupScreen()
        }
    }

    fun disableKioskMode(context: Context) {
        val result = toggleKioskMode.disableKioskMode(context)
        if (result == ToggleKioskMode.Result.NotDeviceOwner) {
            showADBSetupScreen()
        }
    }

    fun showADBSetupScreen() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.Navigate(Routes.ADB_SETUP))
        }
    }

    fun isDeviceOwner(context: Context): Boolean {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return devicePolicyManager.isDeviceOwnerApp(context.packageName)
    }

    fun updateEnabledFlag(app: App) {
        if (app.packageName != context.packageName) {
            viewModelScope.launch {
                repository.upsertApp(app.copy(isEnabled = !app.isEnabled))
            }
        }
    }

    fun disableAllApps() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.disableAllApps()
        }
    }

    fun enableAllApps() {
        viewModelScope.launch((Dispatchers.IO)) {
            repository.enableAllApps()
        }
    }

    // Security Settings Methods
    fun showPasswordManager() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.Navigate(Routes.CHANGE_PASSWORD))
        }
    }

    fun showBiometricSettings() {
        // TODO: Implement biometric settings
    }

    fun showSessionTimeoutSettings() {
        // TODO: Implement session timeout settings
    }

    fun toggleScreenRecordingBlock() {
        // TODO: Implement screen recording block
    }

    // Xiaomi Settings Methods
    fun showSecondSpaceSettings() {
        // TODO: Implement second space settings
    }

    fun showAppLockBypassSettings() {
        // TODO: Implement app lock bypass settings
    }

    fun showMIUISecurityCenter() {
        // TODO: Implement MIUI security center
    }

    // Network Settings Methods
    fun showWiFiSettings() {
        // TODO: Implement WiFi settings
    }

    fun showMobileDataSettings() {
        // TODO: Implement mobile data settings
    }

    fun showBluetoothSettings() {
        // TODO: Implement Bluetooth settings
    }

    fun showNFCSettings() {
        // TODO: Implement NFC settings
    }

    fun showVPNSettings() {
        // TODO: Implement VPN settings
    }

    // Hardware Settings Methods
    fun showCameraSettings() {
        // TODO: Implement camera settings
    }

    fun showMicrophoneSettings() {
        // TODO: Implement microphone settings
    }

    fun showDisplaySettings() {
        // TODO: Implement display settings
    }

    fun showVolumeSettings() {
        // TODO: Implement volume settings
    }

    fun showFlashlightSettings() {
        // TODO: Implement flashlight settings
    }

    // Optimization Methods
    fun showBatteryOptimizationSettings() {
        // TODO: Implement battery optimization settings
    }

    fun showAutostartSettings() {
        // TODO: Implement autostart settings
    }

    fun showGameTurboSettings() {
        // TODO: Implement game turbo settings
    }

    // Boot Configuration Methods
    fun showBootConfigurationScreen() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.Navigate(Routes.BOOT_CONFIGURATION))
        }
    }

    fun setAsDefaultLauncher() {
        viewModelScope.launch {
            try {
                if (isDeviceOwner(context)) {
                    val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                    val adminComponent = ComponentName(context, nu.brandrisk.kioskmode.KioskDeviceAdminReceiver::class.java)
                    val launcherComponent = ComponentName(context, nu.brandrisk.kioskmode.MainActivity::class.java)
                    
                    // Clear current defaults
                    devicePolicyManager.clearPackagePersistentPreferredActivities(
                        adminComponent, 
                        context.packageName
                    )
                    
                    // Set launcher intent filter
                    val intentFilter = IntentFilter().apply {
                        addAction(Intent.ACTION_MAIN)
                        addCategory(Intent.CATEGORY_HOME)
                        addCategory(Intent.CATEGORY_DEFAULT)
                    }
                    
                    // Set as persistent preferred activity
                    devicePolicyManager.addPersistentPreferredActivity(
                        adminComponent,
                        intentFilter,
                        launcherComponent
                    )
                    
                    _uiEvent.send(UiEvent.ShowMessage("Successfully set as default launcher"))
                } else {
                    // Fallback for non-device owner
                    showLauncherSelectionDialog()
                }
            } catch (e: Exception) {
                _uiEvent.send(UiEvent.ShowMessage("Failed to set as default launcher: ${e.message}"))
            }
        }
    }
    
    private fun showLauncherSelectionDialog() {
        viewModelScope.launch {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val chooser = Intent.createChooser(intent, "Select default launcher")
            _uiEvent.send(UiEvent.StartActivity(chooser))
        }
    }

    fun showBootAnimationSettings() {
        // TODO: Implement boot animation settings
    }

    // UI Control Methods
    fun toggleStatusBarVisibility() {
        // TODO: Implement status bar visibility toggle
    }

    fun toggleNavigationBarVisibility() {
        // TODO: Implement navigation bar visibility toggle
    }

    fun toggleImmersiveMode() {
        // TODO: Implement immersive mode toggle
    }

    fun showOrientationSettings() {
        // TODO: Implement orientation settings
    }

    // System Methods
    fun showCPUGovernorSettings() {
        // TODO: Implement CPU governor settings
    }

    fun showRAMManagementSettings() {
        // TODO: Implement RAM management settings
    }

    fun showThermalSettings() {
        // TODO: Implement thermal settings
    }

    fun removeDeviceOwner() {
        viewModelScope.launch {
            try {
                val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                val adminComponent = ComponentName(context, nu.brandrisk.kioskmode.KioskDeviceAdminReceiver::class.java)
                
                if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                    // First disable auto-start to prevent re-enabling
                    disableAutoStartBootManager()
                    
                    // Stop any active kiosk mode
                    try {
                        // Clear lock task packages
                        devicePolicyManager.setLockTaskPackages(adminComponent, emptyArray())
                        
                        // Clear persistent preferred activities
                        devicePolicyManager.clearPackagePersistentPreferredActivities(
                            adminComponent, 
                            context.packageName
                        )
                        
                        // Remove user restrictions
                        devicePolicyManager.clearUserRestriction(adminComponent, android.os.UserManager.DISALLOW_SAFE_BOOT)
                        devicePolicyManager.clearUserRestriction(adminComponent, android.os.UserManager.DISALLOW_MODIFY_ACCOUNTS)
                        devicePolicyManager.clearUserRestriction(adminComponent, android.os.UserManager.DISALLOW_FACTORY_RESET)
                        
                    } catch (e: Exception) {
                        android.util.Log.e("ConfigViewModel", "Error clearing device policies", e)
                    }
                    
                    // Clear device owner
                    try {
                        devicePolicyManager.clearDeviceOwnerApp(context.packageName)
                        android.util.Log.i("ConfigViewModel", "Device owner removed successfully")
                    } catch (e: Exception) {
                        android.util.Log.e("ConfigViewModel", "Failed to remove device owner", e)
                    }
                    
                } else {
                    android.util.Log.w("ConfigViewModel", "App is not device owner")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("ConfigViewModel", "Error removing device owner", e)
            }
        }
    }
    
    /**
     * Disable auto-start in boot manager to prevent re-enabling
     */
    private suspend fun disableAutoStartBootManager() {
        try {
            // Access EnterpriseBootManager via Hilt if available
            // For now, disable via SharedPreferences directly
            val sharedPreferences = context.getSharedPreferences("enterprise_boot_prefs", Context.MODE_PRIVATE)
            sharedPreferences.edit()
                .putBoolean("auto_start_enabled", false)
                .putBoolean("kiosk_mode_enabled", false)
                .apply()
                
            android.util.Log.i("ConfigViewModel", "Auto-start disabled via SharedPreferences")
        } catch (e: Exception) {
            android.util.Log.e("ConfigViewModel", "Failed to disable auto-start", e)
        }
    }
}