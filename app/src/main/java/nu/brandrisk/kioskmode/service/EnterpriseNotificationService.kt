package nu.brandrisk.kioskmode.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.NotificationManager
import android.content.Intent
import nu.brandrisk.kioskmode.util.KioskLogger
import kotlinx.coroutines.*

/**
 * Enterprise Notification Control Service
 * Complete notification management like SureLock
 */
class EnterpriseNotificationService : NotificationListenerService() {
    
    companion object {
        private const val TAG = "EnterpriseNotificationService"
        
        // Allowed notification packages (minimal set like SureLock)
        private val ALLOWED_NOTIFICATION_PACKAGES = setOf(
            "android", // System notifications
            "com.android.systemui" // Essential system UI
        )
        
        // Critical system notifications that should be hidden but not blocked
        private val SYSTEM_NOTIFICATIONS = setOf(
            "android.media.VOLUME_CHANGED_ACTION",
            "android.intent.action.BATTERY_LOW"
        )
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isKioskModeActive = false
    private var allowedPackages = ALLOWED_NOTIFICATION_PACKAGES.toMutableSet()
    
    override fun onCreate() {
        super.onCreate()
        KioskLogger.i(TAG, "Enterprise Notification Service created")
    }
    
    override fun onListenerConnected() {
        super.onListenerConnected()
        KioskLogger.i(TAG, "Notification listener connected")
        
        // Enable kiosk mode by default
        enableKioskMode()
        
        // Clear existing notifications
        clearAllNotifications()
    }
    
    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        KioskLogger.w(TAG, "Notification listener disconnected")
    }
    
    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (!isKioskModeActive || sbn == null) return
        
        val packageName = sbn.packageName
        val notificationKey = sbn.key
        
        KioskLogger.d(TAG, "Notification posted from: $packageName")
        
        serviceScope.launch {
            try {
                if (!isNotificationAllowed(packageName)) {
                    KioskLogger.i(TAG, "Blocking notification from: $packageName")
                    cancelNotification(notificationKey)
                } else {
                    KioskLogger.d(TAG, "Allowing notification from: $packageName")
                }
            } catch (e: Exception) {
                KioskLogger.e(TAG, "Error handling notification", e)
            }
        }
    }
    
    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        if (sbn == null) return
        KioskLogger.d(TAG, "Notification removed from: ${sbn.packageName}")
    }
    
    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
        KioskLogger.i(TAG, "Enterprise Notification Service destroyed")
    }
    
    private fun isNotificationAllowed(packageName: String): Boolean {
        // Allow notifications from kiosk app itself
        if (packageName == this.packageName) return true
        
        // Check against allowed packages
        return allowedPackages.contains(packageName)
    }
    
    private fun clearAllNotifications() {
        serviceScope.launch {
            try {
                val activeNotifications = activeNotifications
                activeNotifications?.forEach { notification ->
                    if (!isNotificationAllowed(notification.packageName)) {
                        cancelNotification(notification.key)
                    }
                }
                KioskLogger.i(TAG, "Cleared ${activeNotifications?.size ?: 0} notifications")
            } catch (e: Exception) {
                KioskLogger.e(TAG, "Error clearing notifications", e)
            }
        }
    }
    
    /**
     * Enable kiosk mode - block all unauthorized notifications
     */
    fun enableKioskMode() {
        isKioskModeActive = true
        KioskLogger.i(TAG, "Notification kiosk mode enabled")
        
        // Clear all existing unauthorized notifications
        clearAllNotifications()
    }
    
    /**
     * Disable kiosk mode - allow all notifications
     */
    fun disableKioskMode() {
        isKioskModeActive = false
        KioskLogger.i(TAG, "Notification kiosk mode disabled")
    }
    
    /**
     * Add package to allowed notifications list
     */
    fun addAllowedPackage(packageName: String) {
        allowedPackages.add(packageName)
        KioskLogger.i(TAG, "Added allowed notification package: $packageName")
    }
    
    /**
     * Remove package from allowed notifications list
     */
    fun removeAllowedPackage(packageName: String) {
        allowedPackages.remove(packageName)
        KioskLogger.i(TAG, "Removed allowed notification package: $packageName")
        
        // Cancel any existing notifications from this package
        serviceScope.launch {
            val activeNotifications = activeNotifications
            activeNotifications?.forEach { notification ->
                if (notification.packageName == packageName) {
                    cancelNotification(notification.key)
                }
            }
        }
    }
    
    /**
     * Get current notification statistics
     */
    fun getNotificationStats(): NotificationStats {
        val activeNotifications = activeNotifications
        val total = activeNotifications?.size ?: 0
        val blocked = activeNotifications?.count { !isNotificationAllowed(it.packageName) } ?: 0
        val allowed = total - blocked
        
        return NotificationStats(
            totalNotifications = total,
            allowedNotifications = allowed,
            blockedNotifications = blocked,
            allowedPackages = allowedPackages.toSet()
        )
    }
    
    /**
     * Force clear all notifications (emergency function)
     */
    fun emergencyClearAll() {
        serviceScope.launch {
            try {
                cancelAllNotifications()
                KioskLogger.w(TAG, "Emergency: Cleared all notifications")
            } catch (e: Exception) {
                KioskLogger.e(TAG, "Error in emergency clear", e)
            }
        }
    }
}

/**
 * Notification statistics data class
 */
data class NotificationStats(
    val totalNotifications: Int,
    val allowedNotifications: Int,
    val blockedNotifications: Int,
    val allowedPackages: Set<String>
)
