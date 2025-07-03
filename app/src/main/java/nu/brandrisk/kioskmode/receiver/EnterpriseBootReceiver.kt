package nu.brandrisk.kioskmode.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.ComponentName
import nu.brandrisk.kioskmode.service.EnterpriseKioskService
import nu.brandrisk.kioskmode.util.KioskLogger

/**
 * Enterprise Boot Receiver - Ensures kiosk mode starts on boot
 * Like SureLock's high-priority boot receiver
 */
class EnterpriseBootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "EnterpriseBootReceiver"
    }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        
        val action = intent.action
        KioskLogger.i(TAG, "Boot event received: $action")
        
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
        }
    }
    
    private fun handleBootCompleted(context: Context) {
        KioskLogger.i(TAG, "System boot completed - starting enterprise services")
        
        try {
            // Start enterprise kiosk service
            val serviceIntent = Intent(context, EnterpriseKioskService::class.java)
            serviceIntent.action = "START_MONITORING"
            context.startForegroundService(serviceIntent)
            
            // Set as default launcher
            setAsDefaultLauncher(context)
            
            // Launch kiosk app
            launchKioskApp(context)
            
            KioskLogger.i(TAG, "Enterprise services started successfully")
            
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Error starting enterprise services", e)
        }
    }
    
    private fun handleLockedBootCompleted(context: Context) {
        KioskLogger.i(TAG, "Locked boot completed - preparing for unlock")
        
        // Prepare for full boot completion
        // Some enterprise features can be initialized here
    }
    
    private fun handleUserUnlocked(context: Context) {
        KioskLogger.i(TAG, "User unlocked - enforcing kiosk mode")
        
        try {
            // Ensure kiosk mode is active after user unlock
            val serviceIntent = Intent(context, EnterpriseKioskService::class.java)
            serviceIntent.action = "ENFORCE_KIOSK"
            context.startForegroundService(serviceIntent)
            
            // Launch kiosk app if not already running
            launchKioskApp(context)
            
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Error enforcing kiosk after unlock", e)
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
