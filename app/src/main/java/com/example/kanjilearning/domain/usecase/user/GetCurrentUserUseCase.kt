package com.example.kanjilearning.domain.usecase.user

import com.example.kanjilearning.domain.model.User
import com.example.kanjilearning.domain.repository.UserRepository
import javax.inject.Inject

/**
 * VI: UseCase lấy user đồng bộ.
 */
class GetCurrentUserUseCase @Inject constructor(
    private val repository: UserRepository
) {
    suspend operator fun invoke(): User? = repository.getCurrentUser()
}
