package com.example.kanjilearning.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kanjilearning.domain.model.User
import com.example.kanjilearning.domain.util.Role

/**
 * VI: Bảng Room lưu thông tin người dùng Google đăng nhập gần nhất.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    @ColumnInfo(name = "google_id")
    val googleId: String,
    @ColumnInfo(name = "display_name")
    val displayName: String,
    @ColumnInfo(name = "email")
    val email: String,
    @ColumnInfo(name = "role")
    val role: Int,
    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long
) {
    /**
     * VI: Map entity sang domain model.
     */
    fun toDomain(): User = User(
        googleId = googleId,
        displayName = displayName,
        email = email,
        role = Role.fromId(role),
        lastSyncedAt = lastSyncedAt
    )

    companion object {
        /**
         * VI: Tạo entity từ domain model để lưu xuống Room.
         */
        fun fromDomain(user: User): UserEntity = UserEntity(
            googleId = user.googleId,
            displayName = user.displayName,
            email = user.email,
            role = user.role.id,
            lastSyncedAt = user.lastSyncedAt
        )
    }
}
