package com.example.kanjilearning.domain.usecase

import com.example.kanjilearning.data.repository.LearningRepository
import com.example.kanjilearning.domain.model.QuizSession
import javax.inject.Inject

/**
 * VI: Chuẩn bị dữ liệu quiz cho lesson.
 * EN: Loads quiz questions for the quiz screen.
 */
class LoadQuizUseCase @Inject constructor(
    private val repository: LearningRepository
) {
    suspend operator fun invoke(lessonId: Long): QuizSession = repository.loadQuiz(lessonId)
}
