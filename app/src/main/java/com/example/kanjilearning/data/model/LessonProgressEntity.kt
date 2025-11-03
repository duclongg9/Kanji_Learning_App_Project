package com.example.kanjilearning.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * VI: Lưu tiến độ luyện tập từng lesson để hiển thị % hoàn thành.
 * EN: Persists lesson progress and best score for the UI.
 */
@Entity(tableName = "lesson_progress")
data class LessonProgressEntity(
    @PrimaryKey
    @ColumnInfo(name = "lesson_id")
    val lessonId: Long,
    @ColumnInfo(name = "best_score")
    val bestScore: Int,
    @ColumnInfo(name = "last_score")
    val lastScore: Int,
    @ColumnInfo(name = "completed")
    val completed: Boolean,
    @ColumnInfo(name = "review_count")
    val reviewCount: Int,
    @ColumnInfo(name = "last_reviewed_at")
    val lastReviewedAt: Long?
)
