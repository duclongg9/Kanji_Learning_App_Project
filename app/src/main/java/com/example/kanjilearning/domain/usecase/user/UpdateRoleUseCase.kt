package com.example.kanjilearning.domain.usecase.user

import com.example.kanjilearning.domain.repository.UserRepository
import com.example.kanjilearning.domain.util.Role
import javax.inject.Inject

/**
 * VI: Dùng cho admin nâng cấp tài khoản.
 */
class UpdateRoleUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(role: Role) {
        repository.updateRole(role)
    }
}
