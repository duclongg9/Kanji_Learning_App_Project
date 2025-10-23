package com.example.kanjilearning.domain.usecase.user

import com.example.kanjilearning.domain.model.User
import com.example.kanjilearning.domain.repository.UserRepository
import javax.inject.Inject

/**
 * VI: UseCase lưu user xuống Room sau khi đăng nhập thành công.
 */
class SaveUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(user: User) {
        repository.saveUser(user)
    }
}
