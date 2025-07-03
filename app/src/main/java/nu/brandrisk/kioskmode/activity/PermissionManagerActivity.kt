package nu.brandrisk.kioskmode.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import nu.brandrisk.kioskmode.util.KioskLogger

/**
 * Permission Manager Activity for enterprise kiosk mode
 * Handles system permission requests and grants
 */
class PermissionManagerActivity : Activity() {
    
    companion object {
        private const val TAG = "PermissionManagerActivity"
        private const val REQUEST_CODE_PERMISSIONS = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        KioskLogger.i(TAG, "Permission Manager activity created")
        
        // Handle permission requests based on intent extras
        val permissionType = intent.getStringExtra("permission_type")
        
        when (permissionType) {
            "device_admin" -> requestDeviceAdminPermission()
            "accessibility" -> requestAccessibilityPermission()
            "system_alert" -> requestSystemAlertPermission()
            "usage_stats" -> requestUsageStatsPermission()
            "notification_access" -> requestNotificationAccessPermission()
            else -> {
                KioskLogger.w(TAG, "Unknown permission type: $permissionType")
                finish()
            }
        }
    }
    
    private fun requestDeviceAdminPermission() {
        try {
            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
            startActivityForResult(intent, REQUEST_CODE_PERMISSIONS)
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to open device admin settings", e)
            finish()
        }
    }
    
    private fun requestAccessibilityPermission() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivityForResult(intent, REQUEST_CODE_PERMISSIONS)
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to open accessibility settings", e)
            finish()
        }
    }
    
    private fun requestSystemAlertPermission() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:$packageName")
            startActivityForResult(intent, REQUEST_CODE_PERMISSIONS)
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to open overlay permission settings", e)
            finish()
        }
    }
    
    private fun requestUsageStatsPermission() {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivityForResult(intent, REQUEST_CODE_PERMISSIONS)
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to open usage stats settings", e)
            finish()
        }
    }
    
    private fun requestNotificationAccessPermission() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivityForResult(intent, REQUEST_CODE_PERMISSIONS)
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Failed to open notification access settings", e)
            finish()
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            KioskLogger.i(TAG, "Permission request completed with result: $resultCode")
            setResult(resultCode)
            finish()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        KioskLogger.i(TAG, "Permission Manager activity destroyed")
    }
}
