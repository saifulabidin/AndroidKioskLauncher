package nu.brandrisk.kioskmode.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.content.ComponentName
import android.content.pm.PackageManager
import nu.brandrisk.kioskmode.util.KioskLogger
import kotlinx.coroutines.*

/**
 * Enterprise Accessibility Service - System-level control
 * Inspired by SureLock's accessibility-based enforcement
 */
class EnterpriseAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "EnterpriseAccessibilityService"
        
        // Blocked system components (like SureLock)
        private val BLOCKED_COMPONENTS = setOf(
            "com.android.systemui/.recents.RecentsActivity",
            "com.android.settings",
            "com.google.android.gms",
            "com.android.vending"
        )
        
        // System UI elements to block
        private val BLOCKED_UI_ELEMENTS = setOf(
            "Recent apps",
            "Settings",
            "Notifications",
            "Quick settings"
        )
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var isKioskModeActive = false
    private var allowedPackages = setOf<String>()
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        KioskLogger.i(TAG, "Enterprise Accessibility Service connected")
        
        configureAccessibilityService()
        loadKioskConfiguration()
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (!isKioskModeActive || event == null) return
        
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                handleWindowStateChange(event)
            }
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED -> {
                handleWindowContentChange(event)
            }
            AccessibilityEvent.TYPE_VIEW_CLICKED -> {
                handleViewClick(event)
            }
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                handleNotificationChange(event)
            }
        }
    }
    
    override fun onInterrupt() {
        KioskLogger.w(TAG, "Accessibility service interrupted")
    }
    
    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
    
    private fun configureAccessibilityService() {
        val info = AccessibilityServiceInfo().apply {
            // Configure service like SureLock
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                    AccessibilityEvent.TYPE_VIEW_CLICKED or
                    AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
            
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_REQUEST_ENHANCED_WEB_ACCESSIBILITY
            
            notificationTimeout = 100
        }
        
        serviceInfo = info
    }
    
    private fun loadKioskConfiguration() {
        // Load allowed packages from configuration
        allowedPackages = setOf(
            packageName,
            "com.android.systemui" // Allow some system components
        )
        
        isKioskModeActive = true
        KioskLogger.i(TAG, "Kiosk mode activated with ${allowedPackages.size} allowed packages")
    }
    
    private fun handleWindowStateChange(event: AccessibilityEvent) {
        val packageName = event.packageName?.toString()
        val className = event.className?.toString()
        
        KioskLogger.d(TAG, "Window state changed: $packageName - $className")
        
        if (packageName != null && !isPackageAllowed(packageName)) {
            KioskLogger.w(TAG, "Blocking unauthorized app: $packageName")
            blockUnauthorizedApp()
        }
        
        // Block specific system components
        if (className != null && isBlockedComponent(className)) {
            KioskLogger.w(TAG, "Blocking system component: $className")
            blockSystemComponent()
        }
    }
    
    private fun handleWindowContentChange(event: AccessibilityEvent) {
        // Monitor for system UI changes that might indicate escape attempts
        val source = event.source
        if (source != null) {
            checkForSystemUIElements(source)
        }
    }
    
    private fun handleViewClick(event: AccessibilityEvent) {
        val source = event.source
        if (source != null) {
            val viewText = source.text?.toString()
            val contentDescription = source.contentDescription?.toString()
            
            // Block clicks on system UI elements
            if (isBlockedUIElement(viewText) || isBlockedUIElement(contentDescription)) {
                KioskLogger.w(TAG, "Blocking system UI interaction")
                blockSystemUIInteraction()
            }
        }
    }
    
    private fun handleNotificationChange(event: AccessibilityEvent) {
        // Block all notifications in kiosk mode (like SureLock)
        if (isKioskModeActive) {
            KioskLogger.d(TAG, "Blocking notification")
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }
    
    private fun isPackageAllowed(packageName: String): Boolean {
        return allowedPackages.contains(packageName) || 
               packageName.startsWith("android") ||
               packageName == this.packageName
    }
    
    private fun isBlockedComponent(className: String): Boolean {
        return BLOCKED_COMPONENTS.any { className.contains(it) }
    }
    
    private fun isBlockedUIElement(text: String?): Boolean {
        if (text.isNullOrEmpty()) return false
        return BLOCKED_UI_ELEMENTS.any { text.contains(it, ignoreCase = true) }
    }
    
    private fun blockUnauthorizedApp() {
        serviceScope.launch {
            try {
                // Multiple strategies to return to kiosk app
                performGlobalAction(GLOBAL_ACTION_HOME)
                delay(100)
                
                // If that fails, force launch our app
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                intent?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(this)
                }
                
                KioskLogger.i(TAG, "Redirected to kiosk app")
            } catch (e: Exception) {
                KioskLogger.e(TAG, "Failed to block unauthorized app", e)
            }
        }
    }
    
    private fun blockSystemComponent() {
        serviceScope.launch {
            try {
                // Press back to exit system component
                performGlobalAction(GLOBAL_ACTION_BACK)
                delay(50)
                performGlobalAction(GLOBAL_ACTION_HOME)
                
                KioskLogger.i(TAG, "Blocked system component")
            } catch (e: Exception) {
                KioskLogger.e(TAG, "Failed to block system component", e)
            }
        }
    }
    
    private fun blockSystemUIInteraction() {
        serviceScope.launch {
            try {
                // Immediate back action to cancel interaction
                performGlobalAction(GLOBAL_ACTION_BACK)
                KioskLogger.i(TAG, "Blocked system UI interaction")
            } catch (e: Exception) {
                KioskLogger.e(TAG, "Failed to block system UI interaction", e)
            }
        }
    }
    
    private fun checkForSystemUIElements(node: AccessibilityNodeInfo) {
        try {
            // Recursively check for system UI elements
            for (i in 0 until node.childCount) {
                val child = node.getChild(i)
                if (child != null) {
                    val text = child.text?.toString()
                    val contentDesc = child.contentDescription?.toString()
                    
                    if (isBlockedUIElement(text) || isBlockedUIElement(contentDesc)) {
                        // Hide or disable the element
                        child.performAction(AccessibilityNodeInfo.ACTION_DISMISS)
                    }
                    
                    checkForSystemUIElements(child)
                    child.recycle()
                }
            }
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Error checking system UI elements", e)
        }
    }
    
    /**
     * Enable kiosk mode enforcement
     */
    fun enableKioskMode(allowedPackagesList: Set<String>) {
        allowedPackages = allowedPackagesList
        isKioskModeActive = true
        KioskLogger.i(TAG, "Kiosk mode enabled via accessibility service")
    }
    
    /**
     * Disable kiosk mode enforcement
     */
    fun disableKioskMode() {
        isKioskModeActive = false
        KioskLogger.i(TAG, "Kiosk mode disabled via accessibility service")
    }
}
