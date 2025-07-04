# 🛠️ TECHNICAL IMPLEMENTATION GUIDE

Panduan teknis detail untuk implementasi fitur Boot & Launcher + UI & Display.

## 📁 **FILE STRUCTURE YANG AKAN DIBUAT/DIMODIFIKASI**

```
app/src/main/java/nu/brandrisk/kioskmode/
├── ui/
│   ├── config/
│   │   ├── ConfigViewModel.kt (✏️ MODIFY)
│   │   └── ConfigView.kt (✏️ MODIFY)
│   ├── bootanimation/
│   │   ├── BootAnimationActivity.kt (🆕 NEW)
│   │   ├── BootAnimationSettingsScreen.kt (🆕 NEW)
│   │   └── BootAnimationViewModel.kt (🆕 NEW)
│   └── orientation/
│       ├── OrientationSettingsScreen.kt (🆕 NEW)
│       └── OrientationViewModel.kt (🆕 NEW)
├── domain/
│   ├── ui/
│   │   ├── UIDisplayController.kt (🆕 NEW)
│   │   └── UIDisplayRepository.kt (🆕 NEW)
│   └── launcher/
│       ├── LauncherManager.kt (🆕 NEW)
│       └── BootAnimationManager.kt (🆕 NEW)
├── data/
│   ├── preferences/
│   │   └── UIDisplayPreferences.kt (🆕 NEW)
│   └── model/
│       ├── DisplaySettings.kt (🆕 NEW)
│       └── OrientationMode.kt (🆕 NEW)
└── utils/
    ├── Routes.kt (✏️ MODIFY)
    └── UIConstants.kt (🆕 NEW)
```

---

## 🔧 **KODE IMPLEMENTASI LENGKAP**

### **1. UIDisplayController.kt**

```kotlin
package nu.brandrisk.kioskmode.domain.ui

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UIDisplayController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val uiDisplayRepository: UIDisplayRepository
) {
    
    private val _displaySettings = MutableStateFlow(DisplaySettings())
    val displaySettings: StateFlow<DisplaySettings> = _displaySettings.asStateFlow()
    
    init {
        // Load saved preferences
        _displaySettings.value = uiDisplayRepository.getDisplaySettings()
    }
    
    /**
     * Hide status bar (notification bar)
     */
    fun hideStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ (API 30+)
            activity.window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Legacy approach for Android 10 and below
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (
                activity.window.decorView.systemUiVisibility or
                View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
        
        _displaySettings.value = _displaySettings.value.copy(statusBarVisible = false)
        uiDisplayRepository.saveDisplaySettings(_displaySettings.value)
    }
    
    /**
     * Show status bar
     */
    fun showStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.show(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (
                activity.window.decorView.systemUiVisibility and
                View.SYSTEM_UI_FLAG_FULLSCREEN.inv()
            )
        }
        
        _displaySettings.value = _displaySettings.value.copy(statusBarVisible = true)
        uiDisplayRepository.saveDisplaySettings(_displaySettings.value)
    }
    
    /**
     * Hide navigation bar
     */
    fun hideNavigationBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (
                activity.window.decorView.systemUiVisibility or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }
        
        _displaySettings.value = _displaySettings.value.copy(navigationBarVisible = false)
        uiDisplayRepository.saveDisplaySettings(_displaySettings.value)
    }
    
    /**
     * Show navigation bar
     */
    fun showNavigationBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.show(WindowInsets.Type.navigationBars())
        } else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (
                activity.window.decorView.systemUiVisibility and
                (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY).inv()
            )
        }
        
        _displaySettings.value = _displaySettings.value.copy(navigationBarVisible = true)
        uiDisplayRepository.saveDisplaySettings(_displaySettings.value)
    }
    
    /**
     * Enable full immersive mode (hide both status and navigation bars)
     */
    fun enableImmersiveMode(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.systemBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
        
        _displaySettings.value = _displaySettings.value.copy(immersiveMode = true)
        uiDisplayRepository.saveDisplaySettings(_displaySettings.value)
    }
    
    /**
     * Disable immersive mode
     */
    fun disableImmersiveMode(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.show(WindowInsets.Type.systemBars())
        } else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
        
        _displaySettings.value = _displaySettings.value.copy(immersiveMode = false)
        uiDisplayRepository.saveDisplaySettings(_displaySettings.value)
    }
    
    /**
     * Lock screen orientation
     */
    fun lockScreenOrientation(activity: Activity, orientation: OrientationMode) {
        val androidOrientation = when (orientation) {
            OrientationMode.PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            OrientationMode.LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            OrientationMode.REVERSE_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            OrientationMode.REVERSE_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            OrientationMode.AUTO -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        
        activity.requestedOrientation = androidOrientation
        
        _displaySettings.value = _displaySettings.value.copy(orientationMode = orientation)
        uiDisplayRepository.saveDisplaySettings(_displaySettings.value)
    }
    
    /**
     * Apply all saved display settings to activity
     */
    fun applyDisplaySettings(activity: Activity) {
        val settings = _displaySettings.value
        
        if (!settings.statusBarVisible) hideStatusBar(activity)
        if (!settings.navigationBarVisible) hideNavigationBar(activity)
        if (settings.immersiveMode) enableImmersiveMode(activity)
        if (settings.orientationMode != OrientationMode.AUTO) {
            lockScreenOrientation(activity, settings.orientationMode)
        }
    }
}
```

### **2. DisplaySettings.kt (Data Model)**

```kotlin
package nu.brandrisk.kioskmode.data.model

data class DisplaySettings(
    val statusBarVisible: Boolean = true,
    val navigationBarVisible: Boolean = true,
    val immersiveMode: Boolean = false,
    val orientationMode: OrientationMode = OrientationMode.AUTO
)

enum class OrientationMode {
    AUTO,
    PORTRAIT,
    LANDSCAPE,
    REVERSE_PORTRAIT,
    REVERSE_LANDSCAPE
}
```

### **3. UIDisplayRepository.kt**

```kotlin
package nu.brandrisk.kioskmode.domain.ui

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import nu.brandrisk.kioskmode.data.model.DisplaySettings
import nu.brandrisk.kioskmode.data.model.OrientationMode
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UIDisplayRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "ui_display_prefs", 
        Context.MODE_PRIVATE
    )
    
    fun saveDisplaySettings(settings: DisplaySettings) {
        prefs.edit()
            .putBoolean("status_bar_visible", settings.statusBarVisible)
            .putBoolean("navigation_bar_visible", settings.navigationBarVisible)
            .putBoolean("immersive_mode", settings.immersiveMode)
            .putString("orientation_mode", settings.orientationMode.name)
            .apply()
    }
    
    fun getDisplaySettings(): DisplaySettings {
        return DisplaySettings(
            statusBarVisible = prefs.getBoolean("status_bar_visible", true),
            navigationBarVisible = prefs.getBoolean("navigation_bar_visible", true),
            immersiveMode = prefs.getBoolean("immersive_mode", false),
            orientationMode = OrientationMode.valueOf(
                prefs.getString("orientation_mode", OrientationMode.AUTO.name) ?: OrientationMode.AUTO.name
            )
        )
    }
}
```

### **4. LauncherManager.kt**

```kotlin
package nu.brandrisk.kioskmode.domain.launcher

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dagger.hilt.android.qualifiers.ApplicationContext
import nu.brandrisk.kioskmode.KioskDeviceAdminReceiver
import nu.brandrisk.kioskmode.MainActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LauncherManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent = ComponentName(context, KioskDeviceAdminReceiver::class.java)
    
    /**
     * Set this app as default launcher (requires device owner)
     */
    fun setAsDefaultLauncher(): Boolean {
        return try {
            if (isDeviceOwner()) {
                val launcherComponent = ComponentName(context, MainActivity::class.java)
                
                // Clear current defaults
                devicePolicyManager.clearPackagePersistentPreferredActivities(
                    adminComponent, 
                    context.packageName
                )
                
                // Create intent filter for home screen
                val intentFilter = IntentFilter().apply {
                    addAction(Intent.ACTION_MAIN)
                    addCategory(Intent.CATEGORY_HOME)
                    addCategory(Intent.CATEGORY_DEFAULT)
                }
                
                // Set as persistent preferred activity
                devicePolicyManager.addPersistentPreferredActivity(
                    adminComponent,
                    intentFilter,
                    launcherComponent
                )
                
                true
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("LauncherManager", "Failed to set as default launcher", e)
            false
        }
    }
    
    /**
     * Remove this app as default launcher
     */
    fun removeAsDefaultLauncher(): Boolean {
        return try {
            if (isDeviceOwner()) {
                devicePolicyManager.clearPackagePersistentPreferredActivities(
                    adminComponent,
                    context.packageName
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("LauncherManager", "Failed to remove as default launcher", e)
            false
        }
    }
    
    /**
     * Show launcher selection dialog (fallback for non-device owner)
     */
    fun showLauncherSelectionDialog(): Intent {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        return Intent.createChooser(intent, "Select default launcher")
    }
    
    private fun isDeviceOwner(): Boolean {
        return devicePolicyManager.isDeviceOwnerApp(context.packageName)
    }
}
```

### **5. Update ConfigViewModel.kt**

```kotlin
// Add these imports at top of ConfigViewModel.kt
import nu.brandrisk.kioskmode.domain.ui.UIDisplayController
import nu.brandrisk.kioskmode.domain.launcher.LauncherManager
import nu.brandrisk.kioskmode.data.model.OrientationMode

// Add these injected dependencies
@Inject
lateinit var uiDisplayController: UIDisplayController

@Inject
lateinit var launcherManager: LauncherManager

// Add current activity reference (helper method needed)
private var currentActivity: Activity? = null

fun setCurrentActivity(activity: Activity) {
    currentActivity = activity
}

// Replace stub implementations with these:

fun setAsDefaultLauncher() {
    viewModelScope.launch {
        try {
            val success = launcherManager.setAsDefaultLauncher()
            if (success) {
                _uiEvent.send(UiEvent.ShowMessage("Successfully set as default launcher"))
            } else {
                // Show launcher selection dialog for non-device owner
                val intent = launcherManager.showLauncherSelectionDialog()
                _uiEvent.send(UiEvent.StartActivity(intent))
            }
        } catch (e: Exception) {
            _uiEvent.send(UiEvent.ShowMessage("Failed to set as default launcher: ${e.message}"))
        }
    }
}

fun showBootAnimationSettings() {
    viewModelScope.launch {
        _uiEvent.send(UiEvent.Navigate(Routes.BOOT_ANIMATION_SETTINGS))
    }
}

fun toggleStatusBarVisibility() {
    currentActivity?.let { activity ->
        val isVisible = uiDisplayController.displaySettings.value.statusBarVisible
        if (isVisible) {
            uiDisplayController.hideStatusBar(activity)
        } else {
            uiDisplayController.showStatusBar(activity)
        }
    }
}

fun toggleNavigationBarVisibility() {
    currentActivity?.let { activity ->
        val isVisible = uiDisplayController.displaySettings.value.navigationBarVisible
        if (isVisible) {
            uiDisplayController.hideNavigationBar(activity)
        } else {
            uiDisplayController.showNavigationBar(activity)
        }
    }
}

fun toggleImmersiveMode() {
    currentActivity?.let { activity ->
        val isEnabled = uiDisplayController.displaySettings.value.immersiveMode
        if (isEnabled) {
            uiDisplayController.disableImmersiveMode(activity)
        } else {
            uiDisplayController.enableImmersiveMode(activity)
        }
    }
}

fun showOrientationSettings() {
    viewModelScope.launch {
        _uiEvent.send(UiEvent.Navigate(Routes.ORIENTATION_SETTINGS))
    }
}

fun lockOrientation(orientation: OrientationMode) {
    currentActivity?.let { activity ->
        uiDisplayController.lockScreenOrientation(activity, orientation)
    }
}
```

### **6. Update UiEvent.kt**

```kotlin
// Add to UiEvent.kt if not exists
sealed class UiEvent {
    data class Navigate(val route: String): UiEvent()
    data class ShowMessage(val message: String): UiEvent()
    data class StartActivity(val intent: Intent): UiEvent() // Add this
}
```

### **7. Update Routes.kt**

```kotlin
// Add to Routes.kt
object Routes {
    // ...existing routes...
    const val BOOT_ANIMATION_SETTINGS = "boot_animation_settings"
    const val ORIENTATION_SETTINGS = "orientation_settings"
}
```

### **8. Update MainActivity.kt Integration**

```kotlin
// Add to MainActivity.kt onCreate
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Set current activity for ConfigViewModel
    // You'll need to get ConfigViewModel instance and call setCurrentActivity(this)
    
    // Apply display settings
    val uiDisplayController = // Get instance via Hilt
    uiDisplayController.applyDisplaySettings(this)
    
    // ...rest of onCreate
}
```

---

## 🧪 **TESTING IMPLEMENTATION**

### **Unit Test Example**

```kotlin
// UIDisplayControllerTest.kt
@Test
fun `test hide status bar updates settings`() {
    val mockActivity = mockk<Activity>()
    val mockWindow = mockk<Window>()
    every { mockActivity.window } returns mockWindow
    
    uiDisplayController.hideStatusBar(mockActivity)
    
    assertFalse(uiDisplayController.displaySettings.value.statusBarVisible)
}
```

### **Integration Test Example**

```kotlin
@Test
fun `test set as default launcher with device owner`() {
    // Mock device policy manager
    // Test launcher manager functionality
}
```

---

## 📱 **USAGE EXAMPLES**

### **In Activity**

```kotlin
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var uiDisplayController: UIDisplayController
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply saved display settings
        uiDisplayController.applyDisplaySettings(this)
    }
}
```

### **In Compose UI**

```kotlin
@Composable
fun DisplayControlPanel(viewModel: ConfigViewModel) {
    val displaySettings by viewModel.uiDisplayController.displaySettings.collectAsState()
    
    Column {
        SwitchRow(
            title = "Hide Status Bar",
            checked = !displaySettings.statusBarVisible,
            onCheckedChange = { viewModel.toggleStatusBarVisibility() }
        )
        
        SwitchRow(
            title = "Hide Navigation Bar", 
            checked = !displaySettings.navigationBarVisible,
            onCheckedChange = { viewModel.toggleNavigationBarVisibility() }
        )
        
        SwitchRow(
            title = "Immersive Mode",
            checked = displaySettings.immersiveMode,
            onCheckedChange = { viewModel.toggleImmersiveMode() }
        )
    }
}
```

---

## 🎯 **IMPLEMENTATION CHECKLIST**

### **Phase 1: Core Classes**
- [ ] Create UIDisplayController.kt
- [ ] Create DisplaySettings.kt
- [ ] Create UIDisplayRepository.kt  
- [ ] Create LauncherManager.kt

### **Phase 2: Integration**
- [ ] Update ConfigViewModel.kt
- [ ] Update UiEvent.kt
- [ ] Update Routes.kt
- [ ] Update MainActivity.kt

### **Phase 3: UI Screens**
- [ ] Create OrientationSettingsScreen.kt
- [ ] Create BootAnimationSettingsScreen.kt
- [ ] Update ConfigView.kt

### **Phase 4: Testing**
- [ ] Unit tests for UIDisplayController
- [ ] Integration tests for LauncherManager
- [ ] UI tests for settings screens

### **Phase 5: Documentation**
- [ ] Code documentation
- [ ] User guide
- [ ] API documentation

---

**Ready untuk implementasi step-by-step!** 🚀
