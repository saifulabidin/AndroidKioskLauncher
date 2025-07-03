package nu.brandrisk.kioskmode.receiver

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import nu.brandrisk.kioskmode.util.KioskLogger

/**
 * System UI Control Receiver
 * Handles system UI control and manipulation for enterprise kiosk mode
 * Inspired by SureLock's system UI management
 */
class SystemUIControlReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SystemUIControlReceiver"
        
        // Custom actions for system UI control
        const val ACTION_HIDE_SYSTEM_UI = "nu.brandrisk.kioskmode.HIDE_SYSTEM_UI"
        const val ACTION_SHOW_SYSTEM_UI = "nu.brandrisk.kioskmode.SHOW_SYSTEM_UI"
        const val ACTION_DISABLE_STATUS_BAR = "nu.brandrisk.kioskmode.DISABLE_STATUS_BAR"
        const val ACTION_ENABLE_STATUS_BAR = "nu.brandrisk.kioskmode.ENABLE_STATUS_BAR"
    }

    override fun onReceive(context: Context, intent: Intent) {
        KioskLogger.i(TAG, "System UI control event received: ${intent.action}")
        
        when (intent.action) {
            ACTION_HIDE_SYSTEM_UI -> {
                KioskLogger.i(TAG, "Hiding system UI")
                hideSystemUI(context)
            }
            
            ACTION_SHOW_SYSTEM_UI -> {
                KioskLogger.i(TAG, "Showing system UI")
                showSystemUI(context)
            }
            
            ACTION_DISABLE_STATUS_BAR -> {
                KioskLogger.i(TAG, "Disabling status bar")
                disableStatusBar(context)
            }
            
            ACTION_ENABLE_STATUS_BAR -> {
                KioskLogger.i(TAG, "Enabling status bar")
                enableStatusBar(context)
            }
            
            Intent.ACTION_SCREEN_ON -> {
                KioskLogger.i(TAG, "Screen turned on - enforcing UI restrictions")
                handleScreenOn(context)
            }
            
            Intent.ACTION_SCREEN_OFF -> {
                KioskLogger.i(TAG, "Screen turned off")
                handleScreenOff()
            }
            
            Intent.ACTION_USER_PRESENT -> {
                KioskLogger.i(TAG, "User present - enforcing kiosk mode")
                handleUserPresent(context)
            }
        }
    }

    private fun hideSystemUI(context: Context) {
        try {
            // Method 1: Use Device Admin to control system UI
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
            val componentName = android.content.ComponentName(context, nu.brandrisk.kioskmode.KioskDeviceAdminReceiver::class.java)
            
            if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                // Device Owner mode - can control system UI
                devicePolicyManager.setLockTaskPackages(componentName, arrayOf(context.packageName))
                KioskLogger.i(TAG, "System UI hidden using Device Owner mode")
            } else {
                // Fallback: Use Accessibility Service or broadcast to hide UI
                val intent = Intent("nu.brandrisk.kioskmode.HIDE_NAVIGATION")
                intent.setPackage(context.packageName)
                context.sendBroadcast(intent)
                KioskLogger.i(TAG, "System UI hide request sent via broadcast")
            }
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to hide system UI", e)
        }
    }

    private fun showSystemUI(context: Context) {
        try {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
            val componentName = android.content.ComponentName(context, nu.brandrisk.kioskmode.KioskDeviceAdminReceiver::class.java)
            
            if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                // Clear lock task packages to allow normal UI
                devicePolicyManager.setLockTaskPackages(componentName, arrayOf())
                KioskLogger.i(TAG, "System UI shown using Device Owner mode")
            } else {
                // Fallback: Broadcast to show UI
                val intent = Intent("nu.brandrisk.kioskmode.SHOW_NAVIGATION")
                intent.setPackage(context.packageName)
                context.sendBroadcast(intent)
                KioskLogger.i(TAG, "System UI show request sent via broadcast")
            }
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to show system UI", e)
        }
    }

    private fun disableStatusBar(context: Context) {
        try {
            // Method 1: Device Admin approach
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
            val componentName = android.content.ComponentName(context, nu.brandrisk.kioskmode.KioskDeviceAdminReceiver::class.java)
            
            if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                // Disable keyguard features including status bar expansion
                devicePolicyManager.setKeyguardDisabledFeatures(
                    componentName,
                    android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_ALL
                )
                KioskLogger.i(TAG, "Status bar disabled using Device Admin")
            } else {
                // Method 2: Send command to Accessibility Service
                val intent = Intent("nu.brandrisk.kioskmode.DISABLE_STATUS_BAR_EXPANSION")
                intent.setPackage(context.packageName)
                context.sendBroadcast(intent)
                KioskLogger.i(TAG, "Status bar disable request sent to Accessibility Service")
            }
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to disable status bar", e)
        }
    }

    private fun enableStatusBar(context: Context) {
        try {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
            val componentName = android.content.ComponentName(context, nu.brandrisk.kioskmode.KioskDeviceAdminReceiver::class.java)
            
            if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                // Re-enable keyguard features
                devicePolicyManager.setKeyguardDisabledFeatures(componentName, 0)
                KioskLogger.i(TAG, "Status bar enabled using Device Admin")
            } else {
                // Send command to re-enable status bar
                val intent = Intent("nu.brandrisk.kioskmode.ENABLE_STATUS_BAR_EXPANSION")
                intent.setPackage(context.packageName)
                context.sendBroadcast(intent)
                KioskLogger.i(TAG, "Status bar enable request sent")
            }
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to enable status bar", e)
        }
    }

    private fun handleScreenOn(context: Context) {
        // Enforce UI restrictions when screen turns on
        hideSystemUI(context)
        disableStatusBar(context)
    }

    private fun handleScreenOff() {
        // Handle screen off event
        KioskLogger.i(TAG, "Handling screen off event")
    }

    private fun handleUserPresent(context: Context) {
        try {
            // Ensure kiosk mode is active when user is present
            hideSystemUI(context)
            disableStatusBar(context)
            
            // Launch kiosk app if not already active
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager

            var isKioskAppActive = false
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                val runningTasks = activityManager.getRunningTasks(1)
                if (runningTasks.isNotEmpty()) {
                    val topActivity = runningTasks[0].topActivity
                    isKioskAppActive = topActivity?.packageName == context.packageName
                }
            } else {
                val runningAppProcesses = activityManager.runningAppProcesses
                val foregroundProcess = runningAppProcesses?.firstOrNull { it.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND }
                val packageName = foregroundProcess?.pkgList?.firstOrNull()
                isKioskAppActive = packageName == context.packageName
            }

            if (!isKioskAppActive) {
                KioskLogger.i(TAG, "Kiosk app not active, launching...")
                val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                launchIntent?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    context.startActivity(this)
                }
            }
            
            // Disable unauthorized apps
            disableUnauthorizedApps(context)
            
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to handle user present", e)
        }
    }
    
    private fun disableUnauthorizedApps(context: Context) {
        try {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
            val componentName = android.content.ComponentName(context, nu.brandrisk.kioskmode.KioskDeviceAdminReceiver::class.java)
            
            if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                // Get list of unauthorized packages
                val packageManager = context.packageManager
                val installedPackages = packageManager.getInstalledPackages(0)
                
                val allowedPackages = setOf(
                    context.packageName,
                    "com.android.systemui",
                    "android",
                    "com.android.settings"
                )
                
                // Hide unauthorized apps
                for (packageInfo in installedPackages) {
                    if (!allowedPackages.contains(packageInfo.packageName)) {
                        try {
                            devicePolicyManager.setApplicationHidden(componentName, packageInfo.packageName, true)
                        } catch (e: Exception) {
                            // Some system packages cannot be hidden
                            KioskLogger.w(TAG, "Cannot hide package: ${packageInfo.packageName}")
                        }
                    }
                }
                
                KioskLogger.i(TAG, "Unauthorized apps disabled")
            } else {
                KioskLogger.w(TAG, "Device Owner required to disable unauthorized apps")
            }
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to disable unauthorized apps", e)
        }
    }
}
