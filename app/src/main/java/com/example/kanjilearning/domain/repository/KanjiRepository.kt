package com.example.kanjilearning.domain.repository

import com.example.kanjilearning.domain.model.Kanji
import com.example.kanjilearning.domain.util.AccessTier
import com.example.kanjilearning.domain.util.JlptLevel
import kotlinx.coroutines.flow.Flow

/**
 * VI: Repository cung cấp dữ liệu Kanji cho domain/presentation.
 */
interface KanjiRepository {

    /**
     * VI: Lấy danh sách Kanji tuỳ theo role và bộ lọc.
     */
    fun observeKanji(
        jlptLevel: JlptLevel?,
        minDifficulty: Int,
        maxDifficulty: Int,
        allowedTiers: List<AccessTier>
    ): Flow<List<Kanji>>

    /**
     * VI: Import danh sách Kanji mới (dành cho admin).
     */
    suspend fun importKanji(items: List<Kanji>)
}
