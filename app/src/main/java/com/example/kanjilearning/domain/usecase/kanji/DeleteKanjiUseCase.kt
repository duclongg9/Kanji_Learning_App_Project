package com.example.kanjilearning.domain.usecase.kanji

import com.example.kanjilearning.domain.repository.KanjiRepository
import javax.inject.Inject

/**
 * VI: Use case xoá Kanji khỏi danh mục quản trị.
 */
class DeleteKanjiUseCase @Inject constructor(
    private val repository: KanjiRepository
) {

    /**
     * VI: Thực thi xoá Kanji theo ID.
     */
    suspend operator fun invoke(id: Long) = repository.deleteKanji(id)
}
