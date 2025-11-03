package com.example.kanjilearning.data.mysql

import com.example.kanjilearning.domain.model.CourseDetail
import com.example.kanjilearning.domain.model.CourseItem
import com.example.kanjilearning.domain.model.KanjiModel
import com.example.kanjilearning.domain.model.LessonDetailModel
import com.example.kanjilearning.domain.model.LessonSummary
import com.example.kanjilearning.domain.model.PaymentReceipt
import com.example.kanjilearning.domain.model.QuizChoice
import com.example.kanjilearning.domain.model.QuizQuestion
import com.example.kanjilearning.domain.model.QuizSession
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException
import java.util.LinkedHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VI: Thao tác trực tiếp với MySQL để lấy dữ liệu khóa học, bài học và quiz.
 * EN: Performs direct MySQL queries to serve courses, lessons, and quiz content.
 */
@Singleton
class MySqlLearningDataSource @Inject constructor(
    private val connectionProvider: MySqlConnectionProvider,
    private val seedExecutor: MySqlSeedExecutor
) {

    private val seedReady = AtomicBoolean(false)
    private val seedLock = Any()

    /**
     * VI: Lấy danh sách khóa học kèm progress tổng quan.
     * EN: Loads the course catalog along with aggregated learner progress.
     */
    fun loadCourses(): List<CourseItem> = executeWithConnection("load courses") { connection ->
        connection.prepareStatement(COURSE_LIST_SQL).use { statement ->
            statement.executeQuery().use { resultSet ->
                val items = mutableListOf<CourseItem>()
                while (resultSet.next()) {
                    items.add(resultSet.toCourseItem())
                }
                items
            }
        }
    }

    /**
     * VI: Lấy chi tiết một khóa học và danh sách lesson thuộc khóa.
     * EN: Fetches a course detail view with all lesson summaries.
     */
    fun loadCourseDetail(courseId: Long): CourseDetail = executeWithConnection(
        "load course detail $courseId"
    ) { connection ->
        val course = connection.prepareStatement(COURSE_DETAIL_SQL).use { statement ->
            statement.setLong(1, courseId)
            statement.executeQuery().use { resultSet ->
                if (!resultSet.next()) {
                    throw MySqlDataException("Course $courseId not found")
                }
                resultSet.toCourseItem()
            }
        }
        val lessons = connection.prepareStatement(LESSON_SUMMARY_SQL).use { statement ->
            statement.setLong(1, courseId)
            statement.executeQuery().use { resultSet ->
                val summaries = mutableListOf<LessonSummary>()
                while (resultSet.next()) {
                    summaries.add(resultSet.toLessonSummary())
                }
                summaries
            }
        }
        CourseDetail(course, lessons)
    }

    /**
     * VI: Lấy chi tiết một lesson bao gồm thông tin kanji liên quan.
     * EN: Loads a lesson detail together with the kanji roster.
     */
    fun loadLessonDetail(lessonId: Long): LessonDetailModel = executeWithConnection(
        "load lesson detail $lessonId"
    ) { connection ->
        val summary = connection.prepareStatement(LESSON_DETAIL_SQL).use { statement ->
            statement.setLong(1, lessonId)
            statement.executeQuery().use { resultSet ->
                if (!resultSet.next()) {
                    throw MySqlDataException("Lesson $lessonId not found")
                }
                resultSet.toLessonSummary()
            }
        }
        val kanjis = connection.prepareStatement(KANJI_BY_LESSON_SQL).use { statement ->
            statement.setLong(1, lessonId)
            statement.executeQuery().use { resultSet ->
                val models = mutableListOf<KanjiModel>()
                while (resultSet.next()) {
                    models.add(resultSet.toKanjiModel())
                }
                models
            }
        }
        LessonDetailModel(summary, kanjis)
    }

    /**
     * VI: Thực hiện upsert trạng thái mở khóa khóa học bằng MoMo và lưu transaction.
     * EN: Upserts the course unlock status using MoMo and records the payment transaction.
     */
    fun unlockCourse(courseId: Long, phoneNumber: String): PaymentReceipt = executeWithConnection(
        "unlock course $courseId"
    ) { connection ->
        connection.autoCommit = false
        try {
            val course = connection.prepareStatement(COURSE_PRICE_SQL).use { statement ->
                statement.setLong(1, courseId)
                statement.executeQuery().use { resultSet ->
                    if (!resultSet.next()) {
                        throw MySqlDataException("Course $courseId not found")
                    }
                    CoursePricing(
                        price = resultSet.getInt("price_vnd")
                    )
                }
            }
            val reference = "MOMO-${System.currentTimeMillis()}-${phoneNumber.takeLast(4)}"
            val now = System.currentTimeMillis()
            connection.prepareStatement(COURSE_UNLOCK_UPSERT_SQL).use { statement ->
                statement.setLong(1, courseId)
                statement.setString(2, "UNLOCKED")
                statement.setString(3, "MoMo")
                statement.setString(4, reference)
                statement.setLong(5, now)
                statement.executeUpdate()
            }
            connection.prepareStatement(PAYMENT_INSERT_SQL).use { statement ->
                statement.setLong(1, courseId)
                statement.setString(2, "MoMo")
                statement.setInt(3, course.price)
                statement.setString(4, "SUCCESS")
                statement.setString(5, reference)
                statement.setLong(6, now)
                statement.executeUpdate()
            }
            connection.commit()
            connection.autoCommit = true
            PaymentReceipt(
                courseId = courseId,
                amount = course.price,
                provider = "MoMo",
                status = "SUCCESS",
                reference = reference
            )
        } catch (error: SQLException) {
            runCatching { connection.rollback() }
            throw MySqlDataException("Unable to unlock course $courseId: ${error.message}", error)
        } finally {
            runCatching { connection.autoCommit = true }
        }
    }

    /**
     * VI: Lấy session quiz gồm câu hỏi và đáp án cho một lesson.
     * EN: Loads the quiz session including questions and choices for a lesson.
     */
    fun loadQuizSession(lessonId: Long): QuizSession = executeWithConnection(
        "load quiz for lesson $lessonId"
    ) { connection ->
        val lessonTitle = connection.prepareStatement(LESSON_TITLE_SQL).use { statement ->
            statement.setLong(1, lessonId)
            statement.executeQuery().use { resultSet ->
                if (!resultSet.next()) {
                    throw MySqlDataException("Lesson $lessonId not found")
                }
                resultSet.getString("title")
            }
        }
        val questions = connection.prepareStatement(QUIZ_QUESTIONS_SQL).use { statement ->
            statement.setLong(1, lessonId)
            statement.executeQuery().use { resultSet ->
                val list = mutableListOf<QuizQuestionRow>()
                while (resultSet.next()) {
                    list.add(
                        QuizQuestionRow(
                            id = resultSet.getLong("question_id"),
                            prompt = resultSet.getString("prompt"),
                            explanation = resultSet.getString("explanation")
                        )
                    )
                }
                list
            }
        }
        val choices = connection.prepareStatement(QUIZ_CHOICES_SQL).use { statement ->
            statement.setLong(1, lessonId)
            statement.executeQuery().use { resultSet ->
                val map = LinkedHashMap<Long, MutableList<QuizChoice>>()
                while (resultSet.next()) {
                    val questionId = resultSet.getLong("question_id")
                    val list = map.getOrPut(questionId) { mutableListOf() }
                    list.add(
                        QuizChoice(
                            id = resultSet.getLong("choice_id"),
                            content = resultSet.getString("content"),
                            isCorrect = resultSet.getBoolean("is_correct")
                        )
                    )
                }
                map
            }
        }
        val quizQuestions = questions.map { row ->
            QuizQuestion(
                id = row.id,
                prompt = row.prompt,
                explanation = row.explanation,
                choices = choices[row.id]?.toList() ?: emptyList()
            )
        }
        QuizSession(lessonId = lessonId, lessonTitle = lessonTitle, questions = quizQuestions)
    }

    /**
     * VI: Ghi nhận điểm quiz và cập nhật tiến độ lesson.
     * EN: Records quiz results while updating the lesson progress entry.
     */
    fun recordQuizResult(lessonId: Long, score: Int, total: Int) = executeWithConnection(
        "record quiz result for lesson $lessonId"
    ) { connection ->
        connection.autoCommit = false
        try {
            val previous = connection.prepareStatement(PROGRESS_SELECT_SQL).use { statement ->
                statement.setLong(1, lessonId)
                statement.executeQuery().use { resultSet ->
                    if (resultSet.next()) {
                        LessonProgressRow(
                            bestScore = resultSet.getInt("best_score"),
                            reviewCount = resultSet.getInt("review_count")
                        )
                    } else {
                        null
                    }
                }
            }
            val bestScore = maxOf(previous?.bestScore ?: 0, score)
            val reviewCount = (previous?.reviewCount ?: 0) + 1
            val completionThreshold = (total * 0.8).toInt()
            val completed = score >= completionThreshold
            val now = System.currentTimeMillis()
            connection.prepareStatement(LESSON_PROGRESS_UPSERT_SQL).use { statement ->
                statement.setLong(1, lessonId)
                statement.setInt(2, bestScore)
                statement.setInt(3, score)
                statement.setBoolean(4, completed)
                statement.setInt(5, reviewCount)
                statement.setLong(6, now)
                statement.executeUpdate()
            }
            connection.commit()
        } catch (error: SQLException) {
            runCatching { connection.rollback() }
            throw MySqlDataException("Unable to record quiz result for $lessonId: ${error.message}", error)
        } finally {
            runCatching { connection.autoCommit = true }
        }
    }

    /**
     * VI: Đảm bảo database đã được seed trước khi thực hiện truy vấn.
     * EN: Makes sure the database is seeded before executing any query.
     */
    private fun ensureSeedReady() {
        if (seedReady.get()) return
        synchronized(seedLock) {
            if (seedReady.get()) return
            try {
                seedExecutor.ensureSeeded()
                seedReady.set(true)
            } catch (error: Throwable) {
                seedReady.set(false)
                throw error
            }
        }
    }

    /**
     * VI: Thực thi khối truy vấn chung với xử lý lỗi chuẩn hóa.
     * EN: Executes the provided block with a connection and normalised error handling.
     */
    private fun <T> executeWithConnection(action: String, block: (Connection) -> T): T {
        ensureSeedReady()
        try {
            connectionProvider.openConnection().use { connection ->
                return block(connection)
            }
        } catch (error: MySqlDataException) {
            throw error
        } catch (error: SQLException) {
            throw MySqlDataException("Failed to $action: ${error.message}", error)
        }
    }

    /**
     * VI: Map ResultSet -> CourseItem.
     * EN: Maps the current row to a CourseItem.
     */
    private fun ResultSet.toCourseItem(): CourseItem {
        val lessonCount = getIntOrZero("lesson_count")
        val completedLessons = getIntOrZero("completed_lessons")
        val status = getString("unlock_status") ?: "LOCKED"
        return CourseItem(
            id = getLong("course_id"),
            title = getString("title"),
            description = getString("description"),
            levelTag = getString("level_tag"),
            coverAsset = getString("cover_asset"),
            durationMinutes = getInt("duration_minutes"),
            lessonCount = lessonCount,
            completedLessons = completedLessons,
            isUnlocked = status == "FREE" || status == "UNLOCKED",
            isPremium = getBoolean("is_premium"),
            priceVnd = getInt("price_vnd")
        )
    }

    /**
     * VI: Map ResultSet -> LessonSummary.
     * EN: Maps the current row to a LessonSummary domain object.
     */
    private fun ResultSet.toLessonSummary(): LessonSummary = LessonSummary(
        id = getLong("lesson_id"),
        title = getString("title"),
        summary = getString("summary"),
        orderIndex = getInt("order_index"),
        durationMinutes = getInt("duration_minutes"),
        questionCount = getInt("question_count"),
        bestScore = getIntOrZero("best_score"),
        lastScore = getIntOrZero("last_score"),
        completed = getBooleanOrFalse("completed"),
        reviewCount = getIntOrZero("review_count")
    )

    /**
     * VI: Map ResultSet -> KanjiModel.
     * EN: Maps the current row to a KanjiModel.
     */
    private fun ResultSet.toKanjiModel(): KanjiModel = KanjiModel(
        character = getString("character"),
        meaningVi = getString("meaning_vi"),
        meaningEn = getString("meaning_en"),
        onyomi = getString("onyomi"),
        kunyomi = getString("kunyomi"),
        strokes = getInt("stroke_count"),
        jlptLevel = getString("jlpt_level"),
        example = getString("example"),
        exampleTranslation = getString("example_translation")
    )

    /**
     * VI: Lấy giá trị int, trả về 0 nếu NULL.
     * EN: Returns the integer value or zero when the column is NULL.
     */
    private fun ResultSet.getIntOrZero(column: String): Int {
        val value = getInt(column)
        return if (wasNull()) 0 else value
    }

    /**
     * VI: Lấy boolean, trả về false nếu NULL.
     * EN: Returns the boolean value or false when the column is NULL.
     */
    private fun ResultSet.getBooleanOrFalse(column: String): Boolean {
        val value = getBoolean(column)
        return if (wasNull()) false else value
    }

    private data class QuizQuestionRow(
        val id: Long,
        val prompt: String,
        val explanation: String
    )

    private data class LessonProgressRow(
        val bestScore: Int,
        val reviewCount: Int
    )

    private data class CoursePricing(
        val price: Int
    )

    companion object {
        private const val COURSE_LIST_SQL =
            """
            SELECT c.course_id, c.title, c.description, c.level_tag, c.cover_asset, c.duration_minutes,
                   c.is_premium, c.price_vnd,
                   COALESCE(cu.status, 'LOCKED') AS unlock_status,
                   COUNT(DISTINCT l.lesson_id) AS lesson_count,
                   COALESCE(SUM(CASE WHEN lp.completed = 1 THEN 1 ELSE 0 END), 0) AS completed_lessons
            FROM courses c
            LEFT JOIN course_unlocks cu ON cu.course_id = c.course_id
            LEFT JOIN lessons l ON l.course_id = c.course_id
            LEFT JOIN lesson_progress lp ON lp.lesson_id = l.lesson_id
            GROUP BY c.course_id, c.title, c.description, c.level_tag, c.cover_asset, c.duration_minutes,
                     c.is_premium, c.price_vnd, unlock_status
            ORDER BY c.level_order
            """

        private const val COURSE_DETAIL_SQL =
            """
            SELECT c.course_id, c.title, c.description, c.level_tag, c.cover_asset, c.duration_minutes,
                   c.is_premium, c.price_vnd,
                   COALESCE(cu.status, 'LOCKED') AS unlock_status,
                   COUNT(DISTINCT l.lesson_id) AS lesson_count,
                   COALESCE(SUM(CASE WHEN lp.completed = 1 THEN 1 ELSE 0 END), 0) AS completed_lessons
            FROM courses c
            LEFT JOIN course_unlocks cu ON cu.course_id = c.course_id
            LEFT JOIN lessons l ON l.course_id = c.course_id
            LEFT JOIN lesson_progress lp ON lp.lesson_id = l.lesson_id
            WHERE c.course_id = ?
            GROUP BY c.course_id, c.title, c.description, c.level_tag, c.cover_asset, c.duration_minutes,
                     c.is_premium, c.price_vnd, unlock_status
            """

        private const val LESSON_SUMMARY_SQL =
            """
            SELECT l.lesson_id, l.title, l.summary, l.order_index, l.duration_minutes, l.question_count,
                   COALESCE(lp.best_score, 0) AS best_score,
                   COALESCE(lp.last_score, 0) AS last_score,
                   COALESCE(lp.completed, 0) AS completed,
                   COALESCE(lp.review_count, 0) AS review_count
            FROM lessons l
            LEFT JOIN lesson_progress lp ON lp.lesson_id = l.lesson_id
            WHERE l.course_id = ?
            ORDER BY l.order_index
            """

        private const val LESSON_DETAIL_SQL =
            """
            SELECT l.lesson_id, l.title, l.summary, l.order_index, l.duration_minutes, l.question_count,
                   COALESCE(lp.best_score, 0) AS best_score,
                   COALESCE(lp.last_score, 0) AS last_score,
                   COALESCE(lp.completed, 0) AS completed,
                   COALESCE(lp.review_count, 0) AS review_count
            FROM lessons l
            LEFT JOIN lesson_progress lp ON lp.lesson_id = l.lesson_id
            WHERE l.lesson_id = ?
            """

        private const val KANJI_BY_LESSON_SQL =
            """
            SELECT k.character, k.meaning_vi, k.meaning_en, k.onyomi, k.kunyomi, k.stroke_count,
                   k.jlpt_level, k.example, k.example_translation
            FROM lesson_kanji_cross_ref lk
            INNER JOIN kanjis k ON k.kanji_id = lk.kanji_id
            WHERE lk.lesson_id = ?
            ORDER BY lk.position
            """

        private const val LESSON_TITLE_SQL =
            """
            SELECT title
            FROM lessons
            WHERE lesson_id = ?
            """

        private const val QUIZ_QUESTIONS_SQL =
            """
            SELECT question_id, prompt, explanation
            FROM quiz_questions
            WHERE lesson_id = ?
            ORDER BY order_index
            """

        private const val QUIZ_CHOICES_SQL =
            """
            SELECT qc.choice_id, qc.question_id, qc.content, qc.is_correct
            FROM quiz_choices qc
            INNER JOIN quiz_questions qq ON qq.question_id = qc.question_id
            WHERE qq.lesson_id = ?
            ORDER BY qc.question_id, qc.choice_id
            """

        private const val COURSE_PRICE_SQL =
            """
            SELECT price_vnd
            FROM courses
            WHERE course_id = ?
            """

        private const val PROGRESS_SELECT_SQL =
            """
            SELECT best_score, review_count
            FROM lesson_progress
            WHERE lesson_id = ?
            """

        private const val COURSE_UNLOCK_UPSERT_SQL =
            """
            INSERT INTO course_unlocks(course_id, status, payment_method, transaction_reference, unlocked_at)
            VALUES (?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                status = VALUES(status),
                payment_method = VALUES(payment_method),
                transaction_reference = VALUES(transaction_reference),
                unlocked_at = VALUES(unlocked_at)
            """

        private const val PAYMENT_INSERT_SQL =
            """
            INSERT INTO payment_transactions(course_id, provider, amount, status, reference, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """

        private const val LESSON_PROGRESS_UPSERT_SQL =
            """
            INSERT INTO lesson_progress(lesson_id, best_score, last_score, completed, review_count, last_reviewed_at)
            VALUES (?, ?, ?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE
                best_score = VALUES(best_score),
                last_score = VALUES(last_score),
                completed = VALUES(completed),
                review_count = VALUES(review_count),
                last_reviewed_at = VALUES(last_reviewed_at)
            """
    }
}
