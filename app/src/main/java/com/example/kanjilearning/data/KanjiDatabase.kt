package com.example.kanjilearning.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.kanjilearning.data.dao.CourseDao
import com.example.kanjilearning.data.dao.KanjiDao
import com.example.kanjilearning.data.dao.LessonDao
import com.example.kanjilearning.data.dao.QuizDao
import com.example.kanjilearning.data.model.CourseEntity
import com.example.kanjilearning.data.model.CourseUnlockEntity
import com.example.kanjilearning.data.model.KanjiEntity
import com.example.kanjilearning.data.model.LessonEntity
import com.example.kanjilearning.data.model.LessonKanjiCrossRef
import com.example.kanjilearning.data.model.LessonProgressEntity
import com.example.kanjilearning.data.model.PaymentTransactionEntity
import com.example.kanjilearning.data.model.QuizChoiceEntity
import com.example.kanjilearning.data.model.QuizQuestionEntity

/**
 * VI: Room database gom toàn bộ entity phục vụ tính năng học Kanji.
 * EN: Room database bundling every entity required by the learning experience.
 */
@Database(
    entities = [
        KanjiEntity::class,
        CourseEntity::class,
        LessonEntity::class,
        LessonKanjiCrossRef::class,
        QuizQuestionEntity::class,
        QuizChoiceEntity::class,
        CourseUnlockEntity::class,
        LessonProgressEntity::class,
        PaymentTransactionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class KanjiDatabase : RoomDatabase() {
    abstract fun kanjiDao(): KanjiDao
    abstract fun courseDao(): CourseDao
    abstract fun lessonDao(): LessonDao
    abstract fun quizDao(): QuizDao
}
