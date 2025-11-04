package com.example.kanjilearning.data.local

import android.content.Context
import com.example.kanjilearning.domain.model.UserAccount
import com.example.kanjilearning.domain.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VI: Lưu session đăng nhập tại chỗ bằng SharedPreferences.
 * EN: Persists the authenticated session locally via SharedPreferences.
 */
@Singleton
class AuthLocalDataSource @Inject constructor(
    @ApplicationContext context: Context
) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * VI: Đọc session hiện tại từ bộ nhớ.
     * EN: Reads the currently cached session from storage.
     */
    fun loadSession(): UserAccount? {
        val userId = preferences.getLong(KEY_USER_ID, -1L)
        if (userId <= 0L) return null
        val email = preferences.getString(KEY_EMAIL, null) ?: return null
        val displayName = preferences.getString(KEY_DISPLAY_NAME, null) ?: return null
        val roleId = preferences.getLong(KEY_ROLE_ID, -1L)
        val roleCode = preferences.getString(KEY_ROLE_CODE, null) ?: return null
        val roleName = preferences.getString(KEY_ROLE_NAME, null) ?: return null
        return UserAccount(
            id = userId,
            email = email,
            displayName = displayName,
            role = UserRole(
                id = roleId,
                code = roleCode,
                displayName = roleName
            )
        )
    }

    /**
     * VI: Lưu session mới sau khi đăng nhập hoặc đăng ký.
     * EN: Stores the session after login or registration.
     */
    fun saveSession(account: UserAccount) {
        preferences.edit().apply {
            putLong(KEY_USER_ID, account.id)
            putString(KEY_EMAIL, account.email)
            putString(KEY_DISPLAY_NAME, account.displayName)
            putLong(KEY_ROLE_ID, account.role.id)
            putString(KEY_ROLE_CODE, account.role.code)
            putString(KEY_ROLE_NAME, account.role.displayName)
        }.apply()
    }

    /**
     * VI: Xóa session khi người dùng đăng xuất.
     * EN: Clears all persisted session values when logging out.
     */
    fun clearSession() {
        preferences.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "kanji_auth_session"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_ROLE_ID = "role_id"
        private const val KEY_ROLE_CODE = "role_code"
        private const val KEY_ROLE_NAME = "role_name"
    }
}
