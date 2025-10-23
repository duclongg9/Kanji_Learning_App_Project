package com.example.kanjilearning.domain.repository

import com.example.kanjilearning.domain.model.User
import com.example.kanjilearning.domain.util.Role
import kotlinx.coroutines.flow.Flow

/**
 * VI: Repository quản lý thông tin user.
 */
interface UserRepository {

    /**
     * VI: Quan sát user hiện hành để UI cập nhật role và profile.
     */
    fun observeCurrentUser(): Flow<User?>

    /**
     * VI: Lấy user đồng bộ (ví dụ Router cần quyết định điều hướng).
     */
    suspend fun getCurrentUser(): User?

    /**
     * VI: Lưu user sau đăng nhập hoặc đồng bộ role.
     */
    suspend fun saveUser(user: User)

    /**
     * VI: Cập nhật role khi được nâng cấp.
     */
    suspend fun updateRole(role: Role)

    /**
     * VI: Xoá user khi đăng xuất.
     */
    suspend fun clear()
}
