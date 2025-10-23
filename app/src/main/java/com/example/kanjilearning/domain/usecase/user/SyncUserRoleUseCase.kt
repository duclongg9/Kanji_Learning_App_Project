package com.example.kanjilearning.domain.usecase.user

import com.example.kanjilearning.domain.model.User
import com.example.kanjilearning.domain.repository.UserRepository
import com.example.kanjilearning.domain.util.Role
import kotlinx.coroutines.delay
import javax.inject.Inject

/**
 * VI: UseCase mô phỏng đồng bộ role từ server.
 * Trong môi trường thật sẽ gọi API; ở đây giả lập bằng cách chờ ngắn.
 */
class SyncUserRoleUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(user: User, online: Boolean): Role {
        return if (online) {
            // VI: Giả lập API call ảo -> chờ 500ms và trả về role hiện tại (có thể thay bằng logic thật).
            delay(500)
            user.role
        } else {
            // VI: Offline → fallback FREE đúng theo yêu cầu.
            Role.FREE
        }
    }
}
