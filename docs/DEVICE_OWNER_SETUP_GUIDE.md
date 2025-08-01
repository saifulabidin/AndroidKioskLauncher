# 🔧 DEVICE OWNER SETUP GUIDE - ACCOUNT REMOVAL

## 🚨 **MASALAH UTAMA**
Error: `Not allowed to set the device owner because there are already some accounts on the device`

## ✅ **SOLUSI BERDASARKAN PRIORITAS**

### **SOLUSI 1: Remove Accounts Via ADB (RECOMMENDED)**
```bash
# 1. Cek akun yang ada
adb shell dumpsys account

# 2. Remove Google Account
adb shell pm uninstall --user 0 com.google.android.gms
adb shell pm disable-user --user 0 com.google.android.gms

# 3. Remove semua akun (hati-hati!)
adb shell content delete --uri content://com.android.providers.contacts/accounts

# 4. Clear account data
adb shell pm clear com.android.providers.contacts
adb shell pm clear com.google.android.gms

# 5. Disable account managers
adb shell pm disable-user --user 0 com.android.contacts
adb shell pm disable-user --user 0 com.google.android.contacts

# 6. Set device owner
adb shell dpm set-device-owner nu.brandrisk.kioskmode/.KioskDeviceAdminReceiver
```

### **SOLUSI 2: Manual Account Removal**
```bash
# Remove specific accounts
adb shell am start -a android.settings.SYNC_SETTINGS
# Manual remove accounts from Settings > Accounts

# Then try device owner setup
adb shell dpm set-device-owner nu.brandrisk.kioskmode/.KioskDeviceAdminReceiver
```

### **SOLUSI 3: Temporary Disable Account Services**
```bash
# Disable account-related services temporarily
adb shell pm disable-user --user 0 com.google.android.gms
adb shell pm disable-user --user 0 com.android.vending
adb shell pm disable-user --user 0 com.whatsapp

# Set device owner
adb shell dpm set-device-owner nu.brandrisk.kioskmode/.KioskDeviceAdminReceiver

# Re-enable services (optional)
adb shell pm enable com.google.android.gms
adb shell pm enable com.android.vending
```

### **SOLUSI 4: Factory Reset (Ultimate Solution)**
```bash
# Backup data first!
adb backup -all

# Factory reset
adb reboot bootloader
# atau Settings > System > Reset options > Erase all data

# Setup device owner immediately after setup
adb shell dpm set-device-owner nu.brandrisk.kioskmode/.KioskDeviceAdminReceiver
```

## 🔧 **AUTOMATION SCRIPT**

### **auto_setup_device_owner.bat**
```batch
@echo off
echo ========================================
echo   ANDROID KIOSK - DEVICE OWNER SETUP
echo ========================================

echo Step 1: Checking ADB connection...
adb devices

echo Step 2: Removing existing accounts...
adb shell pm disable-user --user 0 com.google.android.gms
adb shell pm clear com.android.providers.contacts
adb shell content delete --uri content://com.android.providers.contacts/accounts

echo Step 3: Setting device owner...
adb shell dpm set-device-owner nu.brandrisk.kioskmode/.KioskDeviceAdminReceiver

echo Step 4: Verifying device owner status...
adb shell dpm list-owners

echo Done! Device owner setup completed.
pause
```

## 🎯 **WORKAROUND DALAM KODE**

### **1. Update EnterpriseBootManager.kt**
```kotlin
/**
 * Check if device can be set as device owner
 */
fun canSetDeviceOwner(): Boolean {
    return try {
        // Check if any accounts exist
        val accountManager = AccountManager.get(context)
        val accounts = accountManager.accounts
        
        if (accounts.isNotEmpty()) {
            android.util.Log.w("EnterpriseBootManager", 
                "Cannot set device owner: ${accounts.size} accounts found")
            return false
        }
        true
    } catch (e: Exception) {
        false
    }
}

/**
 * Auto remove accounts if possible (requires system permissions)
 */
suspend fun autoRemoveAccounts(): Boolean = withContext(Dispatchers.IO) {
    return@withContext try {
        if (isDeviceOwner()) {
            // If already device owner, can remove accounts
            val accountManager = AccountManager.get(context)
            val accounts = accountManager.accounts
            
            for (account in accounts) {
                try {
                    accountManager.removeAccountExplicitly(account)
                    android.util.Log.i("EnterpriseBootManager", "Removed account: ${account.name}")
                } catch (e: Exception) {
                    android.util.Log.w("EnterpriseBootManager", "Failed to remove account: ${account.name}")
                }
            }
            true
        } else {
            false
        }
    } catch (e: Exception) {
        android.util.Log.e("EnterpriseBootManager", "Failed to remove accounts", e)
        false
    }
}
```

### **2. Update ConfigViewModel.kt - Smart Account Detection**
```kotlin
fun checkDeviceOwnerEligibility(): DeviceOwnerStatus {
    return try {
        val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        
        when {
            devicePolicyManager.isDeviceOwnerApp(context.packageName) -> {
                DeviceOwnerStatus.ALREADY_DEVICE_OWNER
            }
            hasExistingAccounts() -> {
                DeviceOwnerStatus.ACCOUNTS_EXIST
            }
            else -> {
                DeviceOwnerStatus.READY_FOR_SETUP
            }
        }
    } catch (e: Exception) {
        DeviceOwnerStatus.ERROR
    }
}

private fun hasExistingAccounts(): Boolean {
    return try {
        val accountManager = AccountManager.get(context)
        accountManager.accounts.isNotEmpty()
    } catch (e: Exception) {
        false
    }
}

enum class DeviceOwnerStatus {
    ALREADY_DEVICE_OWNER,
    READY_FOR_SETUP,
    ACCOUNTS_EXIST,
    ERROR
}
```

## 📱 **UI UPDATE - Smart Setup Guide**
```kotlin
@Composable
fun SmartAdbSetupScreen() {
    val deviceOwnerStatus = viewModel.checkDeviceOwnerEligibility()
    
    when (deviceOwnerStatus) {
        DeviceOwnerStatus.ACCOUNTS_EXIST -> {
            AccountRemovalGuide()
        }
        DeviceOwnerStatus.READY_FOR_SETUP -> {
            StandardAdbSetup()
        }
        DeviceOwnerStatus.ALREADY_DEVICE_OWNER -> {
            AlreadySetupMessage()
        }
        else -> {
            ErrorMessage()
        }
    }
}

@Composable
fun AccountRemovalGuide() {
    Column {
        Text("⚠️ Accounts Detected", fontWeight = FontWeight.Bold, color = Color.Red)
        Text("Device has existing accounts. Please remove them first:")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Option 1: Manual Removal", fontWeight = FontWeight.Bold)
        Text("Settings > Accounts > Remove all accounts")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Option 2: ADB Command", fontWeight = FontWeight.Bold)
        SelectableText("adb shell pm disable-user --user 0 com.google.android.gms")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Option 3: Factory Reset", fontWeight = FontWeight.Bold)
        Text("Settings > System > Reset options > Erase all data")
    }
}
```

## 💡 **PRO TIPS**

1. **Urutan Setup yang Benar:**
   - Fresh device atau factory reset
   - Skip Google account setup
   - Enable Developer options
   - Enable USB debugging
   - Set device owner
   - Login ke akun setelah device owner aktif

2. **Kalau Udah Terlanjur Setup:**
   - Gunakan ADB commands di atas
   - Atau factory reset (paling aman)

3. **Untuk Testing:**
   - Gunakan Android emulator tanpa Google Play
   - Atau device bekas yang bisa di-reset

## ⚡ **QUICK FIX COMMAND**
```bash
# One-liner untuk remove accounts dan setup device owner
adb shell pm disable-user --user 0 com.google.android.gms && adb shell pm clear com.android.providers.contacts && adb shell dpm set-device-owner nu.brandrisk.kioskmode/.KioskDeviceAdminReceiver
```

Coba solusi di atas bro! Yang paling aman adalah factory reset, tapi kalau mau coba remove accounts dulu via ADB, bisa dicoba. 

Mau saya implementasi workaround dalam kode juga untuk deteksi dan guide user secara otomatis?
