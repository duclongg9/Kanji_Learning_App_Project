package com.example.kanjilearning.data.repository

import com.example.kanjilearning.domain.model.CourseDetail
import com.example.kanjilearning.domain.model.CourseItem
import com.example.kanjilearning.domain.model.LessonDetailModel
import com.example.kanjilearning.domain.model.PaymentReceipt
import com.example.kanjilearning.domain.model.QuizSession
import kotlinx.coroutines.flow.Flow

/**
 * VI: Định nghĩa hợp đồng dữ liệu cho toàn bộ luồng học Kanji.
 * EN: Repository interface consumed by the domain layer.
 */
interface LearningRepository {
    fun observeCourses(): Flow<List<CourseItem>>
    fun observeCourseDetail(courseId: Long): Flow<CourseDetail>
    fun observeLessonDetail(lessonId: Long): Flow<LessonDetailModel>
    suspend fun unlockCourseWithMomo(courseId: Long, phoneNumber: String): PaymentReceipt
    suspend fun loadQuiz(lessonId: Long): QuizSession
    suspend fun recordQuizResult(lessonId: Long, score: Int, total: Int)
}
