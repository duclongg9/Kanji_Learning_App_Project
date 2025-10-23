package com.example.kanjilearning.data.repository

import com.example.kanjilearning.data.local.datasource.KanjiLocalDataSource
import com.example.kanjilearning.data.local.entity.KanjiEntity
import com.example.kanjilearning.domain.model.Kanji
import com.example.kanjilearning.domain.repository.KanjiRepository
import com.example.kanjilearning.domain.util.AccessTier
import com.example.kanjilearning.domain.util.JlptLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VI: Repository triển khai dựa trên Room.
 */
@Singleton
class KanjiRepositoryImpl @Inject constructor(
    private val localDataSource: KanjiLocalDataSource
) : KanjiRepository {

    /**
     * VI: Room trả về Flow<Entity>; ta map sang Flow<Domain>.
     */
    override fun observeKanji(
        jlptLevel: JlptLevel?,
        minDifficulty: Int,
        maxDifficulty: Int,
        allowedTiers: List<AccessTier>
    ): Flow<List<Kanji>> {
        val tiers = allowedTiers.map { it.name }
        return localDataSource.observeKanji(jlptLevel?.label, minDifficulty, maxDifficulty, tiers)
            .map { entities -> entities.map(KanjiEntity::toDomain) }
    }

    /**
     * VI: Import danh sách Kanji mới (ví dụ admin upload CSV).
     */
    override suspend fun importKanji(items: List<Kanji>) {
        localDataSource.importKanji(items.map(KanjiEntity.Companion::fromDomain))
    }
}
