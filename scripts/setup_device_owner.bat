@echo off
echo ========================================
echo   ANDROID KIOSK - DEVICE OWNER SETUP
echo   Enhanced Account Management
echo ========================================

echo.
echo Checking ADB connection...
adb devices
if %ERRORLEVEL% neq 0 (
    echo ERROR: ADB not found or device not connected!
    echo Please ensure:
    echo - Android device is connected via USB
    echo - USB debugging is enabled
    echo - ADB is installed and in PATH
    pause
    exit /b 1
)

echo.
echo Step 1: Checking current device owner status...
adb shell dpm list-owners

echo.
echo Step 2: Backing up current accounts (optional)...
adb shell dumpsys account > account_backup.txt
echo Account backup saved to account_backup.txt

echo.
echo Step 3: SMART Account Management...
echo ⚠️  WARNING: This will temporarily disable Google services to setup device owner
echo ✅ Google services will be re-enabled after setup for login capability
echo.
echo Temporarily disabling Google Play Services...
adb shell pm disable-user --user 0 com.google.android.gms
echo Temporarily disabling Google Service Framework...
adb shell pm disable-user --user 0 com.google.android.gsf
echo Temporarily disabling Play Store...
adb shell pm disable-user --user 0 com.android.vending
echo Clearing contacts provider cache...
adb shell pm clear com.android.providers.contacts

echo.
echo Step 4: Setting device owner...
echo ⏳ Attempting device owner setup...
adb shell dpm set-device-owner nu.brandrisk.kioskmode/.KioskDeviceAdminReceiver

if %ERRORLEVEL% equ 0 (
    echo.
    echo ✅ SUCCESS! Device owner setup completed successfully!
    echo.
    echo Step 5: Verifying device owner status...
    adb shell dpm list-owners
    echo.
    echo Step 6: 🔄 RE-ENABLING Google Services for Account Login...
    echo 📱 You can now login to Google/WhatsApp accounts after kiosk setup!
    adb shell pm enable com.google.android.gms
    adb shell pm enable com.google.android.gsf  
    adb shell pm enable com.android.vending
    echo.
    echo 🎉 DEVICE OWNER SETUP COMPLETE!
    echo ✅ Kiosk mode configured with account login support
    echo 📲 You can now login to Google/WhatsApp accounts normally
) else (
    echo.
    echo ❌ FAILED to set device owner!
    echo.
    echo 🔄 Re-enabling services...
    adb shell pm enable com.google.android.gms
    adb shell pm enable com.google.android.gsf
    adb shell pm enable com.android.vending
    echo.
    echo Possible causes:
    echo - Device still has active user accounts
    echo - Device is not in a fresh state
    echo - Missing permissions
    echo.
    echo Solutions:
    echo 1. 🏭 Factory reset device (Settings > System > Reset)
    echo 2. 📱 Manually remove all accounts (Settings > Accounts)
    echo 3. 🔄 Try running this script again
    echo.
    echo For complete factory reset: adb reboot bootloader
)

echo.
pause
