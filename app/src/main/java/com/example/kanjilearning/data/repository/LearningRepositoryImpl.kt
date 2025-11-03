package com.example.kanjilearning.data.repository

import com.example.kanjilearning.data.dao.CourseDao
import com.example.kanjilearning.data.dao.KanjiDao
import com.example.kanjilearning.data.dao.LessonDao
import com.example.kanjilearning.data.dao.QuizDao
import com.example.kanjilearning.data.model.CourseUnlockEntity
import com.example.kanjilearning.data.model.LessonProgressEntity
import com.example.kanjilearning.data.model.PaymentTransactionEntity
import com.example.kanjilearning.domain.model.CourseDetail
import com.example.kanjilearning.domain.model.CourseItem
import com.example.kanjilearning.domain.model.KanjiModel
import com.example.kanjilearning.domain.model.LessonDetailModel
import com.example.kanjilearning.domain.model.LessonSummary
import com.example.kanjilearning.domain.model.PaymentReceipt
import com.example.kanjilearning.domain.model.QuizChoice
import com.example.kanjilearning.domain.model.QuizQuestion
import com.example.kanjilearning.domain.model.QuizSession
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

/**
 * VI: Repository triển khai business logic và mapping giữa Room với domain model.
 * EN: Concrete implementation translating Room entities to domain objects.
 */
@Singleton
class LearningRepositoryImpl @Inject constructor(
    private val courseDao: CourseDao,
    private val lessonDao: LessonDao,
    private val kanjiDao: KanjiDao,
    private val quizDao: QuizDao
) : LearningRepository {

    override fun observeCourses(): Flow<List<CourseItem>> =
        courseDao.observeCourses().map { courses ->
            courses.map { it.toCourseItem() }
        }

    override fun observeCourseDetail(courseId: Long): Flow<CourseDetail> =
        courseDao.observeCourse(courseId).map { courseWithContent ->
            val courseItem = courseWithContent.toCourseItem()
            val lessons = courseWithContent.lessons
                .sortedBy { it.lesson.orderIndex }
                .map { it.toLessonSummary() }
            CourseDetail(courseItem, lessons)
        }

    override fun observeLessonDetail(lessonId: Long): Flow<LessonDetailModel> =
        kanjiDao.observeLessonDetail(lessonId).map { detail ->
            val summary = detail.lesson.toSummary(detail.progress)
            val kanjis = detail.kanjis.map { kanji ->
                KanjiModel(
                    character = kanji.character,
                    meaningVi = kanji.meaningVi,
                    meaningEn = kanji.meaningEn,
                    onyomi = kanji.onyomi,
                    kunyomi = kanji.kunyomi,
                    strokes = kanji.strokeCount,
                    jlptLevel = kanji.jlptLevel,
                    example = kanji.example,
                    exampleTranslation = kanji.exampleTranslation
                )
            }
            LessonDetailModel(summary, kanjis)
        }

    override suspend fun unlockCourseWithMomo(courseId: Long, phoneNumber: String): PaymentReceipt {
        val course = courseDao.getCourse(courseId)
        val reference = "MOMO-${System.currentTimeMillis()}-${phoneNumber.takeLast(4)}"
        val now = System.currentTimeMillis()
        val unlock = CourseUnlockEntity(
            courseId = courseId,
            status = "UNLOCKED",
            paymentMethod = "MoMo",
            transactionReference = reference,
            unlockedAt = now
        )
        courseDao.upsertUnlock(unlock)
        courseDao.insertTransaction(
            PaymentTransactionEntity(
                courseId = courseId,
                provider = "MoMo",
                amount = course.priceVnd,
                status = "SUCCESS",
                reference = reference,
                createdAt = now
            )
        )
        return PaymentReceipt(
            courseId = courseId,
            amount = course.priceVnd,
            provider = "MoMo",
            status = "SUCCESS",
            reference = reference
        )
    }

    override suspend fun loadQuiz(lessonId: Long): QuizSession {
        val lesson = lessonDao.getLesson(lessonId)
        val questions = quizDao.getQuizForLesson(lessonId).map { projection ->
            QuizQuestion(
                id = projection.question.questionId,
                prompt = projection.question.prompt,
                explanation = projection.question.explanation,
                choices = projection.choices.map { choice ->
                    QuizChoice(
                        id = choice.choiceId,
                        content = choice.content,
                        isCorrect = choice.isCorrect
                    )
                }
            )
        }
        return QuizSession(
            lessonId = lessonId,
            lessonTitle = lesson.title,
            questions = questions
        )
    }

    override suspend fun recordQuizResult(lessonId: Long, score: Int, total: Int) {
        val detail = lessonDao.getLessonDetailOnce(lessonId)
        val previous = detail.progress
        val bestScore = maxOf(previous?.bestScore ?: 0, score)
        val completionThreshold = (total * 0.8).roundToInt()
        val progress = LessonProgressEntity(
            lessonId = lessonId,
            bestScore = bestScore,
            lastScore = score,
            completed = score >= completionThreshold,
            reviewCount = (previous?.reviewCount ?: 0) + 1,
            lastReviewedAt = System.currentTimeMillis()
        )
        lessonDao.upsertProgress(progress)
    }

    private fun com.example.kanjilearning.data.model.CourseWithContent.toCourseItem(): CourseItem {
        val lessonCount = lessons.size
        val completedCount = lessons.count { it.progress?.completed == true }
        val isUnlocked = unlock?.status in setOf("FREE", "UNLOCKED")
        return CourseItem(
            id = course.courseId,
            title = course.title,
            description = course.description,
            levelTag = course.levelTag,
            coverAsset = course.coverAsset,
            durationMinutes = course.durationMinutes,
            lessonCount = lessonCount,
            completedLessons = completedCount,
            isUnlocked = isUnlocked,
            isPremium = course.isPremium,
            priceVnd = course.priceVnd
        )
    }

    private fun com.example.kanjilearning.data.model.LessonWithProgress.toLessonSummary(): LessonSummary =
        lesson.toSummary(progress)

    private fun com.example.kanjilearning.data.model.LessonEntity.toSummary(
        progress: LessonProgressEntity?
    ): LessonSummary = LessonSummary(
        id = lessonId,
        title = title,
        summary = summary,
        orderIndex = orderIndex,
        durationMinutes = durationMinutes,
        questionCount = questionCount,
        bestScore = progress?.bestScore ?: 0,
        lastScore = progress?.lastScore ?: 0,
        completed = progress?.completed ?: false,
        reviewCount = progress?.reviewCount ?: 0
    )
}
