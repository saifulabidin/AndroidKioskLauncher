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
open class ConfigViewModel @Inject constructor(
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

    // Enhanced App Management Methods
    fun toggleSystemApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentApps = repository.getApps().first()
            val systemApps = currentApps.filter { isSystemApp(it.packageName) }
            val shouldEnable = systemApps.any { !it.isEnabled }
            
            systemApps.forEach { app ->
                repository.upsertApp(app.copy(isEnabled = shouldEnable))
            }
        }
    }

    fun toggleUserApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentApps = repository.getApps().first()
            val userApps = currentApps.filter { !isSystemApp(it.packageName) }
            val shouldEnable = userApps.any { !it.isEnabled }
            
            userApps.forEach { app ->
                repository.upsertApp(app.copy(isEnabled = shouldEnable))
            }
        }
    }

    // Enterprise Policy Presets
    fun applyWhitelistPolicy() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentApps = repository.getApps().first()
            val whitelistedPackages = listOf(
                "com.android.chrome",
                "com.android.vending", // Play Store
                "com.google.android.apps.docs", // Google Drive
                "com.microsoft.office.word",
                "com.microsoft.office.excel",
                "com.microsoft.office.powerpoint",
                "com.adobe.reader"
            )
            
            currentApps.forEach { app ->
                val shouldEnable = whitelistedPackages.contains(app.packageName) || 
                                 app.packageName == context.packageName
                repository.upsertApp(app.copy(isEnabled = shouldEnable))
            }
        }
    }

    fun applyEducationPolicy() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentApps = repository.getApps().first()
            val educationPackages = listOf(
                "com.android.chrome",
                "com.google.android.apps.classroom",
                "com.google.android.apps.docs",
                "com.microsoft.office.word",
                "org.khanacademy.android",
                "com.duolingo",
                "com.wolfram.android.alpha",
                "com.adobe.reader"
            )
            
            currentApps.forEach { app ->
                val shouldEnable = educationPackages.contains(app.packageName) || 
                                 app.packageName == context.packageName ||
                                 isEducationalApp(app.packageName)
                repository.upsertApp(app.copy(isEnabled = shouldEnable))
            }
        }
    }

    fun applyKioskPolicy() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentApps = repository.getApps().first()
            // Only allow launcher and one specified app
            currentApps.forEach { app ->
                val shouldEnable = app.packageName == context.packageName
                repository.upsertApp(app.copy(isEnabled = shouldEnable))
            }
        }
    }

    fun applyBusinessPolicy() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentApps = repository.getApps().first()
            val businessPackages = listOf(
                "com.android.chrome",
                "com.microsoft.teams",
                "com.slack",
                "com.zoom.us",
                "com.google.android.apps.meetings",
                "com.microsoft.office.word",
                "com.microsoft.office.excel",
                "com.microsoft.office.powerpoint",
                "com.microsoft.office.outlook",
                "com.dropbox.android",
                "com.adobe.reader"
            )
            
            currentApps.forEach { app ->
                val shouldEnable = businessPackages.contains(app.packageName) || 
                                 app.packageName == context.packageName ||
                                 isBusinessApp(app.packageName)
                repository.upsertApp(app.copy(isEnabled = shouldEnable))
            }
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

    // App search functionality
    fun searchApps(query: String): Flow<List<App>> {
        return repository.getApps().map { apps ->
            if (query.isBlank()) {
                apps
            } else {
                apps.filter { app ->
                    app.label.contains(query, ignoreCase = true) ||
                    app.packageName.contains(query, ignoreCase = true)
                }
            }
        }
    }

    // App export/import functionality (similar to SureLock)
    fun exportAppList(): String {
        // Export enabled apps list as JSON or CSV
        return "Export functionality to be implemented"
    }

    fun importAppList(data: String) {
        // Import app configuration from file
        viewModelScope.launch {
            // Implementation for importing app configurations
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