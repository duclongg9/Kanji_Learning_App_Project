package com.example.kanjilearning.domain.model

/**
 * VI: Thông tin role người dùng, dùng cho danh sách lựa chọn khi đăng ký.
 * EN: Describes an available role used for registration choices.
 */
data class UserRole(
    val id: Long,
    val code: String,
    val displayName: String
)

/**
 * VI: Thông tin tài khoản đã xác thực cùng role chính.
 * EN: Represents an authenticated user profile with its primary role.
 */
data class UserAccount(
    val id: Long,
    val email: String,
    val displayName: String,
    val role: UserRole
)
