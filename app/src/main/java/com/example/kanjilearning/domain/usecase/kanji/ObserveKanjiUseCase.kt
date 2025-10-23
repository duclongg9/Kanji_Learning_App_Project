package com.example.kanjilearning.domain.usecase.kanji

import com.example.kanjilearning.domain.model.Kanji
import com.example.kanjilearning.domain.repository.KanjiRepository
import com.example.kanjilearning.domain.util.AccessTier
import com.example.kanjilearning.domain.util.JlptLevel
import com.example.kanjilearning.domain.util.Role
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * VI: UseCase lấy danh sách Kanji theo role để presentation layer dễ dùng.
 */
class ObserveKanjiUseCase @Inject constructor(
    private val repository: KanjiRepository
) {

    /**
     * VI: Người dùng FREE chỉ xem được FREE, VIP xem cả 2.
     */
    operator fun invoke(
        role: Role,
        jlptLevel: JlptLevel?,
        minDifficulty: Int,
        maxDifficulty: Int
    ): Flow<List<Kanji>> {
        val tiers = if (role == Role.VIP || role == Role.ADMIN) {
            listOf(AccessTier.FREE, AccessTier.VIP)
        } else {
            listOf(AccessTier.FREE)
        }
        return repository.observeKanji(jlptLevel, minDifficulty, maxDifficulty, tiers)
    }
}
