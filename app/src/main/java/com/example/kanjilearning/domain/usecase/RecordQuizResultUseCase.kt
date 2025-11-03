package com.example.kanjilearning.domain.usecase

import com.example.kanjilearning.data.repository.LearningRepository
import javax.inject.Inject

/**
 * VI: Lưu kết quả quiz để cập nhật tiến độ.
 * EN: Persists quiz results back to the repository.
 */
class RecordQuizResultUseCase @Inject constructor(
    private val repository: LearningRepository
) {
    suspend operator fun invoke(lessonId: Long, score: Int, total: Int) {
        repository.recordQuizResult(lessonId, score, total)
    }
}
