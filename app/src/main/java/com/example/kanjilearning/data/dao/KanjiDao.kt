package com.example.kanjilearning.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kanjilearning.data.model.KanjiEntity
import kotlinx.coroutines.flow.Flow

/**
 * VI: DAO định nghĩa các truy vấn Room cho bảng Kanji.
 * EN: DAO with the queries that Room generates implementations for.
 */
@Dao
interface KanjiDao {
    /**
     * VI: Lấy toàn bộ Kanji, trả về Flow để quan sát realtime.
     * EN: Stream all Kanji rows as a cold Flow.
     */
    @Query("SELECT * FROM kanjis ORDER BY id ASC")
    fun getAllKanjis(): Flow<List<KanjiEntity>>

    /**
     * VI: Thêm danh sách Kanji mẫu, bỏ qua nếu trùng khoá chính.
     * EN: Insert helper used for seeding demo data.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(kanjis: List<KanjiEntity>)
}
