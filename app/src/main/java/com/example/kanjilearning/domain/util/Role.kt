package com.example.kanjilearning.domain.util

/**
 * VI: Role người dùng trong hệ thống để điều hướng đúng màn hình.
 */
enum class Role(val id: Int) {
    ADMIN(1),
    FREE(2),
    VIP(3);

    companion object {
        /**
         * VI: Đọc role từ server/DB; nếu null trả về FREE để đảm bảo offline vẫn vào được app.
         */
        fun fromId(value: Int?): Role = when (value) {
            ADMIN.id -> ADMIN
            VIP.id -> VIP
            else -> FREE
        }
    }
}
