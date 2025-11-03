package com.example.kanjilearning.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * VI: Câu hỏi trắc nghiệm (giống Quizlet) dành cho lesson.
 * EN: Multiple choice question associated with a lesson quiz.
 */
@Entity(tableName = "quiz_questions")
data class QuizQuestionEntity(
    @PrimaryKey
    @ColumnInfo(name = "question_id")
    val questionId: Long,
    @ColumnInfo(name = "lesson_id")
    val lessonId: Long,
    @ColumnInfo(name = "prompt")
    val prompt: String,
    @ColumnInfo(name = "explanation")
    val explanation: String,
    @ColumnInfo(name = "order_index")
    val orderIndex: Int
)
