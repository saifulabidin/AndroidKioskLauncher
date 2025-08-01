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
            // Check if auto-start is explicitly disabled (for development)
            val explicitlyDisabled = sharedPreferences.getBoolean("auto_start_enabled", true) == false
            if (explicitlyDisabled) {
                android.util.Log.i("EnterpriseBootManager", "Auto-start explicitly disabled, skipping enterprise startup")
                return@withContext false
            }
            
            // If device owner but auto-start not enabled, enable it first
            if (isDeviceOwner() && !isAutoStartEnabled()) {
                android.util.Log.i("EnterpriseBootManager", "Device owner detected, auto-enabling kiosk mode")
                enableAutoStart(
                    startupMode = StartupMode.KIOSK_IMMEDIATE,
                    bootDelayMs = DEFAULT_BOOT_DELAY,
                    persistentMode = true
                )
            }
            
            // Execute kiosk startup if device owner (and not explicitly disabled)
            if (isDeviceOwner() && !explicitlyDisabled) {
                val bootDelay = getBootDelay()
                
                // Wait for boot delay
                kotlinx.coroutines.delay(bootDelay)
                
                // Always start kiosk mode for device owner
                startKioskMode()
                
                // 🔥 AUTO-ENABLE ACCOUNT LOGIN SUPPORT AFTER KIOSK SETUP 🔥
                enableAccountLoginSupport()
                
                android.util.Log.i("EnterpriseBootManager", "Enterprise kiosk startup executed successfully with account login support")
                return@withContext true
            }
            
            // Fallback for non-device owner
            if (isAutoStartEnabled()) {
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
                return@withContext true
            }
            
            false
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
     * Check if device can be set as device owner
     */
    fun canSetDeviceOwner(): Boolean {
        return try {
            // Check if any accounts exist
            val accountManager = android.accounts.AccountManager.get(context)
            val accounts = accountManager.accounts
            
            if (accounts.isNotEmpty()) {
                android.util.Log.w("EnterpriseBootManager", 
                    "Cannot set device owner: ${accounts.size} accounts found - ${accounts.map { "${it.type}:${it.name}" }}")
                return false
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("EnterpriseBootManager", "Failed to check accounts", e)
            false
        }
    }

    /**
     * Get existing accounts information
     */
    fun getExistingAccounts(): List<String> {
        return try {
            val accountManager = android.accounts.AccountManager.get(context)
            accountManager.accounts.map { "${it.type}: ${it.name}" }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Auto remove accounts if possible (requires device owner privileges)
     */
    suspend fun autoRemoveAccounts(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            if (isDeviceOwner()) {
                // If already device owner, can remove accounts
                val accountManager = android.accounts.AccountManager.get(context)
                val accounts = accountManager.accounts
                
                var removedCount = 0
                for (account in accounts) {
                    try {
                        accountManager.removeAccountExplicitly(account)
                        android.util.Log.i("EnterpriseBootManager", "Removed account: ${account.name}")
                        removedCount++
                    } catch (e: Exception) {
                        android.util.Log.w("EnterpriseBootManager", "Failed to remove account: ${account.name}", e)
                    }
                }
                
                android.util.Log.i("EnterpriseBootManager", "Removed $removedCount accounts")
                removedCount > 0
            } else {
                android.util.Log.w("EnterpriseBootManager", "Cannot remove accounts - not device owner")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("EnterpriseBootManager", "Failed to remove accounts", e)
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
            
            // ⚠️ DO NOT restrict account modifications to allow Google/WhatsApp login
            // Only restrict profile management, not account login/modification
            // devicePolicyManager.addUserRestriction(adminComponent, android.os.UserManager.DISALLOW_MODIFY_ACCOUNTS)  // COMMENTED OUT - Allow Google/WhatsApp login
            devicePolicyManager.addUserRestriction(adminComponent, android.os.UserManager.DISALLOW_ADD_MANAGED_PROFILE)
            
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
        try {
            // First, ensure kiosk mode is properly configured if device owner
            if (isDeviceOwner()) {
                configureKioskModeSettings()
            }
            
            // Launch main activity with kiosk flags
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("startup_mode", "kiosk")
                putExtra("auto_kiosk", true)
                putExtra("force_kiosk", true)
            }
            context.startActivity(intent)
            
            android.util.Log.i("EnterpriseBootManager", "Kiosk mode startup initiated")
        } catch (e: Exception) {
            android.util.Log.e("EnterpriseBootManager", "Failed to start kiosk mode", e)
        }
    }
    
    /**
     * Configure kiosk mode settings for device owner
     */
    private fun configureKioskModeSettings() {
        if (!isDeviceOwner()) return
        
        try {
            val adminComponent = getAdminComponent()
            
            // Set lock task packages (allow all apps for flexibility)
            val installedApps = context.packageManager.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
            val packageNames = installedApps.map { it.packageName }.toTypedArray()
            devicePolicyManager.setLockTaskPackages(adminComponent, packageNames)
            
            // Set as persistent preferred activity (home launcher)
            val intentFilter = android.content.IntentFilter().apply {
                addAction(Intent.ACTION_MAIN)
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            
            val launcherComponent = ComponentName(context, MainActivity::class.java)
            devicePolicyManager.addPersistentPreferredActivity(
                adminComponent,
                intentFilter,
                launcherComponent
            )
            
            android.util.Log.i("EnterpriseBootManager", "Kiosk mode settings configured")
        } catch (e: Exception) {
            android.util.Log.e("EnterpriseBootManager", "Failed to configure kiosk settings", e)
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
    
    fun isPersistentModeEnabled(): Boolean {
        return if (isDeviceOwner()) {
            sharedPreferences.getBoolean(KEY_PERSISTENT_MODE, false)
        } else {
            false
        }
    }

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
    
    fun launchKioskApp() {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            launchIntent?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(this)
            }
            android.util.Log.i("EnterpriseBootManager", "Launched kiosk app")
        } catch (e: Exception) {
            android.util.Log.e("EnterpriseBootManager", "Error launching kiosk app", e)
        }
    }

    /**
     * Enable Google account login after device owner setup
     * This allows users to login to Google/WhatsApp while maintaining kiosk mode
     */
    suspend fun enableAccountLoginSupport(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            if (isDeviceOwner()) {
                android.util.Log.i("EnterpriseBootManager", "Enabling Google account login support...")
                
                // Re-enable Google services for account login
                val packageManager = context.packageManager
                
                // Enable Google Play Services
                packageManager.setApplicationEnabledSetting(
                    "com.google.android.gms",
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    0
                )
                
                // Enable Google Services Framework
                packageManager.setApplicationEnabledSetting(
                    "com.google.android.gsf", 
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    0
                )
                
                // Enable Play Store
                packageManager.setApplicationEnabledSetting(
                    "com.android.vending",
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    0
                )
                
                android.util.Log.i("EnterpriseBootManager", "Google account login support enabled successfully")
                true
            } else {
                android.util.Log.w("EnterpriseBootManager", "Not device owner - cannot modify app states")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("EnterpriseBootManager", "Failed to enable account login support", e)
            false
        }
    }

    suspend fun handleAppUpdate() {
        withContext(Dispatchers.IO) {
            try {
                android.util.Log.i("EnterpriseBootManager", "Enterprise app updated - re-configuring auto-start")
                // Re-enable auto-start after app update
                val status = getAutoStartStatus()
                if (status.isEnabled) {
                    enableAutoStart(
                        startupMode = status.startupMode,
                        bootDelayMs = status.bootDelay,
                        persistentMode = status.isPersistent
                    )
                } else {
                    android.util.Log.i("EnterpriseBootManager", "Auto-start is not enabled, skipping reconfiguration")
                }
            } catch (e: Exception) {
                android.util.Log.e("EnterpriseBootManager", "Error re-configuring after app update", e)
            }
        }
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
