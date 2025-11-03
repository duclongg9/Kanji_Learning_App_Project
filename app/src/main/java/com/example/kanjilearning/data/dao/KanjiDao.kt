package com.example.kanjilearning.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.example.kanjilearning.data.model.KanjiEntity
import com.example.kanjilearning.data.model.LessonDetail
import kotlinx.coroutines.flow.Flow

/**
 * VI: DAO xử lý các truy vấn liên quan trực tiếp tới bảng Kanji và chi tiết bài học.
 * EN: DAO exposing raw kanji data plus the lesson detail graph.
 */
@Dao
interface KanjiDao {

    /**
     * VI: Lấy toàn bộ Kanji để dùng cho tra cứu nhanh hoặc thống kê.
     * EN: Streams all kanji rows for search screens.
     */
    @Query("SELECT * FROM kanjis ORDER BY character ASC")
    fun observeAll(): Flow<List<KanjiEntity>>

    /**
     * VI: Lấy chi tiết lesson gồm danh sách Kanji và tiến độ.
     * EN: Fetches a lesson, its progress and the included kanji list.
     */
    @Transaction
    @Query("SELECT * FROM lessons WHERE lesson_id = :lessonId")
    fun observeLessonDetail(lessonId: Long): Flow<LessonDetail>
}
