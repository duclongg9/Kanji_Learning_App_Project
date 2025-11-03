package com.example.kanjilearning.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * VI: Bảng Kanji lưu toàn bộ thông tin giúp người học ghi nhớ ngữ nghĩa, âm đọc và ví dụ.
 * EN: Kanji table holding the rich information (meanings, readings, examples) used across the lessons.
 */
@Entity(tableName = "kanjis")
data class KanjiEntity(
    /** VI: ID cố định đồng bộ với file schema.sql. EN: Stable identifier matching the seed SQL. */
    @PrimaryKey
    @ColumnInfo(name = "kanji_id")
    val kanjiId: Long,
    /** VI: Chữ Kanji hiển thị chính. EN: Core Kanji character shown to learners. */
    @ColumnInfo(name = "character")
    val character: String,
    /** VI: Nghĩa tiếng Việt. EN: Vietnamese meaning for localisation. */
    @ColumnInfo(name = "meaning_vi")
    val meaningVi: String,
    /** VI: Nghĩa tiếng Anh. EN: English meaning as an additional hint. */
    @ColumnInfo(name = "meaning_en")
    val meaningEn: String,
    /** VI: Âm On. EN: Onyomi reading. */
    @ColumnInfo(name = "onyomi")
    val onyomi: String,
    /** VI: Âm Kun. EN: Kunyomi reading. */
    @ColumnInfo(name = "kunyomi")
    val kunyomi: String,
    /** VI: Số nét viết. EN: Stroke count metadata. */
    @ColumnInfo(name = "stroke_count")
    val strokeCount: Int,
    /** VI: Cấp độ JLPT. EN: JLPT proficiency level tag. */
    @ColumnInfo(name = "jlpt_level")
    val jlptLevel: String,
    /** VI: Ví dụ sử dụng Kanji. EN: Example phrase using the Kanji. */
    @ColumnInfo(name = "example")
    val example: String,
    /** VI: Giải nghĩa ví dụ. EN: Translation for the example phrase. */
    @ColumnInfo(name = "example_translation")
    val exampleTranslation: String
)
