package com.example.kanjilearning.domain.model

import com.example.kanjilearning.domain.util.Role

/**
 * VI: Model domain mô tả người dùng đăng nhập vào hệ thống.
 */
data class User(
    val googleId: String,
    val displayName: String,
    val email: String,
    val role: Role,
    val lastSyncedAt: Long
)
