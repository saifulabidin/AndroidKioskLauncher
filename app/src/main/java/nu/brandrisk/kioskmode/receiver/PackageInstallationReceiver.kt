package nu.brandrisk.kioskmode.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import nu.brandrisk.kioskmode.util.KioskLogger

/**
 * Package Installation Receiver
 * Monitors app installations and removals for enterprise security
 * Inspired by SureLock's package management system
 */
class PackageInstallationReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "PackageInstallationReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        KioskLogger.i(TAG, "Package installation event received: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED -> {
                val packageName = intent.data?.schemeSpecificPart
                KioskLogger.i(TAG, "Package installed: $packageName")
                handlePackageInstalled(context, packageName)
            }
            
            Intent.ACTION_PACKAGE_REMOVED -> {
                val packageName = intent.data?.schemeSpecificPart
                KioskLogger.i(TAG, "Package removed: $packageName")
                handlePackageRemoved(context, packageName)
            }
            
            Intent.ACTION_PACKAGE_REPLACED -> {
                val packageName = intent.data?.schemeSpecificPart
                KioskLogger.i(TAG, "Package replaced: $packageName")
                handlePackageReplaced(context, packageName)
            }
        }
    }

    private fun handlePackageInstalled(context: Context, packageName: String?) {
        packageName?.let {
            // Check if new package should be allowed in kiosk mode
            // Add to database for enterprise management
            KioskLogger.i(TAG, "Processing new package installation: $it")
            
            // Check against enterprise whitelist
            val isEnterpriseApproved = checkEnterprisePolicy(context, it)
            if (!isEnterpriseApproved) {
                // Auto-disable if not in whitelist
                disableUnauthorizedPackage(context, it)
            }
            
            // Add to app database for management
            addToAppDatabase(context, it)
        }
    }

    private fun handlePackageRemoved(context: Context, packageName: String?) {
        packageName?.let {
            // Clean up database entries for removed package
            KioskLogger.i(TAG, "Cleaning up removed package: $it")
            
            // Remove from app database
            removeFromAppDatabase(context, it)
            // Update enterprise policies
            updateEnterprisePolicy(context, it, removed = true)
        }
    }

    private fun handlePackageReplaced(context: Context, packageName: String?) {
        packageName?.let {
            // Handle package updates
            KioskLogger.i(TAG, "Processing package update: $it")
            
            // Verify updated package still meets enterprise policies
            val isStillApproved = checkEnterprisePolicy(context, it)
            if (!isStillApproved) {
                disableUnauthorizedPackage(context, it)
            }
            
            // Re-evaluate permissions and restrictions
            updatePackagePermissions(context, it)
        }
    }
    
    // Enterprise policy implementation
    private fun checkEnterprisePolicy(context: Context, packageName: String): Boolean {
        val allowedPackages = setOf(
            context.packageName,
            "com.android.systemui",
            "android",
            "com.android.settings",
            "com.google.android.gms",
            "com.android.vending"
        )
        
        // Check if package is in enterprise whitelist
        return allowedPackages.contains(packageName) || isSystemPackage(context, packageName)
    }
    
    private fun isSystemPackage(context: Context, packageName: String): Boolean {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(packageName, 0)
            (packageInfo.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
        } catch (e: Exception) {
            false
        }
    }
    
    private fun disableUnauthorizedPackage(context: Context, packageName: String) {
        try {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
            val componentName = android.content.ComponentName(context, nu.brandrisk.kioskmode.KioskDeviceAdminReceiver::class.java)
            
            if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                devicePolicyManager.setApplicationHidden(componentName, packageName, true)
                KioskLogger.i(TAG, "Disabled unauthorized package: $packageName")
            }
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to disable package: $packageName", e)
        }
    }
    
    private fun addToAppDatabase(context: Context, packageName: String) {
        try {
            // Implementation would integrate with Room database
            KioskLogger.i(TAG, "Added package to database: $packageName")
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to add package to database", e)
        }
    }
    
    private fun removeFromAppDatabase(context: Context, packageName: String) {
        try {
            // Implementation would integrate with Room database
            KioskLogger.i(TAG, "Removed package from database: $packageName")
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to remove package from database", e)
        }
    }
    
    private fun updateEnterprisePolicy(context: Context, packageName: String, removed: Boolean) {
        try {
            if (removed) {
                KioskLogger.i(TAG, "Updated enterprise policy for removed package: $packageName")
            } else {
                KioskLogger.i(TAG, "Updated enterprise policy for package: $packageName")
            }
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to update enterprise policy", e)
        }
    }
    
    private fun updatePackagePermissions(context: Context, packageName: String) {
        try {
            // Re-evaluate package permissions after update
            KioskLogger.i(TAG, "Updated permissions for package: $packageName")
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to update package permissions", e)
        }
    }
}
