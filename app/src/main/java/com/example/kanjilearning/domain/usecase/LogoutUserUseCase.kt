package com.example.kanjilearning.domain.usecase

import com.example.kanjilearning.data.repository.auth.AuthRepository
import javax.inject.Inject

/**
 * VI: Use case đăng xuất, xoá session.
 * EN: Use case clearing the authenticated session.
 */
class LogoutUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() = repository.logout()
}
