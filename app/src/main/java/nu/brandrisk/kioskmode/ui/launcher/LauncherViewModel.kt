package nu.brandrisk.kioskmode.ui.launcher

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import nu.brandrisk.kioskmode.data.model.App
import nu.brandrisk.kioskmode.domain.AppRepository
import nu.brandrisk.kioskmode.domain.enterprise.HardwareControlManager
import nu.brandrisk.kioskmode.domain.enterprise.NetworkManager
import nu.brandrisk.kioskmode.utils.ApplicationUtils
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
    repository: AppRepository,
    @ApplicationContext val context: Context,
    val imageLoader: ImageLoader,
    val applicationUtils: ApplicationUtils,
    private val networkManager: NetworkManager,
    private val hardwareManager: HardwareControlManager
): ViewModel() {

    fun startApplication(app: App) {
        applicationUtils.startApplication(app.packageName)
    }

    val apps = repository.getApps()

    /**
     * Quick Actions - Enterprise Controls
     */
    fun openWiFiSettings() {
        try {
            val intent = Intent(Settings.ACTION_WIFI_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback - do nothing or show toast
        }
    }

    fun openBluetoothSettings() {
        try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback - do nothing
        }
    }

    fun openVolumePanel() {
        viewModelScope.launch {
            // Toggle volume or open volume controls
            val currentVolume = hardwareManager.getVolumeLevel()
            // You could implement a custom volume overlay here
            // For now, just open sound settings
            try {
                val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                // Fallback
            }
        }
    }

    fun openBrightnessSettings() {
        try {
            val intent = Intent(Settings.ACTION_DISPLAY_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback
        }
    }

    /**
     * Enterprise Quick Controls
     */
    fun toggleWiFi() {
        viewModelScope.launch {
            // Toggle WiFi state
            val currentState = networkManager.isWiFiConfigured()
            // Implement WiFi toggle logic
        }
    }

    fun toggleBluetooth() {
        viewModelScope.launch {
            val currentState = networkManager.isBluetoothEnabled()
            networkManager.setBluetoothEnabled(!currentState)
        }
    }

    fun adjustVolume(level: Int) {
        viewModelScope.launch {
            hardwareManager.setVolumeLevel(level)
        }
    }

    fun adjustBrightness(level: Int) {
        viewModelScope.launch {
            hardwareManager.setBrightness(level)
        }
    }

    /**
     * Enterprise Status Monitoring
     */
    fun getSystemStatus(): SystemStatus {
        return SystemStatus(
            wifiEnabled = networkManager.isWiFiConfigured(),
            bluetoothEnabled = networkManager.isBluetoothEnabled(),
            volumeLevel = hardwareManager.getVolumeLevel(),
            brightnessLevel = hardwareManager.getBrightnessLevel(),
            batteryLevel = hardwareManager.getBatteryLevel(),
            isCharging = hardwareManager.isCharging()
        )
    }

    /**
     * Emergency functions
     */
    fun emergencyUnlock() {
        // Emergency unlock for kiosk mode
        // Could require additional authentication
    }

    fun restartKioskMode() {
        // Restart kiosk mode functionality
        // Clear app data and restart launcher
    }
}

/**
 * System status data class
 */
data class SystemStatus(
    val wifiEnabled: Boolean,
    val bluetoothEnabled: Boolean,
    val volumeLevel: Int,
    val brightnessLevel: Int,
    val batteryLevel: Int,
    val isCharging: Boolean
)