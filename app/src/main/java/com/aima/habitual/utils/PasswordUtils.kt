package com.aima.habitual.utils

import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

/**
 * Utility object for secure password hashing using SHA-256 with a random salt.
 *
 * VIVA EXPLANATION:
 * Plain-text password storage is a critical security vulnerability. Even for local storage,
 * we hash passwords so that if the device is compromised, the actual password cannot be
 * recovered. A random 'salt' is prepended to each password before hashing to prevent
 * rainbow table attacks where pre-computed hash dictionaries are used to reverse common passwords.
 */
object PasswordUtils {

    private const val SALT_LENGTH = 16

    /**
     * Generates a cryptographically secure random salt.
     * Each user registration produces a unique salt, stored alongside the hash.
     */
    fun generateSalt(): String {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    /**
     * Hashes a password with the given salt using SHA-256.
     *
     * @param password The plain-text password entered by the user.
     * @param salt The unique salt generated during registration.
     * @return A Base64-encoded string of the SHA-256 hash.
     */
    fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        // Combine salt + password before hashing
        val saltedPassword = salt + password
        val hashBytes = digest.digest(saltedPassword.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(hashBytes, Base64.NO_WRAP)
    }

    /**
     * Verifies a login attempt by hashing the input with the stored salt
     * and comparing it against the stored hash.
     *
     * @param inputPassword The plain-text password entered during login.
     * @param storedSalt The salt that was saved during registration.
     * @param storedHash The hash that was saved during registration.
     * @return True if the password matches, false otherwise.
     */
    fun verifyPassword(inputPassword: String, storedSalt: String, storedHash: String): Boolean {
        val inputHash = hashPassword(inputPassword, storedSalt)
        return MessageDigest.isEqual(
            inputHash.toByteArray(Charsets.UTF_8),
            storedHash.toByteArray(Charsets.UTF_8)
        )
    }
}
