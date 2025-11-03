package com.example.kanjilearning.data.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * VI: Chứa các data class quan hệ phức hợp Room dùng cho repository.
 * EN: Grouped Room relation projections consumed by the repository layer.
 */

data class LessonWithProgress(
    @Embedded
    val lesson: LessonEntity,
    @Relation(
        parentColumn = "lesson_id",
        entityColumn = "lesson_id"
    )
    val progress: LessonProgressEntity?
)

data class CourseWithContent(
    @Embedded
    val course: CourseEntity,
    @Relation(
        parentColumn = "course_id",
        entityColumn = "course_id"
    )
    val unlock: CourseUnlockEntity?,
    @Relation(
        entity = LessonEntity::class,
        parentColumn = "course_id",
        entityColumn = "course_id"
    )
    val lessons: List<LessonWithProgress>
)

data class LessonDetail(
    @Embedded
    val lesson: LessonEntity,
    @Relation(
        parentColumn = "lesson_id",
        entityColumn = "lesson_id"
    )
    val progress: LessonProgressEntity?,
    @Relation(
        parentColumn = "lesson_id",
        entity = KanjiEntity::class,
        associateBy = Junction(
            value = LessonKanjiCrossRef::class,
            parentColumn = "lesson_id",
            entityColumn = "kanji_id"
        )
    )
    val kanjis: List<KanjiEntity>
)

data class QuizQuestionWithChoices(
    @Embedded
    val question: QuizQuestionEntity,
    @Relation(
        parentColumn = "question_id",
        entityColumn = "question_id"
    )
    val choices: List<QuizChoiceEntity>
)
