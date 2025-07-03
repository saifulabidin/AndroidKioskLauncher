package nu.brandrisk.kioskmode.util

import android.util.Log

/**
 * Enterprise Kiosk Logger - Centralized logging utility for enterprise kiosk application
 * Provides structured logging with different log levels and formatting
 */
object KioskLogger {
    
    private const val TAG_PREFIX = "KioskMode"
    private var isDebugEnabled = true
    
    /**
     * Enable or disable debug logging
     */
    fun setDebugEnabled(enabled: Boolean) {
        isDebugEnabled = enabled
    }
    
    /**
     * Log debug message
     */
    fun d(tag: String, message: String) {
        if (isDebugEnabled) {
            Log.d("$TAG_PREFIX-$tag", message)
        }
    }
    
    /**
     * Log info message
     */
    fun i(tag: String, message: String) {
        Log.i("$TAG_PREFIX-$tag", message)
    }
    
    /**
     * Log warning message
     */
    fun w(tag: String, message: String) {
        Log.w("$TAG_PREFIX-$tag", message)
    }
    
    /**
     * Log warning message with throwable
     */
    fun w(tag: String, message: String, throwable: Throwable) {
        Log.w("$TAG_PREFIX-$tag", message, throwable)
    }
    
    /**
     * Log error message
     */
    fun e(tag: String, message: String) {
        Log.e("$TAG_PREFIX-$tag", message)
    }
    
    /**
     * Log error message with throwable
     */
    fun e(tag: String, message: String, throwable: Throwable) {
        Log.e("$TAG_PREFIX-$tag", message, throwable)
    }
    
    /**
     * Log verbose message
     */
    fun v(tag: String, message: String) {
        if (isDebugEnabled) {
            Log.v("$TAG_PREFIX-$tag", message)
        }
    }
    
    /**
     * Log security event with special formatting
     */
    fun security(tag: String, event: String, details: String = "") {
        val message = if (details.isNotEmpty()) {
            "[SECURITY] $event - $details"
        } else {
            "[SECURITY] $event"
        }
        Log.w("$TAG_PREFIX-SECURITY-$tag", message)
    }
    
    /**
     * Log enterprise event with special formatting
     */
    fun enterprise(tag: String, event: String, details: String = "") {
        val message = if (details.isNotEmpty()) {
            "[ENTERPRISE] $event - $details"
        } else {
            "[ENTERPRISE] $event"
        }
        Log.i("$TAG_PREFIX-ENTERPRISE-$tag", message)
    }
    
    /**
     * Log performance metric
     */
    fun performance(tag: String, operation: String, duration: Long) {
        Log.d("$TAG_PREFIX-PERF-$tag", "[PERFORMANCE] $operation took ${duration}ms")
    }
    
    /**
     * Log with custom format
     */
    fun custom(tag: String, level: String, message: String) {
        Log.i("$TAG_PREFIX-$level-$tag", message)
    }
}
