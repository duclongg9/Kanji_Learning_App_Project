package com.example.kanjilearning.data.sample

import com.example.kanjilearning.domain.model.CourseDetail
import com.example.kanjilearning.domain.model.CourseItem
import com.example.kanjilearning.domain.model.KanjiModel
import com.example.kanjilearning.domain.model.LessonDetailModel
import com.example.kanjilearning.domain.model.LessonSummary
import com.example.kanjilearning.domain.model.PaymentReceipt
import com.example.kanjilearning.domain.model.QuizChoice
import com.example.kanjilearning.domain.model.QuizQuestion
import com.example.kanjilearning.domain.model.QuizSession
import java.util.Locale
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * VI: Bộ dữ liệu mẫu giàu hình ảnh để ứng dụng hoạt động offline hoàn chỉnh.
 * EN: Lush sample catalog that keeps the entire app functional offline.
 */
@Singleton
class SampleLearningContent @Inject constructor() {

    private val state = MutableStateFlow(buildInitialState())
    private val receiptCounter = AtomicLong(1L)

    /**
     * VI: Dòng danh sách khoá học với tiến độ cập nhật tức thời.
     * EN: Reactive stream delivering course cards with live progress.
     */
    fun observeCourses(): Flow<List<CourseItem>> = state
        .map { snapshot -> snapshot.values.map { it.recalculate().course } }
        .distinctUntilChanged()

    /**
     * VI: Quan sát chi tiết một khoá học cụ thể.
     * EN: Streams a course detail view for a given id.
     */
    fun observeCourseDetail(courseId: Long): Flow<CourseDetail> = state
        .map { snapshot ->
            val course = snapshot[courseId]?.recalculate()
                ?: error("Course $courseId not found in sample data")
            CourseDetail(course.course, course.lessons.map { it.summary })
        }
        .distinctUntilChanged()

    /**
     * VI: Cung cấp chi tiết bài học và danh sách Kanji theo thời gian thực.
     * EN: Serves a reactive lesson detail with associated kanji roster.
     */
    fun observeLessonDetail(lessonId: Long): Flow<LessonDetailModel> = state
        .map { snapshot ->
            val lessonState = snapshot.values.firstNotNullOfOrNull { course ->
                course.lessons.firstOrNull { it.summary.id == lessonId }
            } ?: error("Lesson $lessonId not found in sample data")
            LessonDetailModel(lessonState.summary, lessonState.kanjis)
        }
        .distinctUntilChanged()

    /**
     * VI: Lấy quiz session tương ứng với một bài học.
     * EN: Returns the quiz session that belongs to the lesson.
     */
    fun loadQuizSession(lessonId: Long): QuizSession {
        val snapshot = state.value
        val lessonState = snapshot.values.firstNotNullOfOrNull { course ->
            course.lessons.firstOrNull { it.summary.id == lessonId }
        } ?: error("Lesson $lessonId not found in sample data")
        return lessonState.quiz
    }

    /**
     * VI: Ghi nhận kết quả quiz và cập nhật tiến độ trong bộ dữ liệu mẫu.
     * EN: Records the quiz result and refreshes sample progress bookkeeping.
     */
    fun recordQuizResult(lessonId: Long, score: Int, total: Int) {
        state.update { snapshot ->
            val entry = snapshot.entries.firstOrNull { (_, course) ->
                course.lessons.any { it.summary.id == lessonId }
            } ?: return@update snapshot
            val (courseId, courseState) = entry
            val updatedLessons = courseState.lessons.map { lessonState ->
                if (lessonState.summary.id != lessonId) {
                    lessonState
                } else {
                    val threshold = max(1, (total * 0.8).roundToInt())
                    val updatedSummary = lessonState.summary.copy(
                        bestScore = max(lessonState.summary.bestScore, score),
                        lastScore = score,
                        completed = score >= threshold,
                        reviewCount = lessonState.summary.reviewCount + 1
                    )
                    lessonState.copy(summary = updatedSummary)
                }
            }
            val updatedCourse = courseState.copy(lessons = updatedLessons).recalculate()
            snapshot.toMutableMap().apply { this[courseId] = updatedCourse }
        }
    }

    /**
     * VI: Mở khoá khoá học trong dữ liệu mẫu và trả về biên nhận tượng trưng.
     * EN: Unlocks a sample course and returns a ceremonial receipt.
     */
    fun unlockCourse(courseId: Long, phoneNumber: String): PaymentReceipt {
        state.update { snapshot ->
            val courseState = snapshot[courseId] ?: return@update snapshot
            val updatedCourse = courseState.recalculate().course.copy(isUnlocked = true)
            snapshot.toMutableMap().apply { this[courseId] = courseState.copy(course = updatedCourse) }
        }
        val reference = String.format(
            Locale.US,
            "SAMPLE-%04d-%s",
            receiptCounter.getAndIncrement(),
            phoneNumber.takeLast(4).padStart(4, '0')
        )
        val course = state.value[courseId]?.course
            ?: error("Course $courseId missing after unlock")
        return PaymentReceipt(
            courseId = courseId,
            amount = course.priceVnd,
            provider = "Sample MoMo",
            status = "SUCCESS",
            reference = reference
        )
    }

    private fun buildInitialState(): Map<Long, CourseState> {
        val sakura = course(
            id = 1L,
            title = "Sakura Foundations",
            description = "Lộ trình Kanji N5 rực sắc anh đào dành cho người mới bắt đầu.",
            levelTag = "N5",
            price = 0,
            isPremium = false,
            isUnlocked = true,
            lessons = listOf(
                lesson(
                    id = 101L,
                    title = "Nhịp mặt trời",
                    summary = "Làm quen những chữ Kanji thiên nhiên đầu tiên.",
                    order = 1,
                    duration = 12,
                    kanjis = listOf(
                        kanji("日", "mặt trời", "sun", "ニチ / ジツ", "ひ", 4, "N5", "日本", "Nhật Bản"),
                        kanji("月", "mặt trăng", "moon", "ゲツ / ガツ", "つき", 4, "N5", "月光", "Ánh trăng"),
                        kanji("山", "núi", "mountain", "サン", "やま", 3, "N5", "富士山", "Núi Phú Sĩ")
                    ),
                    quiz = listOf(
                        question(
                            id = 10101L,
                            prompt = "Kanji nào nghĩa là 'mặt trời'?",
                            explanation = "日 tượng trưng cho mặt trời mọc.",
                            correctChoice = 1010101L,
                            options = listOf(
                                choice(1010101L, "日", true),
                                choice(1010102L, "月", false),
                                choice(1010103L, "山", false)
                            )
                        ),
                        question(
                            id = 10102L,
                            prompt = "Âm kunyomi của 月 là gì?",
                            explanation = "つき là cách đọc chỉ mặt trăng.",
                            correctChoice = 1010203L,
                            options = listOf(
                                choice(1010201L, "ゲツ", false),
                                choice(1010202L, "ガツ", false),
                                choice(1010203L, "つき", true)
                            )
                        )
                    )
                ),
                lesson(
                    id = 102L,
                    title = "Gia đình Edo",
                    summary = "Những chữ diễn tả con người và mối quan hệ.",
                    order = 2,
                    duration = 14,
                    kanjis = listOf(
                        kanji("人", "người", "person", "ジン / ニン", "ひと", 2, "N5", "人間", "Con người"),
                        kanji("女", "nữ", "woman", "ジョ", "おんな", 3, "N5", "女性", "Nữ giới"),
                        kanji("子", "con", "child", "シ", "こ", 3, "N5", "女子", "Nữ sinh")
                    ),
                    quiz = listOf(
                        question(
                            id = 10201L,
                            prompt = "Kanji nào mang nghĩa 'người'?",
                            explanation = "人 đọc kunyomi là ひと.",
                            correctChoice = 1020101L,
                            options = listOf(
                                choice(1020101L, "人", true),
                                choice(1020102L, "女", false),
                                choice(1020103L, "子", false)
                            )
                        ),
                        question(
                            id = 10202L,
                            prompt = "Kanji nào kết hợp với 子 thành 'nữ sinh'?",
                            explanation = "女子 đọc là じょし, dùng 女.",
                            correctChoice = 1020202L,
                            options = listOf(
                                choice(1020201L, "人", false),
                                choice(1020202L, "女", true),
                                choice(1020203L, "子", false)
                            )
                        )
                    )
                ),
                lesson(
                    id = 103L,
                    title = "Ẩm thực mùa xuân",
                    summary = "Khám phá những món ăn quen thuộc.",
                    order = 3,
                    duration = 13,
                    kanjis = listOf(
                        kanji("米", "gạo", "rice", "ベイ", "こめ", 6, "N5", "米粉", "Bột gạo"),
                        kanji("茶", "trà", "tea", "チャ", "ちゃ", 9, "N5", "日本茶", "Trà Nhật"),
                        kanji("魚", "cá", "fish", "ギョ", "さかな", 11, "N5", "金魚", "Cá vàng")
                    ),
                    quiz = listOf(
                        question(
                            id = 10301L,
                            prompt = "Kanji nào nghĩa là 'gạo'?",
                            explanation = "米 xuất hiện trong 日本米 (gạo Nhật).",
                            correctChoice = 1030101L,
                            options = listOf(
                                choice(1030101L, "米", true),
                                choice(1030102L, "茶", false),
                                choice(1030103L, "魚", false)
                            )
                        ),
                        question(
                            id = 10302L,
                            prompt = "Âm kunyomi của 魚?",
                            explanation = "さかな là cách đọc thông dụng.",
                            correctChoice = 1030203L,
                            options = listOf(
                                choice(1030201L, "ギョ", false),
                                choice(1030202L, "ちゃ", false),
                                choice(1030203L, "さかな", true)
                            )
                        )
                    )
                )
            )
        )

        val samurai = course(
            id = 2L,
            title = "Samurai Sprint",
            description = "Tăng tốc lên N3 cùng tinh thần chiến binh.",
            levelTag = "N3",
            price = 199_000,
            isPremium = true,
            isUnlocked = false,
            lessons = listOf(
                lesson(
                    id = 201L,
                    title = "Võ đạo căn bản",
                    summary = "Kanji nói về sức mạnh và con đường luyện tập.",
                    order = 1,
                    duration = 16,
                    kanjis = listOf(
                        kanji("力", "sức mạnh", "power", "リョク", "ちから", 2, "N5", "努力", "Nỗ lực"),
                        kanji("武", "võ", "martial", "ブ", "", 8, "N3", "武士", "Võ sĩ"),
                        kanji("道", "đạo", "way", "ドウ", "みち", 12, "N5", "剣道", "Kiếm đạo")
                    ),
                    quiz = listOf(
                        question(
                            id = 20101L,
                            prompt = "Kanji nào xuất hiện trong từ 武士?",
                            explanation = "武士 đọc là ぶし, bắt đầu bằng 武.",
                            correctChoice = 2010102L,
                            options = listOf(
                                choice(2010101L, "力", false),
                                choice(2010102L, "武", true),
                                choice(2010103L, "道", false)
                            )
                        ),
                        question(
                            id = 20102L,
                            prompt = "Kunyomi của 力 là gì?",
                            explanation = "ちから diễn tả sức mạnh cá nhân.",
                            correctChoice = 2010203L,
                            options = listOf(
                                choice(2010201L, "リョク", false),
                                choice(2010202L, "ドウ", false),
                                choice(2010203L, "ちから", true)
                            )
                        )
                    )
                ),
                lesson(
                    id = 202L,
                    title = "Tinh thần Samurai",
                    summary = "Danh dự và lòng trung thành trong từng nét chữ.",
                    order = 2,
                    duration = 18,
                    kanjis = listOf(
                        kanji("忠", "trung thành", "loyal", "チュウ", "ただ", 8, "N2", "忠誠", "Trung thành"),
                        kanji("義", "nghĩa", "justice", "ギ", "", 13, "N2", "正義", "Chính nghĩa"),
                        kanji("勇", "dũng", "bravery", "ユウ", "いさむ", 9, "N3", "勇気", "Dũng khí")
                    ),
                    quiz = listOf(
                        question(
                            id = 20201L,
                            prompt = "Kanji nào nghĩa là 'dũng khí'?",
                            explanation = "勇気 đọc là ゆうき.",
                            correctChoice = 2020103L,
                            options = listOf(
                                choice(2020101L, "忠", false),
                                choice(2020102L, "義", false),
                                choice(2020103L, "勇", true)
                            )
                        ),
                        question(
                            id = 20202L,
                            prompt = "Âm onyomi của 義?",
                            explanation = "ギ là onyomi thường gặp trong 正義.",
                            correctChoice = 2020202L,
                            options = listOf(
                                choice(2020201L, "チュウ", false),
                                choice(2020202L, "ギ", true),
                                choice(2020203L, "ユウ", false)
                            )
                        )
                    )
                )
            )
        )

        val zen = course(
            id = 3L,
            title = "Zen Master Odyssey",
            description = "Chinh phục Kanji nâng cao bằng hành trình thiền định.",
            levelTag = "N2",
            price = 249_000,
            isPremium = true,
            isUnlocked = false,
            lessons = listOf(
                lesson(
                    id = 301L,
                    title = "Thiền tâm khai mở",
                    summary = "Từ vựng về sự tĩnh tại và thiên nhiên sâu lắng.",
                    order = 1,
                    duration = 20,
                    kanjis = listOf(
                        kanji("静", "tĩnh", "calm", "セイ", "しずか", 14, "N3", "静寂", "Yên tĩnh"),
                        kanji("森", "rừng", "forest", "シン", "もり", 12, "N4", "森林", "Rừng cây"),
                        kanji("霧", "sương", "fog", "ム", "きり", 19, "N1", "濃霧", "Sương mù dày")
                    ),
                    quiz = listOf(
                        question(
                            id = 30101L,
                            prompt = "Kanji nào nghĩa là 'tĩnh lặng'?",
                            explanation = "静 đọc kunyomi là しずか.",
                            correctChoice = 3010101L,
                            options = listOf(
                                choice(3010101L, "静", true),
                                choice(3010102L, "森", false),
                                choice(3010103L, "霧", false)
                            )
                        ),
                        question(
                            id = 30102L,
                            prompt = "Kanji nào xuất hiện trong từ 森林?",
                            explanation = "森 kết hợp với 林 tạo thành rừng rậm.",
                            correctChoice = 3010202L,
                            options = listOf(
                                choice(3010201L, "静", false),
                                choice(3010202L, "森", true),
                                choice(3010203L, "霧", false)
                            )
                        )
                    )
                ),
                lesson(
                    id = 302L,
                    title = "Thiền và cảm xúc",
                    summary = "Kanji mô tả những trạng thái cảm xúc tinh tế.",
                    order = 2,
                    duration = 22,
                    kanjis = listOf(
                        kanji("悟", "ngộ", "enlighten", "ゴ", "さとる", 10, "N2", "悟り", "Giác ngộ"),
                        kanji("慈", "từ bi", "compassion", "ジ", "いつくしむ", 13, "N2", "慈愛", "Lòng từ ái"),
                        kanji("闇", "bóng tối", "darkness", "アン", "やみ", 17, "N1", "闇夜", "Đêm tối")
                    ),
                    quiz = listOf(
                        question(
                            id = 30201L,
                            prompt = "Kanji nào nghĩa là 'giác ngộ'?",
                            explanation = "悟り (さとり) là sự ngộ ra bản chất.",
                            correctChoice = 3020101L,
                            options = listOf(
                                choice(3020101L, "悟", true),
                                choice(3020102L, "慈", false),
                                choice(3020103L, "闇", false)
                            )
                        ),
                        question(
                            id = 30202L,
                            prompt = "Âm kunyomi của 闇?",
                            explanation = "やみ mô tả bóng tối sâu thẳm.",
                            correctChoice = 3020203L,
                            options = listOf(
                                choice(3020201L, "ゴ", false),
                                choice(3020202L, "ジ", false),
                                choice(3020203L, "やみ", true)
                            )
                        )
                    )
                )
            )
        )

        return listOf(sakura, samurai, zen).associateBy { it.course.id }
    }

    private fun course(
        id: Long,
        title: String,
        description: String,
        levelTag: String,
        price: Int,
        isPremium: Boolean,
        isUnlocked: Boolean,
        lessons: List<LessonState>
    ): CourseState {
        val duration = lessons.sumOf { it.summary.durationMinutes }
        return CourseState(
            course = CourseItem(
                id = id,
                title = title,
                description = description,
                levelTag = levelTag,
                coverAsset = "",
                durationMinutes = duration,
                lessonCount = lessons.size,
                completedLessons = lessons.count { it.summary.completed },
                isUnlocked = isUnlocked,
                isPremium = isPremium,
                priceVnd = price
            ),
            lessons = lessons
        )
    }

    private fun lesson(
        id: Long,
        title: String,
        summary: String,
        order: Int,
        duration: Int,
        kanjis: List<KanjiModel>,
        quiz: List<QuizQuestion>
    ): LessonState = LessonState(
        summary = LessonSummary(
            id = id,
            title = title,
            summary = summary,
            orderIndex = order,
            durationMinutes = duration,
            questionCount = quiz.size,
            bestScore = 0,
            lastScore = 0,
            completed = false,
            reviewCount = 0
        ),
        kanjis = kanjis,
        quiz = QuizSession(
            lessonId = id,
            lessonTitle = title,
            questions = quiz
        )
    )

    private fun kanji(
        characters: String,
        meaningVi: String,
        meaningEn: String,
        onyomi: String,
        kunyomi: String,
        strokes: Int,
        jlpt: String,
        example: String,
        translation: String
    ): KanjiModel = KanjiModel(
        characters = characters,
        meaningVi = meaningVi,
        meaningEn = meaningEn,
        onyomi = onyomi,
        kunyomi = kunyomi,
        strokes = strokes,
        jlptLevel = jlpt,
        example = example,
        exampleTranslation = translation
    )

    private fun question(
        id: Long,
        prompt: String,
        explanation: String,
        correctChoice: Long,
        options: List<QuizChoice>
    ): QuizQuestion = QuizQuestion(
        id = id,
        prompt = prompt,
        explanation = explanation,
        choices = options.map { option ->
            if (option.id == correctChoice) option.copy(isCorrect = true) else option.copy(isCorrect = false)
        }
    )

    private fun choice(id: Long, content: String, isCorrect: Boolean): QuizChoice =
        QuizChoice(id = id, content = content, isCorrect = isCorrect)

    private data class LessonState(
        val summary: LessonSummary,
        val kanjis: List<KanjiModel>,
        val quiz: QuizSession
    )

    private data class CourseState(
        val course: CourseItem,
        val lessons: List<LessonState>
    ) {
        fun recalculate(): CourseState {
            val completed = lessons.count { it.summary.completed }
            val updatedCourse = course.copy(
                durationMinutes = lessons.sumOf { it.summary.durationMinutes },
                lessonCount = lessons.size,
                completedLessons = completed
            )
            return copy(course = updatedCourse)
        }
    }
}
