#!/bin/bash

echo "========================================"
echo "  ANDROID KIOSK - DEVICE OWNER SETUP"
echo "  Automated Account Removal & Setup"
echo "========================================"

echo ""
echo "Checking ADB connection..."
if ! command -v adb &> /dev/null; then
    echo "ERROR: ADB not found!"
    echo "Please install Android SDK platform-tools"
    exit 1
fi

adb devices
if [ $? -ne 0 ]; then
    echo "ERROR: Device not connected or ADB not working!"
    exit 1
fi

echo ""
echo "Step 1: Checking current device owner status..."
adb shell dpm list-owners

echo ""
echo "Step 2: Backing up current accounts (optional)..."
adb shell dumpsys account > account_backup.txt
echo "Account backup saved to account_backup.txt"

echo ""
echo "Step 3: Removing conflicting accounts..."
echo "Disabling Google Play Services..."
adb shell pm disable-user --user 0 com.google.android.gms
echo "Clearing contacts provider..."
adb shell pm clear com.android.providers.contacts
echo "Removing account data..."
adb shell content delete --uri content://com.android.providers.contacts/accounts

echo ""
echo "Step 4: Additional cleanup..."
adb shell pm disable-user --user 0 com.android.vending
adb shell pm disable-user --user 0 com.google.android.gsf

echo ""
echo "Step 5: Setting device owner..."
echo "Running: adb shell dpm set-device-owner nu.brandrisk.kioskmode/.KioskDeviceAdminReceiver"
adb shell dpm set-device-owner nu.brandrisk.kioskmode/.KioskDeviceAdminReceiver

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ SUCCESS! Device owner setup completed successfully!"
    echo ""
    echo "Step 6: Verifying device owner status..."
    adb shell dpm list-owners
    echo ""
    echo "Step 7: Re-enabling services (optional)..."
    echo "Note: You can re-enable Google services after kiosk setup if needed"
    echo "adb shell pm enable com.google.android.gms"
    echo "adb shell pm enable com.android.vending"
    echo ""
    echo "🎉 DEVICE OWNER SETUP COMPLETE!"
    echo "You can now use all kiosk mode features."
else
    echo ""
    echo "❌ FAILED to set device owner!"
    echo ""
    echo "Possible causes:"
    echo "- Some accounts still exist"
    echo "- Device is not in a fresh state"
    echo "- Missing permissions"
    echo ""
    echo "Solutions:"
    echo "1. Factory reset the device"
    echo "2. Remove all accounts manually from Settings"
    echo "3. Try running this script again"
    echo ""
    echo "For factory reset: adb reboot bootloader"
fi

echo ""
read -p "Press Enter to continue..."
