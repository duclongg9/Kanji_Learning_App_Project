package com.example.kanjilearning.domain.usecase

import com.example.kanjilearning.data.repository.LearningRepository
import com.example.kanjilearning.domain.model.CourseDetail
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * VI: UseCase cho màn chi tiết khoá học.
 * EN: Streams a specific course with its lessons.
 */
class ObserveCourseDetailUseCase @Inject constructor(
    private val repository: LearningRepository
) {
    operator fun invoke(courseId: Long): Flow<CourseDetail> = repository.observeCourseDetail(courseId)
}
