package com.example.kanjilearning.data.repository.auth

import com.example.kanjilearning.domain.model.UserAccount
import com.example.kanjilearning.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

/**
 * VI: Giao diện repository xử lý xác thực người dùng.
 * EN: Repository abstraction orchestrating user authentication.
 */
interface AuthRepository {

    /**
     * VI: Theo dõi session hiện tại.
     * EN: Observes the active authenticated session.
     */
    fun observeSession(): Flow<UserAccount?>

    /**
     * VI: Thực hiện đăng nhập qua email/mật khẩu.
     * EN: Performs credential-based login.
     */
    suspend fun login(email: String, password: String): UserAccount

    /**
     * VI: Đăng ký người dùng mới với role cụ thể.
     * EN: Registers a new user with the selected role.
     */
    suspend fun register(
        email: String,
        password: String,
        displayName: String,
        roleCode: String
    ): UserAccount

    /**
     * VI: Đăng xuất và xoá session.
     * EN: Logs the user out clearing the session.
     */
    suspend fun logout()

    /**
     * VI: Lấy danh sách role hỗ trợ.
     * EN: Loads the available user roles.
     */
    suspend fun loadRoles(): List<UserRole>
}
