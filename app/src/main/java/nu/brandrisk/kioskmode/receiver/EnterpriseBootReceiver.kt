package nu.brandrisk.kioskmode.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.ComponentName
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nu.brandrisk.kioskmode.domain.enterprise.EnterpriseBootManager
import nu.brandrisk.kioskmode.service.EnterpriseKioskService
import nu.brandrisk.kioskmode.util.KioskLogger
import javax.inject.Inject

/**
 * Enterprise Boot Receiver - Professional auto-start functionality
 * Like SureLock's enterprise boot management
 */
@AndroidEntryPoint
class EnterpriseBootReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var bootManager: EnterpriseBootManager
    
    companion object {
        private const val TAG = "EnterpriseBootReceiver"
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        val action = intent.action
        KioskLogger.i(TAG, "Enterprise boot event received: $action")
        
        when (action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                handleBootCompleted(context)
            }
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {
                handleLockedBootCompleted(context)
            }
            Intent.ACTION_USER_UNLOCKED -> {
                handleUserUnlocked(context)
            }
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                handlePackageReplaced(context, intent)
            }
        }
    }
    
    private fun handleBootCompleted(context: Context) {
        KioskLogger.i(TAG, "System boot completed - executing enterprise startup")
        
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Execute enterprise startup sequence
                val success = bootManager.executeEnterpriseStartup()
                KioskLogger.i(TAG, "Enterprise startup result: $success")
                
                if (!success) {
                    // Fallback: Start basic services
                    startFallbackServices(context)
                }
            } catch (e: Exception) {
                KioskLogger.e(TAG, "Enterprise startup failed", e)
                startFallbackServices(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
    
    private fun handleLockedBootCompleted(context: Context) {
        KioskLogger.i(TAG, "Locked boot completed - preparing enterprise services")
        
        // Start essential services that don't require user unlock
        try {
            val serviceIntent = Intent(context, EnterpriseKioskService::class.java)
            serviceIntent.action = "PREPARE_ENTERPRISE_MODE"
            context.startForegroundService(serviceIntent)
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to start enterprise services on locked boot", e)
        }
    }
    
    private fun handleUserUnlocked(context: Context) {
        KioskLogger.i(TAG, "User unlocked - completing enterprise initialization")
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Complete enterprise startup after user unlock
                bootManager.executeEnterpriseStartup()
                
                // Ensure kiosk mode is active after user unlock
                val serviceIntent = Intent(context, EnterpriseKioskService::class.java)
                serviceIntent.action = "ENFORCE_KIOSK"
                context.startForegroundService(serviceIntent)
                
                // Launch kiosk app if not already running
                launchKioskApp(context)
            } catch (e: Exception) {
                KioskLogger.e(TAG, "Failed to complete enterprise initialization", e)
            }
        }
    }
    
    private fun handlePackageReplaced(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart
        if (packageName == context.packageName) {
            KioskLogger.i(TAG, "Enterprise app updated - re-configuring auto-start")
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Re-enable auto-start after app update
                    val status = bootManager.getAutoStartStatus()
                if (status.isEnabled) {
                    bootManager.enableAutoStart(
                        startupMode = status.startupMode,
                        bootDelayMs = status.bootDelay,
                        persistentMode = status.isPersistent
                    )
                }
                // Launch kiosk app if not already running
                launchKioskApp(context)
            } catch (e: Exception) {
                KioskLogger.e(TAG, "Error enforcing kiosk after unlock", e)
            }
        }
    }
}

private fun startFallbackServices(context: Context) {
    try {
        // Start basic enterprise service as fallback
        val serviceIntent = Intent(context, EnterpriseKioskService::class.java)
        serviceIntent.action = "START_MONITORING"
        context.startForegroundService(serviceIntent)

        KioskLogger.i(TAG, "Fallback services started")
    } catch (e: Exception) {
        KioskLogger.e(TAG, "Failed to start fallback services", e)
    }
}

private fun setAsDefaultLauncher(context: Context) {
        try {
            val packageManager = context.packageManager
            val componentName = ComponentName(context, "nu.brandrisk.kioskmode.MainActivity")
            
            // Enable our launcher
            packageManager.setComponentEnabledSetting(
                componentName,
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            )
            
            KioskLogger.i(TAG, "Set as default launcher")
            
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Error setting default launcher", e)
        }
    }
    
    private fun launchKioskApp(context: Context) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            launchIntent?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                context.startActivity(this)
            }
            
            KioskLogger.i(TAG, "Launched kiosk app")
            
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Error launching kiosk app", e)
        }
    }
}
