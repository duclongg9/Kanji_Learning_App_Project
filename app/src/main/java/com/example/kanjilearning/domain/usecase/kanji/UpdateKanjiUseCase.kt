package com.example.kanjilearning.domain.usecase.kanji

import com.example.kanjilearning.domain.model.Kanji
import com.example.kanjilearning.domain.repository.KanjiRepository
import javax.inject.Inject

/**
 * VI: Use case cập nhật thông tin Kanji hiện có.
 */
class UpdateKanjiUseCase @Inject constructor(
    private val repository: KanjiRepository
) {

    /**
     * VI: Ghi đè nội dung Kanji với dữ liệu mới.
     */
    suspend operator fun invoke(kanji: Kanji) = repository.updateKanji(kanji)
}
