package com.example.kanjilearning.data.repository

import android.util.Log
import com.example.kanjilearning.data.mysql.MySqlLearningDataSource
import com.example.kanjilearning.data.sample.SampleLearningContent
import com.example.kanjilearning.domain.model.CourseDetail
import com.example.kanjilearning.domain.model.CourseItem
import com.example.kanjilearning.domain.model.LessonDetailModel
import com.example.kanjilearning.domain.model.PaymentReceipt
import com.example.kanjilearning.domain.model.QuizSession
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * VI: Repository dựa trên MySQL, ánh xạ dữ liệu domain thông qua datasource JDBC.
 * EN: Repository backed by MySQL that delegates to the JDBC-powered data source.
 */
@Singleton
class LearningRepositoryImpl @Inject constructor(
    private val dataSource: MySqlLearningDataSource,
    private val sampleContent: SampleLearningContent
) : LearningRepository {

    private val fallbackEnabled = AtomicBoolean(false)

    /**
     * VI: Quan sát danh sách khoá học, dữ liệu lấy trực tiếp từ MySQL.
     * EN: Observes the course catalog emitted from the MySQL store.
     */
    override fun observeCourses(): Flow<List<CourseItem>> {
        if (fallbackEnabled.get()) return sampleContent.observeCourses()
        return flow {
            runCatching { withContext(Dispatchers.IO) { dataSource.loadCourses() } }
                .onSuccess { emit(it) }
                .onFailure { error ->
                    activateFallback(error)
                    emitAll(sampleContent.observeCourses())
                }
        }
    }

    /**
     * VI: Quan sát chi tiết khoá học cụ thể.
     * EN: Observes a single course detail entry.
     */
    override fun observeCourseDetail(courseId: Long): Flow<CourseDetail> {
        if (fallbackEnabled.get()) return sampleContent.observeCourseDetail(courseId)
        return flow {
            runCatching { withContext(Dispatchers.IO) { dataSource.loadCourseDetail(courseId) } }
                .onSuccess { emit(it) }
                .onFailure { error ->
                    activateFallback(error)
                    emitAll(sampleContent.observeCourseDetail(courseId))
                }
        }
    }

    /**
     * VI: Quan sát chi tiết bài học bao gồm danh sách kanji.
     * EN: Observes a lesson detail enriched with kanji information.
     */
    override fun observeLessonDetail(lessonId: Long): Flow<LessonDetailModel> {
        if (fallbackEnabled.get()) return sampleContent.observeLessonDetail(lessonId)
        return flow {
            runCatching { withContext(Dispatchers.IO) { dataSource.loadLessonDetail(lessonId) } }
                .onSuccess { emit(it) }
                .onFailure { error ->
                    activateFallback(error)
                    emitAll(sampleContent.observeLessonDetail(lessonId))
                }
        }
    }

    /**
     * VI: Mở khoá khoá học qua MoMo và trả về biên nhận.
     * EN: Unlocks a course using MoMo and returns the payment receipt.
     */
    override suspend fun unlockCourseWithMomo(
        courseId: Long,
        phoneNumber: String
    ): PaymentReceipt {
        if (fallbackEnabled.get()) return sampleContent.unlockCourse(courseId, phoneNumber)
        return runCatching {
            withContext(Dispatchers.IO) { dataSource.unlockCourse(courseId, phoneNumber) }
        }.getOrElse { error ->
            activateFallback(error)
            sampleContent.unlockCourse(courseId, phoneNumber)
        }
    }

    /**
     * VI: Tải quiz cho bài học.
     * EN: Loads the quiz session for a lesson.
     */
    override suspend fun loadQuiz(lessonId: Long): QuizSession {
        if (fallbackEnabled.get()) return sampleContent.loadQuizSession(lessonId)
        return runCatching {
            withContext(Dispatchers.IO) { dataSource.loadQuizSession(lessonId) }
        }.getOrElse { error ->
            activateFallback(error)
            sampleContent.loadQuizSession(lessonId)
        }
    }

    /**
     * VI: Ghi nhận kết quả quiz và cập nhật tiến độ.
     * EN: Records the quiz result and updates lesson progress.
     */
    override suspend fun recordQuizResult(lessonId: Long, score: Int, total: Int) {
        if (fallbackEnabled.get()) {
            sampleContent.recordQuizResult(lessonId, score, total)
            return
        }
        runCatching {
            withContext(Dispatchers.IO) { dataSource.recordQuizResult(lessonId, score, total) }
        }.onFailure { error ->
            activateFallback(error)
            sampleContent.recordQuizResult(lessonId, score, total)
        }
    }

    private fun activateFallback(error: Throwable) {
        if (fallbackEnabled.compareAndSet(false, true)) {
            Log.w(TAG, "Switching to sample content: ${error.message}", error)
        }
    }

    companion object {
        private const val TAG = "LearningRepository"
    }
}
