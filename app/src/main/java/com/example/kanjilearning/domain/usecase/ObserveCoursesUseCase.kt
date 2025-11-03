package com.example.kanjilearning.domain.usecase

import com.example.kanjilearning.data.repository.LearningRepository
import com.example.kanjilearning.domain.model.CourseItem
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * VI: UseCase trả về danh sách khoá học để hiển thị màn dashboard.
 * EN: Streams the list of courses for the dashboard screen.
 */
class ObserveCoursesUseCase @Inject constructor(
    private val repository: LearningRepository
) {
    operator fun invoke(): Flow<List<CourseItem>> = repository.observeCourses()
}
