# 🚀 KIOSK MODE - IMPLEMENTATION WORKFLOW DOCUMENTATION

Dokumentasi lengkap untuk implementasi fitur Boot & Launcher + UI & Display yang masih stub.

## 📋 **OVERVIEW FITUR YANG AKAN DIIMPLEMENTASI**

### 🚀 **Boot & Launcher** 
- ✅ **Set as Default Launcher**: Make default home app
- ✅ **Boot Animation**: Customize startup screen

### 📱 **UI & Display**
- ✅ **Hide Status Bar**: Hide notification bar  
- ✅ **Hide Navigation Bar**: Hide navigation buttons
- ✅ **Immersive Mode**: Full screen mode
- ✅ **Screen Orientation**: Lock screen rotation

---

## 🎯 **FASE 1: SET AS DEFAULT LAUNCHER**

### **1.1 Analysis & Requirements**
```kotlin
// Current State: Stub implementation
fun setAsDefaultLauncher() {
    // TODO: Implement set as default launcher
}

// Target: Full device owner implementation with persistent settings
```

### **1.2 Implementation Plan**

#### **Step 1: Update ConfigViewModel.kt**
```kotlin
fun setAsDefaultLauncher() {
    viewModelScope.launch {
        try {
            if (isDeviceOwner()) {
                val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                val adminComponent = ComponentName(context, KioskDeviceAdminReceiver::class.java)
                val launcherComponent = ComponentName(context, MainActivity::class.java)
                
                // Clear current defaults
                devicePolicyManager.clearPackagePersistentPreferredActivities(
                    adminComponent, 
                    context.packageName
                )
                
                // Set launcher intent filter
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
                
                _uiEvent.send(UiEvent.ShowMessage("Successfully set as default launcher"))
            } else {
                // Fallback for non-device owner
                showLauncherSelectionDialog()
            }
        } catch (e: Exception) {
            _uiEvent.send(UiEvent.ShowMessage("Failed to set as default launcher: ${e.message}"))
        }
    }
}

private fun showLauncherSelectionDialog() {
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_HOME)
    }
    val chooser = Intent.createChooser(intent, "Select default launcher")
    _uiEvent.send(UiEvent.StartActivity(chooser))
}
```

#### **Step 2: Add Required Permissions (AndroidManifest.xml)**
```xml
<!-- Add to manifest if not exists -->
<uses-permission android:name="android.permission.SET_PREFERRED_APPLICATIONS" />

<!-- Update MainActivity intent filter -->
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:launchMode="singleTop">
    <intent-filter android:priority="1000">
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
        <category android:name="android.intent.category.HOME" />
        <category android:name="android.intent.category.DEFAULT" />
    </intent-filter>
</activity>
```

### **1.3 Testing Plan**
1. Test with device owner enabled
2. Test fallback dialog for non-device owner
3. Verify persistence after reboot
4. Test with multiple launcher apps installed

---

## 🎨 **FASE 2: BOOT ANIMATION**

### **2.1 Analysis & Requirements**
```kotlin
// Current State: Stub implementation  
fun showBootAnimationSettings() {
    // TODO: Implement boot animation settings
}

// Target: Custom boot screen with enterprise branding
```

### **2.2 Implementation Plan**

#### **Step 1: Create Boot Animation Activity**
```kotlin
// File: BootAnimationActivity.kt
@Composable
fun BootAnimationActivity() {
    var animationProgress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        // Animate progress
        animateFloatAsState(
            targetValue = 1f,
            animationSpec = tween(3000)
        ).let { animatedProgress ->
            animationProgress = animatedProgress.value
        }
        
        // Auto finish after animation
        delay(3000)
        finish()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Company logo
            Image(
                painter = painterResource(R.drawable.company_logo),
                contentDescription = "Company Logo",
                modifier = Modifier.size(120.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Progress indicator
            LinearProgressIndicator(
                progress = animationProgress,
                modifier = Modifier.width(200.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Enterprise Kiosk Loading...",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}
```

#### **Step 2: Integrate with Boot Receiver**
```kotlin
// Update EnterpriseBootReceiver.kt
private fun startBootAnimation(context: Context) {
    val intent = Intent(context, BootAnimationActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    context.startActivity(intent)
}
```

#### **Step 3: Update ConfigViewModel**
```kotlin
fun showBootAnimationSettings() {
    viewModelScope.launch {
        _uiEvent.send(UiEvent.Navigate(Routes.BOOT_ANIMATION_SETTINGS))
    }
}
```

### **2.3 Assets Required**
- Company logo (PNG/SVG)
- Loading animations
- Sound effects (optional)

---

## 📱 **FASE 3: UI & DISPLAY CONTROLS**

### **3.1 Hide Status Bar Implementation**

#### **Step 1: Create UI Controller Class**
```kotlin
// File: UIDisplayController.kt
class UIDisplayController @Inject constructor(
    private val context: Context
) {
    
    fun hideStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+
            activity.window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Legacy approach
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            )
        }
    }
    
    fun showStatusBar(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.show(WindowInsets.Type.statusBars())
        } else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }
}
```

#### **Step 2: Update ConfigViewModel**
```kotlin
@Inject
lateinit var uiDisplayController: UIDisplayController

private val _statusBarVisible = MutableStateFlow(true)
val statusBarVisible = _statusBarVisible.asStateFlow()

fun toggleStatusBarVisibility() {
    viewModelScope.launch {
        val activity = getCurrentActivity() // Helper method needed
        if (_statusBarVisible.value) {
            uiDisplayController.hideStatusBar(activity)
            _statusBarVisible.value = false
        } else {
            uiDisplayController.showStatusBar(activity)
            _statusBarVisible.value = true
        }
        
        // Save preference
        saveDisplayPreference("status_bar_visible", _statusBarVisible.value)
    }
}
```

### **3.2 Hide Navigation Bar Implementation**

```kotlin
// Add to UIDisplayController.kt
fun hideNavigationBar(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        activity.window.insetsController?.let { controller ->
            controller.hide(WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    } else {
        @Suppress("DEPRECATION")
        activity.window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        )
    }
}

fun showNavigationBar(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        activity.window.insetsController?.show(WindowInsets.Type.navigationBars())
    } else {
        @Suppress("DEPRECATION")
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }
}
```

### **3.3 Immersive Mode Implementation**

```kotlin
// Add to UIDisplayController.kt
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
}

fun disableImmersiveMode(activity: Activity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        activity.window.insetsController?.show(WindowInsets.Type.systemBars())
    } else {
        @Suppress("DEPRECATION")
        activity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }
}
```

### **3.4 Screen Orientation Lock Implementation**

```kotlin
// Add to UIDisplayController.kt
fun lockScreenOrientation(activity: Activity, orientation: Int) {
    activity.requestedOrientation = orientation
}

fun unlockScreenOrientation(activity: Activity) {
    activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
}

// Orientation constants
object ScreenOrientation {
    const val PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    const val LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
    const val REVERSE_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
    const val REVERSE_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
}
```

---

## 🛠️ **FASE 4: INTEGRATION & UI UPDATES**

### **4.1 Update ConfigViewModel dengan Semua Functions**

```kotlin
// Replace all stub methods in ConfigViewModel.kt

// Boot & Launcher Methods
fun setAsDefaultLauncher() {
    // Implementation from Fase 1
}

fun showBootAnimationSettings() {
    viewModelScope.launch {
        _uiEvent.send(UiEvent.Navigate(Routes.BOOT_ANIMATION_SETTINGS))
    }
}

// UI & Display Methods  
fun toggleStatusBarVisibility() {
    // Implementation from Fase 3.1
}

fun toggleNavigationBarVisibility() {
    // Implementation from Fase 3.2  
}

fun toggleImmersiveMode() {
    // Implementation from Fase 3.3
}

fun showOrientationSettings() {
    viewModelScope.launch {
        _uiEvent.send(UiEvent.Navigate(Routes.ORIENTATION_SETTINGS))
    }
}
```

### **4.2 Add Required Dependencies**

```kotlin
// build.gradle (app level)
dependencies {
    // Window Insets
    implementation "androidx.core:core-ktx:1.12.0"
    
    // Activity Result APIs
    implementation "androidx.activity:activity-compose:1.8.2"
    
    // Animation
    implementation "androidx.compose.animation:animation:$compose_version"
}
```

### **4.3 Add Routes & Navigation**

```kotlin
// Update Routes.kt
object Routes {
    // ...existing routes...
    const val BOOT_ANIMATION_SETTINGS = "boot_animation_settings"
    const val ORIENTATION_SETTINGS = "orientation_settings"
}

// Update MainActivity navigation
composable(Routes.BOOT_ANIMATION_SETTINGS) {
    BootAnimationSettingsScreen(navController = navController)
}
composable(Routes.ORIENTATION_SETTINGS) {
    OrientationSettingsScreen(navController = navController)
}
```

---

## 🧪 **FASE 5: TESTING WORKFLOW**

### **5.1 Unit Tests**
```kotlin
// Test file: UIDisplayControllerTest.kt
@Test
fun `test hide status bar on Android 11+`() {
    // Test implementation
}

@Test  
fun `test legacy hide status bar`() {
    // Test implementation
}

@Test
fun `test screen orientation lock`() {
    // Test implementation
}
```

### **5.2 Integration Tests**
- Test dengan device owner enabled/disabled
- Test compatibility Android versions (API 21-34)
- Test dengan berbagai screen sizes
- Test persistence setelah reboot

### **5.3 Manual Testing Checklist**
- [ ] Set as default launcher works
- [ ] Boot animation shows on startup  
- [ ] Status bar hide/show functions
- [ ] Navigation bar hide/show functions
- [ ] Immersive mode toggle
- [ ] Screen orientation lock
- [ ] Settings persist after app restart
- [ ] Works with kiosk mode enabled

---

## 📊 **FASE 6: DOCUMENTATION & DEPLOYMENT**

### **6.1 Update User Documentation**
- Add screenshots untuk setiap fitur
- Buat video demo functionality
- Update README.md dengan new features

### **6.2 Code Documentation**
```kotlin
/**
 * Controls UI display elements for kiosk mode
 * 
 * Features:
 * - Status bar visibility control
 * - Navigation bar visibility control  
 * - Immersive mode toggle
 * - Screen orientation locking
 * 
 * Compatible with Android API 21+ with legacy fallbacks
 */
class UIDisplayController
```

### **6.3 Performance Monitoring**
- Monitor battery impact
- Track UI responsiveness
- Log feature usage analytics

---

## ⚡ **IMPLEMENTATION TIMELINE**

### **Week 1: Boot & Launcher**
- ✅ Day 1-2: Set as Default Launcher
- ✅ Day 3-4: Boot Animation  
- ✅ Day 5: Integration & Testing

### **Week 2: UI & Display**  
- ✅ Day 1-2: Status Bar & Navigation Bar
- ✅ Day 3-4: Immersive Mode & Orientation
- ✅ Day 5: Integration & Testing

### **Week 3: Polish & Deploy**
- ✅ Day 1-2: Bug fixes & optimization
- ✅ Day 3-4: Documentation & testing
- ✅ Day 5: Deployment & monitoring

---

## 🔧 **TOOLS & RESOURCES NEEDED**

### **Development Tools**
- Android Studio latest version
- Device dengan Android 11+ untuk testing
- ADB untuk device owner setup
- Git untuk version control

### **Assets & Resources**
- Company logo untuk boot animation
- Icons untuk UI controls
- Testing devices dengan berbagai Android versions

### **Knowledge Requirements**
- Android Device Admin APIs
- Window Insets Controller  
- Activity lifecycle management
- Jetpack Compose state management

---

## 📞 **SUPPORT & MAINTENANCE**

### **Post-Implementation**
- Monitor crash reports
- User feedback collection
- Performance optimization
- Regular Android version compatibility updates

### **Future Enhancements**
- Custom boot animation editor
- Advanced orientation controls
- Gesture navigation handling
- Multi-display support

---

*Dokumentasi ini akan diupdate seiring dengan progress implementasi.*

**Power Level: ENTERPRISE READY!** 🚀
