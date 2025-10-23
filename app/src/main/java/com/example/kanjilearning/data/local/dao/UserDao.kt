package com.example.kanjilearning.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.kanjilearning.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * VI: DAO quản lý thông tin người dùng đăng nhập.
 */
@Dao
interface UserDao {

    /**
     * VI: Theo dõi người dùng hiện hành; dùng LIMIT 1 vì chỉ lưu một bản ghi.
     */
    @Query("SELECT * FROM users LIMIT 1")
    fun observeCurrentUser(): Flow<UserEntity?>

    /**
     * VI: Tìm user ngay lập tức (dùng cho Router không cần Flow).
     */
    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    /**
     * VI: Lưu/ghi đè người dùng.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    /**
     * VI: Xoá toàn bộ (logout).
     */
    @Query("DELETE FROM users")
    suspend fun clear()
}
