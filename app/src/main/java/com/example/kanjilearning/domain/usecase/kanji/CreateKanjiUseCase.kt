package com.example.kanjilearning.domain.usecase.kanji

import com.example.kanjilearning.domain.model.Kanji
import com.example.kanjilearning.domain.repository.KanjiRepository
import javax.inject.Inject

/**
 * VI: Use case tạo mới một Kanji theo yêu cầu của admin.
 */
class CreateKanjiUseCase @Inject constructor(
    private val repository: KanjiRepository
) {

    /**
     * VI: Lưu Kanji mới và trả về ID cụm trình độ.
     */
    suspend operator fun invoke(kanji: Kanji): Long = repository.createKanji(kanji)
}
