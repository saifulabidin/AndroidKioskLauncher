package nu.brandrisk.kioskmode

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import nu.brandrisk.kioskmode.domain.AppRepository
import nu.brandrisk.kioskmode.service.EnhancedEnterpriseKioskService
import nu.brandrisk.kioskmode.ui.admin.AdminPasswordScreen
import nu.brandrisk.kioskmode.ui.adbsetup.AdbSetupView
import nu.brandrisk.kioskmode.ui.config.ConfigView
import nu.brandrisk.kioskmode.ui.configparentalcheck.ConfigParentalCheckView
import nu.brandrisk.kioskmode.ui.launcher.LauncherView
import nu.brandrisk.kioskmode.ui.theme.KioskModeTheme
import nu.brandrisk.kioskmode.utils.Routes
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var appRepository: AppRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize enterprise services
        initializeEnterpriseServices()

        lifecycleScope.launchWhenStarted {
            appRepository.refreshApps()
        }

        setContent {
            KioskModeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = Routes.LAUNCHER
                    ) {
                        composable(Routes.LAUNCHER) {
                            LauncherView(navController = navController)
                        }
                        composable(Routes.ADMIN_PASSWORD) {
                            AdminPasswordScreen(navController = navController)
                        }
                        composable(Routes.CONFIG_PARENTAL_CHECK) {
                            ConfigParentalCheckView(navController = navController)
                        }
                        composable(Routes.CONFIG) {
                            ConfigView(navController = navController)
                        }
                        composable(Routes.ADB_SETUP) {
                            AdbSetupView()
                        }
                    }
                }
            }
        }
    }

    private fun initializeEnterpriseServices() {
        lifecycleScope.launch {
            try {
                // Start enhanced enterprise monitoring service
                val intent = Intent(this@MainActivity, EnhancedEnterpriseKioskService::class.java)
                startForegroundService(intent)

                // Initialize enterprise features
                initializeEnterpriseFeatures()

            } catch (e: Exception) {
                // Log error but continue
                android.util.Log.e("MainActivity", "Failed to initialize enterprise services", e)
            }
        }
    }

    private fun initializeEnterpriseFeatures() {
        // Check device admin status
        checkDeviceAdminStatus()

        // Check MIUI optimizations
        checkMIUIOptimizations()

        // Check accessibility permissions
        checkAccessibilityPermissions()

        // Check notification access
        checkNotificationAccess()
    }

    private fun checkDeviceAdminStatus() {
        val devicePolicyManager = getSystemService(DEVICE_POLICY_SERVICE) as android.app.admin.DevicePolicyManager
        if (!devicePolicyManager.isDeviceOwnerApp(packageName)) {
            // Device admin not enabled - show setup instructions
            android.util.Log.w("MainActivity", "Device admin not enabled")
        }
    }

    private fun checkMIUIOptimizations() {
        // Check if running on MIUI and suggest optimizations
        if (isMIUIDevice()) {
            // Could show MIUI optimization dialog
            android.util.Log.i("MainActivity", "MIUI device detected - optimizations available")
        }
    }

    private fun checkAccessibilityPermissions() {
        // Check if accessibility service is enabled
        val accessibilityEnabled = android.provider.Settings.Secure.getString(
            contentResolver,
            android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )

        if (accessibilityEnabled?.contains(packageName) != true) {
            android.util.Log.w("MainActivity", "Accessibility service not enabled")
        }
    }

    private fun checkNotificationAccess() {
        // Check notification listener access
        val notificationListeners = android.provider.Settings.Secure.getString(
            contentResolver,
            "enabled_notification_listeners"
        )

        if (notificationListeners?.contains(packageName) != true) {
            android.util.Log.w("MainActivity", "Notification listener not enabled")
        }
    }

    private fun isMIUIDevice(): Boolean {
        return try {
            val miuiVersion = System.getProperty("ro.miui.ui.version.name")
            !miuiVersion.isNullOrEmpty()
        } catch (e: Exception) {
            android.os.Build.MANUFACTURER.equals("Xiaomi", ignoreCase = true) ||
                    android.os.Build.BRAND.equals("Xiaomi", ignoreCase = true) ||
                    android.os.Build.BRAND.equals("Redmi", ignoreCase = true) ||
                    android.os.Build.BRAND.equals("POCO", ignoreCase = true)
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure enterprise monitoring is active
        val intent = Intent(this, EnhancedEnterpriseKioskService::class.java)
        startForegroundService(intent)
    }
}