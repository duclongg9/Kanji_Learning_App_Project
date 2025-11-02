package com.example.kanjilearning.data.repository

import com.example.kanjilearning.data.dao.KanjiDao
import com.example.kanjilearning.data.model.KanjiEntity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * VI: Implementation dùng Room DAO. Có thể mở rộng để thêm remote sau này.
 * EN: Concrete repository using the Room DAO; easy to swap for other data sources.
 */
@Singleton
class KanjiRepositoryImpl @Inject constructor(
    private val kanjiDao: KanjiDao
) : KanjiRepository {

    override fun getAll(): Flow<List<KanjiEntity>> = kanjiDao.getAllKanjis()
}
