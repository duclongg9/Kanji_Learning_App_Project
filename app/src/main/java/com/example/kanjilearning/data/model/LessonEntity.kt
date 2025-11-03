package com.example.kanjilearning.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * VI: Mỗi lesson tập trung vào một nhóm kanji nhỏ, có mô tả và số câu hỏi luyện.
 * EN: Lesson entity grouping a subset of kanji with quiz metadata.
 */
@Entity(tableName = "lessons")
data class LessonEntity(
    @PrimaryKey
    @ColumnInfo(name = "lesson_id")
    val lessonId: Long,
    @ColumnInfo(name = "course_id")
    val courseId: Long,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "summary")
    val summary: String,
    @ColumnInfo(name = "order_index")
    val orderIndex: Int,
    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,
    @ColumnInfo(name = "question_count")
    val questionCount: Int
)
