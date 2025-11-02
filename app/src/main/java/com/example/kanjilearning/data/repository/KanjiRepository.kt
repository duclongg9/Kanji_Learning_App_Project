package com.example.kanjilearning.data.repository

import com.example.kanjilearning.data.dao.KanjiDao
import com.example.kanjilearning.data.model.KanjiEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * VI: Repository đóng vai trò trung gian giữa DAO và tầng ViewModel.
 * EN: Repository that hides the DAO implementation from the rest of the app.
 */
interface KanjiRepository {
    fun getAll(): Flow<List<KanjiEntity>>
}

class KanjiRepositoryImpl @Inject constructor(
    private val kanjiDao: KanjiDao
) : KanjiRepository {
    override fun getAll(): Flow<List<KanjiEntity>> = kanjiDao.getAllKanjis()
}
