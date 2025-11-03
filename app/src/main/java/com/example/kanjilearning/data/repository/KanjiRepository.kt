package com.example.kanjilearning.data.repository

import com.example.kanjilearning.data.model.KanjiEntity
import kotlinx.coroutines.flow.Flow

/**
 * VI: Repository pattern để ViewModel không biết tới chi tiết DAO/Room.
 * EN: Repository abstraction shielding the DAO from the rest of the app.
 */
interface KanjiRepository {
    fun getAll(): Flow<List<KanjiEntity>>
}
