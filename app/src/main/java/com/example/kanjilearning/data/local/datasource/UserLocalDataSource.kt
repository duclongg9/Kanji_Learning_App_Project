package com.example.kanjilearning.data.local.datasource

import com.example.kanjilearning.data.local.dao.UserDao
import com.example.kanjilearning.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * VI: DataSource quản lý Room cho user.
 */
@Singleton
class UserLocalDataSource @Inject constructor(
    private val userDao: UserDao
) {

    /**
     * VI: Theo dõi user đang đăng nhập.
     */
    fun observeUser(): Flow<UserEntity?> = userDao.observeCurrentUser()

    /**
     * VI: Lấy user đồng bộ (dùng ở Router).
     */
    suspend fun getUser(): UserEntity? = userDao.getCurrentUser()

    /**
     * VI: Lưu user mới sau khi đăng nhập hoặc sync.
     */
    suspend fun upsert(user: UserEntity) {
        userDao.upsert(user)
    }

    /**
     * VI: Xoá user khi logout.
     */
    suspend fun clear() {
        userDao.clear()
    }
}
