package com.example.kanjilearning.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * VI: Khoá học gom các lesson theo chủ đề/cấp độ để người học dễ định hướng.
 * EN: Course entity describing themed packs of kanji lessons.
 */
@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey
    @ColumnInfo(name = "course_id")
    val courseId: Long,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "slug")
    val slug: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "level_tag")
    val levelTag: String,
    @ColumnInfo(name = "level_order")
    val levelOrder: Int,
    @ColumnInfo(name = "cover_asset")
    val coverAsset: String,
    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,
    @ColumnInfo(name = "is_premium")
    val isPremium: Boolean,
    @ColumnInfo(name = "price_vnd")
    val priceVnd: Int
)
