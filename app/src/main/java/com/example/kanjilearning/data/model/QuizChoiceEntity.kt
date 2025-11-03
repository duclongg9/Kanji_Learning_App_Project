package com.example.kanjilearning.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * VI: Các đáp án lựa chọn của từng câu hỏi.
 * EN: Individual answer choice for quiz questions.
 */
@Entity(tableName = "quiz_choices")
data class QuizChoiceEntity(
    @PrimaryKey
    @ColumnInfo(name = "choice_id")
    val choiceId: Long,
    @ColumnInfo(name = "question_id")
    val questionId: Long,
    @ColumnInfo(name = "content")
    val content: String,
    @ColumnInfo(name = "is_correct")
    val isCorrect: Boolean
)
