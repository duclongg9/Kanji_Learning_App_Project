package com.example.kanjilearning.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * VI: Bảng phụ lesson <-> kanji để mô tả mỗi bài học bao gồm các chữ nào.
 * EN: Junction table linking lessons to individual kanji.
 */
@Entity(
    tableName = "lesson_kanji_cross_ref",
    primaryKeys = ["lesson_id", "kanji_id"]
)
data class LessonKanjiCrossRef(
    @ColumnInfo(name = "lesson_id")
    val lessonId: Long,
    @ColumnInfo(name = "kanji_id")
    val kanjiId: Long,
    /** VI: Thứ tự gợi ý hiển thị. EN: Suggested display order inside lesson. */
    @ColumnInfo(name = "position")
    val position: Int
)
