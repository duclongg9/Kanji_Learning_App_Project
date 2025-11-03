package com.example.kanjilearning.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.kanjilearning.data.model.LessonDetail
import com.example.kanjilearning.data.model.LessonEntity
import com.example.kanjilearning.data.model.LessonProgressEntity
import com.example.kanjilearning.data.model.LessonWithProgress
import kotlinx.coroutines.flow.Flow

/**
 * VI: DAO lấy danh sách lesson và cập nhật tiến độ.
 * EN: Lesson DAO streaming content and storing progress.
 */
@Dao
interface LessonDao {

    /**
     * VI: Quan sát các lesson thuộc một khoá học (đã kèm progress nếu có).
     * EN: Observe lessons for a course, enriched with progress.
     */
    @Transaction
    @Query("SELECT * FROM lessons WHERE course_id = :courseId ORDER BY order_index ASC")
    fun observeLessons(courseId: Long): Flow<List<LessonWithProgress>>

    /**
     * VI: Lấy chi tiết lesson như KanjiDao.observeLessonDetail nhưng trả về một lần.
     * EN: Helper for use cases that need the latest lesson snapshot.
     */
    @Transaction
    @Query("SELECT * FROM lessons WHERE lesson_id = :lessonId")
    suspend fun getLessonDetailOnce(lessonId: Long): LessonDetail

    /**
     * VI: Cập nhật tiến độ sau khi người học hoàn thành quiz.
     * EN: Upsert lesson progress once the quiz finishes.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(progress: LessonProgressEntity)

    @Query("SELECT * FROM lessons WHERE lesson_id = :lessonId")
    suspend fun getLesson(lessonId: Long): LessonEntity
}
