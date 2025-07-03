package nu.brandrisk.kioskmode.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import android.content.Context
import android.os.Build
import nu.brandrisk.kioskmode.R
import nu.brandrisk.kioskmode.MainActivity
import nu.brandrisk.kioskmode.util.KioskLogger
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.app.ActivityManager
import android.content.ComponentName
import android.content.pm.PackageManager
import kotlinx.coroutines.*

/**
 * Enterprise Kiosk Service - Core background enforcement service
 * Inspired by SureLock's enterprise architecture
 */
class EnterpriseKioskService : Service() {
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "enterprise_kiosk_channel"
        private const val TAG = "EnterpriseKioskService"
        
        // Enterprise monitoring intervals
        private const val MONITORING_INTERVAL = 1000L // 1 second
        private const val SECURITY_CHECK_INTERVAL = 5000L // 5 seconds
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var isMonitoring = false
    private var isEnforcing = false
    
    // Enterprise security components
    private lateinit var securityMonitor: SecurityMonitor
    private lateinit var kioskEnforcer: KioskEnforcer
    private lateinit var systemUIController: SystemUIController
    
    override fun onCreate() {
        super.onCreate()
        KioskLogger.i(TAG, "Enterprise Kiosk Service starting...")
        
        initializeEnterpriseComponents()
        createNotificationChannel()
        startForegroundService()
        
        // Register system event receivers
        registerSystemReceivers()
        
        KioskLogger.i(TAG, "Enterprise Kiosk Service initialized")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        KioskLogger.i(TAG, "Service command received: ${intent?.action}")
        
        when (intent?.action) {
            "START_MONITORING" -> startEnterpriseMonitoring()
            "STOP_MONITORING" -> stopEnterpriseMonitoring()
            "ENFORCE_KIOSK" -> enforceKioskMode()
            "DISABLE_KIOSK" -> disableKioskMode()
            "SECURITY_CHECK" -> {
                CoroutineScope(Dispatchers.Default).launch {
                    performSecurityCheck()
                }
            }
            else -> startEnterpriseMonitoring()
        }
        
        return START_STICKY // Enterprise service must restart if killed
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onDestroy() {
        KioskLogger.i(TAG, "Enterprise Kiosk Service destroying...")
        
        stopEnterpriseMonitoring()
        unregisterSystemReceivers()
        serviceScope.cancel()
        
        // Enterprise services should auto-restart
        restartService()
        
        super.onDestroy()
    }
    
    private fun initializeEnterpriseComponents() {
        securityMonitor = SecurityMonitorImpl(this)
        kioskEnforcer = KioskEnforcerImpl(this)
        systemUIController = SystemUIControllerImpl(this)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Enterprise Kiosk Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Enterprise kiosk mode enforcement service"
                setShowBadge(false)
                setSound(null, null)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun startForegroundService() {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Enterprise Kiosk Active")
            .setContentText("Kiosk mode is enforced and monitoring")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        startForeground(NOTIFICATION_ID, notification)
    }
    
    private fun startEnterpriseMonitoring() {
        if (isMonitoring) return
        
        isMonitoring = true
        KioskLogger.i(TAG, "Starting enterprise monitoring...")
        
        // Start continuous monitoring coroutines
        serviceScope.launch {
            while (isMonitoring) {
                try {
                    monitorActiveApplications()
                    enforceKioskRestrictions()
                    delay(MONITORING_INTERVAL)
                } catch (e: Exception) {
                    KioskLogger.e(TAG, "Monitoring error", e)
                    delay(MONITORING_INTERVAL * 2) // Back off on error
                }
            }
        }
        
        // Start security checks
        serviceScope.launch {
            while (isMonitoring) {
                try {
                    performSecurityCheck()
                    delay(SECURITY_CHECK_INTERVAL)
                } catch (e: Exception) {
                    KioskLogger.e(TAG, "Security check error", e)
                }
            }
        }
    }
    
    private fun stopEnterpriseMonitoring() {
        isMonitoring = false
        isEnforcing = false
        KioskLogger.i(TAG, "Stopped enterprise monitoring")
    }
    
    private fun enforceKioskMode() {
        if (isEnforcing) return
        
        isEnforcing = true
        KioskLogger.i(TAG, "Enforcing kiosk mode...")
        
        serviceScope.launch {
            try {
                // Enable kiosk restrictions
                kioskEnforcer.enableKioskMode()
                systemUIController.hideSystemUI()
                securityMonitor.enableSecurityMode()
                
                KioskLogger.i(TAG, "Kiosk mode enforced successfully")
            } catch (e: Exception) {
                KioskLogger.e(TAG, "Failed to enforce kiosk mode", e)
            }
        }
    }
    
    private fun disableKioskMode() {
        isEnforcing = false
        KioskLogger.i(TAG, "Disabling kiosk mode...")
        
        serviceScope.launch {
            try {
                kioskEnforcer.disableKioskMode()
                systemUIController.showSystemUI()
                securityMonitor.disableSecurityMode()
                
                KioskLogger.i(TAG, "Kiosk mode disabled successfully")
            } catch (e: Exception) {
                KioskLogger.e(TAG, "Failed to disable kiosk mode", e)
            }
        }
    }
    
    private suspend fun monitorActiveApplications() {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTasks = activityManager.getRunningTasks(1)
        
        if (runningTasks.isNotEmpty()) {
            val topActivity = runningTasks[0].topActivity
            
            if (topActivity != null && !isAllowedApplication(topActivity)) {
                KioskLogger.w(TAG, "Unauthorized app detected: ${topActivity.packageName}")
                kioskEnforcer.redirectToKioskApp()
            }
        }
    }
    
    private fun isAllowedApplication(componentName: ComponentName): Boolean {
        // Check against allowed applications list
        val allowedPackages = setOf(
            packageName, // Our kiosk app
            "com.android.systemui",
            "android"
        )
        
        return allowedPackages.contains(componentName.packageName)
    }
    
    private suspend fun enforceKioskRestrictions() {
        if (!isEnforcing) return
        
        // Disable unauthorized apps
        kioskEnforcer.disableUnauthorizedApps()
        
        // Control system UI
        systemUIController.enforceUIRestrictions()
        
        // Monitor security status
        securityMonitor.checkSecurityStatus()
    }
    
    private suspend fun performSecurityCheck() {
        try {
            val securityStatus = securityMonitor.performComprehensiveCheck()
            
            if (!securityStatus.isSecure) {
                KioskLogger.w(TAG, "Security violation detected: ${securityStatus.violations}")
                handleSecurityViolation(securityStatus)
            }
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Security check failed", e)
        }
    }
    
    private fun handleSecurityViolation(securityStatus: SecurityStatus) {
        serviceScope.launch {
            // Take immediate action on security violations
            when (securityStatus.severity) {
                SecuritySeverity.CRITICAL -> {
                    // Lock device or wipe data
                    securityMonitor.handleCriticalViolation()
                }
                SecuritySeverity.HIGH -> {
                    // Force return to kiosk
                    kioskEnforcer.forceKioskMode()
                }
                SecuritySeverity.MEDIUM -> {
                    // Log and monitor
                    KioskLogger.w(TAG, "Medium security violation logged")
                }
                SecuritySeverity.LOW -> {
                    // Monitor only
                    KioskLogger.i(TAG, "Low security event logged")
                }
            }
        }
    }
    
    private fun registerSystemReceivers() {
        // Register for system events that might affect kiosk mode
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
            addDataScheme("package")
        }
        
        registerReceiver(systemEventReceiver, intentFilter)
    }
    
    private fun unregisterSystemReceivers() {
        try {
            unregisterReceiver(systemEventReceiver)
        } catch (e: Exception) {
            KioskLogger.e(TAG, "Error unregistering receivers", e)
        }
    }
    
    private val systemEventReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_PACKAGE_ADDED -> {
                    val packageName = intent.data?.schemeSpecificPart
                    KioskLogger.i(TAG, "Package added: $packageName")
                    // Check if new package should be disabled
                    serviceScope.launch {
                        kioskEnforcer.checkAndDisableNewApp(packageName)
                    }
                }
                Intent.ACTION_SCREEN_ON -> {
                    KioskLogger.i(TAG, "Screen turned on - enforcing kiosk")
                    serviceScope.launch {
                        kioskEnforcer.redirectToKioskApp()
                    }
                }
                Intent.ACTION_USER_PRESENT -> {
                    KioskLogger.i(TAG, "User present - enforcing kiosk")
                    serviceScope.launch {
                        kioskEnforcer.redirectToKioskApp()
                    }
                }
            }
        }
    }
    
    private fun restartService() {
        // Enterprise service auto-restart mechanism
        serviceScope.launch {
            delay(1000) // Brief delay before restart
            val intent = Intent(this@EnterpriseKioskService, EnterpriseKioskService::class.java)
            startService(intent)
        }
    }
}

// Supporting data classes
data class SecurityStatus(
    val isSecure: Boolean,
    val violations: List<String>,
    val severity: SecuritySeverity
)

enum class SecuritySeverity {
    LOW, MEDIUM, HIGH, CRITICAL
}

// Enterprise component interfaces
interface SecurityMonitor {
    suspend fun performComprehensiveCheck(): SecurityStatus
    suspend fun checkSecurityStatus()
    suspend fun enableSecurityMode()
    suspend fun disableSecurityMode()
    suspend fun handleCriticalViolation()
}

interface KioskEnforcer {
    suspend fun enableKioskMode()
    suspend fun disableKioskMode()
    suspend fun redirectToKioskApp()
    suspend fun forceKioskMode()
    suspend fun disableUnauthorizedApps()
    suspend fun checkAndDisableNewApp(packageName: String?)
}

interface SystemUIController {
    suspend fun hideSystemUI()
    suspend fun showSystemUI()
    suspend fun enforceUIRestrictions()
}
