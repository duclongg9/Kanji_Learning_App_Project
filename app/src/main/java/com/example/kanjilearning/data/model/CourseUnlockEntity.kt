package com.example.kanjilearning.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * VI: Thể hiện trạng thái mở khóa khoá học (FREE, UNLOCKED, LOCKED).
 * EN: Tracks whether a course is unlocked and by which payment method.
 */
@Entity(tableName = "course_unlocks")
data class CourseUnlockEntity(
    @PrimaryKey
    @ColumnInfo(name = "course_id")
    val courseId: Long,
    @ColumnInfo(name = "status")
    val status: String,
    @ColumnInfo(name = "payment_method")
    val paymentMethod: String?,
    @ColumnInfo(name = "transaction_reference")
    val transactionReference: String?,
    @ColumnInfo(name = "unlocked_at")
    val unlockedAt: Long?
)
