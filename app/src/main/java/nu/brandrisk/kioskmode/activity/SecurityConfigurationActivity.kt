package nu.brandrisk.kioskmode.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import nu.brandrisk.kioskmode.KioskDeviceAdminReceiver
import nu.brandrisk.kioskmode.util.KioskLogger

/**
 * Security Configuration Activity for enterprise kiosk mode
 * Handles device admin setup and security policy configuration
 */
class SecurityConfigurationActivity : Activity() {
    
    companion object {
        private const val TAG = "SecurityConfigurationActivity"
        private const val REQUEST_ENABLE_ADMIN = 1
    }
    
    private lateinit var devicePolicyManager: DevicePolicyManager
    private lateinit var adminComponent: ComponentName
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        KioskLogger.i(TAG, "Security Configuration activity created")
        
        devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, KioskDeviceAdminReceiver::class.java)
        
        // Prevent force close if intent or action is null
        val action = intent?.action
        if (action == null) {
            KioskLogger.e(TAG, "No action provided to SecurityConfigurationActivity")
            finish()
            return
        }

        // Handle security configuration based on intent action
        when (action) {
            "ENABLE_DEVICE_ADMIN" -> enableDeviceAdmin()
            "CONFIGURE_SECURITY_POLICIES" -> configureSecurityPolicies()
            "SETUP_KIOSK_MODE" -> setupKioskMode()
            else -> {
                KioskLogger.w(TAG, "Unknown action: $action")
                finish()
            }
        }
    }
    
    private fun enableDeviceAdmin() {
        if (!devicePolicyManager.isAdminActive(adminComponent)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Enable device administrator to use enterprise kiosk features"
            )
            
            try {
                startActivityForResult(intent, REQUEST_ENABLE_ADMIN)
            } catch (e: Exception) {
                KioskLogger.e(TAG, "Failed to start device admin request", e)
                finish()
            }
        } else {
            KioskLogger.i(TAG, "Device admin already enabled")
            setResult(RESULT_OK)
            finish()
        }
    }
    
    @SuppressLint("NewApi", "MissingPermission", "DeprecatedDeviceAdmin")
    private fun configureSecurityPolicies() {
        if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
            try {
                // Configure enterprise security policies
                KioskLogger.i(TAG, "Configuring enterprise security policies")
                
                // Set password policies
                devicePolicyManager.setPasswordQuality(
                    adminComponent,
                    DevicePolicyManager.PASSWORD_QUALITY_NUMERIC
                )
                
                // Set camera disabled if required
                val disableCamera = intent.getBooleanExtra("disable_camera", false)
                if (disableCamera) {
                    devicePolicyManager.setCameraDisabled(adminComponent, true)
                }
                
                // Set screen capture disabled
                devicePolicyManager.setScreenCaptureDisabled(adminComponent, true)
                
                KioskLogger.i(TAG, "Security policies configured successfully")
                setResult(RESULT_OK)
                
            } catch (e: Exception) {
                KioskLogger.e(TAG, "Failed to configure security policies", e)
                setResult(RESULT_CANCELED)
            }
        } else {
            KioskLogger.w(TAG, "App is not device owner, cannot configure policies")
            setResult(RESULT_CANCELED)
        }
        
        finish()
    }
    
    private fun setupKioskMode() {
        if (devicePolicyManager.isDeviceOwnerApp(packageName)) {
            try {
                // Setup kiosk mode restrictions
                KioskLogger.i(TAG, "Setting up kiosk mode")
                
                // Enable lock task mode for kiosk
                val packageNames = arrayOf(packageName)
                devicePolicyManager.setLockTaskPackages(adminComponent, packageNames)
                
                // Set user restrictions
                devicePolicyManager.addUserRestriction(adminComponent, android.os.UserManager.DISALLOW_SAFE_BOOT)
                devicePolicyManager.addUserRestriction(adminComponent, android.os.UserManager.DISALLOW_FACTORY_RESET)
                devicePolicyManager.addUserRestriction(adminComponent, android.os.UserManager.DISALLOW_ADD_USER)
                
                KioskLogger.i(TAG, "Kiosk mode setup completed")
                setResult(RESULT_OK)
                
            } catch (e: Exception) {
                KioskLogger.e(TAG, "Failed to setup kiosk mode", e)
                setResult(RESULT_CANCELED)
            }
        } else {
            KioskLogger.w(TAG, "Device owner required for kiosk mode setup")
            setResult(RESULT_CANCELED)
        }
        
        finish()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            REQUEST_ENABLE_ADMIN -> {
                if (resultCode == RESULT_OK) {
                    KioskLogger.i(TAG, "Device admin enabled successfully")
                    setResult(RESULT_OK)
                } else {
                    KioskLogger.w(TAG, "Device admin was not enabled")
                    setResult(RESULT_CANCELED)
                }
                finish()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        KioskLogger.i(TAG, "Security Configuration activity destroyed")
    }
}
