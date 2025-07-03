package nu.brandrisk.kioskmode.activity

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import nu.brandrisk.kioskmode.util.KioskLogger

/**
 * Transparent overlay activity for enterprise kiosk mode
 * Used for system UI blocking and security overlays
 */
class TransparentOverlayActivity : Activity() {
    
    companion object {
        private const val TAG = "TransparentOverlayActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        KioskLogger.i(TAG, "Transparent overlay activity created")
        
        // Make activity transparent and overlay
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
        
        // Set transparent background
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        
        // Handle overlay logic
        handleOverlayMode()
    }
    
    private fun handleOverlayMode() {
        // Enterprise overlay implementation
        // Block unauthorized access, show security warnings, etc.
        KioskLogger.i(TAG, "Enterprise overlay mode activated")
        
        // Auto-finish after overlay purpose is served
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        KioskLogger.i(TAG, "Transparent overlay activity destroyed")
    }
}
