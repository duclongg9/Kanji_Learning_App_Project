package com.example.kanjilearning.domain.model

/**
 * VI: Domain model thuần Kotlin để tách biệt với Room.
 * EN: Pure Kotlin domain models decoupled from database layer.
 */

data class CourseItem(
    val id: Long,
    val title: String,
    val description: String,
    val levelTag: String,
    val coverAsset: String,
    val durationMinutes: Int,
    val lessonCount: Int,
    val completedLessons: Int,
    val isUnlocked: Boolean,
    val isPremium: Boolean,
    val priceVnd: Int
) {
    val progressPercent: Int
        get() = if (lessonCount == 0) 0 else ((completedLessons.toDouble() / lessonCount) * 100).toInt()
}

data class CourseDetail(
    val course: CourseItem,
    val lessons: List<LessonSummary>
)

data class LessonSummary(
    val id: Long,
    val title: String,
    val summary: String,
    val orderIndex: Int,
    val durationMinutes: Int,
    val questionCount: Int,
    val bestScore: Int,
    val lastScore: Int,
    val completed: Boolean,
    val reviewCount: Int
)

data class LessonDetailModel(
    val summary: LessonSummary,
    val kanjis: List<KanjiModel>
)

data class KanjiModel(
    val characters: String,
    val meaningVi: String,
    val meaningEn: String,
    val onyomi: String,
    val kunyomi: String,
    val strokes: Int,
    val jlptLevel: String,
    val example: String,
    val exampleTranslation: String
)

data class QuizSession(
    val lessonId: Long,
    val lessonTitle: String,
    val questions: List<QuizQuestion>
)

data class QuizQuestion(
    val id: Long,
    val prompt: String,
    val explanation: String,
    val choices: List<QuizChoice>
)

data class QuizChoice(
    val id: Long,
    val content: String,
    val isCorrect: Boolean
)

data class PaymentReceipt(
    val courseId: Long,
    val amount: Int,
    val provider: String,
    val status: String,
    val reference: String
)
