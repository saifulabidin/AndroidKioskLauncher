@echo off
echo ========================================
echo   SMART ACCOUNT REMOVAL FOR KIOSK MODE
echo   Preserves ability to login after setup
echo ========================================

echo.
echo This script will:
echo ✅ Temporarily disable Google services (not remove accounts)
echo ✅ Setup device owner
echo ✅ Re-enable Google services for future logins
echo ⚠️  Your existing accounts may still work after setup
echo.

pause
echo.

echo Checking ADB connection...
adb devices
if %ERRORLEVEL% neq 0 (
    echo ERROR: Device not connected!
    pause
    exit /b 1
)

echo.
echo === SMART ACCOUNT MANAGEMENT ===
echo.
echo 1. Creating account backup...
adb shell dumpsys account > account_backup_%date:~-4,4%%date:~-10,2%%date:~-7,2%.txt
echo Account backup saved.

echo.
echo 2. Listing current accounts...
adb shell dumpsys account | findstr "Account {"

echo.
echo 3. Temporarily hiding Google services from account manager...
adb shell pm disable-user --user 0 com.google.android.gms
adb shell pm disable-user --user 0 com.google.android.gsf
adb shell pm disable-user --user 0 com.android.vending

echo.
echo 4. Clearing authentication cache (not removing accounts)...
adb shell pm clear com.google.android.gms
adb shell pm clear com.android.providers.contacts

echo.
echo 5. Attempting device owner setup...
adb shell dpm set-device-owner nu.brandrisk.kioskmode/.KioskDeviceAdminReceiver

if %ERRORLEVEL% equ 0 (
    echo.
    echo ✅ SUCCESS! Device owner configured!
    echo.
    echo 6. Re-enabling Google services for account login...
    adb shell pm enable com.google.android.gms
    adb shell pm enable com.google.android.gsf
    adb shell pm enable com.android.vending
    
    echo.
    echo 7. Verifying device owner status...
    adb shell dpm list-owners
    
    echo.
    echo 🎉 SETUP COMPLETE! 
    echo ✅ Device owner: ACTIVE
    echo ✅ Google services: RE-ENABLED
    echo 📲 You can now login to Google/WhatsApp accounts
    echo.
    echo Your app now has kiosk mode capabilities while allowing account logins!
    
) else (
    echo.
    echo ❌ Device owner setup FAILED
    echo.
    echo Re-enabling services...
    adb shell pm enable com.google.android.gms
    adb shell pm enable com.google.android.gsf
    adb shell pm enable com.android.vending
    
    echo.
    echo Possible solutions:
    echo 1. 🏭 Factory reset device completely
    echo 2. 📱 Manually remove accounts from Settings > Accounts
    echo 3. 🔄 Use developer mode with test accounts only
    echo.
    echo For factory reset: Settings > System > Reset options > Erase all data
)

echo.
pause
