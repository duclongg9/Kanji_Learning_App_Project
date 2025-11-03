package com.example.kanjilearning.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.kanjilearning.data.model.QuizQuestionWithChoices

/**
 * VI: DAO dành cho phần quiz trắc nghiệm.
 * EN: DAO returning quiz questions and their choices.
 */
@Dao
interface QuizDao {

    @Transaction
    @Query("SELECT * FROM quiz_questions WHERE lesson_id = :lessonId ORDER BY order_index ASC")
    suspend fun getQuizForLesson(lessonId: Long): List<QuizQuestionWithChoices>
}
