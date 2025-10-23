package com.example.kanjilearning.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.kanjilearning.domain.model.Kanji
import com.example.kanjilearning.domain.util.AccessTier
import com.example.kanjilearning.domain.util.JlptLevel

/**
 * VI: Bảng Room lưu trữ Kanji để hỗ trợ offline-first.
 */
@Entity(tableName = "kanji")
data class KanjiEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "character")
    val character: String,
    @ColumnInfo(name = "onyomi")
    val onyomi: String,
    @ColumnInfo(name = "kunyomi")
    val kunyomi: String,
    @ColumnInfo(name = "meaning")
    val meaning: String,
    @ColumnInfo(name = "jlpt_level")
    val jlptLevel: String,
    @ColumnInfo(name = "difficulty")
    val difficulty: Int,
    @ColumnInfo(name = "access_tier")
    val accessTier: String
) {
    /**
     * VI: Chuyển entity thành model domain để hiển thị UI.
     */
    fun toDomain(): Kanji = Kanji(
        id = id,
        character = character,
        onyomi = onyomi,
        kunyomi = kunyomi,
        meaning = meaning,
        jlptLevel = JlptLevel.fromLabel(jlptLevel),
        difficulty = difficulty,
        accessTier = if (accessTier == AccessTier.VIP.name) AccessTier.VIP else AccessTier.FREE
    )

    companion object {
        /**
         * VI: Tạo entity từ dữ liệu domain (ví dụ import CSV → lưu DB).
         */
        fun fromDomain(kanji: Kanji): KanjiEntity = KanjiEntity(
            id = kanji.id,
            character = kanji.character,
            onyomi = kanji.onyomi,
            kunyomi = kanji.kunyomi,
            meaning = kanji.meaning,
            jlptLevel = kanji.jlptLevel.label,
            difficulty = kanji.difficulty,
            accessTier = kanji.accessTier.name
        )
    }
}
