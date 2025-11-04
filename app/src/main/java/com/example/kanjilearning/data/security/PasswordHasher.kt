package com.example.kanjilearning.data.security

import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VI: Hỗ trợ băm mật khẩu bằng SHA-256 và so khớp.
 * EN: Provides SHA-256 hashing utilities for password storage and verification.
 */
@Singleton
class PasswordHasher @Inject constructor() {

    /**
     * VI: Băm chuỗi mật khẩu thành hex SHA-256.
     * EN: Hashes a password string to a SHA-256 hexadecimal digest.
     */
    fun hash(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString(separator = "") { byte ->
            ((byte.toInt() and 0xFF) + 0x100).toString(16).substring(1)
        }
    }

    /**
     * VI: So sánh mật khẩu thuần với hash đã lưu.
     * EN: Compares a plain password with a stored hash.
     */
    fun matches(password: String, storedHash: String): Boolean = hash(password) == storedHash
}
