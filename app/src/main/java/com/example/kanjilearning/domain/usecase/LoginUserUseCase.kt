package com.example.kanjilearning.domain.usecase

import com.example.kanjilearning.data.repository.auth.AuthRepository
import com.example.kanjilearning.domain.model.UserAccount
import javax.inject.Inject

/**
 * VI: Use case đăng nhập bằng email/mật khẩu.
 * EN: Use case for authenticating via email/password.
 */
class LoginUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): UserAccount =
        repository.login(email, password)
}
