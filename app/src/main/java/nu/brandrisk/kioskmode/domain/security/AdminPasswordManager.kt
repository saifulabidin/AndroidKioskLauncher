package nu.brandrisk.kioskmode.domain.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enterprise Admin Password Manager
 * Manages secure admin password with default "0000" and change capability
 * Similar to SureLock's password management system
 */
@Singleton
class AdminPasswordManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "enterprise_admin_prefs"
        private const val KEY_PASSWORD_HASH = "admin_password_hash"
        private const val KEY_PASSWORD_SALT = "admin_password_salt"
        private const val KEY_IS_DEFAULT = "is_default_password"
        private const val KEY_ATTEMPT_COUNT = "failed_attempts"
        private const val KEY_LOCKOUT_TIME = "lockout_until"
        
        private const val DEFAULT_PASSWORD = "0000"
        private const val MAX_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 30 * 60 * 1000L // 30 minutes
    }
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback to regular SharedPreferences if encryption fails
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }
    
    /**
     * Initialize admin password system
     * Sets default password "0000" if not already set
     */
    suspend fun initializeAdminPassword(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!isPasswordSet()) {
                setPassword(DEFAULT_PASSWORD, isDefault = true)
            }
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Verify admin password
     * Returns true if password is correct
     */
    suspend fun verifyPassword(inputPassword: String): Boolean = withContext(Dispatchers.IO) {
        try {
            // Check if currently locked out
            if (isLockedOut()) {
                return@withContext false
            }
            
            val storedHash = sharedPreferences.getString(KEY_PASSWORD_HASH, null)
            val storedSalt = sharedPreferences.getString(KEY_PASSWORD_SALT, null)
            
            if (storedHash == null || storedSalt == null) {
                return@withContext false
            }
            
            val inputHash = hashPassword(inputPassword, storedSalt)
            val isCorrect = inputHash == storedHash
            
            if (isCorrect) {
                // Reset attempt counter on successful login
                sharedPreferences.edit()
                    .putInt(KEY_ATTEMPT_COUNT, 0)
                    .apply()
            } else {
                // Increment failed attempts
                incrementFailedAttempts()
            }
            
            isCorrect
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Change admin password
     * Returns true if password was changed successfully
     */
    suspend fun changePassword(oldPassword: String, newPassword: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (!verifyPassword(oldPassword)) {
                return@withContext false
            }
            
            if (newPassword.length < 4) {
                return@withContext false // Minimum 4 characters
            }
            
            setPassword(newPassword, isDefault = false)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Force reset password to default (emergency function)
     * Only available through specific admin procedures
     */
    suspend fun resetToDefault(): Boolean = withContext(Dispatchers.IO) {
        try {
            setPassword(DEFAULT_PASSWORD, isDefault = true)
            sharedPreferences.edit()
                .putInt(KEY_ATTEMPT_COUNT, 0)
                .putLong(KEY_LOCKOUT_TIME, 0)
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Check if using default password
     */
    fun isUsingDefaultPassword(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_DEFAULT, true)
    }
    
    /**
     * Check if currently locked out due to failed attempts
     */
    fun isLockedOut(): Boolean {
        val lockoutUntil = sharedPreferences.getLong(KEY_LOCKOUT_TIME, 0)
        return System.currentTimeMillis() < lockoutUntil
    }
    
    /**
     * Get remaining lockout time in milliseconds
     */
    fun getRemainingLockoutTime(): Long {
        val lockoutUntil = sharedPreferences.getLong(KEY_LOCKOUT_TIME, 0)
        val remaining = lockoutUntil - System.currentTimeMillis()
        return if (remaining > 0) remaining else 0
    }
    
    /**
     * Get current failed attempt count
     */
    fun getFailedAttemptCount(): Int {
        return sharedPreferences.getInt(KEY_ATTEMPT_COUNT, 0)
    }
    
    /**
     * Get max allowed attempts
     */
    fun getMaxAttempts(): Int = MAX_ATTEMPTS
    
    // Private helper methods
    private fun isPasswordSet(): Boolean {
        return sharedPreferences.getString(KEY_PASSWORD_HASH, null) != null
    }
    
    private fun setPassword(password: String, isDefault: Boolean) {
        val salt = generateSalt()
        val hash = hashPassword(password, salt)
        
        sharedPreferences.edit()
            .putString(KEY_PASSWORD_HASH, hash)
            .putString(KEY_PASSWORD_SALT, salt)
            .putBoolean(KEY_IS_DEFAULT, isDefault)
            .apply()
    }
    
    private fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(32)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }
    
    private fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val combined = "$password$salt".toByteArray()
        val hash = digest.digest(combined)
        return hash.joinToString("") { "%02x".format(it) }
    }
    
    private fun incrementFailedAttempts() {
        val currentAttempts = sharedPreferences.getInt(KEY_ATTEMPT_COUNT, 0) + 1
        
        val editor = sharedPreferences.edit()
            .putInt(KEY_ATTEMPT_COUNT, currentAttempts)
        
        // Lock out if max attempts reached
        if (currentAttempts >= MAX_ATTEMPTS) {
            val lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            editor.putLong(KEY_LOCKOUT_TIME, lockoutUntil)
        }
        
        editor.apply()
    }
}

/**
 * Data class for password validation result
 */
data class PasswordValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val attemptsRemaining: Int = 0,
    val lockoutTimeRemaining: Long = 0
)
