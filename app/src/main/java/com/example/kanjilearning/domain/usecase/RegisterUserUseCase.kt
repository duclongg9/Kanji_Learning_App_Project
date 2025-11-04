package com.example.kanjilearning.domain.usecase

import com.example.kanjilearning.data.repository.auth.AuthRepository
import com.example.kanjilearning.domain.model.UserAccount
import javax.inject.Inject

/**
 * VI: Use case đăng ký tài khoản mới.
 * EN: Use case creating a new account.
 */
class RegisterUserUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(
        email: String,
        password: String,
        displayName: String,
        roleCode: String
    ): UserAccount = repository.register(email, password, displayName, roleCode)
}
