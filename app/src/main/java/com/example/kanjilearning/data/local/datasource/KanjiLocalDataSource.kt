package com.example.kanjilearning.data.local.datasource

import com.example.kanjilearning.data.local.dao.KanjiDao
import com.example.kanjilearning.data.local.entity.KanjiEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VI: DataSource gói logic làm việc với Room cho Kanji.
 */
@Singleton
class KanjiLocalDataSource @Inject constructor(
    private val kanjiDao: KanjiDao
) {

    /**
     * VI: Lấy luồng Kanji theo bộ lọc.
     */
    fun observeKanji(
        jlptLevel: String?,
        minDifficulty: Int,
        maxDifficulty: Int,
        tiers: List<String>
    ): Flow<List<KanjiEntity>> = kanjiDao.observeKanji(jlptLevel, minDifficulty, maxDifficulty, tiers)

    /**
     * VI: Import dữ liệu mới.
     */
    suspend fun importKanji(items: List<KanjiEntity>) {
        kanjiDao.upsertAll(items)
    }
}
