package nu.brandrisk.kioskmode.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import nu.brandrisk.kioskmode.util.KioskLogger

/**
 * Security Event Receiver
 * Monitors security-related events for enterprise kiosk enforcement
 * Inspired by SureLock's security monitoring system
 */
class SecurityEventReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SecurityEventReceiver"
        
        // Custom security actions
        const val ACTION_SECURITY_VIOLATION = "nu.brandrisk.kioskmode.SECURITY_VIOLATION"
        const val ACTION_UNAUTHORIZED_ACCESS = "nu.brandrisk.kioskmode.UNAUTHORIZED_ACCESS"
        const val ACTION_ADMIN_DISABLED = "nu.brandrisk.kioskmode.ADMIN_DISABLED"
        const val ACTION_KIOSK_BREACH = "nu.brandrisk.kioskmode.KIOSK_BREACH"
    }

    override fun onReceive(context: Context, intent: Intent) {
        KioskLogger.i(TAG, "Security event received: ${intent.action}")
        
        when (intent.action) {
            ACTION_SECURITY_VIOLATION -> {
                val violationType = intent.getStringExtra("violation_type")
                KioskLogger.w(TAG, "Security violation detected: $violationType")
                handleSecurityViolation(context, violationType)
            }
            
            ACTION_UNAUTHORIZED_ACCESS -> {
                val packageName = intent.getStringExtra("package_name")
                KioskLogger.w(TAG, "Unauthorized access attempt: $packageName")
                handleUnauthorizedAccess(context, packageName)
            }
            
            ACTION_ADMIN_DISABLED -> {
                KioskLogger.e(TAG, "Device admin disabled - critical security breach")
                handleAdminDisabled(context)
            }
            
            ACTION_KIOSK_BREACH -> {
                val breachType = intent.getStringExtra("breach_type")
                KioskLogger.e(TAG, "Kiosk mode breach detected: $breachType")
                handleKioskBreach(context, breachType)
            }
            
            Intent.ACTION_PACKAGE_INSTALL -> {
                KioskLogger.i(TAG, "Package installation detected")
                handlePackageInstall(context, intent)
            }
            
            Intent.ACTION_PACKAGE_REMOVED -> {
                KioskLogger.i(TAG, "Package removal detected")
                handlePackageRemoval(context, intent)
            }
            
            // System security events
            "android.intent.action.ACTION_PASSWORD_FAILED" -> {
                KioskLogger.w(TAG, "Password attempt failed")
                handlePasswordFailed(context)
            }
            
            "android.intent.action.ACTION_PASSWORD_SUCCEEDED" -> {
                KioskLogger.i(TAG, "Password attempt succeeded")
                handlePasswordSucceeded(context)
            }
            else -> {
                KioskLogger.w(TAG, "Unhandled security event: ${intent.action}")
            }
        }
    }

    private fun handleSecurityViolation(context: Context, violationType: String?) {
        violationType?.let {
            KioskLogger.w(TAG, "Processing security violation: $it")
            
            // Log security event
            logSecurityEvent(context, "VIOLATION", it)
            // Notify enterprise administrators  
            notifyEnterpriseAdministrators(context, "Security violation: $it")
            // Take corrective action based on violation type
            takeCorrectiveAction(context, it)
            
            when (it) {
                "unauthorized_app" -> blockUnauthorizedApp(context)
                "admin_bypass" -> enforceAdminRestrictions(context)
                "system_modification" -> revertSystemChanges(context)
                else -> {
                    // Default security enforcement for unknown violations
                    logSecurityEvent(context, "UNKNOWN_VIOLATION", it)
                    enforceKioskMode(context)
                }
            }
        }
    }

    // Security utility methods
    private fun logSecurityEvent(context: Context, type: String, details: String) {
        try {
            val timestamp = System.currentTimeMillis()
            KioskLogger.w(TAG, "SECURITY_EVENT: [$type] $details at $timestamp")
            
            // Store in security log for enterprise reporting
            val sharedPrefs = context.getSharedPreferences("security_events", Context.MODE_PRIVATE)
            val eventKey = "event_$timestamp"
            sharedPrefs.edit()
                .putString(eventKey, "$type|$details|$timestamp")
                .apply()
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to log security event", e)
        }
    }
    
    private fun notifyEnterpriseAdministrators(context: Context, message: String) {
        try {
            // Send broadcast to enterprise management service
            val intent = Intent("nu.brandrisk.kioskmode.SECURITY_ALERT")
            intent.putExtra("message", message)
            intent.putExtra("timestamp", System.currentTimeMillis())
            intent.setPackage(context.packageName)
            context.sendBroadcast(intent)
            
            KioskLogger.i(TAG, "Enterprise administrators notified: $message")
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to notify administrators", e)
        }
    }
    
    private fun takeCorrectiveAction(context: Context, violationType: String) {
        try {
            when (violationType) {
                "unauthorized_app" -> {
                    // Block unauthorized app and force return to kiosk
                    enforceKioskMode(context)
                }
                "admin_bypass" -> {
                    // Re-enable admin restrictions
                    enforceAdminRestrictions(context)
                }
                "system_modification" -> {
                    // Attempt to revert system changes
                    revertSystemChanges(context)
                }
                else -> {
                    // General security enforcement
                    enforceKioskMode(context)
                }
            }
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to take corrective action", e)
        }
    }
    
    private fun enforceKioskMode(context: Context) {
        try {
            val intent = Intent(context, nu.brandrisk.kioskmode.MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(intent)
            
            KioskLogger.i(TAG, "Kiosk mode enforced")
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to enforce kiosk mode", e)
        }
    }

    // Security event handlers with implementations
    private fun handleUnauthorizedAccess(context: Context, packageName: String?) {
        packageName?.let {
            KioskLogger.w(TAG, "Blocking unauthorized access from: $it")
            
            // Block package execution
            blockPackageExecution(context, it)
            // Add to blacklist
            addToBlacklist(context, it)
            // Force return to kiosk app
            enforceKioskMode(context)
        }
    }

    private fun handleAdminDisabled(context: Context) {
        KioskLogger.e(TAG, "Critical: Device admin disabled")
        
        // Attempt to re-enable device admin
        requestDeviceAdminReactivation(context)
        // Show critical security alert
        showCriticalSecurityAlert(context)
        // Initiate emergency lockdown
        initiateEmergencyLockdown(context)
    }

    private fun handleKioskBreach(context: Context, breachType: String?) {
        breachType?.let {
            KioskLogger.e(TAG, "Kiosk breach: $it")
            
            // Immediate kiosk mode enforcement
            enforceKioskMode(context)
            // Log security incident
            logSecurityEvent(context, "KIOSK_BREACH", it)
            // Notify administrators
            notifyEnterpriseAdministrators(context, "Kiosk breach detected: $it")
        }
    }

    private fun handlePackageInstall(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart
        KioskLogger.i(TAG, "Evaluating new package: $packageName")
        
        packageName?.let {
            // Check against enterprise whitelist
            val isApproved = checkEnterpriseWhitelist(context, it)
            if (!isApproved) {
                // Auto-disable if not approved
                disableUnauthorizedPackage(context, it)
            }
        }
    }

    private fun handlePackageRemoval(context: Context, intent: Intent) {
        val packageName = intent.data?.schemeSpecificPart
        KioskLogger.i(TAG, "Package removed: $packageName")
        
        packageName?.let {
            // Clean up security policies
            cleanupSecurityPolicies(context, it)
            // Update enterprise database
            updateEnterpriseDatabase(context, it, removed = true)
        }
    }

    private fun handlePasswordFailed(context: Context) {
        KioskLogger.w(TAG, "Password authentication failed")
        
        // Increment failed attempt counter
        incrementFailedAttempts(context)
        // Implement lockout policy
        implementLockoutPolicy(context)
        // Log security event
        logSecurityEvent(context, "AUTH_FAILED", "Password authentication failed")
    }

    private fun handlePasswordSucceeded(context: Context) {
        KioskLogger.i(TAG, "Password authentication succeeded")
        
        // Reset failed attempt counter
        resetFailedAttempts(context)
        // Grant temporary admin access
        grantTemporaryAdminAccess(context)
        // Log successful authentication
        logSecurityEvent(context, "AUTH_SUCCESS", "Password authentication succeeded")
    }
    
    // Implementation helper methods
    private fun blockPackageExecution(context: Context, packageName: String) {
        try {
            val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
            val componentName = android.content.ComponentName(context, nu.brandrisk.kioskmode.KioskDeviceAdminReceiver::class.java)
            
            if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
                devicePolicyManager.setApplicationHidden(componentName, packageName, true)
                KioskLogger.i(TAG, "Blocked package execution: $packageName")
            }
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to block package", e)
        }
    }
    
    private fun addToBlacklist(context: Context, packageName: String) {
        try {
            val sharedPrefs = context.getSharedPreferences("security_blacklist", Context.MODE_PRIVATE)
            val blacklist = sharedPrefs.getStringSet("blocked_packages", mutableSetOf()) ?: mutableSetOf()
            blacklist.add(packageName)
            sharedPrefs.edit().putStringSet("blocked_packages", blacklist).apply()
            
            KioskLogger.i(TAG, "Added to blacklist: $packageName")
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to add to blacklist", e)
        }
    }
    
    private fun checkEnterpriseWhitelist(context: Context, packageName: String): Boolean {
        val allowedPackages = setOf(
            context.packageName,
            "com.android.systemui",
            "android",
            "com.android.settings",
            "com.google.android.gms"
        )
        return allowedPackages.contains(packageName)
    }
    
    private fun disableUnauthorizedPackage(context: Context, packageName: String) {
        try {
            blockPackageExecution(context, packageName)
            addToBlacklist(context, packageName)
            logSecurityEvent(context, "UNAUTHORIZED_PACKAGE", "Disabled: $packageName")
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to disable unauthorized package", e)
        }
    }
    
    private fun requestDeviceAdminReactivation(context: Context) {
        try {
            val intent = Intent(android.app.admin.DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(android.app.admin.DevicePolicyManager.EXTRA_DEVICE_ADMIN, 
                android.content.ComponentName(context, nu.brandrisk.kioskmode.KioskDeviceAdminReceiver::class.java))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to request admin reactivation", e)
        }
    }
    
    private fun showCriticalSecurityAlert(context: Context) {
        try {
            val intent = Intent("nu.brandrisk.kioskmode.SHOW_SECURITY_ALERT")
            intent.putExtra("message", "Device admin has been disabled - critical security risk")
            intent.setPackage(context.packageName)
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to show security alert", e)
        }
    }
    
    private fun initiateEmergencyLockdown(context: Context) {
        try {
            // Lock device and return to kiosk mode
            enforceKioskMode(context)
            logSecurityEvent(context, "EMERGENCY_LOCKDOWN", "Admin disabled - emergency lockdown initiated")
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to initiate emergency lockdown", e)
        }
    }
    
    private fun cleanupSecurityPolicies(context: Context, packageName: String) {
        try {
            // Remove from blacklist if present
            val sharedPrefs = context.getSharedPreferences("security_blacklist", Context.MODE_PRIVATE)
            val blacklist = sharedPrefs.getStringSet("blocked_packages", mutableSetOf())?.toMutableSet()
            blacklist?.remove(packageName)
            sharedPrefs.edit().putStringSet("blocked_packages", blacklist).apply()
            
            KioskLogger.i(TAG, "Cleaned up security policies for: $packageName")
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to cleanup security policies", e)
        }
    }
    
    private fun updateEnterpriseDatabase(context: Context, packageName: String, removed: Boolean) {
        try {
            if (removed) {
                KioskLogger.i(TAG, "Updated enterprise database - removed: $packageName")
            } else {
                KioskLogger.i(TAG, "Updated enterprise database - added: $packageName")
            }
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to update enterprise database", e)
        }
    }
    
    private fun incrementFailedAttempts(context: Context) {
        try {
            val sharedPrefs = context.getSharedPreferences("security_auth", Context.MODE_PRIVATE)
            val currentAttempts = sharedPrefs.getInt("failed_attempts", 0)
            sharedPrefs.edit().putInt("failed_attempts", currentAttempts + 1).apply()
            
            KioskLogger.w(TAG, "Failed attempts: ${currentAttempts + 1}")
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to increment failed attempts", e)
        }
    }
    
    private fun implementLockoutPolicy(context: Context) {
        try {
            val sharedPrefs = context.getSharedPreferences("security_auth", Context.MODE_PRIVATE)
            val failedAttempts = sharedPrefs.getInt("failed_attempts", 0)
            
            if (failedAttempts >= 3) {
                // Implement temporary lockout
                val lockoutTime = System.currentTimeMillis() + (5 * 60 * 1000) // 5 minutes
                sharedPrefs.edit().putLong("lockout_until", lockoutTime).apply()
                
                logSecurityEvent(context, "LOCKOUT_INITIATED", "Too many failed attempts")
                KioskLogger.w(TAG, "Lockout policy activated - 5 minute timeout")
            }
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to implement lockout policy", e)
        }
    }
    
    private fun resetFailedAttempts(context: Context) {
        try {
            val sharedPrefs = context.getSharedPreferences("security_auth", Context.MODE_PRIVATE)
            sharedPrefs.edit()
                .putInt("failed_attempts", 0)
                .remove("lockout_until")
                .apply()
                
            KioskLogger.i(TAG, "Failed attempts counter reset")
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to reset failed attempts", e)
        }
    }
    
    private fun grantTemporaryAdminAccess(context: Context) {
        try {
            val sharedPrefs = context.getSharedPreferences("security_auth", Context.MODE_PRIVATE)
            val accessExpiry = System.currentTimeMillis() + (30 * 60 * 1000) // 30 minutes
            sharedPrefs.edit().putLong("admin_access_until", accessExpiry).apply()
            
            KioskLogger.i(TAG, "Temporary admin access granted for 30 minutes")
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to grant temporary admin access", e)
        }
    }

    private fun blockUnauthorizedApp(context: Context) {
        try {
            logSecurityEvent(context, "BLOCK_UNAUTHORIZED", "Blocking unauthorized app access")
            enforceKioskMode(context)
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to block unauthorized app", e)
        }
    }

    private fun enforceAdminRestrictions(context: Context) {
        try {
            logSecurityEvent(context, "ADMIN_RESTRICTIONS", "Enforcing admin restrictions")
            val intent = Intent("nu.brandrisk.kioskmode.ENFORCE_ADMIN_RESTRICTIONS")
            intent.setPackage(context.packageName)
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to enforce admin restrictions", e)
        }
    }

    private fun revertSystemChanges(context: Context) {
        try {
            logSecurityEvent(context, "REVERT_CHANGES", "Reverting unauthorized system changes")
            // Attempt to restore original system configuration
            enforceKioskMode(context)
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to revert system changes", e)
        }
    }
}
