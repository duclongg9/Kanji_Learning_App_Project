package com.example.kanjilearning.data.repository

import android.util.Log
import com.example.kanjilearning.data.local.datasource.KanjiLocalDataSource
import com.example.kanjilearning.data.local.entity.KanjiEntity
import com.example.kanjilearning.data.remote.mysql.KanjiRemoteDataSource
import com.example.kanjilearning.domain.model.Kanji
import com.example.kanjilearning.domain.repository.KanjiRepository
import com.example.kanjilearning.domain.util.AccessTier
import com.example.kanjilearning.domain.util.JlptLevel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VI: Repository triển khai dựa trên Room.
 */
@Singleton
class KanjiRepositoryImpl @Inject constructor(
    private val localDataSource: KanjiLocalDataSource,
    private val remoteDataSource: KanjiRemoteDataSource
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
            .onStart {
                try {
                    val remoteKanji = remoteDataSource.fetchKanjiCatalog(allowedTiers)
                    localDataSource.replaceAll(remoteKanji.map(::normalize))
                } catch (error: Exception) {
                    Log.w(TAG, "Không thể đồng bộ Kanji từ MySQL", error)
                }
            }
            .map { entities -> entities.map(KanjiEntity::toDomain) }
    }

    /**
     * VI: Import danh sách Kanji mới (ví dụ admin upload CSV).
     */
    override suspend fun importKanji(items: List<Kanji>) {
        localDataSource.importKanji(items.map(::normalize))
    }

    override suspend fun createKanji(kanji: Kanji): Long {
        val normalized = kanji.normalizeDifficulty()
        val saved = remoteDataSource.insertKanji(normalized)
        localDataSource.upsert(KanjiEntity.fromDomain(saved))
        return saved.id
    }

    override suspend fun updateKanji(kanji: Kanji) {
        val normalized = kanji.normalizeDifficulty()
        remoteDataSource.updateKanji(normalized)
        localDataSource.upsert(KanjiEntity.fromDomain(normalized))
    }

    override suspend fun deleteKanji(id: Long) {
        remoteDataSource.deleteKanji(id)
        localDataSource.delete(id)
    }

    private fun Kanji.normalizeDifficulty(): Kanji = copy(difficulty = when (jlptLevel) {
        JlptLevel.N5 -> 1
        JlptLevel.N4 -> 3
        JlptLevel.N3 -> 5
        JlptLevel.N2 -> 7
        JlptLevel.N1 -> 9
    })

    private fun normalize(kanji: Kanji): KanjiEntity = KanjiEntity.fromDomain(kanji.normalizeDifficulty())

    companion object {
        private const val TAG = "KanjiRepository"
    }
}
