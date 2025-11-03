package com.example.kanjilearning.domain.usecase

import com.example.kanjilearning.data.repository.LearningRepository
import com.example.kanjilearning.domain.model.LessonDetailModel
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * VI: UseCase cung cấp thông tin Kanji cho màn lesson detail.
 * EN: Streams lesson detail with kanji list.
 */
class ObserveLessonDetailUseCase @Inject constructor(
    private val repository: LearningRepository
) {
    operator fun invoke(lessonId: Long): Flow<LessonDetailModel> = repository.observeLessonDetail(lessonId)
}
