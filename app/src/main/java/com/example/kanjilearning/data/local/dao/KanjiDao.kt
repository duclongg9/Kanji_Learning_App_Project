package com.example.kanjilearning.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.kanjilearning.data.local.entity.KanjiEntity
import kotlinx.coroutines.flow.Flow

/**
 * VI: DAO quản lý bảng Kanji.
 */
@Dao
interface KanjiDao {

    /**
     * VI: Lấy danh sách Kanji theo độ khó và JLPT, lọc thêm theo access tier.
     */
    @Query(
        "SELECT * FROM kanji WHERE (jlpt_level = :jlptLevel OR :jlptLevel IS NULL) " +
            "AND difficulty >= :minDifficulty AND difficulty <= :maxDifficulty " +
            "AND access_tier IN (:allowedTiers) ORDER BY difficulty ASC"
    )
    fun observeKanji(
        jlptLevel: String?,
        minDifficulty: Int,
        maxDifficulty: Int,
        allowedTiers: List<String>
    ): Flow<List<KanjiEntity>>

    /**
     * VI: Dùng PagingSource cho tương lai khi dữ liệu lớn.
     */
    @Query("SELECT * FROM kanji ORDER BY difficulty ASC")
    fun pagingKanji(): PagingSource<Int, KanjiEntity>

    /**
     * VI: Import hàng loạt Kanji, ghi đè nếu trùng khoá chính.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<KanjiEntity>)

    /**
     * VI: Lưu một Kanji đơn lẻ, trả về ID mới nếu được tạo.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: KanjiEntity): Long

    /**
     * VI: Xoá toàn bộ Kanji (hỗ trợ re-import).
     */
    @Query("DELETE FROM kanji")
    suspend fun clear()

    /**
     * VI: Xoá Kanji theo ID.
     */
    @Query("DELETE FROM kanji WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * VI: Xoá dữ liệu cũ rồi ghi danh sách mới trong cùng transaction.
     */
    @Transaction
    suspend fun replaceAll(items: List<KanjiEntity>) {
        clear()
        if (items.isNotEmpty()) {
            upsertAll(items)
        }
    }
}
