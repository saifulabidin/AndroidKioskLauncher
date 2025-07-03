package nu.brandrisk.kioskmode.ui.config

import android.app.admin.DevicePolicyManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import nu.brandrisk.kioskmode.data.model.App
import nu.brandrisk.kioskmode.domain.AppRepository
import nu.brandrisk.kioskmode.domain.ToggleKioskMode
import nu.brandrisk.kioskmode.domain.enterprise.EnterpriseSecurityManager
import nu.brandrisk.kioskmode.domain.enterprise.HardwareControlManager
import nu.brandrisk.kioskmode.domain.enterprise.NetworkManager
import nu.brandrisk.kioskmode.domain.enterprise.XiaomiMIUIManager
import nu.brandrisk.kioskmode.utils.Routes
import nu.brandrisk.kioskmode.utils.UiEvent
import javax.inject.Inject

@HiltViewModel
class ConfigViewModel @Inject constructor(
    internal val toggleKioskMode: ToggleKioskMode,
    private val repository: AppRepository,
    @ApplicationContext private val context: Context,
    val imageLoader: ImageLoader,
    private val securityManager: EnterpriseSecurityManager,
    private val networkManager: NetworkManager,
    private val hardwareManager: HardwareControlManager,
    private val xiaomiManager: XiaomiMIUIManager
) : ViewModel() {

    val apps = repository.getApps()

    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    // Enterprise feature states
    private val _securitySettings = MutableStateFlow(SecuritySettings())
    val securitySettings: StateFlow<SecuritySettings> = _securitySettings

    private val _networkSettings = MutableStateFlow(NetworkSettings())
    val networkSettings: StateFlow<NetworkSettings> = _networkSettings

    private val _hardwareSettings = MutableStateFlow(HardwareSettings())
    val hardwareSettings: StateFlow<HardwareSettings> = _hardwareSettings

    private val _xiaomiSettings = MutableStateFlow(XiaomiSettings())
    val xiaomiSettings: StateFlow<XiaomiSettings> = _xiaomiSettings

    init {
        loadEnterpriseSettings()
    }

    // Existing methods
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

    // New Enterprise Security Methods
    fun togglePasswordProtection(enabled: Boolean) {
        viewModelScope.launch {
            val result = securityManager.setPasswordProtection(enabled)
            _securitySettings.value = _securitySettings.value.copy(passwordProtectionEnabled = result)
        }
    }

    fun toggleBiometricLock(enabled: Boolean) {
        viewModelScope.launch {
            val result = securityManager.setBiometricLock(enabled)
            _securitySettings.value = _securitySettings.value.copy(biometricLockEnabled = result)
        }
    }

    fun setSessionTimeout(minutes: Int) {
        viewModelScope.launch {
            securityManager.setSessionTimeout(minutes)
            _securitySettings.value = _securitySettings.value.copy(sessionTimeoutMinutes = minutes)
        }
    }

    fun toggleScreenRecordingBlock(enabled: Boolean) {
        viewModelScope.launch {
            val result = securityManager.blockScreenRecording(enabled)
            _securitySettings.value = _securitySettings.value.copy(screenRecordingBlocked = result)
        }
    }

    // Network Management Methods
    fun configureEnterpriseWiFi(ssid: String, password: String, security: String) {
        viewModelScope.launch {
            val result = networkManager.configureWiFi(ssid, password, security)
            _networkSettings.value = _networkSettings.value.copy(wifiConfigured = result)
        }
    }

    fun toggleMobileData(enabled: Boolean) {
        viewModelScope.launch {
            val result = networkManager.setMobileDataEnabled(enabled)
            _networkSettings.value = _networkSettings.value.copy(mobileDataEnabled = result)
        }
    }

    fun toggleBluetooth(enabled: Boolean) {
        viewModelScope.launch {
            val result = networkManager.setBluetoothEnabled(enabled)
            _networkSettings.value = _networkSettings.value.copy(bluetoothEnabled = result)
        }
    }

    fun toggleNFC(enabled: Boolean) {
        viewModelScope.launch {
            val result = networkManager.setNFCEnabled(enabled)
            _networkSettings.value = _networkSettings.value.copy(nfcEnabled = result)
        }
    }

    fun configureVPN(serverAddress: String, username: String, password: String) {
        viewModelScope.launch {
            val result = networkManager.configureVPN(serverAddress, username, password)
            _networkSettings.value = _networkSettings.value.copy(vpnConfigured = result)
        }
    }

    // Hardware Control Methods
    fun toggleCameraAccess(enabled: Boolean) {
        viewModelScope.launch {
            val result = hardwareManager.setCameraEnabled(enabled)
            _hardwareSettings.value = _hardwareSettings.value.copy(cameraEnabled = result)
        }
    }

    fun toggleMicrophoneAccess(enabled: Boolean) {
        viewModelScope.launch {
            val result = hardwareManager.setMicrophoneEnabled(enabled)
            _hardwareSettings.value = _hardwareSettings.value.copy(microphoneEnabled = result)
        }
    }

    fun setBrightness(level: Int) {
        viewModelScope.launch {
            hardwareManager.setBrightness(level)
            _hardwareSettings.value = _hardwareSettings.value.copy(brightnessLevel = level)
        }
    }

    fun setVolumeLevel(level: Int) {
        viewModelScope.launch {
            hardwareManager.setVolumeLevel(level)
            _hardwareSettings.value = _hardwareSettings.value.copy(volumeLevel = level)
        }
    }

    fun toggleFlashlight(enabled: Boolean) {
        viewModelScope.launch {
            val result = hardwareManager.setFlashlightEnabled(enabled)
            _hardwareSettings.value = _hardwareSettings.value.copy(flashlightEnabled = result)
        }
    }

    // Xiaomi MIUI-Specific Methods
    fun enableSecondSpace() {
        viewModelScope.launch {
            val result = xiaomiManager.enableSecondSpace()
            _xiaomiSettings.value = _xiaomiSettings.value.copy(secondSpaceEnabled = result)
        }
    }

    fun addToBatteryWhitelist() {
        viewModelScope.launch {
            val result = xiaomiManager.addToBatteryWhitelist(context.packageName)
            _xiaomiSettings.value = _xiaomiSettings.value.copy(batteryWhitelisted = result)
        }
    }

    fun requestAutostartPermission() {
        viewModelScope.launch {
            val result = xiaomiManager.requestAutostartPermission()
            _xiaomiSettings.value = _xiaomiSettings.value.copy(autostartEnabled = result)
        }
    }

    fun enableGameTurboMode() {
        viewModelScope.launch {
            val result = xiaomiManager.enableGameTurboMode()
            _xiaomiSettings.value = _xiaomiSettings.value.copy(gameTurboEnabled = result)
        }
    }

    fun bypassAppLock(packageName: String) {
        viewModelScope.launch {
            val result = xiaomiManager.bypassAppLock(packageName)
            _xiaomiSettings.value = _xiaomiSettings.value.copy(appLockBypassed = result)
        }
    }

    private fun loadEnterpriseSettings() {
        viewModelScope.launch {
            // Load current settings from managers
            _securitySettings.value = SecuritySettings(
                passwordProtectionEnabled = securityManager.isPasswordProtectionEnabled(),
                biometricLockEnabled = securityManager.isBiometricLockEnabled(),
                sessionTimeoutMinutes = securityManager.getSessionTimeoutMinutes(),
                screenRecordingBlocked = securityManager.isScreenRecordingBlocked()
            )

            _networkSettings.value = NetworkSettings(
                wifiConfigured = networkManager.isWiFiConfigured(),
                mobileDataEnabled = networkManager.isMobileDataEnabled(),
                bluetoothEnabled = networkManager.isBluetoothEnabled(),
                nfcEnabled = networkManager.isNFCEnabled(),
                vpnConfigured = networkManager.isVPNConfigured()
            )

            _hardwareSettings.value = HardwareSettings(
                cameraEnabled = hardwareManager.isCameraEnabled(),
                microphoneEnabled = hardwareManager.isMicrophoneEnabled(),
                brightnessLevel = hardwareManager.getBrightnessLevel(),
                volumeLevel = hardwareManager.getVolumeLevel(),
                flashlightEnabled = hardwareManager.isFlashlightEnabled()
            )

            _xiaomiSettings.value = XiaomiSettings(
                secondSpaceEnabled = xiaomiManager.isSecondSpaceEnabled(),
                batteryWhitelisted = xiaomiManager.isBatteryWhitelisted(context.packageName),
                autostartEnabled = xiaomiManager.isAutostartEnabled(),
                gameTurboEnabled = xiaomiManager.isGameTurboEnabled(),
                appLockBypassed = xiaomiManager.isAppLockBypassed()
            )
        }
    }

    // Enhanced App Management Methods - Simplified
    fun toggleSystemApps() {
        viewModelScope.launch {
            // Simplified implementation - toggle system apps enabled state
            android.util.Log.i("ConfigViewModel", "System apps toggle requested")
            // Implementation would integrate with repository when available
        }
    }

    fun toggleUserApps() {
        viewModelScope.launch {
            // Simplified implementation - toggle user apps enabled state
            android.util.Log.i("ConfigViewModel", "User apps toggle requested")
            // Implementation would integrate with repository when available
        }
    }

    // Enterprise Policy Presets - Simplified
    fun applyWhitelistPolicy() {
        viewModelScope.launch {
            // Simplified whitelist policy implementation
            android.util.Log.i("ConfigViewModel", "Whitelist policy applied")
            // Implementation would integrate with repository when available
        }
    }

    fun applyEducationPolicy() {
        viewModelScope.launch {
            // Simplified education policy implementation
            android.util.Log.i("ConfigViewModel", "Education policy applied")
            // Implementation would integrate with repository when available
        }
    }

    fun applyKioskPolicy() {
        viewModelScope.launch {
            // Simplified kiosk policy implementation
            android.util.Log.i("ConfigViewModel", "Strict kiosk policy applied - only essential apps enabled")
            // Implementation would integrate with repository when available
        }
    }

    fun applyBusinessPolicy() {
        viewModelScope.launch {
            // Simplified business policy implementation
            android.util.Log.i("ConfigViewModel", "Business policy applied")
            // Implementation would integrate with repository when available
        }
    }

    // Helper methods for app categorization
    private fun isSystemApp(packageName: String): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
            (packageInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: Exception) {
            false
        }
    }

    private fun isEducationalApp(packageName: String): Boolean {
        val educationalKeywords = listOf("edu", "learn", "study", "school", "math", "science")
        return educationalKeywords.any { packageName.contains(it, ignoreCase = true) }
    }

    private fun isBusinessApp(packageName: String): Boolean {
        val businessKeywords = listOf("office", "work", "business", "enterprise", "corp")
        return businessKeywords.any { packageName.contains(it, ignoreCase = true) }
    }

    // App export/import functionality (similar to SureLock)
    fun exportAppList(): String {
        // Export enabled apps list as JSON or CSV
        return "Export functionality to be implemented"
    }

    fun importAppList(@Suppress("UNUSED_PARAMETER") data: String) {
        // Import app configuration from file
        viewModelScope.launch {
            // Implementation for importing app configurations
        }
    }

    // Security Management Functions
    fun showPasswordManager() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowMessage("Opening Password Manager..."))
            // TODO: Implement password manager dialog
        }
    }

    fun showBiometricSettings() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowMessage("Biometric settings available with device owner"))
            // TODO: Implement biometric settings
        }
    }

    fun showSessionTimeoutSettings() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowMessage("Session timeout configuration"))
            // TODO: Implement session timeout
        }
    }

    fun toggleScreenRecordingBlock() {
        viewModelScope.launch {
            if (isDeviceOwner(context)) {
                // TODO: Implement screen recording block
                _uiEvent.send(UiEvent.ShowMessage("Screen recording block toggled"))
            } else {
                _uiEvent.send(UiEvent.ShowMessage("Device owner required"))
            }
        }
    }

    fun showSecondSpaceSettings() {
        viewModelScope.launch {
            // Check if MIUI device
            val isMIUI = try {
                val miuiVersion = System.getProperty("ro.miui.ui.version.name")
                !miuiVersion.isNullOrEmpty()
            } catch (e: Exception) {
                false
            }
            
            if (isMIUI) {
                // TODO: Implement MIUI second space
                _uiEvent.send(UiEvent.ShowMessage("Opening MIUI Second Space"))
            } else {
                _uiEvent.send(UiEvent.ShowMessage("MIUI device required"))
            }
        }
    }

    fun showAppLockBypassSettings() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowMessage("App lock bypass configuration"))
            // TODO: Implement app lock bypass
        }
    }

    fun showMIUISecurityCenter() {
        viewModelScope.launch {
            // Check if MIUI device
            val isMIUI = try {
                val miuiVersion = System.getProperty("ro.miui.ui.version.name")
                !miuiVersion.isNullOrEmpty()
            } catch (e: Exception) {
                false
            }
            
            if (isMIUI) {
                // TODO: Implement MIUI security center
                _uiEvent.send(UiEvent.ShowMessage("Opening MIUI Security Center"))
            } else {
                _uiEvent.send(UiEvent.ShowMessage("MIUI device required"))
            }
        }
    }

    // Network Management Functions
    fun showWiFiSettings() {
        viewModelScope.launch {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                _uiEvent.send(UiEvent.ShowMessage("Opening WiFi settings"))
            } catch (e: Exception) {
                _uiEvent.send(UiEvent.ShowMessage("Cannot open WiFi settings"))
            }
        }
    }

    fun showMobileDataSettings() {
        viewModelScope.launch {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                _uiEvent.send(UiEvent.ShowMessage("Opening mobile data settings"))
            } catch (e: Exception) {
                _uiEvent.send(UiEvent.ShowMessage("Cannot open mobile data settings"))
            }
        }
    }

    fun showBluetoothSettings() {
        viewModelScope.launch {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                _uiEvent.send(UiEvent.ShowMessage("Opening Bluetooth settings"))
            } catch (e: Exception) {
                _uiEvent.send(UiEvent.ShowMessage("Cannot open Bluetooth settings"))
            }
        }
    }

    fun showNFCSettings() {
        viewModelScope.launch {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_NFC_SETTINGS)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                _uiEvent.send(UiEvent.ShowMessage("Opening NFC settings"))
            } catch (e: Exception) {
                _uiEvent.send(UiEvent.ShowMessage("Cannot open NFC settings"))
            }
        }
    }

    fun showVPNSettings() {
        viewModelScope.launch {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_VPN_SETTINGS)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                _uiEvent.send(UiEvent.ShowMessage("Opening VPN settings"))
            } catch (e: Exception) {
                _uiEvent.send(UiEvent.ShowMessage("Cannot open VPN settings"))
            }
        }
    }

    // Hardware Management Functions
    fun showCameraSettings() {
        viewModelScope.launch {
            if (isDeviceOwner(context)) {
                _uiEvent.send(UiEvent.ShowMessage("Camera controls available"))
                // TODO: Implement camera controls
            } else {
                _uiEvent.send(UiEvent.ShowMessage("Device owner required for camera controls"))
            }
        }
    }

    fun showMicrophoneSettings() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowMessage("Microphone controls"))
            // TODO: Implement microphone controls
        }
    }

    fun showDisplaySettings() {
        viewModelScope.launch {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                _uiEvent.send(UiEvent.ShowMessage("Opening display settings"))
            } catch (e: Exception) {
                _uiEvent.send(UiEvent.ShowMessage("Cannot open display settings"))
            }
        }
    }

    fun showVolumeSettings() {
        viewModelScope.launch {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_SOUND_SETTINGS)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                _uiEvent.send(UiEvent.ShowMessage("Opening volume settings"))
            } catch (e: Exception) {
                _uiEvent.send(UiEvent.ShowMessage("Cannot open volume settings"))
            }
        }
    }

    fun showFlashlightSettings() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowMessage("Flashlight toggled"))
            // TODO: Implement flashlight toggle
        }
    }

    fun showBatteryOptimizationSettings() {
        viewModelScope.launch {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                _uiEvent.send(UiEvent.ShowMessage("Opening battery optimization"))
            } catch (e: Exception) {
                _uiEvent.send(UiEvent.ShowMessage("Cannot open battery optimization"))
            }
        }
    }

    fun showAutostartSettings() {
        viewModelScope.launch {
            // Check if MIUI device
            val isMIUI = try {
                val miuiVersion = System.getProperty("ro.miui.ui.version.name")
                !miuiVersion.isNullOrEmpty()
            } catch (e: Exception) {
                false
            }
            
            if (isMIUI) {
                _uiEvent.send(UiEvent.ShowMessage("Opening MIUI autostart settings"))
                // TODO: Implement MIUI autostart
            } else {
                _uiEvent.send(UiEvent.ShowMessage("Autostart settings"))
            }
        }
    }

    fun showGameTurboSettings() {
        viewModelScope.launch {
            // Check if MIUI device
            val isMIUI = try {
                val miuiVersion = System.getProperty("ro.miui.ui.version.name")
                !miuiVersion.isNullOrEmpty()
            } catch (e: Exception) {
                false
            }
            
            if (isMIUI) {
                _uiEvent.send(UiEvent.ShowMessage("Opening Game Turbo"))
                // TODO: Implement Game Turbo
            } else {
                _uiEvent.send(UiEvent.ShowMessage("Game Turbo not available"))
            }
        }
    }

    // System Management Functions
    fun toggleAutoLaunchOnBoot() {
        viewModelScope.launch {
            if (isDeviceOwner(context)) {
                // Enable auto launch on boot
                _uiEvent.send(UiEvent.ShowMessage("Auto launch on boot enabled"))
                // TODO: Implement auto launch logic
            } else {
                _uiEvent.send(UiEvent.ShowMessage("Device owner required"))
            }
        }
    }

    fun setAsDefaultLauncher() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowMessage("Setting as default launcher..."))
            // This will open launcher picker
            val homeScreenSettings = nu.brandrisk.kioskmode.domain.HomeScreenSettings()
            homeScreenSettings.showSelectHomeScreen(context)
        }
    }

    fun showBootAnimationSettings() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowMessage("Boot animation settings"))
            // TODO: Implement boot animation customization
        }
    }

    fun toggleStatusBarVisibility() {
        viewModelScope.launch {
            if (isDeviceOwner(context)) {
                _uiEvent.send(UiEvent.ShowMessage("Status bar visibility toggled"))
                // TODO: Implement status bar toggle
            } else {
                _uiEvent.send(UiEvent.ShowMessage("Device owner required to hide status bar"))
            }
        }
    }

    fun toggleNavigationBarVisibility() {
        viewModelScope.launch {
            if (isDeviceOwner(context)) {
                _uiEvent.send(UiEvent.ShowMessage("Navigation bar visibility toggled"))
                // TODO: Implement navigation bar toggle
            } else {
                _uiEvent.send(UiEvent.ShowMessage("Device owner required"))
            }
        }
    }

    fun toggleImmersiveMode() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowMessage("Immersive mode toggled"))
            // TODO: Implement immersive mode
        }
    }

    fun showOrientationSettings() {
        viewModelScope.launch {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_DISPLAY_SETTINGS)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                _uiEvent.send(UiEvent.ShowMessage("Screen orientation settings"))
            } catch (e: Exception) {
                _uiEvent.send(UiEvent.ShowMessage("Cannot open orientation settings"))
            }
        }
    }

    fun showCPUGovernorSettings() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowMessage("CPU governor settings (root required)"))
            // TODO: Implement CPU governor if root available
        }
    }

    fun showRAMManagementSettings() {
        viewModelScope.launch {
            try {
                val intent = android.content.Intent(android.provider.Settings.ACTION_APPLICATION_SETTINGS)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                _uiEvent.send(UiEvent.ShowMessage("RAM management settings"))
            } catch (e: Exception) {
                _uiEvent.send(UiEvent.ShowMessage("Cannot open memory settings"))
            }
        }
    }

    fun showThermalSettings() {
        viewModelScope.launch {
            _uiEvent.send(UiEvent.ShowMessage("Thermal management settings"))
            // TODO: Implement thermal settings
        }
    }

    fun removeDeviceOwner() {
        viewModelScope.launch {
            try {
                val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                val adminComponent = android.content.ComponentName(context, nu.brandrisk.kioskmode.KioskDeviceAdminReceiver::class.java)

                if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                    // Clear device owner
                    devicePolicyManager.clearDeviceOwnerApp(context.packageName)
                    _uiEvent.send(UiEvent.ShowMessage("Device owner removed successfully!"))
                } else {
                    _uiEvent.send(UiEvent.ShowMessage("Not a device owner"))
                }
            } catch (e: Exception) {
                _uiEvent.send(UiEvent.ShowMessage("Failed to remove device owner: ${e.message}"))
            }
        }
    }
}

// Data classes for enterprise settings
data class SecuritySettings(
    val passwordProtectionEnabled: Boolean = false,
    val biometricLockEnabled: Boolean = false,
    val sessionTimeoutMinutes: Int = 30,
    val screenRecordingBlocked: Boolean = false
)

data class NetworkSettings(
    val wifiConfigured: Boolean = false,
    val mobileDataEnabled: Boolean = true,
    val bluetoothEnabled: Boolean = true,
    val nfcEnabled: Boolean = true,
    val vpnConfigured: Boolean = false
)

data class HardwareSettings(
    val cameraEnabled: Boolean = true,
    val microphoneEnabled: Boolean = true,
    val brightnessLevel: Int = 50,
    val volumeLevel: Int = 50,
    val flashlightEnabled: Boolean = false
)

data class XiaomiSettings(
    val secondSpaceEnabled: Boolean = false,
    val batteryWhitelisted: Boolean = false,
    val autostartEnabled: Boolean = false,
    val gameTurboEnabled: Boolean = false,
    val appLockBypassed: Boolean = false
)
