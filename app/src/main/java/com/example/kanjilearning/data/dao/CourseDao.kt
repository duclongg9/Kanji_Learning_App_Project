package com.example.kanjilearning.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.kanjilearning.data.model.CourseEntity
import com.example.kanjilearning.data.model.CourseUnlockEntity
import com.example.kanjilearning.data.model.CourseWithContent
import com.example.kanjilearning.data.model.PaymentTransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * VI: DAO cho màn hình khoá học và thanh toán.
 * EN: DAO powering course overview, detail and unlock state.
 */
@Dao
interface CourseDao {

    /**
     * VI: Lấy toàn bộ khoá học cùng lesson và trạng thái mở khóa.
     * EN: Observes every course with lessons and unlock metadata.
     */
    @Transaction
    @Query("SELECT * FROM courses ORDER BY level_order ASC")
    fun observeCourses(): Flow<List<CourseWithContent>>

    /**
     * VI: Lấy thông tin chi tiết của một khoá học.
     * EN: Observes the given course including lessons.
     */
    @Transaction
    @Query("SELECT * FROM courses WHERE course_id = :courseId")
    fun observeCourse(courseId: Long): Flow<CourseWithContent>

    @Query("SELECT * FROM courses WHERE course_id = :courseId")
    suspend fun getCourse(courseId: Long): com.example.kanjilearning.data.model.CourseEntity

    /**
     * VI: Ghi nhận giao dịch thanh toán và cập nhật trạng thái mở khóa.
     * EN: Persist the unlock row and audit transaction.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUnlock(unlock: CourseUnlockEntity)

    @Insert
    suspend fun insertTransaction(transaction: PaymentTransactionEntity)

    @Query("SELECT COUNT(*) > 0 FROM course_unlocks WHERE course_id = :courseId AND status IN ('FREE','UNLOCKED')")
    suspend fun isCourseUnlocked(courseId: Long): Boolean
}
