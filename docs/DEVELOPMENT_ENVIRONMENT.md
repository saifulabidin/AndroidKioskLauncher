# 🔧 DEVELOPMENT ENVIRONMENT SETUP

Setup guide untuk development environment implementasi fitur Boot & Launcher + UI & Display.

## 📋 **PREREQUISITES**

### **Required Tools**
- ✅ Android Studio Arctic Fox (2020.3.1) atau yang lebih baru
- ✅ Java JDK 11 atau yang lebih baru
- ✅ Git 2.30+ 
- ✅ ADB (Android Debug Bridge)

### **Required Knowledge**
- ✅ Kotlin programming
- ✅ Jetpack Compose
- ✅ Android Device Admin APIs
- ✅ Hilt Dependency Injection
- ✅ Android Architecture Components

---

## 🔨 **PROJECT SETUP**

### **1. Clone Repository**
```bash
git clone https://github.com/your-org/AndroidKioskLauncher.git
cd AndroidKioskLauncher
```

### **2. Create Feature Branch**
```bash
git checkout -b feature/ui-display-controls
git push -u origin feature/ui-display-controls
```

### **3. Android Studio Configuration**

#### **Build Configuration**
```gradle
// Ensure these in build.gradle (app)
android {
    compileSdk 34
    
    defaultConfig {
        minSdk 21
        targetSdk 34
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_11
        targetCompatibility JavaVersion.VERSION_11
    }
    
    kotlinOptions {
        jvmTarget = '11'
    }
}
```

#### **Dependencies untuk Implementasi**
```gradle
dependencies {
    // Core Android
    implementation "androidx.core:core-ktx:1.12.0"
    implementation "androidx.activity:activity-compose:1.8.2"
    
    // Window Insets (untuk UI controls)
    implementation "androidx.compose.foundation:foundation:$compose_version"
    
    // Animation
    implementation "androidx.compose.animation:animation:$compose_version"
    
    // Device Admin (sudah ada)
    // implementation "androidx.compose.material:material:$compose_version"
    
    // Testing (untuk implementasi)
    testImplementation "junit:junit:4.13.2"
    testImplementation "org.mockito:mockito-core:4.11.0"
    testImplementation "org.mockito:mockito-kotlin:4.1.0"
    androidTestImplementation "androidx.test.ext:junit:1.1.5"
    androidTestImplementation "androidx.compose.ui:ui-test-junit4:$compose_version"
}
```

---

## 📱 **DEVICE SETUP**

### **1. Development Device Requirements**
- Android 7.0+ (API 24+) untuk testing
- Android 11+ (API 30+) untuk full feature testing
- Tablet device untuk screen size testing

### **2. Enable Developer Options**
```bash
# Enable developer options di device settings
Settings > About Phone > Tap Build Number 7 times

# Enable USB Debugging
Settings > Developer Options > USB Debugging = ON

# Enable Stay Awake (optional)
Settings > Developer Options > Stay Awake = ON
```

### **3. Device Owner Setup (untuk full testing)**
```bash
# Factory reset device first (WARNING: will erase all data)

# Install APK
adb install app-debug.apk

# Set as device owner
adb shell dpm set-device-owner nu.brandrisk.kioskmode/.KioskDeviceAdminReceiver
```

### **4. Testing Device Configuration**
```bash
# Verify device owner status
adb shell dpm list-owners

# Check if app is device owner
adb shell cmd device_policy print-active-admins
```

---

## 🛠️ **DEVELOPMENT WORKFLOW**

### **1. File Structure Setup**
```bash
# Create directories untuk new files
mkdir -p app/src/main/java/nu/brandrisk/kioskmode/domain/ui
mkdir -p app/src/main/java/nu/brandrisk/kioskmode/domain/launcher  
mkdir -p app/src/main/java/nu/brandrisk/kioskmode/data/preferences
mkdir -p app/src/main/java/nu/brandrisk/kioskmode/ui/bootanimation
mkdir -p app/src/main/java/nu/brandrisk/kioskmode/ui/orientation

# Create test directories
mkdir -p app/src/test/java/nu/brandrisk/kioskmode/domain/ui
mkdir -p app/src/androidTest/java/nu/brandrisk/kioskmode/ui
```

### **2. Code Style Configuration**

#### **Android Studio Settings**
```
File > Settings > Editor > Code Style > Kotlin
- Use default Android Kotlin style guide
- Line length: 120 characters
- Indent: 4 spaces

File > Settings > Editor > Inspections
- Enable all Kotlin inspections
- Enable Android Lint checks
```

#### **.editorconfig** (create in project root)
```ini
root = true

[*.{kt,kts}]
indent_style = space
indent_size = 4
max_line_length = 120
insert_final_newline = true

[*.{xml,gradle}]
indent_style = space
indent_size = 4
```

### **3. Git Configuration**
```bash
# Configure git hooks untuk quality checks
cp scripts/pre-commit .git/hooks/
chmod +x .git/hooks/pre-commit

# Configure git ignore untuk generated files
echo "*.iml" >> .gitignore
echo ".gradle/" >> .gitignore
echo "build/" >> .gitignore
```

---

## 🧪 **TESTING SETUP**

### **1. Unit Testing Configuration**

#### **Test Directory Structure**
```
app/src/test/java/nu/brandrisk/kioskmode/
├── domain/
│   ├── ui/
│   │   ├── UIDisplayControllerTest.kt
│   │   └── UIDisplayRepositoryTest.kt
│   └── launcher/
│       └── LauncherManagerTest.kt
└── data/
    └── model/
        └── DisplaySettingsTest.kt
```

#### **Test Dependencies (in build.gradle)**
```gradle
testImplementation "junit:junit:4.13.2"
testImplementation "org.mockito:mockito-core:5.8.0"
testImplementation "org.mockito:mockito-kotlin:5.1.0"
testImplementation "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3"
testImplementation "androidx.arch.core:core-testing:2.2.0"
```

### **2. Instrumentation Testing**

#### **Device Testing Script**
```bash
#!/bin/bash
# test-on-device.sh

echo "Running instrumentation tests..."

# Install test APK
adb install -r app-debug.apk
adb install -r app-debug-androidTest.apk

# Run specific test class
adb shell am instrument -w \
    -e class nu.brandrisk.kioskmode.ui.ConfigViewUITest \
    nu.brandrisk.kioskmode.test/androidx.test.runner.AndroidJUnitRunner

echo "Tests completed!"
```

### **3. Manual Testing Checklist**

#### **Create Test Cases File**
```markdown
# Manual Test Cases

## UI Display Controls
- [ ] Status bar hide/show on Android 11+
- [ ] Status bar hide/show on Android 10-
- [ ] Navigation bar hide/show with gestures
- [ ] Navigation bar hide/show with buttons
- [ ] Immersive mode toggle
- [ ] Orientation lock (portrait/landscape)

## Launcher Management  
- [ ] Set as default launcher (device owner)
- [ ] Launcher selection dialog (non-device owner)
- [ ] Persistence after reboot

## Boot Animation
- [ ] Animation shows on boot
- [ ] Custom logo displays correctly
- [ ] Performance on different devices
```

---

## 🔍 **DEBUGGING SETUP**

### **1. Logging Configuration**

#### **Custom Logger Class**
```kotlin
// File: utils/KioskLogger.kt
object KioskLogger {
    private const val TAG_PREFIX = "KioskMode_"
    
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d("$TAG_PREFIX$tag", message)
        }
    }
    
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        android.util.Log.e("$TAG_PREFIX$tag", message, throwable)
    }
}
```

### **2. ADB Debugging Commands**

#### **Common Debug Commands**
```bash
# Monitor logs for kiosk-related messages
adb logcat | grep "KioskMode"

# Check device policy status
adb shell dumpsys device_policy

# Check window manager state
adb shell dumpsys window displays

# Check current launcher
adb shell cmd package query-services \
    --include-stopped \
    --intent android.intent.action.MAIN \
    --category android.intent.category.HOME

# Force stop app
adb shell am force-stop nu.brandrisk.kioskmode

# Clear app data
adb shell pm clear nu.brandrisk.kioskmode
```

### **3. Performance Monitoring**

#### **Memory Usage Monitoring**
```bash
# Monitor memory usage
adb shell dumpsys meminfo nu.brandrisk.kioskmode

# Monitor CPU usage  
adb shell top | grep nu.brandrisk.kioskmode

# Monitor battery usage
adb shell dumpsys batterystats | grep nu.brandrisk.kioskmode
```

---

## 📊 **QUALITY ASSURANCE**

### **1. Code Quality Tools**

#### **Static Analysis Setup**
```gradle
// Add to build.gradle (app)
apply plugin: 'kotlin-android'

android {
    lintOptions {
        checkReleaseBuilds false
        abortOnError false
        xmlReport true
        htmlReport true
    }
}
```

#### **Detekt Configuration**
```yaml
# detekt.yml
style:
  MaxLineLength:
    maxLineLength: 120
  
complexity:
  ComplexMethod:
    threshold: 15
    
naming:
  FunctionNaming:
    functionPattern: '[a-z][a-zA-Z0-9]*'
```

### **2. Code Review Checklist**

#### **Review Points**
- [ ] Code follows Android best practices
- [ ] Proper error handling implemented
- [ ] Memory leaks avoided
- [ ] Thread safety considered
- [ ] Accessibility features included
- [ ] Performance optimized
- [ ] Documentation complete

---

## 🚀 **BUILD & DEPLOYMENT**

### **1. Build Configuration**

#### **Gradle Build Script**
```bash
#!/bin/bash
# build.sh

echo "Building Kiosk Launcher..."

# Clean build
./gradlew clean

# Run tests
./gradlew test

# Run lint checks
./gradlew lint

# Build debug APK
./gradlew assembleDebug

# Build release APK (if certificates available)
# ./gradlew assembleRelease

echo "Build completed!"
```

### **2. Release Configuration**

#### **Signing Configuration** (for production)
```gradle
android {
    signingConfigs {
        release {
            keyAlias keystoreProperties['keyAlias']
            keyPassword keystoreProperties['keyPassword']
            storeFile file(keystoreProperties['storeFile'])
            storePassword keystoreProperties['storePassword']
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

---

## 📚 **DOCUMENTATION TOOLS**

### **1. Code Documentation**
```bash
# Generate KDoc documentation
./gradlew dokkaHtml

# View generated docs
open build/dokka/html/index.html
```

### **2. API Documentation**
```kotlin
/**
 * Controls UI display elements for enterprise kiosk mode.
 * 
 * This class provides methods to hide/show system UI elements like status bar,
 * navigation bar, and enable immersive mode for kiosk applications.
 * 
 * @since 1.0
 * @author Development Team
 */
class UIDisplayController
```

---

## 🆘 **TROUBLESHOOTING**

### **Common Issues & Solutions**

#### **Issue: Cannot set as device owner**
```bash
# Solution: Factory reset device and try again
adb reboot bootloader
# Or manually factory reset in settings
```

#### **Issue: UI controls not working**
```bash
# Check API level compatibility
adb shell getprop ro.build.version.sdk

# Verify permissions in manifest
adb shell dumpsys package nu.brandrisk.kioskmode | grep permission
```

#### **Issue: Tests failing**
```bash
# Clear test cache
./gradlew cleanTest

# Run specific test
./gradlew test --tests UIDisplayControllerTest

# Debug test
./gradlew test --debug-jvm
```

---

## 🔗 **USEFUL RESOURCES**

### **Documentation Links**
- [Android Device Admin](https://developer.android.com/guide/topics/admin/device-admin)
- [Window Insets](https://developer.android.com/develop/ui/views/layout/edge-to-edge)
- [Jetpack Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)

### **Tools & Utilities**
- [scrcpy](https://github.com/Genymobile/scrcpy) - Screen mirroring untuk testing
- [Android Debug Database](https://github.com/amitshekhariitbhu/Android-Debug-Database) - Database debugging
- [LeakCanary](https://square.github.io/leakcanary/) - Memory leak detection

---

**Environment setup completed! Ready untuk development!** 🚀
