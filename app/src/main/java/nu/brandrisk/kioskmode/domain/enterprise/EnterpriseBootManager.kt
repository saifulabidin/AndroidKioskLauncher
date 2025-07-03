package nu.brandrisk.kioskmode.domain.enterprise

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.app.admin.DevicePolicyManager
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nu.brandrisk.kioskmode.MainActivity
import nu.brandrisk.kioskmode.receiver.EnterpriseBootReceiver
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enterprise Boot Manager
 * Handles professional auto-start functionality similar to SureLock
 * Includes device owner policies, boot receiver management, and enterprise startup
 */
@Singleton
class EnterpriseBootManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "enterprise_boot_settings"
        private const val KEY_AUTO_START_ENABLED = "auto_start_enabled"
        private const val KEY_BOOT_DELAY_MS = "boot_delay_ms"
        private const val KEY_STARTUP_MODE = "startup_mode"
        private const val KEY_PERSISTENT_MODE = "persistent_mode"
        
        private const val DEFAULT_BOOT_DELAY = 5000L // 5 seconds
        
        enum class StartupMode {
            NORMAL,           // Normal startup
            SILENT,           // Silent background startup
            KIOSK_IMMEDIATE,  // Immediate kiosk mode
            LAUNCHER_ONLY     // Only as launcher
        }
    }
    
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val packageManager = context.packageManager
    
    /**
     * Enable enterprise auto-start on boot
     * Configures boot receiver and device policies
     */
    suspend fun enableAutoStart(
        startupMode: StartupMode = StartupMode.KIOSK_IMMEDIATE,
        bootDelayMs: Long = DEFAULT_BOOT_DELAY,
        persistentMode: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Enable boot receiver
            enableBootReceiver()
            
            // Configure device owner policies if available
            if (isDeviceOwner()) {
                configureDeviceOwnerBootPolicies()
            }
            
            // Save settings
            sharedPreferences.edit()
                .putBoolean(KEY_AUTO_START_ENABLED, true)
                .putLong(KEY_BOOT_DELAY_MS, bootDelayMs)
                .putString(KEY_STARTUP_MODE, startupMode.name)
                .putBoolean(KEY_PERSISTENT_MODE, persistentMode)
                .apply()
            
            // Configure persistent app if device owner
            if (isDeviceOwner() && persistentMode) {
                setPersistentApp()
            }
            
            true
        } catch (e: Exception) {
            android.util.Log.e("EnterpriseBootManager", "Failed to enable auto-start", e)
            false
        }
    }
    
    /**
     * Disable auto-start functionality
     */
    suspend fun disableAutoStart(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Disable boot receiver
            disableBootReceiver()
            
            // Remove persistent app if set
            if (isDeviceOwner()) {
                removePersistentApp()
            }
            
            // Clear settings
            sharedPreferences.edit()
                .putBoolean(KEY_AUTO_START_ENABLED, false)
                .apply()
            
            true
        } catch (e: Exception) {
            android.util.Log.e("EnterpriseBootManager", "Failed to disable auto-start", e)
            false
        }
    }
    
    /**
     * Configure enterprise startup sequence
     * Called by boot receiver
     */
    suspend fun executeEnterpriseStartup(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!isAutoStartEnabled()) {
                return@withContext false
            }
            
            val startupMode = getStartupMode()
            val bootDelay = getBootDelay()
            
            // Wait for boot delay
            kotlinx.coroutines.delay(bootDelay)
            
            // Execute startup based on mode
            when (startupMode) {
                StartupMode.NORMAL -> startNormalMode()
                StartupMode.SILENT -> startSilentMode()
                StartupMode.KIOSK_IMMEDIATE -> startKioskMode()
                StartupMode.LAUNCHER_ONLY -> startLauncherMode()
            }
            
            true
        } catch (e: Exception) {
            android.util.Log.e("EnterpriseBootManager", "Enterprise startup failed", e)
            false
        }
    }
    
    /**
     * Set app as persistent (requires device owner)
     */
    fun setPersistentApp(): Boolean {
        return try {
            if (isDeviceOwner()) {
                val packageNames = listOf(context.packageName)
                devicePolicyManager.setLockTaskPackages(getAdminComponent(), packageNames.toTypedArray())
                true
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("EnterpriseBootManager", "Failed to set persistent app", e)
            false
        }
    }
    
    /**
     * Remove persistent app setting
     */
    fun removePersistentApp(): Boolean {
        return try {
            if (isDeviceOwner()) {
                devicePolicyManager.setLockTaskPackages(getAdminComponent(), emptyArray())
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Configure device owner boot policies
     */
    private fun configureDeviceOwnerBootPolicies() {
        if (!isDeviceOwner()) return
        
        try {
            val adminComponent = getAdminComponent()
            
            // Disable safe mode to prevent bypassing kiosk
            devicePolicyManager.addUserRestriction(adminComponent, android.os.UserManager.DISALLOW_SAFE_BOOT)
            
            // Prevent users from adding accounts
            devicePolicyManager.addUserRestriction(adminComponent, android.os.UserManager.DISALLOW_MODIFY_ACCOUNTS)
            
            // Disable factory reset
            devicePolicyManager.addUserRestriction(adminComponent, android.os.UserManager.DISALLOW_FACTORY_RESET)
            
            // Set global settings for enterprise mode
            devicePolicyManager.setGlobalSetting(adminComponent, "stay_on_while_plugged_in", "7") // Always on when plugged
            
        } catch (e: Exception) {
            android.util.Log.e("EnterpriseBootManager", "Failed to configure device owner policies", e)
        }
    }
    
    /**
     * Start normal mode - standard app launch
     */
    private fun startNormalMode() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("startup_mode", "normal")
        }
        context.startActivity(intent)
    }
    
    /**
     * Start silent mode - background service only
     */
    private fun startSilentMode() {
        // Start enterprise services in background
        val serviceIntent = Intent(context, nu.brandrisk.kioskmode.service.EnhancedEnterpriseKioskService::class.java)
        context.startForegroundService(serviceIntent)
    }
    
    /**
     * Start immediate kiosk mode
     */
    private fun startKioskMode() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("startup_mode", "kiosk")
            putExtra("auto_kiosk", true)
        }
        context.startActivity(intent)
        
        // Enable lock task if device owner
        if (isDeviceOwner()) {
            // This will be handled by MainActivity
        }
    }
    
    /**
     * Start launcher mode only
     */
    private fun startLauncherMode() {
        // Set as default launcher and wait for home intent
        setAsDefaultLauncher()
    }
    
    /**
     * Set application as default launcher
     */
    fun setAsDefaultLauncher(): Boolean {
        return try {
            if (isDeviceOwner()) {
                val adminComponent = getAdminComponent()
                val launcherComponent = ComponentName(context, MainActivity::class.java)
                
                // Clear current default launcher
                devicePolicyManager.clearPackagePersistentPreferredActivities(adminComponent, context.packageName)
                
                // Set this app as preferred launcher
                val intentFilter = android.content.IntentFilter().apply {
                    addAction(Intent.ACTION_MAIN)
                    addCategory(Intent.CATEGORY_HOME)
                    addCategory(Intent.CATEGORY_DEFAULT)
                }
                
                devicePolicyManager.addPersistentPreferredActivity(
                    adminComponent,
                    intentFilter,
                    launcherComponent
                )
                
                true
            } else {
                // Fallback: Open launcher chooser
                val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("EnterpriseBootManager", "Failed to set default launcher", e)
            false
        }
    }
    
    /**
     * Enable boot receiver component
     */
    private fun enableBootReceiver() {
        val component = ComponentName(context, EnterpriseBootReceiver::class.java)
        packageManager.setComponentEnabledSetting(
            component,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
    
    /**
     * Disable boot receiver component
     */
    private fun disableBootReceiver() {
        val component = ComponentName(context, EnterpriseBootReceiver::class.java)
        packageManager.setComponentEnabledSetting(
            component,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
    
    // Getter methods
    fun isAutoStartEnabled(): Boolean = sharedPreferences.getBoolean(KEY_AUTO_START_ENABLED, false)
    
    fun getStartupMode(): StartupMode {
        val modeName = sharedPreferences.getString(KEY_STARTUP_MODE, StartupMode.NORMAL.name)
        return try {
            StartupMode.valueOf(modeName!!)
        } catch (e: Exception) {
            StartupMode.NORMAL
        }
    }
    
    fun getBootDelay(): Long = sharedPreferences.getLong(KEY_BOOT_DELAY_MS, DEFAULT_BOOT_DELAY)
    
    fun isPersistentModeEnabled(): Boolean = sharedPreferences.getBoolean(KEY_PERSISTENT_MODE, false)
    
    private fun isDeviceOwner(): Boolean {
        return devicePolicyManager.isDeviceOwnerApp(context.packageName)
    }
    
    private fun getAdminComponent(): ComponentName {
        return ComponentName(context, nu.brandrisk.kioskmode.KioskDeviceAdminReceiver::class.java)
    }
    
    /**
     * Get auto-start status for UI display
     */
    fun getAutoStartStatus(): AutoStartStatus {
        return AutoStartStatus(
            isEnabled = isAutoStartEnabled(),
            startupMode = getStartupMode(),
            bootDelay = getBootDelay(),
            isPersistent = isPersistentModeEnabled(),
            isDeviceOwner = isDeviceOwner(),
            bootReceiverEnabled = isBootReceiverEnabled()
        )
    }
    
    private fun isBootReceiverEnabled(): Boolean {
        val component = ComponentName(context, EnterpriseBootReceiver::class.java)
        val state = packageManager.getComponentEnabledSetting(component)
        return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
    }
}

/**
 * Data class for auto-start status
 */
data class AutoStartStatus(
    val isEnabled: Boolean,
    val startupMode: EnterpriseBootManager.Companion.StartupMode,
    val bootDelay: Long,
    val isPersistent: Boolean,
    val isDeviceOwner: Boolean,
    val bootReceiverEnabled: Boolean
)
